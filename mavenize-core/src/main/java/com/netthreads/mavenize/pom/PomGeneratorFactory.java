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

import com.netthreads.mavenize.project.ProjectType;
import java.util.HashMap;
import java.util.Map;

/**
 * Target project type properties factory singleton.
 * 
 */
public class PomGeneratorFactory
{

    private static PomGeneratorFactory _instance = null;
    private Map<ProjectType, PomGenerator> generators;

    /**
     * Singleton access.
     * 
     * @return  Instance of singleton.
     */
    public static synchronized PomGeneratorFactory instance()
    {
        if (_instance == null)
        {
            _instance = new PomGeneratorFactory();
        }

        return _instance;
    }

    private PomGeneratorFactory()
    {
        generators = new HashMap<ProjectType, PomGenerator>();
    }

    /**
     * Return instance of generator.
     * 
     * @param projectType
     * 
     * @return An instance of the target generator.
     */
    public PomGenerator getGenerator(ProjectType projectType)
    {
        PomGenerator pomGenerator = generators.get(projectType);

        if (pomGenerator == null)
        {
            switch (projectType.getType())
            {
                case NETBEANS:
                    pomGenerator = new NetBeansPomGenerator();
                    break;
                default:
                    pomGenerator = new DefaultPomGenerator();
                    break;
            }
            
            generators.put(projectType, pomGenerator);
        }

        return pomGenerator;
    }
}
