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
package com.davidehringer.bamboo.maven.extractor;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;

/**
 * @author David Ehringer
 */
public class PomValueExtractorMavenModelTest {

    private File getFile(String name) {
        URL url = getClass().getResource(name);
        return new File(url.getFile());
    }

    @Test
    public void theGAVCanBeExtractedFromAPom() throws IOException,
            XmlPullParserException {
        File file = getFile("/pom-basic.xml");
        PomValueExtractor extractor = new PomValueExtractorMavenModel(file);
        assertThat(extractor.getValue("groupId"),
                is("com.davidehringer.bamboo.maven"));
        assertThat(extractor.getValue("artifactId"), is("maven-pom-parser"));
        assertThat(extractor.getValue("version"), is("2.3-SNAPSHOT"));
    }

    @Test
    public void arrayStyleElementsCanBeExtractedUsingIndexedPropertySyntax()
            throws IOException, XmlPullParserException {
        File file = getFile("/pom-basic.xml");
        PomValueExtractor extractor = new PomValueExtractorMavenModel(file);

        assertThat(extractor.getValue("dependencies[0].groupId"),
                is("org.apache.maven"));
        assertThat(extractor.getValue("dependencies[0].artifactId"),
                is("maven-model"));
        assertThat(extractor.getValue("dependencies[0].version"), is("3.0.4"));

        assertThat(extractor.getValue("dependencies[3].groupId"),
                is("org.hamcrest"));
        assertThat(extractor.getValue("dependencies[3].artifactId"),
                is("hamcrest-all"));
        assertThat(extractor.getValue("dependencies[3].version"), is("1.1"));
        assertThat(extractor.getValue("dependencies[3].scope"), is("test"));
    }

    @Test
    public void ifAValueIsNotInThePomAnEmptyStringIsReturned()
            throws IOException, XmlPullParserException {
        File file = getFile("/pom-basic.xml");
        PomValueExtractor extractor = new PomValueExtractorMavenModel(file);
        assertThat(extractor.getValue("description"), is(""));
        assertThat(extractor.getValue("dependencies[0].scope"), is(""));
    }

    @Test
    public void simplePomPropertiesCanBeExtracted() throws IOException,
            XmlPullParserException {
        File file = getFile("/pom-basic.xml");
        PomValueExtractor extractor = new PomValueExtractorMavenModel(file);
        assertThat(extractor.getValue("properties.myProperty"), is("myValue"));
    }

    @Test
    public void pomPropertiesWithNamesContainingPeriodsCanBeExtractedUsingTheMappedPropertyNotation() throws IOException,
            XmlPullParserException {
        File file = getFile("/pom-basic.xml");
        PomValueExtractor extractor = new PomValueExtractorMavenModel(file);
        assertThat(extractor.getValue("properties(source.code.level)"), is("1.6"));
    }

    @Test
    public void youCanOnlyGetTheNamesOfModulesAndNotExtractValuesFromTheirActualPoms() throws IOException,
            XmlPullParserException {
        File file = getFile("/parent-pom.xml");
        PomValueExtractor extractor = new PomValueExtractorMavenModel(file);
        assertThat(extractor.getValue("modules[0]"), is("module-1"));
    }
}
