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
package com.netthreads.javafx.mavenize.client;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

import com.netthreads.javafx.mavenize.controller.ImplementsRefresh;
import com.netthreads.javafx.mavenize.model.ProjectResult;
import com.netthreads.javafx.mavenize.service.MavenizeService;

/**
 * Mavenize Client behaves like a middle layer between the controller and the service.
 * 
 */
public class MavenizeClient
{
	private MavenizeService mavenizeService;

	private SimpleBooleanProperty activeProperty;

	/**
	 * Mavenize Client
	 * 
	 * @param observableList
	 */
	public MavenizeClient(ObservableList<ProjectResult> observableList, ImplementsRefresh refreshView)
	{
		// Properties
		activeProperty = new SimpleBooleanProperty();

		mavenizeService = new MavenizeService(observableList, refreshView);

		// Bind our active property to the service property.
		activeProperty.bind(mavenizeService.getActiveProperty());
	}

	/**
	 * Process folders.
	 * 
	 * @param sourcePath
	 * @param targetPath
	 * 
	 * @return True if successful.
	 */
	public boolean process(String sourcePath, String targetPath, String version, String packaging)
	{
		boolean status = true;

		if (sourcePath == null || sourcePath.length() == 0 || targetPath == null || targetPath.length() == 0)
		{
			throw new IllegalArgumentException();
		}
		else
		{
			mavenizeService.setSourcePath(sourcePath);
			mavenizeService.setTargetPath(targetPath);
			mavenizeService.setVersion(version);
			mavenizeService.setPackaging(packaging);

			mavenizeService.reset();

			mavenizeService.start();
		}

		return status;
	}

	/**
	 * Return property bound to service activity status.
	 * 
	 * @return The active property.
	 */
	public SimpleBooleanProperty getActiveProperty()
	{
		return activeProperty;
	}

}
