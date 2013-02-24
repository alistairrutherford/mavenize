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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.maven.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netthreads.mavenize.model.ProjectFiles;
import com.netthreads.mavenize.pom.PomGenerator;
import com.netthreads.mavenize.pom.PomGeneratorFactory;
import com.netthreads.mavenize.project.NetbeansJavaProjectType;
import com.netthreads.mavenize.project.ProjectType;
import com.netthreads.mavenize.project.ProjectTypeFactory;

/**
 * Mavenize
 * 
 * Attempts to convert an existing project into a Maven project by splitting the
 * source code and resources into the relevant dirs and making a stab at the pom
 * if you specify a source project type i.e. "netbeans".
 * 
 * The only specific types currently implemented are:
 * 
 * - Normal java project "default". - NetBeans java module netbeans.
 * 
 */
@SuppressWarnings("unchecked")
public class Mavenize
{
	
	private static Logger logger = LoggerFactory.getLogger(ProjectFileFinder.class);
	private static final String APP_MESSAGE = "Mavenize  version 1.0.0\nAlistair Rutherford, www.netthreads.co.uk, 2011.\nLicensed under the Apache License, Version 2.0.\n\n";
	private static final String ARGS_MESSAGE = "Arguments: -i<source dir> -o<target dir> -t<project type>[Optional] -v<version>[Optional] -p<packaging>[Optional]";
	private static final String TEXT_SRC = "src";
	private static final String TEXT_MAIN = "main";
	private static final String TEXT_TEST = "test";
	private static final String TEXT_JAVA = "java";
	private static final String TEXT_RESOURCES = "resources";
	private static final String DIR_MAIN_JAVA = "/" + TEXT_MAIN + "/" + TEXT_JAVA;
	private static final String DIR_MAIN_RESOURCES = "/" + TEXT_MAIN + "/" + TEXT_RESOURCES;
	private static final String DIR_TEST_JAVA = "/" + TEXT_TEST + "/" + TEXT_JAVA;
	private static final String DIR_TEST_RESOURCES = "/" + TEXT_TEST + "/" + TEXT_RESOURCES;
	private static final String ARG_INPUT = "-i";
	private static final String ARG_OUTPUT = "-o";
	private static final String ARG_TYPE = "-t";
	private static final String ARG_VERSION = "-v";
	private static final String ARG_PACKAGING = "-p";
	
	private MavenizeListener mavenizeListener;
	
	/**
	 * Construct mavenize instance.
	 * 
	 * @param mavenizeListener
	 */
	public Mavenize(MavenizeListener mavenizeListener)
	{
		this.mavenizeListener = mavenizeListener;
	}
	
	/**
	 * Main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		if (args.length > 1)
		{
			String sourcePath = "";
			String targetPath = "";
			String projectTypeName = ProjectType.Types.DEFAULT.toString();
			String version = PomGenerator.DEFAULT_VERSION;
			String packaging = PomGenerator.PACKAGE_TYPES[0];
			
			boolean isInput = false;
			boolean isOutput = false;
			for (String arg : args)
			{
				try
				{
					if (arg.startsWith(ARG_INPUT))
					{
						sourcePath = arg.substring(ARG_INPUT.length());
						isInput = true;
					}
					else if (arg.startsWith(ARG_OUTPUT))
					{
						targetPath = arg.substring(ARG_OUTPUT.length());
						isOutput = true;
					}
					else if (arg.startsWith(ARG_TYPE))
					{
						projectTypeName = arg.substring(ARG_TYPE.length());
					}
					else if (arg.startsWith(ARG_VERSION))
					{
						version = arg.substring(ARG_VERSION.length());
					}
					else if (arg.startsWith(ARG_PACKAGING))
					{
						packaging = arg.substring(ARG_PACKAGING.length());
					}
				}
				catch (Exception e)
				{
					logger.error("Can't process argument, " + arg + ", " + e.getMessage());
				}
			}
			
			// Project type.
			ProjectType projectType = ProjectTypeFactory.instance().getProjectType(projectTypeName);
			
			// Execute conversion.
			try
			{
				if (isInput && isOutput)
				{
					if (!sourcePath.equals(targetPath))
					{
						Mavenize mvnGather = new Mavenize(null);
						
						mvnGather.process(sourcePath, targetPath, projectType, version, packaging);
					}
					else
					{
						throw new MavenizeException("Input and output directories cannot be the same.");
					}
				}
				else
				{
					throw new MavenizeException("You must specify input and output directories");
				}
			}
			catch (MavenizeException e)
			{
				logger.error("Application error, " + e);
			}
			catch (IOException ioe)
			{
				logger.error("Application error, " + ioe);
			}
		}
		else
		{
			System.out.println(APP_MESSAGE + ARGS_MESSAGE);
		}
	}
	
	/**
	 * Main process method.
	 * 
	 * @param sourcePath
	 * @param targetPath
	 * @throws IOException
	 */
	public void process(String sourcePath, String targetPath, ProjectType projectType, String version, String packaging) throws IOException
	{
		// Generate records for all source parent, child and target directories.
		List<ProjectFiles> sourceFiles = buildProjectFiles(projectType, sourcePath, targetPath);
		
		logger.info("Source Files, " + sourceFiles.size());
		
		List<ProjectFiles> resourceFiles = buildResourceFiles(projectType, sourcePath, targetPath);
		
		logger.info("Resource Files, " + resourceFiles.size());
		
		logger.info("Generate directory structure, " + sourceFiles.size());
		
		// Generate Maven directory structure
		makeMavenDirs(projectType, sourceFiles);
		
		logger.info("Copy main files.");
		
		// Copy over project files.
		insertMainProjectFiles(projectType, sourceFiles, resourceFiles);
		
		logger.info("Copy test files.");
		
		// Copy over test files.
		insertTestProjectFiles(projectType, sourceFiles, resourceFiles);
		
		logger.info("Generate pom(s).");
		
		// Generate pom(s).
		insertProjectPom(projectType, version, packaging, sourceFiles);
		
		logger.info("Done.");
	}
	
	/**
	 * Make Maven directory structure.
	 * 
	 * @param projectType
	 *            The conversion project type.
	 * @param projects
	 *            The profile files structure.
	 * 
	 * @throws IOException
	 */
	private void makeMavenDirs(ProjectType projectType, List<ProjectFiles> projects) throws IOException
	{
		logger.info("Creating Maven directory structure.");
		
		for (ProjectFiles project : projects)
		{
			File targetSrc = project.getTargetSrc();
			String targetSrcPath = targetSrc.getAbsolutePath();
			
			if (isValidSourceDir(projectType, targetSrcPath))
			{
				// Create directories
				createDir(DIR_MAIN_JAVA, targetSrcPath);
				createDir(DIR_MAIN_RESOURCES, targetSrcPath);
				createDir(DIR_TEST_JAVA, targetSrcPath);
				createDir(DIR_TEST_RESOURCES, targetSrcPath);
				
				if (mavenizeListener != null)
				{
					mavenizeListener.createDirectories(project);
				}
			}
		}
	}
	
	/**
	 * Will copy project files to main area.
	 * 
	 * @param projectType
	 * @param sourceFiles
	 * @param resourceFiles
	 */
	private void insertMainProjectFiles(ProjectType projectType, List<ProjectFiles> sourceFiles, List<ProjectFiles> resourceFiles) throws IOException
	{
		copyMainFiles(projectType, DIR_MAIN_JAVA, sourceFiles);
		
		copyMainFiles(projectType, DIR_MAIN_RESOURCES, resourceFiles);
	}
	
	/**
	 * Will copy any test files into test area.
	 * 
	 * @param projectType
	 * @param sourceFiles
	 * @param resourceFiles
	 */
	private void insertTestProjectFiles(ProjectType projectType, List<ProjectFiles> sourceFiles, List<ProjectFiles> resourceFiles) throws IOException
	{
		
		copyTestFiles(projectType, DIR_TEST_JAVA, sourceFiles);
		
		copyTestFiles(projectType, DIR_TEST_RESOURCES, resourceFiles);
		
	}
	
	/**
	 * Attempt to generate pom.
	 * 
	 * We can't do much for a general java project but for specific types it
	 * might be possible to glean something from the project definition files.
	 * 
	 * @param projectType
	 * @param projectFiles
	 * 
	 * @throws IOException
	 */
	private void insertProjectPom(ProjectType projectType, String version, String packaging, List<ProjectFiles> projectFiles) throws IOException
	{
		PomGenerator pomGenerator = PomGeneratorFactory.instance().getGenerator(projectType);
		
		// For each project generate an appropriate pom file.
		for (ProjectFiles project : projectFiles)
		{
			File targetSrc = project.getTargetSrc();
			String targetSrcPath = targetSrc.getAbsolutePath();
			
			// If is a valid source directory according to type.
			if (isValidSourceDir(projectType, targetSrcPath))
			{
				Model model = pomGenerator.generate(project, version, packaging);
				
				if (mavenizeListener != null)
				{
					mavenizeListener.generatePom(projectType, project, model);
				}
			}
		}
	}
	
	/**
	 * Copy test files.
	 * 
	 * @param projectType
	 * @param subDir
	 * @param projectFiles
	 * 
	 * @throws IOException
	 */
	private void copyMainFiles(ProjectType projectType, String subDir, List<ProjectFiles> projectFiles) throws IOException
	{
		for (ProjectFiles project : projectFiles)
		{
			File targetSrc = project.getTargetSrc();
			String targetSrcPath = targetSrc.getAbsolutePath();
			
			// If is a valid source direcory according to type.
			if (isValidSourceDir(projectType, targetSrcPath))
			{
				copyFiles(targetSrcPath, subDir, project);
				
				if (mavenizeListener != null)
				{
					mavenizeListener.copyFiles(project, subDir);
				}
				
			}
			
		}
	}
	
	/**
	 * Copy test files.
	 * 
	 * @param projectType
	 * @param subDir
	 * @param projectFiles
	 * 
	 * @throws IOException
	 */
	private void copyTestFiles(ProjectType projectType, String subDir, List<ProjectFiles> projectFiles) throws IOException
	{
		// If not a specific type of project then don't bother.
		ProjectType.Types type = projectType.getType();
		if (type != ProjectType.Types.DEFAULT)
		{
			for (ProjectFiles project : projectFiles)
			{
				File sourceSrc = project.getSourceSrc();
				String sourceSrcPath = sourceSrc.getAbsolutePath();
				
				// If is source of tests according to project type.
				if (isTestDir(projectType, sourceSrcPath))
				{
					File targetSrc = project.getTargetSrc();
					String targetSrcPath = targetSrc.getAbsolutePath();
					
					// Adjust according to target type.
					String adjustedTestPath = adjustTestDir(projectType, targetSrcPath);
					
					copyFiles(adjustedTestPath, subDir, project);
					
					if (mavenizeListener != null)
					{
						mavenizeListener.copyFiles(project, subDir);
					}
				}
			}
		}
	}
	
	/**
	 * Adjust the supplied directory to Maven style path.
	 * 
	 */
	private String adjustTestDir(ProjectType projectType, String sourceDir)
	{
		String adjustedDir = sourceDir;
		
		switch (projectType.getType())
		{
		// If netbeans then ignore the test/unit directory
			case NETBEANS:
				int index = sourceDir.length() - NetbeansJavaProjectType.TEST_DIR.length();
				adjustedDir = sourceDir.substring(0, index) + TEXT_SRC;
				break;
			default:
				break;
		}
		
		return adjustedDir;
	}
	
	/**
	 * Copy files to target.
	 * 
	 * @param targetPath
	 * @param targetPathType
	 * @param projectFiles
	 */
	private void copyFiles(String targetSrcPath, String subDir, ProjectFiles projectFiles) throws IOException
	{
		List<File> files = projectFiles.getFiles();
		
		for (File file : files)
		{
			String sourceSrcPath = projectFiles.getSourceSrc().getAbsolutePath();
			String filePath = file.getAbsolutePath();
			
			String partial = filePath.substring(sourceSrcPath.length());
			
			String targetPath = targetSrcPath + subDir + partial;
			
			logger.debug("From : " + filePath + ", To :" + targetPath);
			
			FileUtils.copyFile(file, new File(targetPath), true);
		}
	}
	
	/**
	 * Depending on the project target type test to see if we are interested in
	 * target directory.
	 * 
	 * @param projectType
	 * @param sourceDirParent
	 * 
	 * @return Directory is valid.
	 */
	private boolean isValidSourceDir(ProjectType projectType, String sourceDir)
	{
		boolean status = true;
		
		switch (projectType.getType())
		{
		// If netbeans then ignore the test/unit directory
			case NETBEANS:
				status = !isTestDir(projectType, sourceDir);
				break;
			default:
				break;
		}
		
		return status;
	}
	
	/**
	 * Checks to see if supplied directory is a test directory.
	 * 
	 * @param projectType
	 * @param sourceDir
	 * 
	 * @return True is test directory for supplied project type.
	 */
	private boolean isTestDir(ProjectType projectType, String sourceDir)
	{
		boolean status = false;
		
		switch (projectType.getType())
		{
		// If netbeans then ignore the test/unit directory
			case NETBEANS:
				int index = sourceDir.length() - NetbeansJavaProjectType.TEST_DIR.length();
				String partial = sourceDir.substring(index);
				status = pathEquals(partial, NetbeansJavaProjectType.TEST_DIR);
				break;
			default:
				break;
		}
		
		return status;
	}
	
	/**
	 * Get project source files.
	 * 
	 * @param sourcePath
	 *            The source directory path.
	 * @param targetPath
	 *            The target directory path.
	 * 
	 * @return List of parent source directories and child directories.
	 * 
	 * @throws IOException
	 */
	private List<ProjectFiles> buildProjectFiles(ProjectType projectType, String sourcePath, String targetPath) throws IOException
	{
		List<ProjectFiles> results = new LinkedList<ProjectFiles>();
		
		IOFileFilter srcDirFilter = DirectoryFileFilter.DIRECTORY;
		
		// Create a filter for Files ending in ".txt"
		String suffix = projectType.getSuffix();
		IOFileFilter srcFileFilter = FileFilterUtils.suffixFileFilter(suffix);
		
		// Combine the directory and file filters using an OR condition
		java.io.FileFilter srcFilter = FileFilterUtils.or(srcDirFilter, srcFileFilter);
		
		// Finder for all directories, no depth limit but we will limit on name.
		ProjectFileFinder projectFileFinder = new ProjectFileFinder(srcFilter, -1);
		
		results = projectFileFinder.find(sourcePath, targetPath, TEXT_SRC);
		
		return results;
	}
	
	/**
	 * Get project resource files.
	 * 
	 * @param sourcePath
	 *            The source directory path.
	 * @param targetPath
	 *            The target directory path.
	 * 
	 * @return List of parent source directories and child directories.
	 * 
	 * @throws IOException
	 */
	private List<ProjectFiles> buildResourceFiles(ProjectType projectType, String sourcePath, String targetPath) throws IOException
	{
		List<ProjectFiles> results = new LinkedList<ProjectFiles>();
		
		IOFileFilter resourceDirFilter = DirectoryFileFilter.DIRECTORY;
		
		// Create a filter for Files ending in ".txt"
		String suffix = projectType.getSuffix();
		IOFileFilter resourceFileFilter = FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(suffix));
		
		// Combine the directory and file filters using an OR condition
		java.io.FileFilter srcFilter = FileFilterUtils.or(resourceDirFilter, resourceFileFilter);
		
		// Finder for all directories, no depth limit but we will limit on name.
		ProjectFileFinder projectFileFinder = new ProjectFileFinder(srcFilter, -1);
		
		results = projectFileFinder.find(sourcePath, targetPath, TEXT_SRC);
		
		return results;
	}
	
	/**
	 * Create directory if it doesn't already exist.
	 * 
	 * @param name
	 *            The directory name.
	 * 
	 * @param directory
	 *            The parent directory file object.
	 */
	private void createDir(String name, String path) throws IOException
	{
		String dirPath = path + name;
		
		File newDir = new File(dirPath);
		
		if (!newDir.exists())
		{
			logger.debug("Creating target directory, " + path);
			
			FileUtils.forceMkdir(newDir);
		}
	}
	
	/**
	 * Helper method to replace the Windows type backslash with a proper path
	 * one.
	 * 
	 * @param path
	 * 
	 * @return Fixed path string.
	 */
	private String fixPath(String path)
	{
		return path.replace("\\", "/");
	}
	
	/**
	 * Compare paths so that the backslashes are removed.
	 * 
	 * @param pathA
	 * @param pathB
	 * 
	 * @return True if paths are equal.
	 */
	private boolean pathEquals(String pathA, String pathB)
	{
		return fixPath(pathA).equals(fixPath(pathB));
	}
	
	/*
	 * Project File Finder class.
	 * 
	 * This class takes a target directory name and builds a list of files which
	 * reside under that name. There can be multiple instances i.e multiple
	 * projects.
	 */
	@SuppressWarnings("rawtypes")
	private class ProjectFileFinder extends DirectoryWalker
	{
		
		private String match;
		private String sourcePath;
		private String targetPath;
		private List results;
		
		/**
		 * Takes target directory name and filter object.
		 * 
		 * @param target
		 *            If this is null then
		 * @param fileFilter
		 */
		public ProjectFileFinder(FileFilter fileFilter, int depth)
		{
			super(fileFilter, depth);
		}
		
		/**
		 * Find instances of named target directory starting at the path
		 * specified.
		 * 
		 * @param path
		 *            The starting directory.
		 * @param target
		 *            The name of the target directory to search for.
		 * 
		 * @return List of search results.
		 * 
		 * @throws IOException
		 */
		public List find(String sourcePath, String targetPath, String match) throws IOException
		{
			this.results = new ArrayList();
			
			this.match = match;
			this.sourcePath = sourcePath;
			this.targetPath = targetPath;
			
			File file = new File(sourcePath);
			
			walk(file, results);
			
			return results;
		}
		
		/**
		 * Handle hitting a file. We look for owning project in our collection
		 * and if we find one then add it else create a new project entry and
		 * add it.
		 * 
		 * @param file
		 * @param depth
		 * @param results
		 * @throws IOException
		 */
		@Override
		protected void handleFile(File file, int depth, Collection results) throws IOException
		{
			// Look for existing entry
			ProjectFiles projectFiles = (ProjectFiles) CollectionUtils.find(results, new FilePredicate(file));
			
			if (projectFiles != null)
			{
				logger.debug(projectFiles.getSourceSrc().getAbsolutePath() + ", " + file.getAbsolutePath());
				
				projectFiles.getFiles().add(file);
				
				if (mavenizeListener != null)
				{
					mavenizeListener.addProjectFile(projectFiles, file);
				}
			}
			
		}
		
		/**
		 * Handle finding a directory.
		 * 
		 * @param directory
		 * @param depth
		 * @param results
		 * 
		 * @return Returns true because we keep going,
		 */
		@Override
		protected boolean handleDirectory(File directory, int depth, Collection results)
		{
			// If we have hit "target" i.e. "src" directory then we look for
			// existing project, if none found then create one.
			if (directory.getName().equals(match))
			{
				String name = directory.getParent();
				
				// Look for existing entry
				ProjectFiles projectFiles = (ProjectFiles) CollectionUtils.find(results, new NamePredicate(name));
				
				if (projectFiles == null)
				{
					logger.debug(directory.getAbsolutePath());
					
					// Build target src dir to match source project
					String partial = directory.getAbsolutePath().substring(sourcePath.length());
					String targetSrc = targetPath + "/" + partial;
					File target = new File(targetSrc);
					
					projectFiles = new ProjectFiles(name, directory, target);
					results.add(projectFiles);
					
					if (mavenizeListener != null)
					{
						mavenizeListener.addProjectFiles(projectFiles);
					}
				}
			}
			
			return true;
		}
	}
	
	/**
	 * Implements Project file search predicate to find 'named' project from
	 * collection.
	 * 
	 */
	private class NamePredicate implements Predicate
	{
		
		String name;
		
		public NamePredicate(String name)
		{
			this.name = name;
		}
		
		@Override
		public boolean evaluate(Object object)
		{
			ProjectFiles projectFiles = (ProjectFiles) object;
			
			boolean status = projectFiles.getName().equals(name);
			
			return status;
		}
	}
	
	/**
	 * Implements File predicate.
	 * 
	 */
	private class FilePredicate implements Predicate
	{
		
		File file;
		
		public FilePredicate(File file)
		{
			this.file = file;
		}
		
		/**
		 * Find a match for projects files to which the supplied File belongs.
		 * 
		 * @param object
		 * 
		 * @return True if found.
		 */
		@Override
		public boolean evaluate(Object object)
		{
			boolean status = false;
			
			ProjectFiles projectFiles = (ProjectFiles) object;
			
			String projectSourceSrc = projectFiles.getSourceSrc().getAbsolutePath();
			String fileSourceSrc = file.getAbsolutePath();
			int projectSourceSrcLen = projectSourceSrc.length();
			int fileSourceSrcLen = fileSourceSrc.length();
			
			if (fileSourceSrcLen > projectSourceSrcLen)
			{
				// Is file part of project src dir?
				String partial = fileSourceSrc.substring(0, projectSourceSrcLen);
				status = projectSourceSrc.equals(partial);
			}
			
			return status;
		}
	}
	
}
