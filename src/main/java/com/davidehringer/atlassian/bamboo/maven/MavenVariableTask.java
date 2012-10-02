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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.agent.remote.sender.BambooAgentMessageSender;
import com.atlassian.bamboo.variable.VariableDefinitionManager;
import com.davidehringer.bamboo.maven.extractor.InvalidPomException;
import com.davidehringer.bamboo.maven.extractor.PomValueExtractor;
import com.davidehringer.bamboo.maven.extractor.PomValueExtractorMavenModel;

/**
 * @author David Ehringer
 */
public class MavenVariableTask implements TaskType {

	private static final String DEFAULT_POM = "pom.xml";

	private PlanManager planManager;
	private VariableDefinitionManager variableDefinitionManager;
	private BambooAgentMessageSender bambooAgentMessageSender;

	public void setPlanManager(PlanManager planManager) {
		this.planManager = planManager;
	}

	public void setBambooAgentMessageSender(
			BambooAgentMessageSender bambooAgentMessageSender) {
		this.bambooAgentMessageSender = bambooAgentMessageSender;
	}

	public void setVariableDefinitionManager(
			VariableDefinitionManager variableDefinitionManager) {
		this.variableDefinitionManager = variableDefinitionManager;
	}

	@NotNull
	@Override
	public TaskResult execute(@NotNull TaskContext taskContext)
			throws TaskException {

		BuildContext parentBuildContext = taskContext.getBuildContext()
				.getParentBuildContext();
		if (parentBuildContext == null) {
			return TaskResultBuilder.create(taskContext).success().build();
		}
		String topLevelPlanKey = parentBuildContext.getPlanResultKey().getKey();
		String buildResultKey = taskContext.getBuildContext()
				.getBuildResultKey();
		BuildLogger buildLogger = taskContext.getBuildLogger();

		File pomFile = getPomFile(taskContext, buildLogger);

		PomValueExtractor extractor = null;
		try {
			extractor = new PomValueExtractorMavenModel(pomFile);
		} catch (FileNotFoundException e) {
			buildLogger.addErrorLogEntry(
					"POM file not found at " + pomFile.getAbsolutePath(), e);
			return TaskResultBuilder.create(taskContext).failed().build();
		} catch (InvalidPomException e) {
			buildLogger.addErrorLogEntry("Unable to read POM file.", e);
			return TaskResultBuilder.create(taskContext).failed().build();
		}

		List<Variable> variables = extractVariables(taskContext, extractor);

		saveOrUpdateVariables(topLevelPlanKey, buildResultKey, buildLogger,
				variables, taskContext);

		return TaskResultBuilder.create(taskContext).success().build();
	}

	private List<Variable> extractVariables(TaskContext taskContext,
			PomValueExtractor extractor) {
		List<Variable> variables = new ArrayList<Variable>();

		String gavOrCustom = taskContext.getConfigurationMap().get(
				"gavOrCustom");
		if ("0".equals(gavOrCustom)) {
			String value = extractor.getValue("groupId");
			variables.add(new Variable(fullVariableName("groupId", taskContext), value));

			value = extractor.getValue("artifactId");
			variables.add(new Variable(fullVariableName("artifactId", taskContext), value));

			value = extractor.getValue("version");
			variables.add(new Variable(fullVariableName("version", taskContext), value));
		} else if ("1".equals(gavOrCustom)) {
			String variableName = taskContext.getConfigurationMap().get(
					"customVariableName");
			String element = taskContext.getConfigurationMap().get(
					"customElement");
			String value = extractor.getValue(element);
			variables.add(new Variable(variableName, value));
		} else {
			// TODO
		}
		return variables;
	}
	
	private String fullVariableName(String name, TaskContext taskContext){
		String prefix = "maven.";
		String prefixOption = taskContext.getConfigurationMap().get(
				"prefixOption");
		if("0".equals(prefixOption)){
			prefix = taskContext.getConfigurationMap().get(
					"customPrefix");
		}
		return prefix + name;
	}

	private void saveOrUpdateVariables(String topLevelPlanKey,
			String buildResultKey, BuildLogger buildLogger,
			List<Variable> variables, TaskContext taskContext) {
		for (Variable variable : variables) {
			String name = variable.getName();
			String value = variable.getValue();

			Map<String, String> customBuildData = taskContext.getBuildContext()
					.getBuildResult().getCustomBuildData();
			customBuildData.put(name, value);
		}
	}

	private File getPomFile(final TaskContext taskContext,
			BuildLogger buildLogger) {
		File rootDir = taskContext.getRootDirectory();

		String projectFile = taskContext.getConfigurationMap().get(
				"projectFile");
		buildLogger.addBuildLogEntry("projectFile override: " + projectFile);
		File pomFile = new File(rootDir, DEFAULT_POM);
		if (projectFile != null && !"".equals(projectFile)) {
			pomFile = new File(rootDir, projectFile);
		}
		return pomFile;
	}

}