/*
 * Copyright 2013 the original author or authors.
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

import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.VARIABLE_TYPE;
import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.VARIABLE_TYPE_JOB;
import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.VARIABLE_TYPE_PLAN;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.CommonTaskContext;

/**
 * @author David Ehringer
 */
public class TaskConfigurationTest {

    @Mock
    private ConfigurationMap configurationMap;
    @Mock
    private CommonTaskContext context;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(context.getConfigurationMap()).thenReturn(configurationMap);
    }

    @Test
    public void whenVariableTypeIsNullThenPlanVariableIsFalse() {
        // To support tasks that were configured prior to version 1.3 of the
        // plugin where VARIABLE_TYPE didn't exist
        when(configurationMap.get(VARIABLE_TYPE)).thenReturn(null);

        TaskConfiguration taskConfiguration = new TaskConfiguration(context);
        assertFalse(taskConfiguration.isPlanVariable());
    }

    @Test
    public void whenVariableTypeIsJobThenPlanVariableIsFalse() {
        when(configurationMap.get(VARIABLE_TYPE)).thenReturn(VARIABLE_TYPE_JOB);

        TaskConfiguration taskConfiguration = new TaskConfiguration(context);
        assertFalse(taskConfiguration.isPlanVariable());
    }

    @Test
    public void whenVariableTypeIsPlanThenPlanVariableIsTrue() {
        when(configurationMap.get(VARIABLE_TYPE)).thenReturn(VARIABLE_TYPE_PLAN);

        TaskConfiguration taskConfiguration = new TaskConfiguration(context);
        assertTrue(taskConfiguration.isPlanVariable());
    }
}
