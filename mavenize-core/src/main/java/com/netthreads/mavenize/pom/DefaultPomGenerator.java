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
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * I have borrowed heavily from the Maven And Task.
 * 
 * @author Alistair
 */
public class DefaultPomGenerator implements PomGenerator
{
    private static Logger logger = LoggerFactory.getLogger(DefaultPomGenerator.class);

    public static final String TEXT_UNKNOWN = "unknown";

    /**
     * Generate default pom info.
     * 
    /**
     * Generate pom from project files.
     * 
     * @param projectFiles
     * @param version The version number to assign in the generated pom.
     * @param packaging  The "packaging" type to assign in the pom.
     */
    @Override
    public Model generate(ProjectFiles projectFiles, String version, String packaging)
    {
        // Lets create a simple pom to match the project
        Model model = new Model();

        model.setModelVersion(DEFAULT_MODEL_VERSION);
        model.setVersion(version);

        // Packaging name doesn't like upper case letters.
        model.setPackaging(packaging.toLowerCase());

        populate(projectFiles, model);
        
        writePom(model, projectFiles);
        
        return model;
    }

    /**
     * Populate model.
     * 
     * @param projectFiles 
     */
    @Override
    public void populate(ProjectFiles projectFiles, Model model)
    {
        trimModel(model);

        String groupId = findGroupId(projectFiles);
        String artifactId = findArtifactId(projectFiles);

        model.setGroupId(groupId);
        model.setArtifactId(artifactId);
        
        Properties properties = new Properties();
        properties.put("project.build.sourceEncoding", "UTF-8");
        
        model.setProperties(properties);
    }
    

    /**
     * Write pom to target "src" directory.
     * 
     * @param model
     * @param projectFiles 
     */
    public void writePom(Model model, ProjectFiles projectFiles)
    {
        String path = projectFiles.getTargetSrc().getParent();

        MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();

        Writer fileWriter = null;
        try
        {
            File file = new File(path + "/" + POM_NAME);

            fileWriter = WriterFactory.newXmlWriter(file);

            mavenXpp3Writer.write(fileWriter, model);

        }
        catch (IOException e)
        {
            logger.error(e.getMessage());
        }
        finally
        {
            IOUtil.close(fileWriter);
        }
    }

    /**
     * Generate group id.
     * 
     * This uses the "longest common substring" algorithm to attempt to extract a decent
     * package group identifier. The most common string is found then we will attempt to make
     * it into a nicer name by shortening it a little.
     * 
     * @param projectFiles
     * 
     * @return The group id.
     */
    public String findGroupId(ProjectFiles projectFiles)
    {
        String groupId = "";

        Map<String, Integer> counts = new HashMap<String, Integer>();
        List<File> files = projectFiles.getFiles();

        int size = files.size();
        if (size > 1)
        {
            int indexA = 0;
            int indexB = 1;

            while (indexB < size)
            {
                File fileA = files.get(indexA);
                File fileB = files.get(indexB);

                String partialA = fileA.getAbsolutePath().substring(projectFiles.getSourceSrc().getAbsolutePath().length());
                String partialB = fileB.getAbsolutePath().substring(projectFiles.getSourceSrc().getAbsolutePath().length());

                int index = StringHelper.longestSubstr(partialA, partialB);
                if (index > 0)
                {
                    String common = partialA.substring(0, index);

                    logger.debug("common = " + common);

                    Integer count = counts.get(common);
                    if (count != null)
                    {
                        count++;
                        counts.put(common, count);
                    }
                    else
                    {
                        counts.put(common, 1);
                    }
                }

                indexA++;
                indexB++;
            }

            if (counts.size() > 0)
            {
                String target = findMostCommon(counts);

                groupId = target.replace('\\', '.').replace('/', '.');

                // At this point we mught have something like ".org.gephi.data.attributes.type."
                // Lets try and chop it down a bit further and tidy it up
                if (groupId.charAt(0) == '.')
                {
                    groupId = groupId.substring(1);
                }

                if (groupId.charAt(groupId.length() - 1) == '.')
                {
                    groupId = groupId.substring(0, groupId.length() - 1);
                }

                // Attempt to chop the string up to the stated number of dots.
                groupId = StringHelper.chopFromLeft(groupId, '.', 3);

            }
            else
            {
                groupId = TEXT_UNKNOWN;
            }
        }
        else
        {
            groupId = TEXT_UNKNOWN;
        }

        // Lets's sift through our files for common package name.

        return groupId;
    }

    /**
     * Look through the string collected and find the one with the highest number
     * of occurrences.
     * 
     * @param counts
     * 
     * @return String from test with highest number of occurrences.
     */
    private String findMostCommon(Map<String, Integer> counts)
    {
        // Find common string with highest count.
        Set<String> keys = counts.keySet();
        int highest = 0;
        String target = "";
        for (String key : keys)
        {
            Integer count = counts.get(key);
            if (count > highest)
            {
                highest = count;
                target = key;
            }
        }
        
        return target;
    }

    /**
     * Generate the artifact id.
     * 
     * @param projectFiles
     * 
     * @return The artifact id.
     */
    public String findArtifactId(ProjectFiles projectFiles)
    {
        String artifactId = "";

        // We'll got for a lower case version of our project name.
        artifactId = new File(projectFiles.getSourceSrc().getParent()).getName().toLowerCase();

        return artifactId;
    }

    /**
     * Removes a lot of unnecessary information from the POM.
     * This includes the build section, reporting, repositories, etc.
     */
    public void trimModel(Model model)
    {
        model.setBuild(null);
        model.setReporting(null);
        model.setProperties(null);
        model.setRepositories(null);
        model.setPluginRepositories(null);
        model.setProfiles(null);
        model.setDistributionManagement(null);
        model.setModules(null);
    }
}
