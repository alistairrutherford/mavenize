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
package com.netthreads.mavenize.project;

/**
 * Target project type properties factory singleton.
 * 
 */
public class ProjectTypeFactory
{
    private static ProjectTypeFactory _instance = null;

    private ProjectTypeFactory()
    {
    }
    
    /**
     * Singleton access.
     * 
     * @return  Instance of singleton.
     */
    public static synchronized ProjectTypeFactory instance()
    {
        if (_instance==null)
        {
            _instance = new ProjectTypeFactory();
        }
        
        return _instance;
    }

    /**
     * Returns instance of project type properties depending on name. Defaults
     * to java is none found for name.
     * 
     * @param name
     * 
     * @return The target project type
     */
    public ProjectType getProjectType(String name)
    {
        ProjectType projectType = null;
        
        if (name.compareToIgnoreCase(ProjectType.Types.NETBEANS.toString())==0)
        {
            projectType = new NetbeansJavaProjectType();
        }
        else
        {
            projectType = new JavaProjectType();
        }
        
        return projectType;
    }
 
    
}
