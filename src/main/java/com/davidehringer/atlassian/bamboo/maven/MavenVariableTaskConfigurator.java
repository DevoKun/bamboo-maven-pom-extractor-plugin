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

import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.CUSTOM_ELEMENT;
import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.CUSTOM_VARIABLE_NAME;
import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.EXTRACT_MODE;
import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.EXTRACT_MODE_CUSTOM;
import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.EXTRACT_MODE_GAV;
import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.PREFIX_OPTION;
import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.PREFIX_OPTION_CUSTOM;
import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.PREFIX_OPTION_CUSTOM_VALUE;
import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.PREFIX_OPTION_DEFAULT;
import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.PROJECT_FILE;

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

	private static final Log LOG = LogFactory
			.getLog(MavenVariableTaskConfigurator.class);

	@NotNull
	@Override
	public Map<String, String> generateTaskConfigMap(
			@NotNull final ActionParametersMap params,
			@Nullable final TaskDefinition previousTaskDefinition) {
		final Map<String, String> config = super.generateTaskConfigMap(params,
				previousTaskDefinition);
		config.put(PROJECT_FILE, params.getString(PROJECT_FILE));
		config.put(EXTRACT_MODE, params.getString(EXTRACT_MODE));

		config.put(PREFIX_OPTION, params.getString(PREFIX_OPTION));
		config.put(PREFIX_OPTION_CUSTOM_VALUE,
				params.getString(PREFIX_OPTION_CUSTOM_VALUE));

		config.put(CUSTOM_VARIABLE_NAME, params.getString(CUSTOM_VARIABLE_NAME));
		config.put(CUSTOM_ELEMENT, params.getString(CUSTOM_ELEMENT));
		return config;
	}

	@Override
	public void populateContextForCreate(
			@NotNull final Map<String, Object> context) {
		super.populateContextForCreate(context);
		context.put(EXTRACT_MODE, EXTRACT_MODE_GAV);
		context.put(PREFIX_OPTION, PREFIX_OPTION_DEFAULT);
		populateContextForAll(context);
	}

	@Override
	public void populateContextForEdit(
			@NotNull final Map<String, Object> context,
			@NotNull final TaskDefinition taskDefinition) {
		super.populateContextForEdit(context, taskDefinition);
		context.put(EXTRACT_MODE,
				taskDefinition.getConfiguration().get(EXTRACT_MODE));
		context.put(PROJECT_FILE,
				taskDefinition.getConfiguration().get(PROJECT_FILE));

		context.put(PREFIX_OPTION,
				taskDefinition.getConfiguration().get(PREFIX_OPTION));
		context.put(PREFIX_OPTION_CUSTOM_VALUE, taskDefinition
				.getConfiguration().get(PREFIX_OPTION_CUSTOM_VALUE));

		context.put(CUSTOM_VARIABLE_NAME, taskDefinition.getConfiguration()
				.get(CUSTOM_VARIABLE_NAME));
		context.put(CUSTOM_ELEMENT,
				taskDefinition.getConfiguration().get(CUSTOM_ELEMENT));
		populateContextForAll(context);
	}

	private void populateContextForAll(
			@NotNull final Map<String, Object> context) {
		Map<String, String> servers = Maps.newHashMap();
		servers.put(EXTRACT_MODE_CUSTOM,
				getI18nBean().getText("config.option.extract.custom"));
		servers.put(EXTRACT_MODE_GAV,
				getI18nBean().getText("config.option.extract.gav"));
		context.put("options", servers);

		Map<String, String> prefixOptions = Maps.newHashMap();
		prefixOptions.put(PREFIX_OPTION_DEFAULT,
				getI18nBean().getText("config.option.prefix.maven"));
		prefixOptions.put(PREFIX_OPTION_CUSTOM,
				getI18nBean().getText("config.option.prefix.custom"));
		context.put("prefixOptions", prefixOptions);
	}

	@Override
	public void populateContextForView(
			@NotNull final Map<String, Object> context,
			@NotNull final TaskDefinition taskDefinition) {
		super.populateContextForView(context, taskDefinition);
		context.put(EXTRACT_MODE,
				taskDefinition.getConfiguration().get(EXTRACT_MODE));
		context.put(PROJECT_FILE,
				taskDefinition.getConfiguration().get(PROJECT_FILE));

		context.put(PREFIX_OPTION,
				taskDefinition.getConfiguration().get(PREFIX_OPTION));
		context.put(PREFIX_OPTION_CUSTOM_VALUE, taskDefinition
				.getConfiguration().get(PREFIX_OPTION_CUSTOM_VALUE));

		context.put(CUSTOM_VARIABLE_NAME, taskDefinition.getConfiguration()
				.get(CUSTOM_VARIABLE_NAME));
		context.put(CUSTOM_ELEMENT,
				taskDefinition.getConfiguration().get(CUSTOM_ELEMENT));
	}

	@Override
	public void validate(@NotNull final ActionParametersMap params,
			@NotNull final ErrorCollection errorCollection) {
		super.validate(params, errorCollection);

		String gavOrCustom = params.getString(EXTRACT_MODE);
		if (EXTRACT_MODE_CUSTOM.equals(gavOrCustom)) {
			String variableName = params.getString(CUSTOM_VARIABLE_NAME);
			String element = params.getString(CUSTOM_ELEMENT);
			if (StringUtils.isEmpty(variableName)) {
				errorCollection.addError(CUSTOM_VARIABLE_NAME, getI18nBean()
						.getText("config.custom.variable.name.error"));
			}
			if (StringUtils.isEmpty(element)) {
				errorCollection.addError(CUSTOM_ELEMENT,
						getI18nBean().getText("config.custom.element.error"));
			}
		}
		if (LOG.isDebugEnabled()) {
			if (errorCollection.hasAnyErrors()) {
				LOG.debug("Submitted configuration has validation errors.");
			}
		}
	}
}
