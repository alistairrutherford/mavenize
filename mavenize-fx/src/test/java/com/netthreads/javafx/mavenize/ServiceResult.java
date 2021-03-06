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
package com.netthreads.javafx.mavenize;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Service Result Object.
 * 
 */
public class ServiceResult
{
	private IntegerProperty valueProperty;

	/**
	 * Construct property object.
	 * 
	 */
	public ServiceResult(int value)
	{
		valueProperty = new SimpleIntegerProperty();

		setValue(value);
	}

	public int getValue()
	{
		return valueProperty.get();
	}

	public void setValue(int value)
	{
		this.valueProperty.set(value);
	}

	public IntegerProperty valueProperty()
	{
		return valueProperty;
	}
}
