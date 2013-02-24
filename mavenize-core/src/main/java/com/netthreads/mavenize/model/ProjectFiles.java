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
package com.netthreads.mavenize.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a collection of src files which form a project.
 * 
 */
public class ProjectFiles
{
	private String name;
	private File sourceSrc;
	private File targetSrc;
	private List<File> files;
	
	/**
	 * Construct a project.
	 * 
	 * @param name
	 *            The unique project 'name'.
	 * @param source
	 *            The project source directory.
	 * @param target
	 *            The project target directory
	 */
	public ProjectFiles(String name, File source, File target)
	{
		this.name = name;
		this.sourceSrc = source;
		this.targetSrc = target;
		
		files = new LinkedList<File>();
	}
	
	public List<File> getFiles()
	{
		return files;
	}
	
	public void setFiles(List<File> files)
	{
		this.files = files;
	}
	
	public File getSourceSrc()
	{
		return sourceSrc;
	}
	
	public void setSourceSrc(File parent)
	{
		this.sourceSrc = parent;
	}
	
	public File getTargetSrc()
	{
		return targetSrc;
	}
	
	public void setTargetSrc(File target)
	{
		this.targetSrc = target;
	}
	
	/**
	 * Unique name for this project.
	 * 
	 * @return The project name.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Set unique name for the project.
	 * 
	 * @param name
	 *            The project name.
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
}
