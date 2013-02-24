/*
 * Copyright 2011 - Alistair Rutherford - www.netthreads.co.uk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.netthreads.mavenize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application properties.
 * 
 */
public class ApplicationProperties
{
    private static Logger logger = LoggerFactory.getLogger(ApplicationProperties.class);

    private static final String PROPERTIES_FILE = "/mavenize.properties";
    private static final String PROP_POM_PACKAGE_PREFIX = "pom.package.prefix";
    private static ApplicationProperties _instance = null;
    private Properties properties;
    private Map<String, String> prefixes;

    private ApplicationProperties()
    {
        properties = new Properties();
        prefixes = new HashMap<String, String>();
        
        load();
    }

    /**
     * Singleton access.
     * 
     * @return  Instance of singleton.
     */
    public static synchronized ApplicationProperties instance()
    {
        if (_instance == null)
        {
            _instance = new ApplicationProperties();
        }

        return _instance;
    }

    private void load()
    {
        try
        {
            properties.load(ApplicationProperties.class.getResourceAsStream(PROPERTIES_FILE));
            
            // Build stuff
            preProcess();
        }
        catch (IOException ex)
        {
            logger.equals(ex.getMessage());
        }

    }

    /**
     * Perform any pre-processing on properties i.e. for list etc.
     * 
     */
    private void preProcess()
    {
        buildPrefixes();
    }

    /**
     * Generate prefix list which will help us out building pom.
     * 
     */
    private void buildPrefixes()
    {
        String property = properties.getProperty(PROP_POM_PACKAGE_PREFIX);
        if (property != null && !property.isEmpty())
        {
            String[] parts = property.split(",");
            for (String prefix : parts)
            {
                prefixes.put(prefix, prefix);
            }
        }
    }

    /**
     * Return map of package prefix strings.
     * 
     * @return The map of package prefix strings.
     */
    public Map<String, String> getPrefixes()
    {
        return prefixes;
    }
}
