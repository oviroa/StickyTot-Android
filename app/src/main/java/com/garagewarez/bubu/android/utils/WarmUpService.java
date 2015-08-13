package com.garagewarez.bubu.android.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;

import com.garagewarez.bubu.android.proxy.Proxy;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

public class WarmUpService extends IntentService 
{
    

	public WarmUpService() 
	{
		super("WarmUpService");
	}

	@Override
    protected void onHandleIntent(Intent workIntent) 
	{
        String response = null;
        
        //connect to warm up service via proxy
        Proxy myProxy = Proxy.getInstance();
        try 
        {
        	response = myProxy.warmUpBackend(getApplicationContext(),"warmlet");
        	myProxy.warmUpBackend(getApplicationContext(),"servlet/image");
        	response += " image";
        	/*
        	response += myProxy.warmUpBackend(getApplicationContext(),"parent");
        	response += myProxy.warmUpBackend(getApplicationContext(),"child");
        	response += myProxy.warmUpBackend(getApplicationContext(),"event");
        	myProxy.warmUpBackend(getApplicationContext(),"servlet/image");
        	response += "image";
        	*/
		} 
        catch (SocketTimeoutException e) 
        {
        	 Log.w("BUBU WARMUP SERVICE","ERROR :: 36 :: " + e.getMessage());
		} 
        catch (UnsupportedEncodingException e) 
        {
        	Log.w("BUBU WARMUP SERVICE","ERROR :: 40 :: " + e.getMessage());
		} 
        catch (NotFoundException e) 
		{
        	Log.w("BUBU WARMUP SERVICE","ERROR :: 44 :: " + e.getMessage());;
		} 
        catch (IOException e) 
        {
        	Log.w("BUBU WARMUP SERVICE","ERROR :: 48 :: " + e.getMessage());
		}
        catch(Exception e)
        {
        	Log.w("BUBU WARMUP SERVICE","ERROR :: 52 :: " + e.getMessage());
        }
        finally
        {
        	Log.w("BUBU WARMUP SERVICE","RESPONSE :: " + response);
        }
        
    }
}
