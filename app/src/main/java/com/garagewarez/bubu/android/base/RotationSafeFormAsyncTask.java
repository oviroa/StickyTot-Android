package com.garagewarez.bubu.android.base;

import android.os.AsyncTask;

/**
 * Custom AsyncTask base class, used for form activities (submission of new or edited objects)
 * @author oviroa
 *
 */
public class RotationSafeFormAsyncTask extends AsyncTask<Object, Integer, Object> 
{

	//activity attached to the task
	protected BubuFormActivity activity =  null;
	
	/**
	 * Constructor, attaches activity
	 * @param activity
	 */
	protected RotationSafeFormAsyncTask(BubuFormActivity activity) 
	{
		attach(activity);
	}
	
	/**
	 * Detaches activity from task
	 */
	public void detach() 
	{
		activity=null;
	}

	@Override
	protected void onPreExecute()
	{
		//display dialog
		activity.createProgressDialog();			
	}
	
		
	@Override
	protected Object doInBackground(Object... params) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Attaches activity to task
	 * @param activity
	 */
	public void attach(BubuFormActivity activity) 
	{
		this.activity=activity;		
	}
	

}
