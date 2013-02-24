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
package com.netthreads.mavenize.pom;

import com.netthreads.mavenize.model.ProjectFiles;
import org.apache.maven.model.Model;

/**
 * Interface pom generator
 * 
 */
public interface PomGenerator
{
    public static final String DEFAULT_VERSION = "1.0.0-SNAPSHOT";
    public static final String DEFAULT_MODEL_VERSION = "4.0.0";
    public static final String POM_NAME = "pom.xml";

	public static final String[] PACKAGE_TYPES =
	{
	        "JAR", "WAR", "POM", "EJB", "EAR", "RAR", "PAR"
	};
    
    /**
     * Generate pom from project files.
     * 
     * @param projectFiles
     * @param version The version number to assign in the generated pom.
     * @param packaging  The "packaging" type to assign in the pom.
     * 
     * @return The maven model object.
     */
    public Model generate(ProjectFiles projectFiles, String version, String packaging);

    /**
     * Populate model.
     * 
     * @param projectFiles
     * @param model 
     */
    public void populate(ProjectFiles projectFiles, Model model);
}
