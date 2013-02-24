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
