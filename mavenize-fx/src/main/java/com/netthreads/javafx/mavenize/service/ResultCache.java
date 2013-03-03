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

import java.util.HashMap;
import java.util.Map;

import com.netthreads.javafx.mavenize.model.ProjectResult;

/**
 * Result cache.
 *
 */
public class ResultCache
{
	private static ResultCache instance = null;
	
	public synchronized static ResultCache instance()
	{
		if (instance == null)
		{
			instance = new ResultCache();
		}
		
		return instance;
	}
	
	private Map<String, ProjectResult> map;
	
	/**
	 * Construct object.
	 * 
	 */
	private ResultCache()
	{
		map = new HashMap<String, ProjectResult>();
	}
	
	/**
	 * Synchronised get from map.
	 * 
	 * @param name
	 * 
	 * @return The object or null if none found.
	 */
	public synchronized ProjectResult get(String name)
	{
		return map.get(name);
	}
	
	/**
	 * Put in cache.
	 * 
	 * @param name
	 *            The key.
	 * 
	 * @param projectResult
	 *            The value.
	 */
	public synchronized void put(String name, ProjectResult projectResult)
	{
		if (!map.containsKey(name))
		{
			map.put(name, projectResult);
		}
	}

	public void clear()
	{
		map.clear();
	}
}
