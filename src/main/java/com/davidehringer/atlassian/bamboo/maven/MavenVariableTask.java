/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.davidehringer.atlassian.bamboo.maven;

import static com.davidehringer.atlassian.bamboo.maven.VariableType.JOB;
import static com.davidehringer.atlassian.bamboo.maven.VariableType.PLAN;
import static com.davidehringer.atlassian.bamboo.maven.VariableType.RESULT;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import com.atlassian.bamboo.agent.bootstrap.AgentContext;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.task.CommonTaskContext;
import com.atlassian.bamboo.task.CommonTaskType;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.agent.remote.RemoteAgent;
import com.atlassian.bamboo.v2.build.agent.remote.sender.BambooAgentMessageSender;
import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionManager;
import com.atlassian.spring.container.ContainerManager;
import com.davidehringer.bamboo.maven.extractor.InvalidPomException;
import com.davidehringer.bamboo.maven.extractor.PomValueExtractor;
import com.davidehringer.bamboo.maven.extractor.PomValueExtractorMavenModel;

/**
 * @author David Ehringer
 */
public class MavenVariableTask implements CommonTaskType {

    private static final Log LOG = LogFactory.getLog(MavenVariableTask.class);

    private static final String POM_ELEMENT_VERSION = "version";
    private static final String POM_ELEMENT_ARTIFACT_ID = "artifactId";
    private static final String POM_ELEMENT_GROUP_ID = "groupId";

    private static final String DEFAULT_VARIABLE_PREFIX = "maven.";
    private static final String DEFAULT_POM = "pom.xml";

    // Stuff for creating Plan variables
    private PlanManager planManager;
    private VariableDefinitionManager variableDefinitionManager;
    private BambooAgentMessageSender bambooAgentMessageSender;

    public void setPlanManager(PlanManager planManager) {
        this.planManager = planManager;
    }

    public void setBambooAgentMessageSender(BambooAgentMessageSender bambooAgentMessageSender) {
        this.bambooAgentMessageSender = bambooAgentMessageSender;
    }

    public void setVariableDefinitionManager(VariableDefinitionManager variableDefinitionManager) {
        this.variableDefinitionManager = variableDefinitionManager;
    }

    @NotNull
    @Override
    public TaskResult execute(@NotNull CommonTaskContext taskContext) throws TaskException {

        BuildLogger buildLogger = taskContext.getBuildLogger();
        TaskConfiguration config = new TaskConfiguration(taskContext);
        validateVariableType(taskContext, config);

        File pomFile = getPomFile(config, buildLogger);

        PomValueExtractor extractor = null;
        try {
            extractor = new PomValueExtractorMavenModel(pomFile);
        } catch (FileNotFoundException e) {
            buildLogger.addErrorLogEntry("POM file not found at " + pomFile.getAbsolutePath(), e);
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        } catch (InvalidPomException e) {
            buildLogger.addErrorLogEntry("Unable to read POM file.", e);
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        }

        List<Variable> variables = extractVariables(config, extractor);
        saveOrUpdateVariables(variables, config);

        return TaskResultBuilder.newBuilder(taskContext).success().build();
    }

    private void validateVariableType(CommonTaskContext taskContext, TaskConfiguration config) throws TaskException {
        if (config.areVariablesOfType(PLAN) && !(taskContext instanceof TaskContext)) {
            throw new TaskException("Plan variables can only be set for Build Plans.");
        }
    }

    private List<Variable> extractVariables(TaskConfiguration config, PomValueExtractor extractor) {
        List<Variable> variables = new ArrayList<Variable>();
        if (config.isCustomExtract()) {
            String variableName = config.getCustomVariableName();
            String element = config.getCustomElement();
            doExtract(element, variableName, extractor, variables, config);
        } else {
            doExtract(POM_ELEMENT_GROUP_ID, fullVariableName(POM_ELEMENT_GROUP_ID, config), extractor, variables,
                    config);
            doExtract(POM_ELEMENT_ARTIFACT_ID, fullVariableName(POM_ELEMENT_ARTIFACT_ID, config), extractor, variables,
                    config);
            doExtract(POM_ELEMENT_VERSION, fullVariableName(POM_ELEMENT_VERSION, config), extractor, variables, config);
        }
        return variables;
    }

    private void doExtract(String element, String variableName, PomValueExtractor extractor, List<Variable> variables,
            TaskConfiguration config) {
        String value = extractor.getValue(element);
        variables.add(new Variable(variableName, value));
        
        BuildLogger logger = config.getBuildLogger();
        StringBuilder message = new StringBuilder();
        message.append("Extracted ");
        message.append(element);
        message.append(" from POM. Setting ");
        message.append(config.getVariableType());
        message.append(" variable ");
        message.append(variableName);
        message.append(" to ");
        message.append(value);
        logger.addBuildLogEntry(message.toString());
    }

    private String fullVariableName(String name, TaskConfiguration config) {
        String prefix = DEFAULT_VARIABLE_PREFIX;
        if (config.isCustomPrefix()) {
            prefix = config.getCustomPrefix();
            LOG.debug("Overriding default maven. variable prefix with " + prefix);
        }
        return prefix + name;
    }

    private void saveOrUpdateVariables(List<Variable> variables, TaskConfiguration config) {
        if (config.areVariablesOfType(PLAN)) {
            saveAsPlanVariables(variables, config);
        } else {
            saveAsJobOrResultVariables(variables, config);
        }
    }

	private void saveAsJobOrResultVariables(List<Variable> variables, TaskConfiguration config) {
		for (Variable variable : variables) {
		    String name = variable.getName();
		    String value = variable.getValue();

		    final VariableContext variableContext = config.getTaskContext().getCommonContext().getVariableContext();
		    if(config.areVariablesOfType(RESULT)){
		    	variableContext.addResultVariable(name, value);
		    }else if(config.areVariablesOfType(JOB)){
		    	variableContext.addLocalVariable(name, value);
		    }else{
		    	throw new IllegalArgumentException("Unknown variable type '" + config.getVariableType() + "'");
		    }
		}
	}

	private void saveAsPlanVariables(List<Variable> variables,
			TaskConfiguration config) {
		TaskContext taskContext = (TaskContext) config.getTaskContext();
		
		BuildContext parentBuildContext = taskContext.getBuildContext().getParentBuildContext();
		String topLevelPlanKey = parentBuildContext.getPlanResultKey().getKey();
		String buildResultKey = taskContext.getBuildContext().getBuildResultKey();
		
		AgentContext agentContext = RemoteAgent.getContext();
		if (agentContext != null) {
		    // We're in a remote agent and we can't get access to managers
		    // we want. Send something back home so they can do what we want
		    // instead.
		    if (bambooAgentMessageSender == null) {
		        bambooAgentMessageSender = (BambooAgentMessageSender) ContainerManager
		                .getComponent("bambooAgentMessageSender");
		    }
		    bambooAgentMessageSender.send(new CreateOrUpdateVariableMessage(topLevelPlanKey, buildResultKey,
		            variables));
		} else {
		    VariableManager manager = new VariableManager(planManager, variableDefinitionManager,
		            config.getBuildLogger());
		    manager.addOrUpdateVariables(topLevelPlanKey, variables);
		}
	}

    private File getPomFile(TaskConfiguration config, BuildLogger buildLogger) {
        File rootDir = config.getBaseDir();
        File pomFile = new File(rootDir, DEFAULT_POM);
        if (config.isCustomProjectFile()) {
            String projectFile = config.getProjectFile();
            buildLogger.addBuildLogEntry("Overriding " + DEFAULT_POM + " with " + projectFile);
            pomFile = new File(rootDir, projectFile);
        }
        return pomFile;
    }
}