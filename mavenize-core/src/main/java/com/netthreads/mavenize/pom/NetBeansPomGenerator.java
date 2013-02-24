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

import com.netthreads.mavenize.ApplicationProperties;
import com.netthreads.mavenize.model.ProjectFiles;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.apache.maven.model.Build;
import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.ns.nb_module_project._3.Data;
import org.netbeans.ns.nb_module_project._3.Dependency;
import org.netbeans.ns.nb_module_project._3.ModuleDependencies;
import org.netbeans.ns.project._1.Configuration;
import org.netbeans.ns.project._1.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The "NetBeans" module pom generator has added ability to read the project definition files and make a stab at
 * dependencies.
 * 
 * @author Alistair
 */
public class NetBeansPomGenerator extends DefaultPomGenerator
{

	private static Logger logger = LoggerFactory.getLogger(NetBeansPomGenerator.class);
	private static final String PROJECT_DIR = "nbproject";
	private static final String PROJECT_FILE = "project.xml";
	private static final String PACKAGING_TYPE = "nbm";
	private static final String NETBEANS_REPO = "http://bits.netbeans.org/maven2/";
	private static final String NETBEANS_REPO_ID = "netbeans";
	private static final String NETBEANS_REPO_NAME = "netbeans";
	/**
	 * When trying to generate the appropriate groupid and artifactid for a dependency we are going to split the
	 * "code-name-base" from the project.xml in two along this division from the right to left. So something like
	 * "org.gephi.project.api" splits into groupid=org.gephi and artifactid=project.api. Obviously this isn't going to
	 * work in every case but it's a start.
	 */
	private static final int NETBEANS_DEPENDENCY_GROUP_ARTIFACT_SPLIT = 2;
	private static final int NETBEANS_DEPENDENCY_GROUP_PACKAGE_SPLIT = 2;

	/**
	 * NetBeans specific processing.
	 * 
	 * @param projectFiles
	 * @param model
	 */
	@Override
	public void populate(ProjectFiles projectFiles, Model model)
	{
		super.populate(projectFiles, model);

		// Make path to project file.
		String projectPath = projectFiles.getSourceSrc().getParent() + "/" + PROJECT_DIR + "/" + PROJECT_FILE;

		// Load project specific items.
		List<Dependency> dependencies = load(projectPath.replace('/', '\\'));

		if (dependencies != null)
		{
			for (Dependency dependency : dependencies)
			{
				logger.debug(dependency.getCodeNameBase());

				org.apache.maven.model.Dependency mavenDependency = new org.apache.maven.model.Dependency();
				String groupId = getGroupId(dependency.getCodeNameBase());
				mavenDependency.setGroupId(groupId);

				String artifactId = getArtifactId(dependency.getCodeNameBase(), groupId);
				mavenDependency.setArtifactId(artifactId);

				mavenDependency.setVersion(dependency.getRunDependency().getSpecificationVersion());

				model.addDependency(mavenDependency);
			}
		}

		// Set up packagaing type.
		model.setPackaging(PACKAGING_TYPE);

		// Set up repo
		List<Repository> repositories = new LinkedList<Repository>();
		Repository repository = new DeploymentRepository();
		repository.setId(NETBEANS_REPO_ID);
		repository.setName(NETBEANS_REPO_NAME);
		repository.setUrl(NETBEANS_REPO);
		RepositoryPolicy policy = new RepositoryPolicy();
		policy.setEnabled(false);
		repository.setSnapshots(policy);
		repositories.add(repository);
		model.setRepositories(repositories);

		// Plugins
		populateBuild(model);
	}

	/**
	 * Setup plugins.
	 * 
	 * @param model
	 */
	private void populateBuild(Model model)
	{
		List<Plugin> plugins = new LinkedList<Plugin>();

		// Compiler
		Plugin pluginCompiler = new Plugin();
		pluginCompiler.setGroupId("org.apache.maven.plugins"); // TODO Bug in
																// API?
		pluginCompiler.setArtifactId("maven-compiler-plugin");
		pluginCompiler.setVersion("2.3.2");
		Xpp3Dom pluginCompilerConfiguration = new Xpp3Dom("configuration");
		addConfig(pluginCompilerConfiguration, "source", "1.6");
		addConfig(pluginCompilerConfiguration, "target", "1.6");
		pluginCompiler.setConfiguration(pluginCompilerConfiguration);

		plugins.add(pluginCompiler);

		// Maven plugin
		Plugin mavenPlugin = new Plugin();
		mavenPlugin.setGroupId("org.codehaus.mojo");
		mavenPlugin.setArtifactId("nbm-maven-plugin");
		mavenPlugin.setVersion("3.5");
		mavenPlugin.setExtensions(true);
		Xpp3Dom mavenPluginConfiguration = new Xpp3Dom("configuration");
		Xpp3Dom mavenConfigurationPublicPackages = new Xpp3Dom("publicPackages");
		addConfig(mavenConfigurationPublicPackages, "publicPackage", model.getGroupId());
		mavenPluginConfiguration.addChild(mavenConfigurationPublicPackages);
		mavenPlugin.setConfiguration(mavenPluginConfiguration);

		plugins.add(mavenPlugin);

		// JAR plugin
		Plugin jarPlugin = new Plugin();
		jarPlugin.setGroupId("org.apache.maven.plugins"); // TODO Bug in API?
		jarPlugin.setArtifactId("maven-jar-plugin");
		jarPlugin.setVersion("2.3.1");
		Xpp3Dom jarPluginConfiguration = new Xpp3Dom("configuration");
		addConfig(jarPluginConfiguration, "useDefaultManifestFile", "true");
		jarPlugin.setConfiguration(jarPluginConfiguration);

		plugins.add(jarPlugin);

		Build build = new Build();
		build.setPlugins(plugins);
		model.setBuild(build);
	}

	private void addConfig(Xpp3Dom config, String key, String value)
	{
		Xpp3Dom child = new Xpp3Dom(key);
		child.setValue(value);
		config.addChild(child);
	}

	/**
	 * Load dependencies.
	 * 
	 * @param name
	 *            The resource file name.
	 * @throws Exception
	 */
	private List<Dependency> load(String path)
	{
		logger.debug("Load template definitions..");

		List<Dependency> dependencies = null;

		try
		{
			InputStream inputStream = new FileInputStream(path);

			JAXBContext jc = JAXBContext.newInstance(Project.class);
			Unmarshaller um = jc.createUnmarshaller();
			Project project = ((Project) um.unmarshal(inputStream));

			Configuration configuration = project.getConfiguration();

			if (configuration != null)
			{
				Data data = configuration.getData();

				if (data != null)
				{
					ModuleDependencies moduleDependencies = data.getModuleDependencies();

					if (moduleDependencies != null)
					{
						dependencies = moduleDependencies.getDependency();
					}
				}
			}

		}
		catch (Exception e)
		{
			logger.error(e.getLocalizedMessage());
		}

		return dependencies;

	}

	/**
	 * Generate a groupId by splitting the codeNamBase value across a divide.
	 * 
	 * @param codeNameBase
	 * 
	 * @return The groupId
	 */
	private String getGroupId(String codeNameBase)
	{
		String groupId = codeNameBase;

		if (matchPrefix(codeNameBase))
		{
			// Try to get com.blah, org.blah, net.blah etc
			groupId = StringHelper.chopFromLeft(codeNameBase, '.', NETBEANS_DEPENDENCY_GROUP_ARTIFACT_SPLIT);
		}
		else
		{
			int points = StringHelper.countOf('.', codeNameBase);

			groupId = StringHelper.chopFromLeft(codeNameBase, '.', points - NETBEANS_DEPENDENCY_GROUP_ARTIFACT_SPLIT);
		}

		return groupId;
	}

	/**
	 * Generate a artifactId by removing the groupId from the codeNameBase.
	 * 
	 * @param codeNameBase
	 * @param partial
	 * 
	 * @return The artifactId
	 */
	private String getArtifactId(String codeNameBase, String partial)
	{
		String artifactId = codeNameBase;

		int index = partial.length() + 1;
		if (index < codeNameBase.length())
		{
			artifactId = codeNameBase.substring(index);
		}

		return artifactId;
	}

	/**
	 * Returns true if codeNameBase has starting component to package name which matches one of those loaded from
	 * properties.
	 * 
	 * @param codeNameBase
	 * 
	 * @return A match was found.
	 */
	private boolean matchPrefix(String codeNameBase)
	{
		String match = null;

		Map<String, String> prefixes = ApplicationProperties.instance().getPrefixes();

		String prefix = StringHelper.chopFromLeft(codeNameBase, '.', 1);
		if (prefix != null)
		{
			match = prefixes.get(prefix);
		}

		return match != null;
	}
}
