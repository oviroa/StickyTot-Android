package com.garagewarez.bubu.android;

import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import android.widget.TextView;

import com.garagewarez.bubu.android.base.BubuBaseActivity;

/**
 * Displays error messages
 * @author oviroa
 *
 */
public class ErrorMessageActivity extends BubuBaseActivity 
{
	TextView errorDetail;
	Button backButton;
	Button logOutButton;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState); 
    	
        //Register as a receiver to listen for event/broadcasts
        IntentFilter filter = new IntentFilter(getResources().getString(R.string.bbUpdateEvent));
        registerReceiver(mReceiver, filter);
        
        overridePendingTransition (R.anim.up_in, R.anim.up_out);
        
        //populate views with messaging
        setContentView(R.layout.error);
    	
    	errorDetail = (TextView)findViewById(R.id.bbErrorMessageDetail);
    	errorDetail.setTypeface(tf);
    	Bundle extras = getIntent().getExtras();
    	errorDetail.setText(extras.getString(getResources().getString(R.string.bbErrorMessage)));
    	
    	//back button
    	backButton = (Button)findViewById(R.id.bbErrorBackButton);
    	backButton.setTypeface(tf,1);
    	
    	//set click handler for loading local image into picture view
    	backButton.setOnClickListener(new OnClickListener() 
 	   	{
 	   	    public void onClick(View v) 
 	   	    {
 	   	    	finish();	   	    	
 	   	    }
 	   	}); 
    	
    	
    	//log out button
    	logOutButton = (Button)findViewById(R.id.bbErrorLogOutButton);
    	logOutButton.setTypeface(tf,1);
    	
    	//log out
    	logOutButton.setOnClickListener(new OnClickListener() 
 	   	{
 	   	    public void onClick(View v) 
 	   	    {
 	   	    	if(isOnline())
 	   	    		switchAccount();	   	    	
 	   	    }
 	   	});
    	
    	constructTopBar(R.id.homeButtonError, R.id.textViewErrorTitle);
    	
    	TextView errorSubtitle = (TextView)findViewById(R.id.bbErrorMessage);
    	errorSubtitle.setTypeface(tf,1);
    	
    	TextView errorInstructions = (TextView)findViewById(R.id.bbErrorMessageInstructions);
    	errorInstructions.setTypeface(tf);
    	
    }
	
	@Override
    protected void onPause() 
    {
        super.onPause();
        if(!this.isFinishing())
        	finish();
        overridePendingTransition (R.anim.down_in, R.anim.down_out);
    }
	
	@Override
	protected void broacastHandler(Bundle extras)
	{
		//if new data
    	if(extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)) != null 
    			&& extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)).equals(getResources().getString(R.string.bbCD2MInviteMessage)))
    	{	
    		displayInviteRefreshDialog();
    		
    		//footprint
    		getProxy().storeEventFootPrint(getApplicationContext(), 3);
    		
    		finish();
    	
    	}
		
		if(extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)) != null 
    			&& extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)).equals(getResources().getString(R.string.bbCD2MChildMessage)))
    	{
			finish();
    	}
	}
}
