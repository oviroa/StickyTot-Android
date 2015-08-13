package com.garagewarez.bubu.android;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.http.client.ClientProtocolException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.garagewarez.bubu.android.common.RestletResponse;
import com.garagewarez.bubu.android.proxy.Proxy;
import com.garagewarez.bubu.android.utils.Debug;

public class MyC2dmReceiver extends BroadcastReceiver 
{
	private AsyncTask<String, Void, Boolean> registerAppTask;
	private AsyncTask<Void, Void, Boolean> unregisterAppTask;
	private Context myContext;
	
	private String registration;
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		myContext = context;
		
		if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) 
		{
	        handleRegistration(context, intent);
	    } 
		else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) 
		{
	        
			//handles returned data within CD2M service intent
			handleMessage(context, intent);
			
	    }
	 }

	private void handleRegistration(Context context, Intent intent) 
	{
	    myContext = context;
		
		registration = intent.getStringExtra("registration_id");
	    if (intent.getStringExtra("error") != null) 
	    {
	        // Registration failed, should try again later.
	    	Log.w(Debug.TAG,"BUBU::c2dm :: registration failed");
		    String error = intent.getStringExtra("error");
		    if(error == "SERVICE_NOT_AVAILABLE")
		    {
		    	Log.w(Debug.TAG, "BUBU::c2dm :: SERVICE_NOT_AVAILABLE");
		    }
		    else if(error == "ACCOUNT_MISSING")
		    {
		    	Log.w(Debug.TAG,"BUBU::c2dm :: ACCOUNT_MISSING");
		    }
		    else if(error == "AUTHENTICATION_FAILED")
		    {
		    	Log.w(Debug.TAG,"BUBU::c2dm :: AUTHENTICATION_FAILED");
		    }
		    else if(error == "TOO_MANY_REGISTRATIONS")
		    {
		    	Log.w(Debug.TAG,"BUBU::c2dm :: TOO_MANY_REGISTRATIONS");
		    }
		    else if(error == "INVALID_SENDER")
		    {
		    	Log.w(Debug.TAG,"BUBU::c2dm :: INVALID_SENDER");
		    }
		    else if(error == "PHONE_REGISTRATION_ERROR")
		    {
		    	Log.w(Debug.TAG,"BUBU::c2dm :: PHONE_REGISTRATION_ERROR");
		    }
	    } 
	    else if (intent.getStringExtra("unregistered") != null) 
	    {
	    	unregisterAppTask = new UnregisterAppTask();
		    unregisterAppTask.execute();
	        	

	    } 
	    else if (registration != null) 
	    {
	    	
	    	registerAppTask = new RegisterAppTask();
	        registerAppTask.execute(registration);
	        
	    }
	}
	
	
	/**
     * Registers app for CD2M
     * @author oviroa
     *
     */
    private class RegisterAppTask extends AsyncTask<String, Void, Boolean> 
    {
        
    	protected Boolean doInBackground(String... arg0) 
    	{
    		
    		Proxy myProxy = Proxy.getInstance();
	    	
	    	
	    		RestletResponse myResponse;
				try 
				{
					myResponse = myProxy.storeC2DMId(myContext.getApplicationContext(),arg0[0]);
					if(!myResponse.getMessage().equals("ok"))
		    			return false;
				} 
				catch (ClientProtocolException e) 
				{
					
					e.printStackTrace();
					return false;
					
				} 
				catch (IOException e) 
				{
					//server connection issue, try again
					try 
					{
						myResponse = myProxy.storeC2DMId(myContext.getApplicationContext(),arg0[0]);
						if(!myResponse.getMessage().equals("ok"))
			    			return false;
					} 
					catch (ClientProtocolException e1) 
					{
						
						return false;
						
					} 
					catch (IOException e1) 
					{
						return false;
					}
					catch (Exception e1) 
					{
						return false;
					}
				}
				catch (Exception e) 
				{
					e.printStackTrace();
					return false;
				}
    		
    		return true;
    	}
    	
        protected void onPostExecute(Boolean result) 
        {
        	if(result)
        		Log.w(Debug.TAG,"BUBU::MyC2dmReceiver.RegisterAppTask.doInBackground :: success");
        	else
        		Log.w(Debug.TAG,"BUBU::MyC2dmReceiver.RegisterAppTask.doInBackground :: error");
        		
        }

    }
    
    /**
     * Unregisters app from CD2M
     * @author oviroa
     *
     */
    private class UnregisterAppTask extends AsyncTask<Void, Void, Boolean> 
    {
        
    	protected Boolean doInBackground(Void... arg0) 
    	{
    		
    		Proxy myProxy = Proxy.getInstance();
	    	
    		RestletResponse myResponse;
			try 
			{
				myResponse = myProxy.removeC2DMId(myContext.getApplicationContext());
				if(!myResponse.getMessage().equals("ok"))
	    			return false;
		    	
			} 
			catch (ClientProtocolException e) 
			{
				e.printStackTrace();
				return false;
			} 
			catch (IOException e) 
			{
				try 
				{
					myResponse = myProxy.removeC2DMId(myContext.getApplicationContext());
					if(!myResponse.getMessage().equals("ok"))
		    			return false;
			    	
				} 
				catch (ClientProtocolException e1) 
				{
					e1.printStackTrace();
					return false;
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
					return false;
				}
				catch (Exception e1) 
				{
					e1.printStackTrace();
					return false;
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
				return false;
			}
    		
    		return true;
    	}
    	
        protected void onPostExecute(Boolean result) 
        {
        	if(result)
        		Log.w(Debug.TAG,"BUBU::MyC2dmReceiver.UnegisterAppTask.doInBackground :: success");
        	else
        		Log.w(Debug.TAG,"BUBU::MyC2dmReceiver.UnregisterAppTask.doInBackground :: error");
        	
        }

    }
	
    
    UpdateAllTask uaTask;
    
    /**
     * Launch async task (away from UI thread)
     * @param context
     * @param intent
     */
	private void handleMessage(Context context, Intent intent)
	{
		
		uaTask  = new UpdateAllTask(context,intent);
		uaTask.execute();
	}
	
	/**
	 * Pulls new data from server and updates local storage with
	 * @author oviroa
	 *
	 */
	private class UpdateAllTask extends AsyncTask<Void, Void, Boolean> 
	{
	    Context context = null;
	    Intent intent = null;
	    
	    UpdateAllTask(Context context, Intent intent)
	    {
	    	this.context = context;
	    	this.intent = intent;
	    }
		
		protected Boolean doInBackground(Void... voids) 
	    {
			Proxy myProxy = Proxy.getInstance();
			
				try 
				{
					myProxy.updateAll
						(
								context.getApplicationContext(), 
								intent.getStringExtra(context.getResources().getString(R.string.bbUpdateEventUpdated)),
								intent.getStringExtra(context.getResources().getString(R.string.bbChildKey)), 
								intent.getStringExtra(context.getResources().getString(R.string.bbReset))
						);
				} 
				catch (ClassNotFoundException e) 
		    	{
					e.printStackTrace();
					return false;
				}
				catch (MalformedURLException e) 
				{
					e.printStackTrace();
					return false;
				} 
				catch (NotFoundException e) 
				{
					e.printStackTrace();
					return false;
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
					return false;
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
					return false;
					
				}
				
				
				
				return true;
			
	    }

	    protected void onPostExecute(Boolean success) 
	    {
	    	if(success)
	    	{	
	    	
		    	Intent i;
		 		
		 		/**
		     	 * create intent and extras
		     	 */
		 		i = new Intent(context.getResources().getString(R.string.bbUpdateEvent));
		 		i.putExtra(context.getResources().getString(R.string.bbUpdateEventUpdated), intent.getStringExtra(context.getResources().getString(R.string.bbUpdateEventUpdated)));
		 		
		 		if(intent.getStringExtra(context.getResources().getString(R.string.bbChildKey)) != null)
		 		{
		 			i.putExtra(context.getResources().getString(R.string.bbKey), intent.getStringExtra(context.getResources().getString(R.string.bbChildKey)));
		 			
		 		}
		 		
		 		if(intent.getStringExtra(context.getResources().getString(R.string.bbUpdateEventAction)) != null)
		 		{
		 			i.putExtra(context.getResources().getString(R.string.bbUpdateEventAction), intent.getStringExtra(context.getResources().getString(R.string.bbUpdateEventAction)));
		 			
		 		}
		 		
		 		if(intent.getStringExtra(context.getResources().getString(R.string.bbReset)) != null)
		 		{
		 			i.putExtra(context.getResources().getString(R.string.bbReset), intent.getStringExtra(context.getResources().getString(R.string.bbReset)));
		 			
		 		}
		 		
		 		if(intent.getStringExtra(context.getResources().getString(R.string.bbUpdateEventUpdated)) != null 
		    			&& intent.getStringExtra(context.getResources().getString(R.string.bbUpdateEventUpdated)).equals(context.getResources().getString(R.string.bbCD2MChildMessage)))
		 		{
		 			Proxy myProxy = Proxy.getInstance();
		 			myProxy.storeEventFootPrint(context, 0);
		 		}	
		 		
		 		//invite new activity
		 		if(intent.getStringExtra(context.getResources().getString(R.string.bbUpdateEventUpdated)) != null 
		    			&& intent.getStringExtra(context.getResources().getString(R.string.bbUpdateEventUpdated)).equals(context.getResources().getString(R.string.bbCD2MInviteMessage)))
		 		{
		 			
		 			Proxy myProxy = Proxy.getInstance();
		 			myProxy.storeEventFootPrint(context, 3);
		 		}	
		 		
		 		/**
		     	 * broadcast event and extras
		     	 */
		     	context.sendBroadcast(i);
	    	}	
	     }
	 }
}
