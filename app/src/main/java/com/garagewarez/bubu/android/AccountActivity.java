package com.garagewarez.bubu.android;

import java.io.IOException;
import java.util.Date;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.garagewarez.bubu.android.base.BubuBaseActivity;
import com.garagewarez.bubu.android.common.ChildResponse;
import com.garagewarez.bubu.android.common.ParentData;
import com.garagewarez.bubu.android.utils.VerticalScrollView;


/**
 * Main activity, displays welcome message and list of Google accounts to chose from
 * It checks for stired state (chosen account info, data) and moves to the KidsActivity if that info exists
 * @author oviroa
 *
 */
public class AccountActivity extends BubuBaseActivity 
{
	//accounts
	protected AccountManager accountManager;
	
	//processing view bar
	private LinearLayout processingView;
	
	//AsyncTask for token retrieval form device
	private GetTokenTask tokenTask;
	
	//current account name, will be loaded from local storage
	private String accountName;
	
	
	//list of accounts
	private ListView accountListView;
	
	
	//last login date
	private Date lastLoginDate;
	
	private Intent intent;
	
    /** Called when the activity is first created. */
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        
    	super.onCreate(savedInstanceState); 
    	
    	//set view
    	setContentView(R.layout.main);
    	
    	intent = getIntent();
    	
    	//get stuff from intent
    	Bundle extras = intent.getExtras();
    	String action = intent.getAction();
    	
    	//show messaging
    	//action message
    	TextView accountMessage = (TextView)this.findViewById(R.id.textViewChoose);
    	accountMessage.setTypeface(getTypeface(), 1);
    	
    	//inflate list from view into class
        accountListView =(ListView)this.findViewById(R.id.bbAccountListView);
    	
    	//check if something got shared and if this is an image
        if (Intent.ACTION_SEND.equals(action) && extras.containsKey(Intent.EXTRA_STREAM))
        {
            //hide menu button
        	//activate top menu button 
        	ImageButton menuButton = (ImageButton)this.findViewById(R.id.bbMenuHome);
    		menuButton.setVisibility(8);
        	
        	//hide list
    		accountListView.setVisibility(8);
        	
        	//no stuff here
        	accountMessage.setText(R.string.bbLogInForSharing);
        	
		}
        else
        {	
    	
        	//activate top menu button 
        	constructMenuButton(R.id.bbMenuHome );
	    	
        	//inflates processing bar container
	    	processingView = (LinearLayout)this.findViewById(R.id.bbProcessingView);
	    	
	    	//welcome message
	    	TextView welcomeMessage = (TextView)this.findViewById(R.id.textViewWelcome);
	    	welcomeMessage.setTypeface(getTypeface());
	    	
	    	accountMessage.setText(R.string.bbLogInWithGoogle);
	    	
	        //retrieve list of accounts
	        accountManager = AccountManager.get(getApplicationContext());
	        final Account[] accounts = accountManager.getAccountsByType(getResources().getString(R.string.bbGoogleAcctType));
	        
	        //get account name from preferences if stored
	        accountName = getProxy().getStoredAccountName(getApplicationContext());
	        
	        
	        //instantiated async task
	        tokenTask=(GetTokenTask)getLastNonConfigurationInstance();
	        
	        //handles running async task on device rotation or re-draws after other interruptions
	        if (tokenTask != null) 
	        {
	        	tokenTask.attach(this);     	   	
	        }
	
	        
	        //if a locally stored account name was found, retrieve account and get a token (async) 
	        if(accountName != null && tokenTask != null && tokenTask.getStatus() != AsyncTask.Status.RUNNING)
	        {
	        	
				//show porcessing view
				processingView.setVisibility(0);
				
				//get token from account async
				tokenTask = new GetTokenTask(AccountActivity.this);
				tokenTask.execute(accountName);
				
	        	
	        }	
	        else//no account name was stored locally, get list from device
	        {	
	        	
	        	//set adapter loaded with array of accounts
		        accountListView.setAdapter(new AccountsAdapter(this, R.layout.account_list_item, accounts, getTypeface()));
		        
		        //set click event handler for clicks on list 
		        accountListView.setOnItemClickListener
		        (
		        		new OnItemClickListener() 
		        		{
		        		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
		        		    {
		        		    	//retrieve account token from device
		        		    	
		        		    	//show loding bar
		        		    	processingView.setVisibility(0);
		        		    	//hide list of accounts
		        		    	accountListView.setVisibility(8);
		        		    	
		        		    	//retrieve account based on selection id
		        		    	accountName = accounts[(int)id].name;
		        		    	//get token from account manager
		        		    	tokenTask = new GetTokenTask(AccountActivity.this);
		        		    	
		        		    	tokenTask.execute(accountName);
		        		    	
		        		    	
		        		    }
		        		  }
		        
		        );
	        }
	       
	        //fix scrolling for landscape on phones
	        final VerticalScrollView sv = (VerticalScrollView)findViewById(R.id.bbScrollView);
	        sv.post
	        (
	        	new Runnable() 
	        	{ 
	        		public void run() 
	        		{ 
	        			sv.scrollTo(0, sv.getTop());
	        		} 
	        	}
	        );
        
        }     
        
    }
    
    /**
     * Async retrieval of authToken form account manager, parent data from server, corresponding child data
     * @author oviroa
     *
     */
    private static class GetTokenTask extends AsyncTask<String, Integer, ChildResponse> 
    {
        
		//prep async task for device rotation and unexpected interruptions 
    	AccountActivity activity =  null;
		
    	private Exception carriedException = null;
    	
		GetTokenTask(AccountActivity activity) 
		{
			attach(activity);
		}
		
		void detach() 
		{
			activity=null;
		}
		
		void attach(AccountActivity activity) 
		{
			this.activity = activity;
		}
    	
		protected ChildResponse doInBackground(String... arg0) 
		{
		   
			
			//retrieve token 
			String mToken = activity.getProxy().getCookieFactory().getNewToken(arg0[0], activity.getApplicationContext(), activity, null);
			
			//authenticate against server with token (string)
			if(mToken != null)//access to accounts was granted
			{	 
				
				//store account locally(state and preferences) if authentication successful
				activity.getProxy().storeAccountName(activity.getApplicationContext(),activity.accountName);
				activity.getProxy().storeAccountToken(activity.getApplicationContext(), mToken);
				
				//store last login date
				activity.lastLoginDate = new Date();
				activity.getProxy().storeLastLoginDate(activity.getApplicationContext(), activity.lastLoginDate);
	   
				//initiate async retrieval of parent object
				try 
				{
					//get parent from server or local storage
					ParentData pd = activity.getProxy().getParent(activity.getApplicationContext(), activity, false);
					
					if(pd != null)
					{	
						try 
						{
							//get kids from server
							return activity.getProxy().getKids(activity.getApplicationContext(), pd.getEncodedKey(), activity, false);
							
						}
						catch (ClassNotFoundException e) 
						{
							e.printStackTrace();
							return null;
						}
						catch (IOException e) 
						{
							//no connection, try again
							try 
							{
								//get kids from server
								return activity.getProxy().getKids(activity.getApplicationContext(), pd.getEncodedKey(), activity, false);
	                        
							} 
							catch (ClassNotFoundException e1) 
							{
								//show error message if server send one 
								carriedException = e1;
								return null;
							}
							catch (IOException e1) 
							{
								carriedException = e1;
								return null;
							}
							catch (Exception e1) 
							{
								carriedException = e1;
								return null;
							} 
						}
					}
					else
					{	
						carriedException = new Exception(activity.getApplicationContext().getResources().getString(R.string.bbErrorNoConnection));
						return null;
					}
	               
				}
				catch (ClassNotFoundException e)
				{
					carriedException = e;
					return null;
				}
				catch (IOException e) 
				{
					carriedException = e;
					return null;
				}
				catch (Exception e) 
				{
					carriedException = e;
					return null;
				}
		   
			}	   
			else//no access to accounts, just show the view again
			{
				return null;
			}
		   
		}
       
		protected void onPostExecute(ChildResponse result) 
		{
    	  
			//hide processing/loading message
			activity.processingView = (LinearLayout)activity.findViewById(R.id.bbProcessingView);
			//activity.processingView.setVisibility(8);
			if(result != null)
	       	{	
	       		//check for errors
	           	if(result.getResponse().getMessage().equals(activity.getApplicationContext().getResources().getString(R.string.bbOK)))
	           	{
	           		//all ok, display list
	           		//create intent for transition to new activity
	           		Intent i = new Intent(activity, KidsActivity.class);
	           		activity.startActivity(i);
	           		
	           		//finish current activity
	           		activity.finish();
	           	}
	           	else
	           	{
	           		
	           		activity.processingView.setVisibility(8);
		       		activity.accountListView.setVisibility(0);
		       		activity.tokenTask = null;
	           		
	           		this.cancel(true);
	           		//show error message if server send one 
	           		ErrorHandler.execute
	           			(
	           					activity, 
	           					activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
	           					"AccountActivity 299: " + result.getResponse().getMessage(), 
	           					null
	           			);
	           	}	
	       	}
	       	else
	       	{
	       		
	       		activity.processingView.setVisibility(8);
	       		activity.accountListView.setVisibility(0);
	       		activity.tokenTask = null;
	    		
	       		this.cancel(true);
	       		
	           	if(carriedException != null)
	    			ErrorHandler.execute
	    			(
	    					activity,
	    					activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
	    					"AccountActivity 316: ", 
	    					carriedException
	    			);
	           	else
	           		ErrorHandler.execute
					(
							activity, 
							activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
							null, 
							null
					);
	       	}	
			
			detach();
    	   
       }
	   	
    } 	
   
   
    /**
     * Detaches task from activity and returns it so it can continue running afetr rotation
     */
    @Override
	public Object onRetainNonConfigurationInstance() 
    {
    	if(tokenTask != null)
    		tokenTask.detach();
		
    	//hide processing view by default
    	if(processingView != null)
    		processingView.setVisibility(8);
    	
    	return(tokenTask);
	}
    
    
    @Override
    protected void onStart()
    {
		super.onStart();
		
		//handles running async task on device rotation or re-draws after other interruptions
        if (tokenTask != null) 
        {
        	
        	tokenTask.attach(this);
        	//show porcessing view
			processingView.setVisibility(0);
			accountListView.setVisibility(8);
        }       
       
    }
    
    @Override
	protected void broacastHandler(Bundle extras)
	{
    	return;
	}
    
    
    
    /**
     * Creates options menu 
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
	   //inflate menu based on xml menu
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.privacy_menu, menu);
	    
	    //styling hack
	    try
	    {
	    	setMenuBackground();
	    }
	    catch(Exception e)
	    {
	    	//hack did not work, ignore	    	
	    }
	    return true;
	}
	
    
    /**
	 * Event handler for options inside menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    // Handle item selection
	    switch (item.getItemId()) 
	    {
	    	//log out button
	    	case R.id.bbPrivacyButton:
	    		if(isOnline())//when online, show privacy
	    		{
	    			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.bbPrivacyUrl)));
	    			startActivity(browserIntent);
	    		}
	    		else//otherwise display error message
	    			ErrorHandler.execute
	    			(
	    					this, 
	    					getResources().getString(R.string.bbErrorNoConnection),
	    					null,
	    					null
	    			);
	    		return true;
	        
	    
	    		
	    	default:
	    	return super.onOptionsItemSelected(item);
	    }
	}
	
	/**
	 * Kill on pause if shared
	 */
	@Override
    protected void onPause()
    {
    	super.onPause();
    	
    	//get stuff from intent
    	Bundle extras = intent.getExtras();
    	String action = intent.getAction();
    	
    	//check if something got shared and if this is an image
        if (Intent.ACTION_SEND.equals(action) && extras.containsKey(Intent.EXTRA_STREAM))
        {
        	finish();
        }
    }	
}

