/**
 * -----------------------------------------------------------------------
 * (c) - Alistair Rutherford - www.netthreads.co.uk - March 2013
 * -----------------------------------------------------------------------
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * -----------------------------------------------------------------------
 */
package com.netthreads.javafx.mavenize.service;

import java.io.File;
import java.io.IOException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.apache.maven.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netthreads.javafx.mavenize.controller.ImplementsRefresh;
import com.netthreads.javafx.mavenize.model.ProjectResult;
import com.netthreads.mavenize.Mavenize;
import com.netthreads.mavenize.MavenizeListener;
import com.netthreads.mavenize.model.ProjectFiles;
import com.netthreads.mavenize.pom.PomGenerator;
import com.netthreads.mavenize.project.ProjectType;
import com.netthreads.mavenize.project.ProjectTypeFactory;

/**
 * Mavenize background service which clients can bind to.
 * 
 */
public class MavenizeService extends Service<Void> implements MavenizeListener
{
	private Logger logger = LoggerFactory.getLogger(MavenizeService.class);

	private String sourcePath;
	private String targetPath;
	private String projectTypeName;
	private String version;
	private String packaging;

	private Mavenize mavenize;

	private ResultCache resultCache;

	// ---------------------------------------------------------------
	// Active Property
	// ---------------------------------------------------------------

	private BooleanProperty activeProperty;

	public BooleanProperty getActiveProperty()
	{
		return activeProperty;
	}

	public boolean getActive()
	{
		return activeProperty.get();
	}

	public void setActive(boolean activeProperty)
	{
		this.activeProperty.set(activeProperty);
	}

	private ObservableList<ProjectResult> observableList;

	private ImplementsRefresh refreshView;
	
	/**
	 * Construct service.
	 * 
	 */
	public MavenizeService(ObservableList<ProjectResult> list, ImplementsRefresh refreshView)
	{
		this.observableList = list;

		this.refreshView = refreshView;
		
		resultCache = ResultCache.instance();

		// ---------------------------------------------------------------
		// Service properties
		// ---------------------------------------------------------------
		activeProperty = new SimpleBooleanProperty();

		// ---------------------------------------------------------------
		// Parameters.
		// ---------------------------------------------------------------
		sourcePath = "";
		targetPath = "";
		projectTypeName = ProjectType.Types.DEFAULT.toString();
		version = PomGenerator.DEFAULT_VERSION;
		packaging = PomGenerator.PACKAGE_TYPES[0];

		// ---------------------------------------------------------------
		// Core Worker
		// ---------------------------------------------------------------
		mavenize = new Mavenize(this);
	}

	/**
	 * Task creation.
	 * 
	 */
	@Override
	protected Task<Void> createTask()
	{
		return new Task<Void>()
		{
			protected Void call()
			{
				try
				{
					setActive(true);

					process();
				}
				catch (Exception e)
				{
					logger.error(e.getLocalizedMessage());
				}
				finally
				{
					setActive(false);
				}

				return null;
			}
		};
	}

	/**
	 * Main process.
	 * 
	 */
	public void process()
	{
		observableList.clear();
		
		resultCache.clear();

		ProjectType projectType = ProjectTypeFactory.instance().getProjectType(projectTypeName);

		try
		{
			mavenize.process(sourcePath, targetPath, projectType, version, packaging);
		}
		catch (IOException e)
		{
			logger.error(e.getLocalizedMessage());
		}
	}
	
	/**
	 * Callback - Project found.
	 * 
	 * This may be called more than once on the same project. The reason for this is the mechanism is set to find 'src'
	 * and the 'resource' files in separate passes. You have to use the project 'name' to determine if we've already
	 * created an entry for it.
	 */
	@Override
	public void addProjectFiles(ProjectFiles projectFiles)
	{
		ProjectResult target = resultCache.get(projectFiles.getName());

		if (target == null)
		{
			target = new ProjectResult();

			target.setFilePath(projectFiles.getName());
			target.setFileCount(0);

			logger.info("Found project, " + sourcePath);

			String name = projectFiles.getName();
			
			resultCache.put(name, target);
			
			// ---------------------------------------------------------------
			// Broadcast object. This will get picked up by bound listener.
			// ---------------------------------------------------------------
			if (!observableList.contains(target))
			{
				observableList.add(target);
			}
			
			target.setWorking(ProjectResult.WORKING_BUSY);
			
			refreshView.refresh();
		}
	}

	/**
	 * Callback - File found.
	 * 
	 */
	@Override
	public void addProjectFile(ProjectFiles projectFiles, File file)
	{
		//logger.info("Found file, " + file.getName());

		ProjectResult target = resultCache.get(projectFiles.getName());

		if (target != null)
		{
			int current = target.getFileCount() + 1;
			target.setFileCount(current);
		}

		// ---------------------------------------------------------------
		// Broadcast object. This will get picked up by bound listener.
		// ---------------------------------------------------------------
		target.setStatus(ProjectResult.STATUS_FILE);
		
		refreshView.refresh();
	}

	/**
	 * Callback - Generate pom in target path.
	 * 
	 */
	@Override
	public void generatePom(ProjectType projectType, ProjectFiles projectFiles, Model model)
	{
		logger.info("Generate pom, " + projectFiles.getTargetSrc());

		ProjectResult target = resultCache.get(projectFiles.getName());

		if (target != null)
		{
			target.setGroupId(model.getGroupId());
			target.setArtifactId(model.getArtifactId());
		}

		target.setStatus(ProjectResult.STATUS_POM);
		
		target.setWorking(ProjectResult.WORKING_DONE);
		
		refreshView.refresh();
	}

	/**
	 * Callback - Create Maven directories.
	 * 
	 */
	@Override
	public void createDirectories(ProjectFiles projectFiles)
	{
		logger.debug("Create directories, " + projectFiles.getTargetSrc());

		ProjectResult target = resultCache.get(projectFiles.getName());

		if (target != null)
		{
			target.setStatus(ProjectResult.STATUS_CREATE);
		}
		
		refreshView.refresh();
		
	}

	/**
	 * Callback - Copy source files.
	 * 
	 */
	@Override
	public void copyFiles(ProjectFiles projectFiles, String subDir)
	{
		logger.debug("Copy files, " + projectFiles.getTargetSrc());

		ProjectResult target = resultCache.get(projectFiles.getName());

		if (target != null)
		{
			target.setStatus(ProjectResult.STATUS_COPY);
		}

		refreshView.refresh();

	}

	/**
	 * Setters/Getters
	 * 
	 */

	@Override
	public boolean isActive()
	{
		return false;
	}

	public String getSourcePath()
	{
		return sourcePath;
	}

	public void setSourcePath(String sourcePath)
	{
		this.sourcePath = sourcePath;
	}

	public String getTargetPath()
	{
		return targetPath;
	}

	public void setTargetPath(String targetPath)
	{
		this.targetPath = targetPath;
	}

	public String getProjectTypeName()
	{
		return projectTypeName;
	}

	public void setProjectTypeName(String projectTypeName)
	{
		this.projectTypeName = projectTypeName;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getPackaging()
	{
		return packaging;
	}

	public void setPackaging(String packaging)
	{
		this.packaging = packaging;
	}

	public Mavenize getMavenize()
	{
		return mavenize;
	}

}
