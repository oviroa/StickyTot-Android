package com.garagewarez.bubu.android.base;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;

import android.widget.Button;



import com.garagewarez.bubu.android.PrettyRefreshDialog;
import com.garagewarez.bubu.android.R;

/**
 * Abstract class to be subclassed by all collection based activities
 * @author oviroa
 *
 */
public abstract class BubuCollectionActivity extends BubuBaseActivity 
{
	
    /**
     * AsyncTask flavor, used for deletion of elements in the collection
     */
	protected RotationSafeCollectionAsyncTask deleteTask;
	
	/**
	 * Refresh dialog to display when new data is available 
	 */
	protected PrettyRefreshDialog refreshDialog;
	
	/**
	 * Add button, used to trigger activity used to collect data for new object in the collection
	 */
	protected Button addButton;
	
	
	//Dialog for confirming deletion
	protected DialogInterface.OnClickListener dialogClickListener;
	
	//dialog for yes/no at deletion
	protected AlertDialog alert;
	
	/**
	 * parent key for data set
	 */
	public String parentKey;
	
	public String childKey;

	protected boolean selectedChildIsJoint;
	
	/**
     * Hide dialog and kill running async task
     * 
     */
    public void killProgressDialog()
    {
    	if(progressDialog.isShowing())
    		progressDialog.dismiss();
    	
    	deleteTask = null;
    }
    
	
    /**
     * Used to refresh content of collection
     */
    protected abstract void handleRefresh();
    
    
    /**
     * Handles selector for pagination 
     */
    public abstract void handleSelector();
    
    
    /**
     * Loads data into activity state
     */
    public abstract void loadData();
 
    
    /**
     * Initializes progress dialog with copy and custom font 
     */
    protected void createProgressDialog()
    {
		progressDialog = new PrettyProgressDialog(this);
		progressDialog.show(getResources().getString(R.string.bbLoading), getTypeface());
    		
    }
    
    /**
     * Handles task state at screen rotation
     * @param lastNonConfigurationInstance
     */
    protected void handleRunningAsyncTask(RotationSafeCollectionAsyncTask lastNonConfigurationInstance)
    {
    	//if progress dialog is not instantiated, create one and make it invisible 
    	if(progressDialog == null)
    	{	
    		progressDialog = new PrettyProgressDialog(this);
    		progressDialog.show(getResources().getString(R.string.bbLoading), getTypeface());
    		progressDialog.dismiss();
    	}
    		
    	
    	//handle async task
    	deleteTask = lastNonConfigurationInstance;

        if (deleteTask != null) 
        {
        	deleteTask.attach(this);
     	    progressDialog.show();
        }
    }
    
    /**
     * Populates collection with content (parameter)
     */
	public abstract void refresh();
	

	/**
	 * Navigates to activity used to enter new object content
	 * @param target
	 */
	protected void addObject(Class<?> target)
	{
		//clear persistent data
    	clearForm();
        
        // Perform action on clicks
        Intent i = new Intent(this, target);
        startActivity(i);
	}
	
	/**
	 * Clear image related persistent data before it navigates to activity used to edit selected object content
	 * @param id
	 */
	protected void onEditObject(long id)
	{
		//clear persistent data
		clearForm();
	}
	
	/**
	 * Removes selected object
	 * @param id
	 */
	protected abstract void onDeleteObject(long id);
	
	/**
	 * Construct refresh dialog
	 */
	protected void displayRefreshDialog()
	{
		//create dimensions dialog if null
    	if(refreshDialog == null)
    		refreshDialog = new PrettyRefreshDialog(BubuCollectionActivity.this, R.style.BubuDialogTheme);
    	
    	//display and populate dialog
    	refreshDialog.show(tf);
    	
    	//event handler for closing dialog
    	//set state with data collected in dialog
    	refreshDialog.setOnDismissListener(new OnDismissListener() 
    	{
    		@Override
    		public void onDismiss(DialogInterface dialog) 
    		{
    			loadData();
    			refresh();
    		}
    	});
	}
}
