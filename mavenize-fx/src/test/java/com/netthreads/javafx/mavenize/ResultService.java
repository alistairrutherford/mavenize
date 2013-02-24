package com.netthreads.javafx.mavenize;

import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ResultService extends Service<Void>
{
	public static final int ITEM_COUNT = 100;
	
	private ObservableList<ServiceResult> observableList;
	
	private RefreshDataView refresher;
	
	/**
	 * Construct service.
	 * 
	 */
	public ResultService(ObservableList<ServiceResult> observableList, RefreshDataView refresher)
	{
		this.observableList = observableList;
		
		this.refresher = refresher;
	}
	
	@Override
	protected Task<Void> createTask()
	{
		return new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				process();
				
				return null;
			}
		};
	}
	
	public void process()
	{
		for (int i = 0; i < ITEM_COUNT; i++)
		{
			observableList.add(new ServiceResult(i));
			
			refresher.refresh();
		}
		
	}
	
}
