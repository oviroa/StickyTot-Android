package com.garagewarez.bubu.android.base;

import android.os.AsyncTask;

/**
 * Custom AsyncTask base class, used for collection (deletion of collection element)
 * @author oviroa
 *
 */
public class RotationSafeCollectionAsyncTask extends AsyncTask<String, Void, Object> 
{

	//activity attached to the task
	protected BubuCollectionActivity activity =  null;
	
	/**
	 * Constructor, attaches activity
	 * @param activity
	 */
	protected RotationSafeCollectionAsyncTask(BubuCollectionActivity activity) 
	{
		attach(activity);
	}
	
	/**
	 * Clears activity
	 */
	public void detach() 
	{
		activity=null;
	}

	@Override
	protected void onPreExecute()
	{
		//show dialog
		activity.createProgressDialog();			
	}
	
	@Override
	protected Object doInBackground(String... params) 
	{
		return null;
	}

	/**
	 * Attaches activity to task
	 * @param activity
	 */
	public void attach(BubuCollectionActivity activity) 
	{
		this.activity=activity;		
	}
	
}
