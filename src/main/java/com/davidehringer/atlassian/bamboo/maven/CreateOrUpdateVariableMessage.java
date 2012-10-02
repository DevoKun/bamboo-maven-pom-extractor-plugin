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

import java.util.List;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.v2.build.agent.messages.AbstractBambooAgentMessage;
import com.atlassian.bamboo.variable.VariableDefinitionManager;

/**
 * @author David Ehringer
 */
public class CreateOrUpdateVariableMessage extends AbstractBambooAgentMessage {

	private final String topLevelPlanKey;
	private final String buildResultKey;
	private final List<Variable> variables;

	public CreateOrUpdateVariableMessage(String topLevelPlanKey,
			String buildResultKey, List<Variable> variables) {
		this.topLevelPlanKey = topLevelPlanKey;
		this.buildResultKey = buildResultKey;
		this.variables = variables;
	}

	@Override
	public Object deliver() {
		PlanManager planManager = getComponent(PlanManager.class, "planManager");
		VariableDefinitionManager variableDefinitionManager = getComponent(
				VariableDefinitionManager.class, "variableDefinitionManager");
		BuildLoggerManager buildLoggerManager = getComponent(
				BuildLoggerManager.class, "buildLoggerManager");

		PlanResultKey planResultKey = PlanKeys.getPlanResultKey(buildResultKey);

		BuildLogger buildLogger = buildLoggerManager
				.getBuildLogger(planResultKey);

		VariableManager manager = new VariableManager(planManager,
				variableDefinitionManager, buildLogger);
		manager.addOrUpdateVariables(topLevelPlanKey, variables);

		return null;
	}

}
