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
import java.util.List;

import com.atlassian.bamboo.agent.AgentType;
import com.atlassian.bamboo.agent.bootstrap.AgentContext;
import org.jetbrains.annotations.NotNull;

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

    private static final String DEFAULT_POM = "pom.xml";

    // Stuff for creating Plan variables
    private PlanManager planManager;
    private VariableDefinitionManager variableDefinitionManager;
    private BambooAgentMessageSender bambooAgentMessageSender;
    private AgentContext agentContext;

    public void setAgentContext(AgentContext agentContext) {
        this.agentContext = agentContext;
    }

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
        VariablesExtractor variablesExtractor = new VariablesExtractor(extractor);
        return variablesExtractor.extractVariables(config);
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

        AgentContext agentContext = null;
        try {
            // In 5.10, RemoteAgent.getContext() started throwing an exception instead of returning null.
            // I'm not sure of an alternative way to determine if we are running in a remote agent so this
            // ugly hack exists.
            agentContext = RemoteAgent.getContext();
        }catch (IllegalStateException e){

        }
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
		    BambooVariableManager manager = new BambooVariableManager(planManager, variableDefinitionManager,
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