package com.garagewarez.bubu.android.proxy;

import java.io.File;
import java.io.IOException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;

import com.garagewarez.bubu.android.BubuApp;
import com.garagewarez.bubu.android.R;
import com.garagewarez.bubu.android.common.ChildData;
import com.garagewarez.bubu.android.common.ChildResponse;
import com.garagewarez.bubu.android.common.EventData;
import com.garagewarez.bubu.android.common.EventResponse;
import com.garagewarez.bubu.android.common.JointUser;
import com.garagewarez.bubu.android.common.MediaResponse;
import com.garagewarez.bubu.android.common.ParentData;
import com.garagewarez.bubu.android.common.Pic;
import com.garagewarez.bubu.android.common.RestletResponse;
import com.garagewarez.bubu.android.proxy.Connector.ProgressListener;
import com.garagewarez.bubu.android.utils.CookieFactory;
import com.garagewarez.bubu.android.utils.Serializer;
import com.garagewarez.bubu.android.utils.UrlImageView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Singleton class, used for data retrieval (from remote server and local storage and memory) 
 * @author oviroa
 *
 */
public class Proxy 
{
        //base URL for application
		private static final String GAE_APP_BASE_URL = "https://sticky-tot.appspot.com/";
		
		//singleton instance
        private static Proxy ref = null;
        
        private static final String KEY = "c2dmPref";
        private static final String HOME_NOTIFICATION_KEY = "homeNotificationKey";
    	private static final String REGISTRATION_KEY = "registrationKey";
        
        private Proxy()
        {
        	cookieFactory = new CookieFactory(this);
        	cd2mCookieFactory = new CookieFactory(this);
        }
        
        public CookieFactory getCookieFactory()
        {
        	return this.cookieFactory;
        }
        
        //class used for retrieving auth cookie from GAE
        private CookieFactory cookieFactory;
        private CookieFactory cd2mCookieFactory;
        
        /**
         * Returns brand new instance of class 
         * @return
         */
        public static Proxy getNewInstance()
        {
            ref = new Proxy();               
            return ref;
        }
        
        /**
         * Does the singleton thing
         * @return
         */
		public static Proxy getInstance()
		{
			if (ref == null)
			{	// it's ok, we can call this constructor
		        ref = new Proxy();
		       
			}
			
			return ref;
		}
 
       
        /**
         * Disallows cloning
         */
        public Object clone() throws CloneNotSupportedException
        {
        	throw new CloneNotSupportedException(); 
        	// that'll teach 'em
        }        
       
       
        /**
         * Retrives parent data, returns {@link ParentData}
         * @param context
         * @param activity
         * @return
         * @throws IOException
         * @throws ClassNotFoundException 
         * @throws Exception
         */
        public ParentData getParent(Context context, Activity activity, Boolean refresh) throws IOException, ClassNotFoundException, SocketTimeoutException, Exception
        {
        		
        		//attemt to retrieve from memory or local storage
        		ParentData pd = getStoredParentData(context);
        	
        		//if not in memory or local storage, retrieve from server
        		if(pd == null || refresh)
        		{
        			if(getStoredAccountToken(context) != null && cookieFactory.connect( getStoredAccountToken(context), context, activity ))
        			{	
        				
	        			//stores JSON response
		                String result = null;
		                //url
		                String destUrl = new StringBuffer().append(GAE_APP_BASE_URL).append(context.getResources().getString(R.string.bbParent)).toString(); 
		                
		                
		                
		                if (cookieFactory.getAuthCookie() != null)
		                {
			                result = Connector.process
			                			(
			                					destUrl, 
			                					null, 
			                					true, 
			                					false, 
			                					"GET", 
			                					cookieFactory.getAuthCookie(),
			                					context.getResources().getInteger(R.integer.bbTimeOutMS)
			                			);
		                
			                //deserialize JSON to ParentData
			                GsonBuilder gsonb = new GsonBuilder();
			                Gson gson = gsonb.create();
			                pd = gson.fromJson(result, ParentData.class);
			                
			                //store result locally (memory and preferences)
			                if(pd.getResponse().getMessage().equals("ok"))
			                {	
			                	storeParent(context, pd);
			                	//register for receiving events
			                	registerC2DM(context);
			                }	
		                
		                }
		                
        			}
        			else
           		 	{
                		throw new IOException(context.getResources().getString(R.string.bbGeneralAuthenticationErrorMessage));
           		 	}
				}
        		
                
        		return pd;
                
        }
        
        
        
	    /**
	     * Register app for receiving events
	     * @param context
	     */
        private void registerC2DM(Context context)
        {
	    	
        	Intent registrationIntent = new Intent(context.getResources().getString(R.string.bbCD2MRegistrationPackage));
	    	registrationIntent.putExtra
	    	(
	    		context.getResources().getString(R.string.bbCD2MApp), 
	    		PendingIntent.getBroadcast(context, 0, new Intent(), 0)
	    	); 
	    	
	    	registrationIntent.putExtra
	    	(
	    		context.getResources().getString(R.string.bbCD2MSender), 
	    		context.getResources().getString(R.string.bbAdminEmail)
	    	);
	    	
	    	context.startService(registrationIntent); 
            
        }	
        
        /**
         * Unregister app from cloud messaging
         * @param contex
         */
        private void unregisterC2DM(Context context)
        {
        	Intent unregIntent = new Intent(context.getResources().getString(R.string.bbCD2MUnregistrationPackage));
        	unregIntent.putExtra
        	(
        		context.getResources().getString(R.string.bbCD2MApp), 
        		PendingIntent.getBroadcast(context, 0, new Intent(), 0)
        	);
        	context.startService(unregIntent);
        }
        
        
        /**
         * Store registration id for app
         * @param context
         * @param id
         * @throws IOException 
         * @throws ClientProtocolException 
         * @throws Exception
         */
        public RestletResponse storeC2DMId (Context context, String id) throws ClientProtocolException, IOException, ConnectTimeoutException
        {
        	
        	RestletResponse myResponse = new RestletResponse();
        	myResponse.setMessage(context.getResources().getString(R.string.bbErrorNoData));
        	
        	//activate cookie, and if successfull proceed
        	if(getStoredAccountToken(context) != null && cookieFactory.connect( getStoredAccountToken(context), context, null ))         
        	{
	        	
        		String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	    		
        		String urlStr = new StringBuffer().
			    				append(GAE_APP_BASE_URL).
			    				append("servlet/c2dm?regId=").
			    				append(id).
			    				append(deviceId == null ? "" : new StringBuffer().append("&deviceId=").append(deviceId).toString()).
			    				toString();
	    		
	    		if(cookieFactory.getAuthCookie() != null)
	    		{
	    			String response = Connector.process(
	    											urlStr, 
	    											"", 
	    											true, 
	    											true, 
	    											"POST", 
	    											cookieFactory.getAuthCookie(),
	    											context.getResources().getInteger(R.integer.bbTimeOutMS));
	    			
	    			if(response != null)
	    			{	
		    			//deserialize JSON to ParentData
		                GsonBuilder gsonb = new GsonBuilder();
		                Gson gson = gsonb.create();
		                myResponse = gson.fromJson(response, RestletResponse.class);
		                
		                //store locally
		            	if(myResponse.getMessage().equals("ok"))
		            	{	
		            		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
		            		editor.putString(REGISTRATION_KEY, id);
		            		editor.commit();
		            	}
	    			}	
	    		}	
                
        	}
        	else
    		{
    			throw new IOException(context.getResources().getString(R.string.bbGeneralAuthenticationErrorMessage));
    		}
        	
        	return myResponse;
        }
        
        /**
         * Removes CD2M id
         * @param context
         * @return
         * @throws ClientProtocolException
         * @throws IOException
         */
        public RestletResponse removeC2DMId (Context context) throws ClientProtocolException, IOException, ConnectTimeoutException
        {
        	
        	RestletResponse myResponse = new RestletResponse();
        	myResponse.setMessage(context.getResources().getString(R.string.bbErrorNoData));
        	
        	//check auth token
        	SharedPreferences settings = context.getSharedPreferences(context.getResources().getString(R.string.bbTokenReserveLabel), 0);
        	String accountToken = settings.getString(context.getResources().getString(R.string.bbTokenLabel), null);
        	
        	
        	String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        	
        	String urlStr = new StringBuffer().
								append(GAE_APP_BASE_URL).
								append("servlet/c2dm?").
								append(deviceId == null ? "" : new StringBuffer().append("&deviceId=").append(deviceId).toString()).
								toString();
        	
        	
    		
    		
    		//refresh cookie, if null
    		if(cd2mCookieFactory.getAuthCookie() == null)
    		{	
    			cd2mCookieFactory.connect(accountToken, context, null);	    			
    		}	
    		
    		if(cd2mCookieFactory.getAuthCookie() == null)
    			throw new IOException(context.getResources().getString(R.string.bbGeneralAuthenticationErrorMessage));
    		
    		
    		String response =  Connector.process(
												urlStr, 
												"", 
												true, 
												true, 
												"POST", 
												cd2mCookieFactory.getAuthCookie(),
												context.getResources().getInteger(R.integer.bbTimeOutMS));
            
            if(response != null)
            {	
    		
	    		//deserialize JSON to ParentData
	            GsonBuilder gsonb = new GsonBuilder();
	            Gson gson = gsonb.create();
	            myResponse = gson.fromJson(response, RestletResponse.class);
	            
	            //clear cookie
	            cd2mCookieFactory.setAuthCookie(null);
	    		
            }
	    		
    		
        	return myResponse;
        }
        
        
        
        /**
         * Retrieves events data list as {@link EventResponse} based on parent key
         * @param context
         * @param key
         * @param activity
         * @return
         * @throws MalformedURLException 
         * @throws ClassNotFoundException 
         * @throws Exception
         * @throws SocketTimeoutException
         */
        public EventResponse getEvents(Context context, String parentKey, String childKey, Activity activity, Boolean refresh) throws MalformedURLException, IOException, ClassNotFoundException, SocketTimeoutException
        {
        	
        	 //attempt to get data from memory or local storage	
        	 EventResponse er = getStoredEventResponse(context, childKey);
             
        	 //if not there, retrieve from server
        	 if(er == null || refresh)
        	 {	 
        		 
        		if(getStoredAccountToken(context) != null && cookieFactory.connect( getStoredAccountToken(context), context, activity))
        		 {
	        		String urlStr = new StringBuffer().append(GAE_APP_BASE_URL).
	        			append(context.getResources().getString(R.string.bbEvent)).
	        			append("?parentKey=").append(parentKey).
	        			append("&childKey=").append(childKey).toString(); 
	               
	                
	        		if (cookieFactory.getAuthCookie() != null)
	                {
	                	String response = Connector.process(
								urlStr, 
								"", 
								true, 
								false, 
								"GET", 
								cookieFactory.getAuthCookie(),
								context.getResources().getInteger(R.integer.bbTimeOutMS));
	                	
	                	//deserialize from server to EventResponse object
		                GsonBuilder gsonb = new GsonBuilder();
		                gsonb.setDateFormat(15);
		                Gson gson = gsonb.create();
		                er = gson.fromJson(response, EventResponse.class);
		                
		                
		                if(er != null && er.getResponse().getMessage().equals("ok"))
		                {	
			                //retrieve Event hash map from        
			                HashMap<String, EventResponse> eMap = this.getStoredEventMap(context);
			                
			                if(eMap == null)
			                {
			                	eMap = new HashMap<String, EventResponse>();
			                }	
			                
			                eMap.put(childKey, er);
			                
			                storeEvents(context, eMap);
		                }   
	                }
	                
	                
        		 }
        		 else
        		 {
        			 throw new IOException(context.getResources().getString(R.string.bbGeneralAuthenticationErrorMessage));
        		 }
        	 }    
        	
        	return er;
        }

        /**
         * Retrieves child data list as {@link ChildResponse} based on parent key
         * @param context
         * @param key
         * @param activity
         * @return
         * @throws IOException
         * @throws ClassNotFoundException 
         * @throws Exception
         */
        public ChildResponse getKids(Context context, String parentKey, Activity activity, Boolean refresh) throws IOException, ClassNotFoundException, SocketTimeoutException
        {
        	
        	//try to get it from local resources or memory
        	ChildResponse cr = getStoredChildResponse(context);
        	
        	//if not there, retrieve form server
        	if(cr == null || refresh) 
        	{	
        		if(getStoredAccountToken(context) != null && cookieFactory.connect( getStoredAccountToken(context), context, activity ))
        		{	
	        		
	                String urlStr = new StringBuffer().append(GAE_APP_BASE_URL).append(context.getResources().getString(R.string.bbChild)).append("?parentKey=").append(parentKey).toString(); 
	                
	                if (cookieFactory.getAuthCookie() != null)
	                {
		                
	                	String response = Connector.process(
								urlStr, 
								"", 
								true, 
								false, 
								"GET", 
								cookieFactory.getAuthCookie(),
								context.getResources().getInteger(R.integer.bbTimeOutMS));
		            
	                	//deserialize JSON from server to ChildResponse object
		            	GsonBuilder gsonb = new GsonBuilder();
		                gsonb.setDateFormat(15);
		                Gson gson = gsonb.create();
		                cr = gson.fromJson(response, ChildResponse.class);
		                
		                //store locally
		                if(cr != null && cr.getResponse().getMessage().equals("ok"))
		                	storeKids(context,cr);
	                }
	                
        		}
        		else
	       		{
        			throw new IOException(context.getResources().getString(R.string.bbGeneralAuthenticationErrorMessage));
	       		}
        	}
        	
        	return cr;
        }
        
        
        /**
         * Retrieve account name from preferences
         * @param context
         * @return
         */
        public String getStoredAccountName(Context context)
        {
        	//try memory
        	String ac = ((BubuApp)context).getAccountName();
        	
        	//if not there, try local storage
        	if (ac == null)
        	{
        		SharedPreferences settings = context.getSharedPreferences(context.getResources().getString(R.string.bbChosenAccount), 0);
                ac = settings.getString(context.getResources().getString(R.string.bbChosenAccount), null);
                
                //set memory version if not null
                if(ac != null)
                {	
                	((BubuApp)context).setAccountName(ac);
                }
        	}
        	
        	return ac;
        }
        
        /**
         * Store account name in preferences
         * @param context
         * @param accountName
         */
        public void storeAccountName(Context context, String accountName)
        {
        	
        	if(accountName != null)
        	{	
        		((BubuApp)context).setAccountName(accountName);
        	   	
        		
        		//store in preferences
            	SharedPreferences settings;
            	SharedPreferences.Editor editor;
            	settings = context.getSharedPreferences(context.getResources().getString(R.string.bbChosenAccount), 0);
            	editor = settings.edit();
        	
            	//store account
            	editor.putString(context.getResources().getString(R.string.bbChosenAccount), accountName);
            	
            	editor.commit();
     
        	}   	
        	
        }
        
        
        /**
         * Store account token in preferences
         * @param context
         * @param accountName
         */
        public void storeAccountToken(Context context, String accountToken)
        {
        	((BubuApp)context).setAccountToken(accountToken);
        	PreferenceManager.getDefaultSharedPreferences(context).edit().putString
        	(
        		context.getResources().getString(R.string.bbAccountToken), 
        		accountToken
        	).commit();
        	
        	//store in preferences
        	SharedPreferences settings;
        	SharedPreferences.Editor editor;
        	settings = context.getSharedPreferences(context.getResources().getString(R.string.bbTokenReserveLabel), 0);
        	editor = settings.edit();
    	
        	//store Parent
        	editor.putString(context.getResources().getString(R.string.bbTokenLabel), accountToken);
        	
        	editor.commit();
        	
        }   
        
        /**
         * Store last login date in delta milliseconds
         * @param context
         * @param lostLoginDate
         */
        public void storeLastLoginDate(Context context, Date lostLoginDate)
        {
        	PreferenceManager.getDefaultSharedPreferences(context).edit().putLong
        	(
        		context.getResources().getString(R.string.bbLastLoginDate), 
        		lostLoginDate.getTime()
        	).commit();
        }
        
        /**
         * Stores value of last event triggered by CD2M
         * @param context
         * @param eventFootprint
         */
        public void storeEventFootPrint(Context context, int eventFootprint )
        {
        	PreferenceManager.getDefaultSharedPreferences(context).edit().putInt
        	(
        		context.getResources().getString(R.string.bbEventFootprint), 
        		eventFootprint
        	).commit();
        }
        
        
        /**
         * Retrieve value of last event triggered by CD2M)
         * @param context
         */
        public int retrieveEventFootPrint(Context context)
        {
        	return PreferenceManager.getDefaultSharedPreferences(context).getInt
        	(
        			context.getResources().getString(R.string.bbEventFootprint), 
        			-1
        	);        	
        	
        }
        
        /**
         * Retrieve last login date from shared preferences
         * @param context
         * @return
         */
        public long getStoredLastLoginDate(Context context)
        {
        	long lastLoginDate;
        	
        	lastLoginDate = PreferenceManager.getDefaultSharedPreferences(context).getLong
    		(
    			context.getResources().getString(R.string.bbLastLoginDate), 
    			0
    		);
        	
        	return lastLoginDate;
        	
        }
        
        /**
         * Retrieve account token from shared preferences
         * @param context
         * @return
         */
        private String getStoredAccountToken(Context context)
        {
        	String accountToken = ((BubuApp)context).getAccountToken();
        	
        	if(accountToken == null)
        	{	
        		accountToken = PreferenceManager.getDefaultSharedPreferences(context).getString
        		(
        			context.getResources().getString(R.string.bbAccountToken), 
        			null
        		);
        	}	
        	
        	return accountToken;
        } 
        
        /**
         * Retrieves stored parent data
         * @param context
         * @return
         * @throws IOException 
         * @throws ClassNotFoundException 
         * @throws StreamCorruptedException 
         * @throws OptionalDataException 
         */
        public ParentData getStoredParentData(Context context) throws OptionalDataException, StreamCorruptedException, ClassNotFoundException, IOException
        {
        	//try memory first
        	ParentData pd = ((BubuApp)context).getParentData();
        	
        	//if not there, try local storage
        	if(pd == null)
     	    {	
        		
        		SharedPreferences settings = context.getSharedPreferences(context.getResources().getString(R.string.bbBubuPrefsLabel), 0);
                String parentString = settings.getString(context.getResources().getString(R.string.bbParentLabel), null);
                
                //set memory version if not null
                if(parentString != null)
                {	
                	pd = (ParentData)Serializer.stringToObject(parentString);
                
                	((BubuApp)context).setParentData(pd);
                }	
     	    }
        	
        	return pd;
        }
        
        /**
         * Retrieves kids data if stored locally
         * @param context
         * @return
         * @throws IOException 
         * @throws ClassNotFoundException 
         * @throws StreamCorruptedException 
         * @throws OptionalDataException 
         */
        public ChildResponse getStoredChildResponse(Context context) throws OptionalDataException, StreamCorruptedException, ClassNotFoundException, IOException
        {
        	//try memory first
        	ChildResponse cr = ((BubuApp)context).getChildResponse();
        	
        	//not in memory, try local storage
        	if(cr == null)
            {
        		SharedPreferences settings = context.getSharedPreferences
        		(
        			context.getResources().getString(R.string.bbBubuPrefsLabel), 
        			0
        		);
                String childString = settings.getString(context.getResources().getString(R.string.bbKidsLabel), null);
                
                
                //update/set memory version
                if(childString != null)
                {	
                	cr = (ChildResponse)Serializer.stringToObject(childString);
                    ((BubuApp)context).setChildResponse(cr);
                }    
            }
        	
        		
        	return cr;
        }
	    
        /**
         * Retrieves stored event
         * @param context
         * @param key
         * @return
         * @throws IOException 
         * @throws ClassNotFoundException 
         * @throws StreamCorruptedException 
         * @throws OptionalDataException 
         */
        public EventResponse getStoredEventResponse(Context context, String key) throws OptionalDataException, StreamCorruptedException, ClassNotFoundException, IOException
        {
        	
        	EventResponse er = null;
        	
        	//get map from local storage 
        	HashMap<String, EventResponse> eMap = getStoredEventMap(context);
        	
        	//if map was retrieved
        	if(eMap != null)
	         {
            	//fetch event list
        		er = eMap.get(key);
        	    
        		//update memory
            	if(er != null)
	        	{
	        		eMap.put(key, er);
	                ((BubuApp)context).setEventMap(eMap);       		
	        	}
	                
              }     
        	
        	return er;
        }
        
        /**
         * Retrieves stored event map
         * @param context
         * @return
         * @throws IOException 
         * @throws ClassNotFoundException 
         * @throws StreamCorruptedException 
         * @throws OptionalDataException 
         */
        @SuppressWarnings("unchecked")
        public HashMap<String, EventResponse> getStoredEventMap(Context context) throws OptionalDataException, StreamCorruptedException, ClassNotFoundException, IOException
        {
        	//try to fetch map from memory first
        	HashMap<String, EventResponse> eMap = ((BubuApp)context).getEventMap();
        	
        	//if not in memory, fetch from local storage
        	if(eMap == null)
        	{
        		SharedPreferences settings = context.getSharedPreferences
        		(
        			context.getResources().getString(R.string.bbBubuPrefsLabel), 
        			0
        		);
                
        		String eventString = settings.getString(context.getResources().getString(R.string.bbEventsLabel), null);
                
                //update local storage version
                if(eventString != null)
                {	
                	eMap = (HashMap<String, EventResponse>)Serializer.stringToObject(eventString);
                }     
        	}
        	
        	return eMap;
        }
        
        
        /**
         * Cleans all local variables form memory and storage
         * @param context
         */
        public void clearLocalStorage(Context context, Boolean unregister)
        {
        	cookieFactory.setAuthCookie(null);
        	
        	if(unregister)
        		unregisterC2DM(context);
        	
        	PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit();
    		//reset state data
    		((BubuApp)context).setParentData(null);
    		((BubuApp)context).setChildResponse(null);
    		((BubuApp)context).setEventMap(null);
    		((BubuApp)context).setAccountName(null);
    		((BubuApp)context).setHintWasShown(false);
    		((BubuApp)context).setUpdateTextWasShown(false);
    		((BubuApp)context).setHomeNotificationWasShown(false);
    		((BubuApp)context).setAccountToken(null);
    		
    		SharedPreferences settings = context.getSharedPreferences(context.getResources().getString(R.string.bbBubuPrefsLabel), 0);
    		SharedPreferences.Editor editor = settings.edit();
    		editor.clear().commit();
    		
    		
    		UrlImageView.cleanupAll(context);
    		
    		//clear cache - phone memory
    		trimCache(context);
    		trimCacheExternal(context);
    		//clear cache - SD
    		
        }
        
       
        
        /**
         * Stores parent in memory and inside preferences
         * @param context
         * @param pd
         */
        public void storeParent(Context context, ParentData pd)
        {
	     
        	if(pd != null)
        	{	
        		//store in memory
	        	((BubuApp)context).setParentData(pd);
	        	
	        	//store in preferences
	        	SharedPreferences settings;
	        	SharedPreferences.Editor editor;
	        	settings = context.getSharedPreferences(context.getResources().getString(R.string.bbBubuPrefsLabel), 0);
	        	editor = settings.edit();
        	
	        	//store Parent
	        	editor.putString(context.getResources().getString(R.string.bbParentLabel), Serializer.objectToString(pd));
    	        // Commit the edits!
    	        editor.commit();
    	        
        	}  
        }
        
        /**
         * Stores kids in memory and inside preferences
         * @param context
         * @param cr
         */
        public void storeKids(Context context, ChildResponse cr)
        {
	     
        	if(cr != null)
        	{	
        		//store in memory
	        	((BubuApp)context).setChildResponse(cr);
	        	
	        	//store in preferences
	        	SharedPreferences settings;
	        	SharedPreferences.Editor editor;
	        	settings = context.getSharedPreferences(context.getResources().getString(R.string.bbBubuPrefsLabel), 0);
	        	editor = settings.edit();
        	
	        	//store Parent
	        	editor.putString(context.getResources().getString(R.string.bbKidsLabel), null);
	        	editor.putString(context.getResources().getString(R.string.bbKidsLabel), Serializer.objectToString(cr));
    	        // Commit the edits!
    	        editor.commit();
    	        
        	}  
        }
        
        /**
         * Removes events for deleted child if existent
         * @param context
         * @param key
         * @throws IOException 
         * @throws ClassNotFoundException 
         * @throws StreamCorruptedException 
         * @throws OptionalDataException 
         */
        public void removeEventsByChild(Context context, String key) throws OptionalDataException, StreamCorruptedException, ClassNotFoundException, IOException
        {
        	HashMap<String, EventResponse> eMap = getStoredEventMap(context);
        	if(eMap != null && eMap.containsKey(key))
        	{
        		eMap.remove(key);
        		storeEvents(context,eMap);
        	}	
        }
        
        /**
         * Stores events in memory and inside preferences
         * @param context
         * @param em
         */
        
        public void storeEvents(Context context, HashMap<String, EventResponse> em)
        {
	     
        	if(em != null)
        	{	
        		//store in memory
	        	((BubuApp)context).setEventMap(em);
	        	
	        	//store in preferences
	        	SharedPreferences settings;
	        	SharedPreferences.Editor editor;
	        	settings = context.getSharedPreferences(context.getResources().getString(R.string.bbBubuPrefsLabel), 0);
	        	editor = settings.edit();
        	
	        	//store Parent
	        	editor.putString(context.getResources().getString(R.string.bbEventsLabel), Serializer.objectToString(em));
	        	
    	        // Commit the edits!
    	        editor.commit();
    	        
        	}  
        }
        
        /**
         * Clear cache/internal
         * @param context
         */
        private void trimCache(Context context) 
        { 
            File dir = context.getCacheDir(); 
			if (dir != null && dir.isDirectory()) 
			{ 
			    deleteDir(dir); 
			}
        } 
        
        /**
         * Remove files from image cache directory on card
         * @param context
         */
        private void trimCacheExternal(Context context)
        { 
           File dir = context.getExternalCacheDir(); 
           
           if (dir != null && dir.isDirectory()) 
           { 
        	   deleteImageCachedContent(dir); 
           } 
            

        } 
        
        
        /**
         * Remove all directories inside dir, recursively
         * @param dir
         * @return
         */
        private boolean deleteDir(File dir) 
        {
        	
            if (dir!=null && dir.isDirectory()) 
            { 
                String[] children = dir.list(); 
                boolean success;
                for (int i = 0; i < children.length; i++) 
                { 
                	success = deleteDir(new File(dir, children[i])); 
                    if (!success) 
                    { 
                        return false; 
                    } 
                } 
            } 

            // The directory is now empty so delete it 
            return dir.delete(); 
        } 
        
        
        /**
         * Remove images from two levels deep
         * @param dir
         */
        private void deleteImageCachedContent(File dir) 
        { 
           if (dir!=null && dir.isDirectory()) 
        	{ 
                String[] children = dir.list(); 
                
               if(children.length != 0)
               {	   
	                //level 1
	                dir = new File(dir, children[0]);
	                children = dir.list();
	                
	                //level 2
	                if(children != null && children.length != 0)
	                {	
	                	dir = new File(dir, children[0]);
	                	children = dir.list();
	                	
			        	for (int i = 0; i < children.length; i++) 
			            { 
			            	new File(dir, children[i]).delete(); 
			            } 
	                }
               }
                
               
            }
            
        } 
        
        
        /**
         * Returns whether or not device is online
         * @param context
         * @return
         */
        public Boolean isOnline(Context context)
        {
        
        	Boolean proxyIsOnline = false;
        	
        	ConnectivityManager conMgr =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
	        if ( conMgr.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED 
	            ||  conMgr.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED  ) 
	        {
	
	
	        	proxyIsOnline = true;
	
	        }
	        else if ( conMgr.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED 
	            ||  conMgr.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) 
	        {
	        	proxyIsOnline = false;	     
	        } 
	        
	        return proxyIsOnline;
        }
        
        
       
        /**
         * Deletes child that corresponds to parent key
         * @param context
         * @param childKey
         * @param parentKey
         * @param activity
         * @return
         * @throws ClientProtocolException
         * @throws IOException
         * @throws ConnectTimeoutException
         * @throws ClassNotFoundException
         */
        public ChildResponse deleteChild(Context context, String childKey, String parentKey, Activity activity) throws ClientProtocolException, IOException, ConnectTimeoutException, ClassNotFoundException
        {
        	ChildResponse cr = null;
        	
        	//if not in memory or local storage, retrieve from server
    		if(getStoredAccountToken(context) != null && cookieFactory.connect( getStoredAccountToken(context), context, activity ) )
    		{	
    			
    			//get device id
            	String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
				
            	//String parentKey = getStoredParentData(context).getEncodedKey();
				
            	String urlStr = new StringBuffer()
									.append(GAE_APP_BASE_URL)
									.append(context.getResources().getString(R.string.bbChild))
									.append("?source=android&parentKey=")
									.append(parentKey)
									.append("&childKey=")
									.append(childKey)
									.append("&deviceId=").append(deviceId)
									.toString();
            	
            	
        		if(cookieFactory.getAuthCookie() != null)
        		{	
        			String response = Connector.process(
														urlStr, 
														"", 
														true, 
														false, 
														"DELETE", 
														cookieFactory.getAuthCookie(),
														context.getResources().getInteger(R.integer.bbTimeOutMS));
        			
	                //deserialize JSON to ParentData
	                GsonBuilder gsonb = new GsonBuilder();
	                Gson gson = gsonb.create();
	                cr = gson.fromJson(response, ChildResponse.class);
        		}   
    			
			} 
    		else
	   		{
    			throw new IOException(context.getResources().getString(R.string.bbGeneralAuthenticationErrorMessage));
	   		}
    		
        	return cr;
        }
        
        /**
         * Deletes child that corresponds to parent key
         * @param context
         * @param parentKey
         * @param activity
         * @return
         * @throws IOException 
         * @throws ClientProtocolException 
         */
        public EventResponse deleteEvent(Context context, String parentKey, String childKey, String eventKey, Activity activity) throws ClientProtocolException, IOException, ConnectTimeoutException
        {
        	EventResponse er = null;
        	
        	//if not in memory or local storage, retrieve from server
    		if(getStoredAccountToken(context) != null && cookieFactory.connect( getStoredAccountToken(context), context, activity ) )
    		{	
    			
				//get device id
            	String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
				
            	String urlStr = new StringBuffer()
										.append(GAE_APP_BASE_URL)
										.append(context.getResources().getString(R.string.bbEvent))
										.append("?source=android&parentKey=")
										.append(parentKey)
										.append("&childKey=")
										.append(childKey)
										.append("&eventKey=")
										.append(eventKey)
										.append("&deviceId=").append(deviceId)
										.toString();
            	
        		if(cookieFactory.getAuthCookie() != null)
        		{
        			String response = Connector.process(
							urlStr, 
							"", 
							true, 
							false, 
							"DELETE", 
							cookieFactory.getAuthCookie(),
							context.getResources().getInteger(R.integer.bbTimeOutMS));
        			
        			//deserialize JSON to ParentData
                    GsonBuilder gsonb = new GsonBuilder();
                    Gson gson = gsonb.create();
                    er = gson.fromJson(response, EventResponse.class);
        		}
			} 
    		else
   		 	{
   			 	throw new IOException(context.getResources().getString(R.string.bbGeneralAuthenticationErrorMessage));
   		 	}
    		
        	return er;
        }
        
       
        public String warmUpBackend(Context context, String entry) throws SocketTimeoutException, UnsupportedEncodingException, NotFoundException, IOException
        {
        	String response = null;
        	response = Connector.process
	         		(
	         				new StringBuffer().append(GAE_APP_BASE_URL).append(entry).toString(), 
	         				null,
	         				true, 
	         				false,
	         				"GET", 
	         				cookieFactory.getAuthCookie(),
	         				context.getResources().getInteger(R.integer.bbTimeOutMS)
	         		);
	    		
    		
    		return response;
        }
        
        
        /**
         * Store new/updated Event information
         * @param context
         * @param event
         * @param parentKey
         * @param activity
         * @return
         * @throws IOException 
         * @throws ClientProtocolException 
         * @throws ClassNotFoundException 
         * @throws Exception
         */
        public EventResponse putEvent(Context context, EventData event, Activity activity, ProgressListener listener) throws ClientProtocolException, IOException, ClassNotFoundException
        {
        	EventResponse er = null;
        	
        	//if event has bitmap, push that to server first
        	if(event.getImage() != null)
        	{
        		MediaResponse mr = storeNewImage(context, event.getImage(), activity, listener);
        		
        		//image was saved successfully
	        	if(mr != null && mr.getResponse().getMessage().equals(context.getResources().getString(R.string.bbOK)))
	        	{
	        		
	        		//set pic urls
	        		
	        		Pic pic = new Pic();
	        		pic.setMain(
	        						new StringBuffer()
	        									.append(context.getResources().getString(R.string.bbProtocol))
	        									.append(context.getResources().getString(R.string.bbDomain))
	        									.append("/servlet/image?id=")
	        									.append(mr.getImageIdList().get(0))
	        									.append("&type=.jpg")
	        									.toString()
	        					);
	        		pic.setThumb(
	        						new StringBuffer()
	        									.append(context.getResources().getString(R.string.bbProtocol))
												.append(context.getResources().getString(R.string.bbDomain))
												.append("/servlet/image?id=")
												.append(mr.getImageIdList().get(0))
												.append("&t=1&type=.jpg")
												.toString()
	        					);
	        		
	        		event.setPic(pic);
	        		
	        	}
	        	else
	        	{	
	        		er = new EventResponse();
	        		if(mr != null)
	        			er.setResponse(mr.getResponse());
	        		return er;
	        	}	
        	}
        	
        	//store data only
        	er = putBitmaplessEvent(context, event, activity);
        	
        	return er;
        }
        
        
        /**
         * Store child data
         * @param context
         * @param child
         * @param activity
         * @return
         * @throws IOException 
         * @throws ClientProtocolException 
         * @throws ClassNotFoundException 
         * @throws Exception
         */
        public ChildResponse putChild(Context context, ChildData child, Activity activity, ProgressListener listener) throws ClientProtocolException, IOException, ClassNotFoundException
        {
        	ChildResponse cr = null;  
        	
        	
        	
        	
        	//if child has bitmap, push to server first
        	if(child.getImage() != null)
        	{
        	
	        	MediaResponse mr = storeNewImage(context, child.getImage(), activity, listener);
	        	
	        	//something went wrong with image upload
	        	if(mr == null || mr.getImageIdList().size() == 0)
	        	{
	        		cr = new ChildResponse();
	        		cr.setResponse(mr.getResponse());
	        		return cr;
	        	}
	        	
	        	//image was saved
	        	if(mr.getResponse().getMessage().equals(context.getResources().getString(R.string.bbOK)))
	        	{
	        		
	        		//set pic urls
	        		
	        		Pic pic = new Pic();
	        		pic.setMain(
	        						new StringBuffer()
	        									.append(context.getResources().getString(R.string.bbProtocol))
	        									.append(context.getResources().getString(R.string.bbDomain))
	        									.append("/servlet/image?id=")
	        									.append(mr.getImageIdList().get(0))
	        									.append("&type=.jpg")
	        									.toString()
	        					);
	        		pic.setThumb(
	        						new StringBuffer()
	        									.append(context.getResources().getString(R.string.bbProtocol))
												.append(context.getResources().getString(R.string.bbDomain))
												.append("/servlet/image?id=")
												.append(mr.getImageIdList().get(0))
												.append("&t=1&type=.jpg")
												.toString()
	        					);
	        		
	        		child.setPic(pic);
	        		
	        	}
	        	else
	        	{
	        		cr = new ChildResponse();
	        		cr.setResponse(mr.getResponse());
	        		return cr;
	        	}	
        	}
        	
        	//store data only
        	cr = putBitmaplessChild(context, child, activity);
        	
        	return cr;
        }
        
        /**
         * Initiates joint user invitation, calls get method of /invite web service
         * @param context
         * @param activity
         * @param invite
         * @return
         * @throws ClientProtocolException
         * @throws IOException
         * @throws ClassNotFoundException
         */
        public JointUser inviteJointUser(Context context, Activity activity, JointUser invite) throws ClientProtocolException, IOException, ClassNotFoundException
        {
        	//activate cookie, and if successful proceed
        	if(getStoredAccountToken(context) != null && cookieFactory.connect( getStoredAccountToken(context), context, activity))         
        	{
    			
        		String keyQuery = "";
            		            	
            	keyQuery = new StringBuffer()
            				.append("/")
            				.append(context.getResources().getString(R.string.bbInvite))
            				.append("?source=android")
            				.append("&parentKey="+this.getStoredParentData(context).getEncodedKey())
            				.toString();
            	
            	//get device id
            	String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
            	
        		String urlStr = new StringBuffer().append(GAE_APP_BASE_URL).append(keyQuery).append("&deviceId=").append(deviceId).toString(); 
            	
        		//ChildData to JSON
        		GsonBuilder gsonb = new GsonBuilder();
                Gson gson = gsonb.disableHtmlEscaping().create();
                
                
                
                if(cookieFactory.getAuthCookie() != null)
        		{
        			
        			String response = Connector.process
             		(
             				urlStr, 
             				gson.toJson(invite),
             				true, 
             				true, 
             				"PUT", 
             				cookieFactory.getAuthCookie(),
             				context.getResources().getInteger(R.integer.bbTimeOutMS)
             		);
        			
        			invite = gson.fromJson(response, JointUser.class);
        		}
        	} 
        	else
   		 	{
        		throw new IOException(context.getResources().getString(R.string.bbGeneralAuthenticationErrorMessage));
   		 	}
        	
        	
        	return invite;
        }
        
        /**
         * Initiates joint user deletion, calls delete method of /invite web service
         * @param context
         * @param activity
         * @return
         * @throws ClientProtocolException
         * @throws IOException
         * @throws ClassNotFoundException
         */
        public RestletResponse deleteJointUser(Context context, Activity activity) throws ClientProtocolException, IOException, ClassNotFoundException
        {
        	
        	RestletResponse rr = null;
        	
        	//activate cookie, and if successful proceed
        	if(getStoredAccountToken(context) != null && cookieFactory.connect( getStoredAccountToken(context), context, activity))         
        	{
    			
        		String keyQuery = "";
            		            	
            	keyQuery = new StringBuffer()
            				.append("/")
            				.append(context.getResources().getString(R.string.bbInvite))
            				.append("?source=android")
            				.append("&parentKey="+this.getStoredParentData(context).getEncodedKey())
            				.toString();
            	
            	//get device id
            	String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
            	
        		String urlStr = new StringBuffer().append(GAE_APP_BASE_URL).append(keyQuery).append("&deviceId=").append(deviceId).toString(); 
            	
        		if(cookieFactory.getAuthCookie() != null)
        		{
        			String response = Connector.process(
							urlStr, 
							"", 
							true, 
							false, 
							"DELETE", 
							cookieFactory.getAuthCookie(),
							context.getResources().getInteger(R.integer.bbTimeOutMS));
        			
        			//deserialize JSON to RestletResponse
                    GsonBuilder gsonb = new GsonBuilder();
                    Gson gson = gsonb.create();
                    rr = gson.fromJson(response, RestletResponse.class);
        		}
        	} 
        	else
   		 	{
        		throw new IOException(context.getResources().getString(R.string.bbGeneralAuthenticationErrorMessage));
   		 	}
        	
        	return rr;
        }
        
        
        /**
         * 
         * @param context
         * @param activity
         * @return
         * @throws ClientProtocolException
         * @throws IOException
         * @throws ClassNotFoundException
         */
        public ChildResponse approveJointUserInvite(Context context, Activity activity) throws ClientProtocolException, IOException, ClassNotFoundException
        {
        	
        	ChildResponse rr = null;
        	
        	//activate cookie, and if successful proceed
        	if(getStoredAccountToken(context) != null && cookieFactory.connect( getStoredAccountToken(context), context, activity))         
        	{
    			
        		String keyQuery = "";
            		            	
            	keyQuery = new StringBuffer()
            				.append("/")
            				.append(context.getResources().getString(R.string.bbInvite))
            				.append("?source=android")
            				.append("&parentKey="+this.getStoredParentData(context).getEncodedKey())
            				.append("&isApproved=true")
            				.toString();
            	
            	//get device id
            	String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
            	
        		String urlStr = new StringBuffer().append(GAE_APP_BASE_URL).append(keyQuery).append("&deviceId=").append(deviceId).toString(); 
            	
        		if(cookieFactory.getAuthCookie() != null)
        		{
        			String response = Connector.process(
							urlStr, 
							"", 
							true, 
							false, 
							"GET", 
							cookieFactory.getAuthCookie(),
							context.getResources().getInteger(R.integer.bbTimeOutMS));
        			
        			//deserialize JSON to RestletResponse
                    GsonBuilder gsonb = new GsonBuilder();
                    Gson gson = gsonb.create();
                    rr = gson.fromJson(response, ChildResponse.class);
        		}
        	} 
        	else
   		 	{
        		throw new IOException(context.getResources().getString(R.string.bbGeneralAuthenticationErrorMessage));
   		 	}
        	
        	return rr;
        }
        
        /**
         * Sends invite rejection via get method of /invite service 
         * @param context
         * @param activity
         * @return
         * @throws ClientProtocolException
         * @throws IOException
         * @throws ClassNotFoundException
         */
        public ChildResponse rejectJointUserInvite(Context context, Activity activity) throws ClientProtocolException, IOException, ClassNotFoundException
        {
        	
        	ChildResponse rr = null;
        	
        	//activate cookie, and if successful proceed
        	if(getStoredAccountToken(context) != null && cookieFactory.connect( getStoredAccountToken(context), context, activity))         
        	{
    			
        		String keyQuery = "";
            		            	
            	keyQuery = new StringBuffer()
            				.append("/")
            				.append(context.getResources().getString(R.string.bbInvite))
            				.append("?source=android")
            				.append("&parentKey="+this.getStoredParentData(context).getEncodedKey())
            				.append("&isApproved=false")
            				.toString();
            	
            	//get device id
            	String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
            	
        		String urlStr = new StringBuffer().append(GAE_APP_BASE_URL).append(keyQuery).append("&deviceId=").append(deviceId).toString(); 
            	
        		if(cookieFactory.getAuthCookie() != null)
        		{
        			String response = Connector.process(
							urlStr, 
							"", 
							true, 
							false, 
							"GET", 
							cookieFactory.getAuthCookie(),
							context.getResources().getInteger(R.integer.bbTimeOutMS));
        			
        			//deserialize JSON to RestletResponse
                    GsonBuilder gsonb = new GsonBuilder();
                    Gson gson = gsonb.create();
                    rr = gson.fromJson(response, ChildResponse.class);
        		}
        	} 
        	else
   		 	{
        		throw new IOException(context.getResources().getString(R.string.bbGeneralAuthenticationErrorMessage));
   		 	}
        	
        	return rr;
        }
        
        /**
         * Push eventData instance (no bitmap) to server
         * @param context
         * @param event
         * @param parentKey
         * @param activity
         * @return
         * @throws IOException 
         * @throws ClientProtocolException 
         */
        private EventResponse putBitmaplessEvent(Context context, EventData event, Activity activity) throws ClientProtocolException, IOException, ConnectTimeoutException
        {
        	EventResponse er = null;
        	
        	
        	//activate cookie, and if successfull proceed
        	if(getStoredAccountToken(context) != null && cookieFactory.connect( getStoredAccountToken(context), context, activity ))         
        	{
    			//reset image data
			 	event.setImage(null);
			 
			 	//EventData to JSON
				GsonBuilder gsonb = new GsonBuilder();
		        Gson gson = gsonb.disableHtmlEscaping().create();
			 	
			 	//construct string for URL
			 	String keyQuery = "";
             	
             	keyQuery = new StringBuffer().append("/").append(context.getResources().getString(R.string.bbEvent)).append("?source=android").toString();
             	
             	//get device id
             	String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
         	
             	String urlStr = new StringBuffer().append(GAE_APP_BASE_URL).append(keyQuery).append("&deviceId=").append(deviceId).toString();
             	
             	if(cookieFactory.getAuthCookie() != null)
             	{	
	             	String serverResponse = Connector.process
	             		(
	             				urlStr, 
	             				gson.toJson(event),
	             				true, 
	             				true, 
	             				"PUT", 
	             				cookieFactory.getAuthCookie(),
	             				context.getResources().getInteger(R.integer.bbTimeOutMS)
	             		);
		                 	
	        			 	
	             	er = gson.fromJson(serverResponse, EventResponse.class);
             	}	 	
        			  
	        }
        	else
   		 	{
        		throw new IOException(context.getResources().getString(R.string.bbGeneralAuthenticationErrorMessage));
   		 	}
        	
        	return er;        	
        }	
        
        
        /**
         * Push childData instance (no bitmap) to server
         * @param context
         * @param child
         * @param activity
         * @return
         * @throws IOException 
         * @throws ClientProtocolException 
         * @throws ClassNotFoundException 
         */
        private ChildResponse putBitmaplessChild(Context context, ChildData child, Activity activity) throws ClientProtocolException, IOException, ClassNotFoundException, ConnectTimeoutException
        {
        	ChildResponse cr = null;
        	
        	//activate cookie, and if successful proceed
        	if(getStoredAccountToken(context) != null && cookieFactory.connect( getStoredAccountToken(context), context, activity))         
        	{
    			
        		String keyQuery = "";
            		            	
            	keyQuery = new StringBuffer().append("/").append(context.getResources().getString(R.string.bbChild)).append("?source=android").toString();
            	
            	//get device id
            	String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
            	
        		String urlStr = new StringBuffer().append(GAE_APP_BASE_URL).append(keyQuery).append("&deviceId=").append(deviceId).toString(); 
            	
        		//ChildData to JSON
        		GsonBuilder gsonb = new GsonBuilder();
                Gson gson = gsonb.disableHtmlEscaping().create();
                
                child.setImage(null);
                
                if(child.getParentKey() == null)
                	child.setParentKey(this.getStoredParentData(context).getEncodedKey());
        		
        		if(cookieFactory.getAuthCookie() != null)
        		{
        			
        			String response = Connector.process
             		(
             				urlStr, 
             				gson.toJson(child),
             				true, 
             				true, 
             				"PUT", 
             				cookieFactory.getAuthCookie(),
             				context.getResources().getInteger(R.integer.bbTimeOutMS)
             		);
        			
        			cr = gson.fromJson(response, ChildResponse.class);
        		}	
        		
        	} 
        	else
   		 	{
        		throw new IOException(context.getResources().getString(R.string.bbGeneralAuthenticationErrorMessage));
   		 	}
        	
            return cr;
        }
      
        long totalSize;
        
        
        
        
	    /**
	     * Upload new image
	     * @param context
	     * @param iBitMap
	     * @param activity
	     * @param listener
	     * @return
	     * @throws ClientProtocolException
	     * @throws IOException
	     * @throws ClassNotFoundException
	     * @throws ConnectTimeoutException
	     */
        private MediaResponse storeNewImage(Context context, Bitmap iBitMap, Activity activity, ProgressListener listener) throws ClientProtocolException, IOException, ClassNotFoundException, ConnectTimeoutException
        {
        	MediaResponse mr = null;
        	
        	//retrieve parent key used to identify account
        	String parentKey = getStoredParentData(context).getEncodedKey();
        	
        	//activate cookie, and if successfull proceed
        	if(getStoredAccountToken(context) != null && cookieFactory.connect( getStoredAccountToken(context), context, activity ))         
        	{
	        	
        		String urlStr = new StringBuffer().append(GAE_APP_BASE_URL).append("servlet/media?parentKey=").append(parentKey).toString();
        		
        		
        		if(cookieFactory.getAuthCookie() != null)
        		{
        			String response = Connector.processImage
        				(
        						urlStr, 
        						iBitMap, 
        						true, 
        						true, 
        						"POST", 
        						cookieFactory.getAuthCookie(),
        						listener,
        						context.getResources().getInteger(R.integer.bbTimeOutMS));
        			
        			//deserialize JSON from server to ChildResponse object
                	GsonBuilder gsonb = new GsonBuilder();
                    gsonb.setDateFormat(15);
                    Gson gson = gsonb.create();
                    mr = gson.fromJson(response, MediaResponse.class);
        						
        		}
        		
        		
        	}	            
        	else
   		 	{
        		throw new IOException(context.getResources().getString(R.string.bbGeneralAuthenticationErrorMessage));
   		 	}
        	
        	return mr;
        }        
        
        /**
         * Update all local storage objects with live data
         * @param context
         * @throws IOException 
         * @throws MalformedURLException 
         * @throws ClassNotFoundException 
         */
        public void updateAll(Context context, String sectionId, String childKey, String reset) throws MalformedURLException, IOException, ClassNotFoundException, SocketTimeoutException, Exception
        {
        	//update selected data
        	if(reset == null)
        	{
	        	//get local parent data
	        	ParentData pd = getStoredParentData(context);
	        	
	        	if(pd != null)
	        	{
	        		ChildResponse cr;
	        		
	        		HashMap<String, EventResponse> em = this.getStoredEventMap(context);
		        	
	    			if(sectionId.equals(context.getResources().getString(R.string.bbCD2MChildMessage)))//if child updates, update local kids data
	    			{
	    				getKids(context, pd.getEncodedKey(), null, true);
	    			}
	    			else if(sectionId.equals(context.getResources().getString(R.string.bbCD2MEventMessage))) //if event updates
	    			{
	    				if(childKey != null && em != null)//if key available, update event list for that key
	        	        {
	        	        	getEvents(context, pd.getEncodedKey(), childKey, null, true);
	        	        }	
	        	        else
	        	        {	
	        	        	cr = getKids(context, pd.getEncodedKey(), null, false);
	            	        
	        	        	for( ChildData cd : cr.getList())
	            			{
	            				getEvents(context, pd.getEncodedKey(), cd.getEncodedKey(), null, true);
	            			}
	        	        }	
	    			}
	    			else if(sectionId.equals(context.getResources().getString(R.string.bbCD2MInviteMessage)))//updates to invite acivity
	    			{
	    				getParent(context,null, true);
	    				
	    				getKids(context, pd.getEncodedKey(), null, true);
	        	        
	        	        ((BubuApp)context).setKidListFreshStatus(true);
	        	        
	    			}	
        				
	        	}
	        		
        		
        	}
        	else//clear all
        	{
        		clearLocalStorage(context, false);
        	}
        	
        }
        
        /**
         * Checks memory and local storage to see if hint was shown
         * @param context 
         * @return
         */
        public boolean hintWasShown(Context context)
        {
        	
        	if(((BubuApp)context).isHintWasShown())
        	{
        		return true;
        	}
        	else
        	{
        		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean
        		(
        			context.getResources().getString(R.string.bbHintShown), 
        			false
        		);
        	}		
        }
        
        
        /**
         * Set to true after hint has been shown
         * @param context
         */
        public void setHintWasShown(Context context)
        {
        	((BubuApp)context).setHintWasShown(true);
        	
        	PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean
        	(
        		context.getResources().getString(R.string.bbHintShown), 
        		true
        	).commit();

        }
        
        /**
         * Checks memory and local storage to see if update text was shown
         * @param context 
         * @return
         */
        public boolean updateTextWasShown(Context context)
        {
        	if(((BubuApp)context).isUpdateTextWasShown())
        	{
        		return true;
        	}
        	else
        	{
        		
        		SharedPreferences settings = context.getSharedPreferences
        		(
        			HOME_NOTIFICATION_KEY, 
        			Context.MODE_PRIVATE
        		);
                
        		int version = settings.getInt(context.getResources().getString(R.string.bbUpdateVersion), 0);
        		
        		if (
        				(version > 0 && version < context.getResources().getInteger(R.integer.bbCurrentVersion)) //older version was installed
        				|| settings.getBoolean(context.getResources().getString(R.string.bbUpdateTextShown),false) //older version was shown before
        			)	
        		{
        			return false;
        		}	
        		else
        		{	
        			return true;
        		}
        	}		
        }
        
        
        /**
         * Set to true after update text has been shown
         * @param context
         */
        public void setUpdateTextWasShown(Context context)
        {
        	((BubuApp)context).setUpdateTextWasShown(true);
        	
        	Editor editor = context.getSharedPreferences(HOME_NOTIFICATION_KEY, Context.MODE_PRIVATE).edit();
    		
        	editor.putBoolean
    		(
            		context.getResources().getString(R.string.bbUpdateTextShown), 
            		false
            ).putInt
            (
            	context.getResources().getString(R.string.bbUpdateVersion),
            	context.getResources().getInteger(R.integer.bbCurrentVersion)
            ).commit();
    		
    		

        }
        
        /**
         * Checks memory and local storage to see if joint user notification was shown
         * @param context 
         * @return
         */
        public boolean homeNotificationWasShown(Context context)
        {
        	
        	if(((BubuApp)context).getHomeNotificationWasShown())
        	{
        		return true;
        	}
        	else
        	{
        		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean
        		(
        			context.getResources().getString(R.string.bbHomeNotificationShown), 
        			false
        		);
        	}		
        }
        
        
        /**
         * Set to true after joint user notification has been shown
         * @param context
         */
        public void setHomeNotificationShown(Context context)
        {
        	((BubuApp)context).setHomeNotificationWasShown(true);
        	
        	PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean
        	(
        		context.getResources().getString(R.string.bbHomeNotificationShown), 
        		true
        	).commit();

        }
        
}
