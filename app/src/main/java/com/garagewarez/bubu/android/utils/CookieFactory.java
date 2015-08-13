package com.garagewarez.bubu.android.utils;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.client.ClientProtocolException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.garagewarez.bubu.android.R;
import com.garagewarez.bubu.android.proxy.Connector;
import com.garagewarez.bubu.android.proxy.Proxy;


/**
 * Retrieves token from account, submits to GAE, retrieves auth token, invalidates old auth token if expired
 * @author oviroa
 *
 */
public class CookieFactory 
{
	
	 //base URL for application
	private final String gaeAppBaseUrl = "https://sticky-tot.appspot.com/";
	
	//authentication url
    private final String gaeAppLoginUrl = new StringBuffer().append(gaeAppBaseUrl).append("_ah/login").toString();
    
    //auth cookie from GAE
    private String authCookie = null;
    
    //data proxy
    private Proxy myProxy;
    
    public String getAuthCookie() 
    {
            return authCookie;
    }

    public void setAuthCookie(String authCookie) 
    {
            this.authCookie = authCookie;
    }

    
    public CookieFactory(Proxy proxy)
    {
    	myProxy = proxy;
    }
    
    /**
     * Calls server with authToken, retrieves cookie, loads property
     * @param accountToken
     * @param context
     * @param activity
     * @return
     * @throws IOException 
     * @throws ClientProtocolException 
     */
	public boolean connect(String accountToken, Context context, Activity activity) throws ClientProtocolException, IOException  
	{

		boolean retVal = true;
        
		//get cookie, load singleton property
		if(authCookie == null)
		{	
			authCookie = getAuthCookie(accountToken, context, activity);
			
			if(authCookie == null)
				return false;
		}
        
        return retVal;
	}
	
	/**
	 * Retrieves cookie from server
	 * @param authToken
	 * @param context
	 * @param activity
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws Exception
	 */
    private String getAuthCookie(String authToken, Context context, Activity activity) throws ClientProtocolException, IOException
    {
            
    		//cookie
            String cookieStr = null;
            
            //auth url
            String cookieUrl = new StringBuffer().append(gaeAppLoginUrl).append("?continue=") 
                    .append(URLEncoder.encode(gaeAppBaseUrl,"UTF-8")).append("&auth=") 
                    .append(URLEncoder.encode(authToken,"UTF-8")).toString(); 
            
            
            cookieStr = Connector.processCookie
			(
					cookieUrl, 
					context.getResources().getInteger(R.integer.bbTimeOutMS)
			);
           
            //if nothing came back, try again with new token
            if(cookieStr == null)
            {
            	authToken = getNewToken(myProxy.getStoredAccountName(context), context, activity, true);
            	
            	if(authToken == null) 
            		return null;
            	
            	try
            	{
	            	cookieUrl = new StringBuffer().append(gaeAppLoginUrl).append("?continue=") 
	                .append(URLEncoder.encode(gaeAppBaseUrl,"UTF-8")).append("&auth=") 
	                .append(URLEncoder.encode(authToken,"UTF-8")).toString();
            	}
            	catch(Exception e)
            	{
            		return null;
            	}
            	
            	cookieStr = Connector.processCookie
    			(
    					cookieUrl, 
    					context.getResources().getInteger(R.integer.bbTimeOutMS)
    			);
            	
            }	
            
            return cookieStr;
             
    }
    
    /**
     * Gets new token, invalidates if expired
     * @param accountName
     * @param context
     * @param activity
     * @param invalidate
     * @return
     */
    public String getNewToken(String accountName, Context context, Activity activity, Boolean invalidate)
    {
    	String token = null;
    	
    	//retrieve list of accounts
    	AccountManager accountManager = AccountManager.get(context);
        final Account[] accounts = accountManager.getAccountsByType("com.google");
    	
        //get account name from preferences if stored
        if(accountName == null)
        	accountName = myProxy.getStoredAccountName(context); 
        
        //if a locally stored account name was found, retrieve account and get a token (async) 
        if(accountName != null)
        {
        	for(Account acct : accounts)
        	{
        		if(acct.name.equals(accountName) && acct.type.equals("com.google"))
        		{
        			   
        			   if(invalidate != null && invalidate)
        			   {	   
        				   token = getToken(acct, context, activity);
        				   accountManager.invalidateAuthToken(acct.type, token);
        			   }
        			   
        	    	   token = getToken(acct, context, activity);
        		}
        	}	
        }
    	
    	return token;
    }
    
    /**
     * Gets account token from account manager
     * @param account
     * @param context
     * @param activity
     * @return
     */
    private String getToken(Account account, Context context, Activity activity)	
    {
		
    	
		AccountManager accountManager = AccountManager.get(context);
    	//get bundle
		AccountManagerFuture<Bundle> accountManagerFuture = accountManager.getAuthToken(account, "ah", null, activity, null, null);
		
		Bundle authTokenBundle = null;
		String authToken = null;
		
		try 
		{
	        //get token
			authTokenBundle = accountManagerFuture.getResult();
			authToken = authTokenBundle.get(AccountManager.KEY_AUTHTOKEN).toString();
			return authToken;
			
		} //handle errors if token retrieval not successful
		catch (OperationCanceledException e) 
		{
			return null;
		} 
		catch (AuthenticatorException e) 
		{
			return null;
		} 
		catch (IOException e) 
		{
			return null;
		} 
		catch (Exception e)
		{
			return null;
		}
	  
	}
}
