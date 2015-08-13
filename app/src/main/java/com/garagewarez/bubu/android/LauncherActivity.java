package com.garagewarez.bubu.android;

import java.io.IOException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


import com.garagewarez.bubu.android.proxy.Proxy;

/**
 * Forks towards account activity or kids activity, depending on the existence of locally stored account and data
 * @author oviroa
 *
 */
public class LauncherActivity extends Activity  
{
	//proxy - instance class for storing/retrieving data from state, local storage, server - singletom
	private Proxy myProxy; 
	private Intent intent;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        
    	super.onCreate(savedInstanceState); 
    	
    	//get intent to check if something got shared
    	intent = getIntent();
    	//get stuff from intent
		Bundle extras = intent.getExtras();
        String action = intent.getAction();
        
    	//instantiate Proxy singleton - will be used for auth and server calls
    	myProxy = Proxy.getInstance();
    	
    	//account name was stored locally
    	if(myProxy.getStoredAccountName(getApplicationContext()) != null)
    	{
    		
    		try 
    		{
				//retrieve kids data
    			if(myProxy.getStoredChildResponse(getApplicationContext()) != null)
				{
    				//check if something got shared
        	        if (Intent.ACTION_SEND.equals(action) && extras.containsKey(Intent.EXTRA_STREAM))
    	            {
    	        		//add image to intent for next activity
    	        		intent.setClass(LauncherActivity.this, KidsActivity.class);
    	            }
        	        else
    	        	{
    	        		intent = new Intent(LauncherActivity.this, KidsActivity.class);
    	        	}	
        	        	
    				
    				navigateToNextActivity(intent);
					
				}
				else
				{
					//check if something got shared
			        if (Intent.ACTION_SEND.equals(action) && extras.containsKey(Intent.EXTRA_STREAM))
			        {
			        	
			        	//add image to intent for next activity
		        		intent.setClass(LauncherActivity.this, AccountActivity.class);
		        		navigateToNextActivity(intent);
		            }
			        else
			        	navigateToNextActivity(new Intent(LauncherActivity.this, AccountActivity.class));
				}	
				
				
			} 
    		catch (OptionalDataException e) 
			{
				e.printStackTrace();
				navigateToNextActivity(new Intent(LauncherActivity.this, AccountActivity.class));
			} 
    		catch (StreamCorruptedException e) 
    		{
				e.printStackTrace();
				navigateToNextActivity(new Intent(LauncherActivity.this, AccountActivity.class));
			} 
    		catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
				navigateToNextActivity(new Intent(LauncherActivity.this, AccountActivity.class));
			} 
    		catch (IOException e) 
    		{
				e.printStackTrace();
				navigateToNextActivity(new Intent(LauncherActivity.this, AccountActivity.class));
			}
    	}	
    	else//no data, go to account selection
		{
    		//check if something got shared
	        if (Intent.ACTION_SEND.equals(action) && extras.containsKey(Intent.EXTRA_STREAM))
	        {
	        	
	        	//add image to intent for next activity
        		intent.setClass(LauncherActivity.this, AccountActivity.class);
        		navigateToNextActivity(intent);
            }
	        else
	        	navigateToNextActivity(new Intent(LauncherActivity.this, AccountActivity.class));
		}		
    	
    }
	
	private void navigateToNextActivity(Intent i)
	{
		startActivity(i);
		finish();
	}
}
