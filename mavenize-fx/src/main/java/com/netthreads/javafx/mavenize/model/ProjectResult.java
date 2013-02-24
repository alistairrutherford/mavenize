package com.netthreads.javafx.mavenize.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Project result bean.
 * 
 */
public class ProjectResult
{
	public static final String ATTR_GROUP_ID = "groupId";
	public static final String ATTR_ARTIFACT_ID = "artifactId";
	public static final String ATTR_FILE_PATH = "filePath";
	public static final String ATTR_FILE_COUNT = "fileCount";
	public static final String ATTR_STATUS = "status";
	public static final String ATTR_WORKING = "working";

	public static final String TITLE_GROUP_ID = "groupId";
	public static final String TITLE_ARTIFACT_ID = "artifactId";
	public static final String TITLE_FILE_PATH = "File Path";
	public static final String TITLE_FILE_COUNT = "File Count";
	public static final String TITLE_STATUS = "Status";
	public static final String TITLE_WORKING = "~";

	public static final String STATUS_CREATE = "Creating";
	public static final String STATUS_COPY = "Copying";
	public static final String STATUS_FILE = "Add File";
	public static final String STATUS_POM = "Pom";

	public static final int WORKING_READY = 0;
	public static final int WORKING_BUSY = 1;
	public static final int WORKING_DONE = 2;

	private StringProperty groupIdProperty;
	private StringProperty artifactIdProperty;
	private StringProperty filePathProperty;
	private IntegerProperty fileCountProperty;
	private StringProperty statusProperty;
	private IntegerProperty workingProperty;

	/**
	 * Construct results.
	 * 
	 */
	public ProjectResult()
	{
		groupIdProperty = new SimpleStringProperty(this, ATTR_GROUP_ID);
		artifactIdProperty = new SimpleStringProperty(this, ATTR_ARTIFACT_ID);
		filePathProperty = new SimpleStringProperty(this, ATTR_FILE_PATH);
		fileCountProperty = new SimpleIntegerProperty(this, ATTR_FILE_COUNT);
		statusProperty = new SimpleStringProperty(this, ATTR_STATUS);
		workingProperty = new SimpleIntegerProperty(this, ATTR_WORKING);

		groupIdProperty.set("");
		artifactIdProperty.set("");
		filePathProperty.set("");
		fileCountProperty.set(0);
		statusProperty.set("");
		workingProperty.set(WORKING_READY);
	}

	public final String getGroupId()
	{
		return groupIdProperty.get();
	}

	public final void setGroupId(String groupId)
	{
		this.groupIdProperty.set(groupId);
	}

	public final String getArtifactId()
	{
		return artifactIdProperty.get();
	}

	public final void setArtifactId(String artifactId)
	{
		this.artifactIdProperty.set(artifactId);
	}

	public final String getFilePath()
	{
		return filePathProperty.get();
	}

	public final void setFilePath(String filePath)
	{
		this.filePathProperty.set(filePath);
	}

	public final int getFileCount()
	{
		return fileCountProperty.get();
	}

	public final void setFileCount(int fileCount)
	{
		this.fileCountProperty.set(fileCount);
	}

	public String getStatus()
	{
		return statusProperty.get();
	}

	public void setStatus(String status)
	{
		this.statusProperty.set(status);
	}

	public int getWorking()
	{
		return workingProperty.get();
	}

	public void setWorking(int working)
	{
		this.workingProperty.set(working);
	}

	/**
	 * Properties.
	 * 
	 */

	/**
	 * Return property.
	 * 
	 * @return The property.
	 */
	public final StringProperty groupIdProperty()
	{
		return groupIdProperty;
	}

	/**
	 * Return property.
	 * 
	 * @return The property.
	 */
	public StringProperty artifactIdProperty()
	{
		return groupIdProperty;
	}

	/**
	 * Return property.
	 * 
	 * @return The property.
	 */
	public StringProperty filePathProperty()
	{
		return filePathProperty;
	}

	/**
	 * Return property.
	 * 
	 * @return The property.
	 */
	public IntegerProperty fileCountProperty()
	{
		return fileCountProperty;
	}

	/**
	 * Return property.
	 * 
	 * @return The property.
	 */
	public IntegerProperty workingProperty()
	{
		return workingProperty;
	}

}
