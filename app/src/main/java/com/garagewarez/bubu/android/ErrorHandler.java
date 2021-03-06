package com.garagewarez.bubu.android;

import android.content.Context;
import android.content.Intent;


/**
 * Handles error messaging, sends message to error message view activity 
 * @author oviroa
 *
 */
public class ErrorHandler 
{

	static public void execute(Context context, String userMessage, String errorMessage, Exception exception)
	{
		Intent i = new Intent(context, ErrorMessageActivity.class);
		
		if(exception != null)
		{	
			
			//no connection
			if(
					exception.getClass().toString().equals("class java.net.UnknownHostException") 
					||
					exception.getClass().toString().equals("class org.apache.http.conn.HttpHostConnectException")
					||
					exception.getClass().toString().equals("class java.net.ConnectException")
			  )
			{
				userMessage = context.getResources().getString(R.string.bbErrorNoConnection);
			}
		}
		
		i.putExtra(context.getResources().getString(R.string.bbErrorMessage), userMessage);
		context.startActivity(i);
	}
	
	
}
