package com.netthreads.mavenize;

import java.io.File;

import org.apache.maven.model.Model;

import com.netthreads.mavenize.model.ProjectFiles;
import com.netthreads.mavenize.project.ProjectType;

/**
 * Defines a listener which provides call backs for tracking the status of the
 * mavenize process.
 * 
 */
public interface MavenizeListener
{
	public void addProjectFiles(ProjectFiles projectFiles);
	
	public void addProjectFile(ProjectFiles projectFiles, File file);
	
	public void createDirectories(ProjectFiles projectFiles);
	
	public void copyFiles(ProjectFiles projectFiles, String subDir);
	
	public void generatePom(ProjectType projectType, ProjectFiles projectFiles, Model model);
	
	public boolean isActive();
}
