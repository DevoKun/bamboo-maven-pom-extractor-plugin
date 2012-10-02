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

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.google.common.collect.Maps;

/**
 * @author David Ehringer
 */
public class MavenVariableTaskConfigurator extends AbstractTaskConfigurator {
	
	private static final Log LOG = LogFactory.getLog(MavenVariableTaskConfigurator.class);

	@NotNull
	@Override
	public Map<String, String> generateTaskConfigMap(
			@NotNull final ActionParametersMap params,
			@Nullable final TaskDefinition previousTaskDefinition) {
		final Map<String, String> config = super.generateTaskConfigMap(params,
				previousTaskDefinition);
		config.put("projectFile", params.getString("projectFile"));
		config.put("gavOrCustom", params.getString("gavOrCustom"));
		LOG.debug("1");
		
		config.put("prefixOption", params.getString("prefixOption"));
		config.put("customPrefix", params.getString("customPrefix"));
		LOG.debug("2");
		
		config.put("customVariableName", params.getString("customVariableName"));
		config.put("customElement", params.getString("customElement"));
		return config;
	}

	@Override
	public void populateContextForCreate(
			@NotNull final Map<String, Object> context) {
		super.populateContextForCreate(context);
		context.put("gavOrCustom", "0");
		context.put("prefixOption", "1");
		populateContextForAll(context);
	}

	@Override
	public void populateContextForEdit(
			@NotNull final Map<String, Object> context,
			@NotNull final TaskDefinition taskDefinition) {
		super.populateContextForEdit(context, taskDefinition);
		context.put("gavOrCustom", taskDefinition.getConfiguration().get("gavOrCustom"));
		context.put("projectFile", taskDefinition.getConfiguration().get("projectFile"));
		
		context.put("prefixOption", taskDefinition.getConfiguration().get("prefixOption"));
		context.put("customPrefix", taskDefinition.getConfiguration().get("customPrefix"));
		
		context.put("customVariableName", taskDefinition.getConfiguration().get("customVariableName"));
		context.put("customElement", taskDefinition.getConfiguration().get("customElement"));
		populateContextForAll(context);
	}

	private void populateContextForAll(
			@NotNull final Map<String, Object> context) {
		Map<String, String> servers = Maps.newHashMap();
		servers.put("1", getI18nBean().getText("config.option.extract.custom"));
		servers.put("0", getI18nBean().getText("config.option.extract.gav"));
		context.put("options", servers);
		

		Map<String, String> prefixOptions = Maps.newHashMap();
		prefixOptions.put("1", getI18nBean().getText("config.option.prefix.maven"));
		prefixOptions.put("0", getI18nBean().getText("config.option.prefix.custom"));
		context.put("prefixOptions", prefixOptions);
	}

	@Override
	public void populateContextForView(
			@NotNull final Map<String, Object> context,
			@NotNull final TaskDefinition taskDefinition) {
		super.populateContextForView(context, taskDefinition);
		context.put("gavOrCustom", taskDefinition.getConfiguration().get("gavOrCustom"));
		context.put("projectFile", taskDefinition.getConfiguration().get("projectFile"));
		
		context.put("prefixOption", taskDefinition.getConfiguration().get("prefixOption"));
		context.put("customPrefix", taskDefinition.getConfiguration().get("customPrefix"));
		
		context.put("customVariableName", taskDefinition.getConfiguration().get("customVariableName"));
		context.put("customElement", taskDefinition.getConfiguration().get("customElement"));
	}

	@Override
	public void validate(@NotNull final ActionParametersMap params,
			@NotNull final ErrorCollection errorCollection) {
		super.validate(params, errorCollection);
		
		String gavOrCustom = params.getString(
				"gavOrCustom");
		if("1".equals(gavOrCustom)){
			String variableName = params.getString(
					"customVariableName");
			String element = params.getString(
					"customElement");
			if(StringUtils.isEmpty(variableName)){
				errorCollection
						.addError(
								"customVariableName",
								getI18nBean()
										.getText("config.custom.variable.name.error"));
			}
			if(StringUtils.isEmpty(element)){
				errorCollection
						.addError(
								"customElement",
								getI18nBean()
										.getText("config.custom.element.error"));
			}
		}
	}
}
