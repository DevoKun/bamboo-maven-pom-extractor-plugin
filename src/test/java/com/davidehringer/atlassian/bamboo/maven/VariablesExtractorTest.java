/*
 * Copyright 2015 the original author or authors.
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

import static com.davidehringer.atlassian.bamboo.maven.TaskConfiguration.*;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bamboo.build.logger.NullBuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.configuration.ConfigurationMapImpl;
import com.atlassian.bamboo.task.CommonTaskContext;
import com.davidehringer.bamboo.maven.extractor.InvalidPomException;
import com.davidehringer.bamboo.maven.extractor.PomValueExtractor;
import com.davidehringer.bamboo.maven.extractor.PomValueExtractorMavenModel;

/**
 * @author David Ehringer
 */
public class VariablesExtractorTest {

    private ConfigurationMap configurationMap;
    private CommonTaskContext taskContext;
    private VariablesExtractor extractor;

    @Before
    public void init() throws FileNotFoundException, InvalidPomException {
        taskContext = mock(CommonTaskContext.class);

        configurationMap = new ConfigurationMapImpl();
        configurationMap.put(PROJECT_FILE, "pom-basic.xml");
        configurationMap.put(VARIABLE_TYPE, VARIABLE_TYPE_RESULT);
        when(taskContext.getConfigurationMap()).thenReturn(configurationMap);

        when(taskContext.getBuildLogger()).thenReturn(new NullBuildLogger());

        PomValueExtractor pomExtractor = new PomValueExtractorMavenModel(getFile("/pom-basic.xml"));
        extractor = new VariablesExtractor(pomExtractor);
    }

    private File getFile(String name) {
        URL url = getClass().getResource(name);
        return new File(url.getFile());
    }

    @Test
    public void extractGavWithDefaultPrefix() {
        // Given
        configurationMap.put(EXTRACT_MODE, EXTRACT_MODE_GAV);
        configurationMap.put(PREFIX_OPTION, PREFIX_OPTION_DEFAULT);

        TaskConfiguration config = new TaskConfiguration(taskContext);

        // When
        List<Variable> variables = extractor.extractVariables(config);

        // Then
        assertThat(variables, hasItem(new Variable("maven.groupId", "com.davidehringer.bamboo.maven")));
        assertThat(variables, hasItem(new Variable("maven.artifactId", "maven-pom-parser")));
        assertThat(variables, hasItem(new Variable("maven.version", "2.3-SNAPSHOT")));
    }

    @Test
    public void extractGavWithCustomPrefix() {
        // Given
        configurationMap.put(EXTRACT_MODE, EXTRACT_MODE_GAV);
        configurationMap.put(PREFIX_OPTION, PREFIX_OPTION_CUSTOM);
        configurationMap.put(PREFIX_OPTION_CUSTOM_VALUE, "customVal.");

        TaskConfiguration config = new TaskConfiguration(taskContext);

        // When
        List<Variable> variables = extractor.extractVariables(config);

        // Then
        assertThat(variables, hasItem(new Variable("customVal.groupId", "com.davidehringer.bamboo.maven")));
        assertThat(variables, hasItem(new Variable("customVal.artifactId", "maven-pom-parser")));
        assertThat(variables, hasItem(new Variable("customVal.version", "2.3-SNAPSHOT")));
    }

    @Test
    public void extractCustomValue() {
        // Given
        configurationMap.put(EXTRACT_MODE, EXTRACT_MODE_CUSTOM);
        configurationMap.put(CUSTOM_ELEMENT, "properties.myProperty");
        configurationMap.put(CUSTOM_VARIABLE_NAME, "myProperty");

        TaskConfiguration config = new TaskConfiguration(taskContext);

        // When
        List<Variable> variables = extractor.extractVariables(config);

        // Then
        assertThat(variables, hasItem(new Variable("myProperty", "myValue")));
    }
    
    @Test
    public void removeSnapshotFromVersion() {
        // Given
        configurationMap.put(EXTRACT_MODE, EXTRACT_MODE_GAV);
        configurationMap.put(STRIP_SNAPSHOT, "true");
        configurationMap.put(PREFIX_OPTION, PREFIX_OPTION_DEFAULT);

        TaskConfiguration config = new TaskConfiguration(taskContext);

        // When
        List<Variable> variables = extractor.extractVariables(config);

        // Then
        assertThat(variables, hasItem(new Variable("maven.version", "2.3")));
    }
}
