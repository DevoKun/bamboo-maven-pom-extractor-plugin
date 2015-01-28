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
import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.VARIABLE_TYPE;
import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.VARIABLE_TYPE_JOB;
import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.VARIABLE_TYPE_PLAN;
import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.VARIABLE_TYPE_RESULT;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskConfiguratorHelper;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.opensymphony.xwork.TextProvider;

/**
 * @author David Ehringer
 */
public class MavenVariableTaskConfigurator extends AbstractTaskConfigurator {

    private static final Log LOG = LogFactory.getLog(MavenVariableTaskConfigurator.class);

    private static final List<String> FIELDS_TO_COPY = ImmutableList.of(PROJECT_FILE, EXTRACT_MODE, VARIABLE_TYPE,
            PREFIX_OPTION, PREFIX_OPTION_CUSTOM_VALUE, CUSTOM_VARIABLE_NAME, CUSTOM_ELEMENT);

    private TextProvider textProvider;
    
    public MavenVariableTaskConfigurator(TextProvider textProvider, TaskConfiguratorHelper taskConfiguratorHelper) {
        this.textProvider = textProvider;
        this.taskConfiguratorHelper = taskConfiguratorHelper;
    }

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params,
            @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        taskConfiguratorHelper.populateTaskConfigMapWithActionParameters(config, params, FIELDS_TO_COPY);
        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
        context.put(EXTRACT_MODE, EXTRACT_MODE_GAV);
        context.put(VARIABLE_TYPE, VARIABLE_TYPE_RESULT);
        context.put(PREFIX_OPTION, PREFIX_OPTION_DEFAULT);
        populateContextForAll(context);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context,
            @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELDS_TO_COPY);
        populateContextForAll(context);

    }

    private void populateContextForAll(@NotNull final Map<String, Object> context) {
        Map<String, String> servers = Maps.newHashMap();
        servers.put(EXTRACT_MODE_CUSTOM, textProvider.getText("maven.extractor.config.option.extract.custom"));
        servers.put(EXTRACT_MODE_GAV, textProvider.getText("maven.extractor.config.option.extract.gav"));
        context.put("options", servers);

        Map<String, String> prefixOptions = Maps.newHashMap();
        prefixOptions.put(PREFIX_OPTION_DEFAULT, textProvider.getText("maven.extractor.config.option.prefix.maven"));
        prefixOptions.put(PREFIX_OPTION_CUSTOM, textProvider.getText("maven.extractor.config.option.prefix.custom"));
        context.put("prefixOptions", prefixOptions);

        Map<String, String> variableTypeOptions = Maps.newHashMap();
        variableTypeOptions.put(VARIABLE_TYPE_JOB,
                textProvider.getText("maven.extractor.config.option.variableType.job"));
        variableTypeOptions.put(VARIABLE_TYPE_RESULT,
                textProvider.getText("maven.extractor.config.option.variableType.result"));
        variableTypeOptions.put(VARIABLE_TYPE_PLAN,
                textProvider.getText("maven.extractor.config.option.variableType.plan"));
        context.put("variableTypeOptions", variableTypeOptions);
    }

    @Override
    public void populateContextForView(@NotNull final Map<String, Object> context,
            @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForView(context, taskDefinition);
        taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELDS_TO_COPY);
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);

        String gavOrCustom = params.getString(EXTRACT_MODE);
        if (EXTRACT_MODE_CUSTOM.equals(gavOrCustom)) {
            String variableName = params.getString(CUSTOM_VARIABLE_NAME);
            String element = params.getString(CUSTOM_ELEMENT);
            if (StringUtils.isEmpty(variableName)) {
                errorCollection.addError(CUSTOM_VARIABLE_NAME,
                        textProvider.getText("maven.extractor.config.custom.variable.name.error"));
            }
            if (StringUtils.isEmpty(element)) {
                errorCollection.addError(CUSTOM_ELEMENT,
                        textProvider.getText("maven.extractor.config.custom.element.error"));
            }
        }
        if (LOG.isDebugEnabled()) {
            if (errorCollection.hasAnyErrors()) {
                LOG.debug("Submitted configuration has validation errors.");
            }
        }
    }
}
