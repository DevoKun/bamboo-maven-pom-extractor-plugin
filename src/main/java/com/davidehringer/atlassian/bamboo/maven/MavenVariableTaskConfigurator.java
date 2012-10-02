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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.google.common.collect.Maps;
import com.opensymphony.xwork.TextProvider;

/**
 * @author David Ehringer
 */
public class MavenVariableTaskConfigurator extends AbstractTaskConfigurator {
	private TextProvider textProvider;

	@NotNull
	@Override
	public Map<String, String> generateTaskConfigMap(
			@NotNull final ActionParametersMap params,
			@Nullable final TaskDefinition previousTaskDefinition) {
		final Map<String, String> config = super.generateTaskConfigMap(params,
				previousTaskDefinition);
		config.put("say", params.getString("say"));
		config.put("projectFile", params.getString("projectFile"));
		config.put("gavOrCustom", params.getString("gavOrCustom"));
		System.out.println("projectFile: " + params.getString("projectFile"));
		System.out.println("Options provides: " + params.getString("options"));
		System.out.println("gavOrCustom: " + params.getString("gavOrCustom"));
		return config;
	}

	@Override
	public void populateContextForCreate(
			@NotNull final Map<String, Object> context) {
		super.populateContextForCreate(context);
		context.put("say", "Hello, World!");
		context.put("gavOrCustom", "0");
		populateContextForAll(context);
	}

	@Override
	public void populateContextForEdit(
			@NotNull final Map<String, Object> context,
			@NotNull final TaskDefinition taskDefinition) {
		super.populateContextForEdit(context, taskDefinition);
		context.put("say", taskDefinition.getConfiguration().get("say"));
		context.put("gavOrCustom", taskDefinition.getConfiguration().get("gavOrCustom"));
		context.put("projectFile", taskDefinition.getConfiguration().get("projectFile"));
		populateContextForAll(context);
	}

	private void populateContextForAll(
			@NotNull final Map<String, Object> context) {
		Map<String, String> servers = Maps.newHashMap();
		servers.put("0", getI18nBean().getText("config.option.extract.gav"));
		servers.put("1", getI18nBean().getText("config.option.extract.custom"));
		context.put("options", servers);
	}

	@Override
	public void populateContextForView(
			@NotNull final Map<String, Object> context,
			@NotNull final TaskDefinition taskDefinition) {
		super.populateContextForView(context, taskDefinition);
		context.put("say", taskDefinition.getConfiguration().get("say"));
		context.put("gavOrCustom", taskDefinition.getConfiguration().get("gavOrCustom"));
		context.put("projectFile", taskDefinition.getConfiguration().get("projectFile"));
	}

	@Override
	public void validate(@NotNull final ActionParametersMap params,
			@NotNull final ErrorCollection errorCollection) {
		super.validate(params, errorCollection);

		final String sayValue = params.getString("say");
		if (StringUtils.isEmpty(sayValue)) {
			errorCollection
					.addError(
							"say",
							textProvider
									.getText("com.davidehringer.atlassian.bamboo.maven.say.error"));
		}
	}

	public void setTextProvider(final TextProvider textProvider) {
		this.textProvider = textProvider;
	}
}
