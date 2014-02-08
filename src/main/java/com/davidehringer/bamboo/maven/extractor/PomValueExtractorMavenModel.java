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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author David Ehringer
 */
public class PomValueExtractorMavenModel implements PomValueExtractor {

    private Model model;

    public PomValueExtractorMavenModel(File pomFile) throws FileNotFoundException, InvalidPomException {
        FileReader reader = new FileReader(pomFile);
        MavenXpp3Reader mavenreader = new MavenXpp3Reader();
        try {
            model = mavenreader.read(reader);
        } catch (IOException e) {
            throw new InvalidPomException(e);
        } catch (XmlPullParserException e) {
            throw new InvalidPomException(e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        model.setPomFile(pomFile);
    }

    public String getValue(String property) throws NoSuchPropertyException {
        try {
            Object value = PropertyUtils.getProperty(model, property);
            if (value == null) {
                return "";
            }
            return value.toString();
        } catch (IllegalAccessException e) {
            throw new NoSuchPropertyException(e);
        } catch (InvocationTargetException e) {
            throw new NoSuchPropertyException(e);
        } catch (NoSuchMethodException e) {
            throw new NoSuchPropertyException(e);
        }
    }
}
