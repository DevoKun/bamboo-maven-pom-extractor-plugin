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

import org.apache.commons.lang.StringUtils;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.CommonTaskContext;
import com.atlassian.bamboo.task.TaskContext;

/**
 * @author David Ehringer
 */
public class TaskConfiguration {

	public static final String EXTRACT_MODE = "gavOrCustom";
	public static final String EXTRACT_MODE_GAV = "0";
	public static final String EXTRACT_MODE_CUSTOM = "1";
	
	public static final String VARIABLE_TYPE = "variableType";
    public static final String VARIABLE_TYPE_JOB = "0";
    public static final String VARIABLE_TYPE_PLAN = "1";

	public static final String PREFIX_OPTION = "prefixOption";
	public static final String PREFIX_OPTION_DEFAULT = "1";
	public static final String PREFIX_OPTION_CUSTOM = "0";
	public static final String PREFIX_OPTION_CUSTOM_VALUE = "customPrefix";

	public static final String PROJECT_FILE = "projectFile";

	public static final String CUSTOM_VARIABLE_NAME = "customVariableName";
	public static final String CUSTOM_ELEMENT = "customElement";

	private String projectFile;
	private String customPrefix;

	private boolean customExtract = false;
	private String customVariableName;
	private String customElement;
	
	private boolean planVariable = false;
	
	private CommonTaskContext taskContext;

	public TaskConfiguration(CommonTaskContext taskContext) {
		this.taskContext = taskContext;
		ConfigurationMap configurationMap= taskContext.getConfigurationMap();
		projectFile = configurationMap.get(PROJECT_FILE);
		if (PREFIX_OPTION_CUSTOM.equals(configurationMap.get(PREFIX_OPTION))) {
				customPrefix = configurationMap.get(PREFIX_OPTION_CUSTOM_VALUE);
		}
		if(EXTRACT_MODE_CUSTOM.equals(configurationMap.get(EXTRACT_MODE))){
			customExtract = true;
			customVariableName = configurationMap.get(CUSTOM_VARIABLE_NAME);
			customElement = configurationMap.get(CUSTOM_ELEMENT);
		}
		if(VARIABLE_TYPE_PLAN.equals(configurationMap.get(VARIABLE_TYPE))){
		    planVariable = true;
		}
	}
	
	public CommonTaskContext getTaskContext(){
		return taskContext;
	}
	
	public BuildLogger getBuildLogger(){
		return taskContext.getBuildLogger();
	}

	public boolean isCustomProjectFile() {
		return !StringUtils.isEmpty(projectFile);
	}
	
	public String getProjectFile() {
		return projectFile;
	}
	
	public File getBaseDir(){
		return taskContext.getRootDirectory();
	}

	public boolean isPlanVariable() {
        return planVariable;
    }

    public boolean isCustomPrefix(){
		return !StringUtils.isEmpty(customPrefix);
	}

	public String getCustomPrefix() {
		return customPrefix;
	}

	public boolean isCustomExtract(){
		return customExtract;
	}
	
	public String getCustomVariableName() {
		return customVariableName;
	}

	public String getCustomElement() {
		return customElement;
	}
}
