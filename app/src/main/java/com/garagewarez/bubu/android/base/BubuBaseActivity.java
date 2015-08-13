package com.garagewarez.bubu.android.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Minutes;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.garagewarez.bubu.android.AccountActivity;
import com.garagewarez.bubu.android.BubuApp;
import com.garagewarez.bubu.android.JointUserFormActivity;
import com.garagewarez.bubu.android.PrettyRefreshDialog;
import com.garagewarez.bubu.android.R;
import com.garagewarez.bubu.android.proxy.Proxy;
import com.garagewarez.bubu.android.utils.Debug;
import com.garagewarez.bubu.android.utils.WarmUpService;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * 
 * Base Activity Class, all app activities need to subclass it
 * @author oviroa
 *
 */
public abstract class BubuBaseActivity extends Activity 
{
	/**
	 * global typeface
	 */
	public static Typeface tf;
	
	/**
	 * progress dialog object
	 */
	public PrettyProgressDialog progressDialog;
	
	public GoogleAnalyticsTracker tracker;
	
	public Boolean dialogIsVisible = false;
	
	//title of view
	protected TextView viewTitle; 
	
	protected final int currentapiVersion = android.os.Build.VERSION.SDK_INT;
	
	/**
	 * Retrieves typeface
	 * @return
	 */
	public Typeface getTypeface()
	{
		return BubuBaseActivity.tf;
	}
	
	/**
	 * Retrieve instance of proxy that handles all model operations
	 * @return
	 */
	public Proxy getProxy()
	{
		return Proxy.getInstance();
	}
	
	/**
	 * Base onCreate from standard activity
	 */
	@Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState); 
        
        //start GA
        tracker = GoogleAnalyticsTracker.getInstance();
        //Start the tracker in manual dispatch mode...
        tracker.startNewSession(getResources().getString(R.string.bbGA), 20, this);
        tracker.trackPageView(this.getClass().toString());
        
       
        //loads custom font
    	tf = Typeface.createFromAsset(getAssets(), "fonts/ExpletusSans-Regular.ttf");
    }    
	
	
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		/**
		 * Start warm up service if app gone cold
		 */
		DateTime current = new DateTime();
		DateTime lastWarmup = ((BubuApp)getApplicationContext()).getWarmupTime();
		
		if( lastWarmup  == null  ||   Minutes.minutesBetween(current, lastWarmup).getMinutes() > 5  )
		{	
			Intent warmapServiceIntent = new Intent(this, WarmUpService.class);
			startService(warmapServiceIntent);
			
			((BubuApp)getApplicationContext()).setWarmupTime(new DateTime());
		}	
	}
	
	/**
	 * Check if login expired, if so, log out
	 */
	private void handleExpirationDate()
	{
		
		long storedDate = getProxy().getStoredLastLoginDate(getApplicationContext());
		
		if(storedDate != 0 && isOnline() )
		{
		
			Days age = Days.daysBetween(new DateTime(storedDate), new DateTime());
			
			//longer that expiration period, 
			if(age.getDays() >= getResources().getInteger(R.integer.bbDaysForExpiration))
			{
				switchAccount();
			}
		}	
		
	}
	
	/**
	 * Reset state left overs from forms
	 */
	public void clearForm()
    {
    	//reset state
    	((BubuApp)getApplicationContext()).setSelectedImage(null);
        ((BubuApp)getApplicationContext()).setSelectedImageUri(null);
        ((BubuApp)getApplicationContext()).setSelectedImageLarge(null);
       
    }
	
	/**
	 * Checks if device is online
	 * @return
	 */
	public boolean isOnline() 
	{
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) 
	    {
	        return true;
	    }
	    return false;
	}
	
	/**
	 * Reset persistent account information and provide the option to re-select account
	 */
	public void switchAccount()
	{
		
		//clear all local data and cache
		getProxy().clearLocalStorage(getApplicationContext(), true);
		
		Intent i = new Intent(this, AccountActivity.class);
		startActivity(i);
		//kill activity
		finish();
	}
	
	
	/**
	 * Enhances bade onPause with new transition (fade) that will be used by all activities
	 */
	@Override
    protected void onPause()
    {
    	super.onPause();
    	
    	//set default transition (fade)
    	overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    	
    	if(inviteRefreshDialog != null && inviteRefreshDialog.isShowing())
    	{	
    		inviteRefreshDialog.setOnDismissListener(null);
    		inviteRefreshDialog.dismiss();
    	}	
    }	
	
	
	//activity is resumed, set calendar
	@Override
    protected void onResume() 
    {
        super.onResume();
        //check if grace period expired
    	handleExpirationDate();
    	
    	//check if an update footprint was left while app was closed
    	if(getProxy().retrieveEventFootPrint(getApplicationContext()) == 0)
	     {
	    	//kill if that was the case
    		finish();
    		overridePendingTransition(0, 0);
	   		
	    }  
    	else if(getProxy().retrieveEventFootPrint(getApplicationContext()) == 3)
    	{
    		displayInviteRefreshDialog();
    	}	
    	else if(inviteRefreshDialog != null && inviteRefreshDialog.isShowing())
    	{	
    		inviteRefreshDialog.setOnDismissListener(null);
    		inviteRefreshDialog.dismiss();
    	}	
    	
    }
	
	
	/**
	 * Construct top bar with back button(logo) and title
	 * @param logoId
	 * @param titleId
	 */
	protected void constructTopBar(int logoId, int titleId)
	{
		//set click for header home button
        ImageButton homeButton = (ImageButton)this.findViewById(logoId);
        
        //set click handler for loading local image into picture view
        homeButton.setOnClickListener(new OnClickListener() 
 	   	{
 	   	    public void onClick(View v) 
 	   	    {
 	   	    	finish();	   	    	
 	   	    }
 	   	}); 
        
        //populete title text
   		viewTitle = (TextView)this.findViewById(titleId);
   		viewTitle.setTypeface(getTypeface(), 1);
	}	
	
	/**
	 * creates ICS style menu button
	 * @param menuButtonId
	 */
	protected void constructMenuButton(int menuButtonId)
	{
		//activate top menu button 
    	ImageButton menuButton = (ImageButton)this.findViewById(menuButtonId);
		menuButton.setVisibility(0);
		menuButton.setOnClickListener(new OnClickListener() 
    	{
    	    public void onClick(View v) 
    	    {
    	    	if(!optionMenuIsOpen)
    	    	{	
    	    		try
    	    		{
    	    			openOptionsMenu();
    	    			optionMenuIsOpen = true;
    	    		}
    	    		catch(Exception e)
    	    		{
    	    			Log.w(Debug.TAG, "BUBU::OPTION MENU OPEN :: constructMenuButton EXCEPTION " + e.getMessage());
    	    		}
    	    	}	
    	    	else
    	    	{	
    	    		try
	    			{
    	    			closeOptionsMenu();
	    			}
	    	    	catch(Exception e)
		    		{
	    	    		Log.w(Debug.TAG, "BUBU::OPTION MENU CLOSE :: constructMenuButton EXCEPTION " + e.getMessage());
		    		}
    	    	}	
    	    }
    	});
	}
	
	/**
	 * Hack to style the options menu
	 * @throws Exception
	 */
	protected void setMenuBackground() throws Exception
	{
	    getLayoutInflater().setFactory
	    (
	    new Factory() 
	    {

	        @Override
	        public View onCreateView(final String name, final Context context,
	                final AttributeSet attrs) 
	        {
	        	
	        	if (name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuItemView")) 
	            {

	                try 
	                { // Ask our inflater to create the view
	                    final LayoutInflater f = getLayoutInflater();
	                    final View[] view = new View[1];
	                    try 
	                    {
	                        view[0] = f.createView(name, null, attrs); 
	                    } 
	                    catch (InflateException e) 
	                    {
	                        hackAndroid23(name, attrs, f, view);
	                    }
	                    // Kind of apply our own background
	                    new Handler().post
	                    (
	                    new Runnable() 
	                    {
	                        public void run() 
	                        {
	                            try
	                            {
	                            	view[0].setBackgroundResource(R.drawable.button_selector);
	                            	((TextView)view[0]).setTypeface(tf);
		                        }
	                            catch (Exception e) 
	        	                {
	        	                	//style stays as default
	        	                }
	                        }
	                    }
	                    );
	                    return view[0];
	                } 
	                catch (InflateException e) 
	                {
	                } 
	                catch (ClassNotFoundException e) 
	                {
	                }
	            }
	            else
            	if (name.equalsIgnoreCase("com.android.internal.view.menu.ListMenuItemView")) 
	            {	
            		try 
	                { // Ask our inflater to create the view
	                    final LayoutInflater f = getLayoutInflater();
	                    final View[] view = new View[1];
	                    try 
	                    {
	                        view[0] = f.createView(name, null, attrs);
	                    } 
	                    catch (InflateException e) 
	                    {
	                         hackAndroid23(name, attrs, f, view);
	                    }
	                    // Kind of apply our own background
	                    new Handler().post
	                    (
	                    new Runnable() 
	                    {
	                        public void run() 
	                        {
	                            try
	                            {
	                            	//if(currentapiVersion >= 11)
	                            		//((ViewGroup)view[0]).setBackgroundResource(R.drawable.button_selector_straight);
	                            	
	                            	((TextView)((ViewGroup)((ViewGroup)view[0]).getChildAt(0)).getChildAt(0)).setTypeface(tf);
	                            	((TextView)((ViewGroup)((ViewGroup)view[0]).getChildAt(0)).getChildAt(0)).setTextColor(getResources().getColor(R.color.bbMainTextColor));
	                            }
	                            catch (Exception e) 
	        	                {
	        	                	//style stays as default
	        	                }
	                            
	                        }
	                    }
	                    );
	                    return view[0];
	                } 
	                catch (InflateException e) 
	                {
	                } 
	                catch (ClassNotFoundException e) 
	                {
	                }
	                
	            }
	            
	            return null;
	        }
	    }
	    );	    
	}
	
	/**
	 * Extra menu layout hack for 2.3 (G'bread)
	 * @param name
	 * @param attrs
	 * @param f
	 * @param view
	 */
	static void hackAndroid23(final String name,  final android.util.AttributeSet attrs, final LayoutInflater f, final View[] view) 
	{
	    // mConstructorArgs[0] is only non-null during a running call to
	    // inflate()
	    // so we make a call to inflate() and inside that call our dully
	    // XmlPullParser get's called
	    // and inside that it will work to call
	    // "f.createView( name, null, attrs );"!
	    try {
	        	f.inflate
	        	(
	        	new XmlPullParser() 
	        	{
		            @Override
		            public int next() throws XmlPullParserException, IOException 
		            {
		                try 
		                {
		                   
		                	view[0] =  f.createView(name, null, attrs);
		                   
		                } 
		                catch (InflateException e) 
		                {
		                } 
		                catch (ClassNotFoundException e) 
		                {
		                }
		                throw new XmlPullParserException("exit");
		            }

					@Override
					public void defineEntityReplacementText(String entityName,
							String replacementText) throws XmlPullParserException 
					{
						// TODO Auto-generated method stub
						
					}

					@Override
					public int getAttributeCount() {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public String getAttributeName(int index) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getAttributeNamespace(int index) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getAttributePrefix(int index) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getAttributeType(int index) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getAttributeValue(int index) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getAttributeValue(String namespace, String name) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public int getColumnNumber() {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public int getDepth() {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public int getEventType() throws XmlPullParserException {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public boolean getFeature(String name) {
						// TODO Auto-generated method stub
						return false;
					}
	
					@Override
					public String getInputEncoding() {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public int getLineNumber() {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public String getName() {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getNamespace() {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getNamespace(String prefix) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public int getNamespaceCount(int depth)
							throws XmlPullParserException {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public String getNamespacePrefix(int pos)
							throws XmlPullParserException {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getNamespaceUri(int pos)
							throws XmlPullParserException {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getPositionDescription() {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getPrefix() {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public Object getProperty(String name) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getText() {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public char[] getTextCharacters(int[] holderForStartAndLength) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public boolean isAttributeDefault(int index) {
						// TODO Auto-generated method stub
						return false;
					}
	
					@Override
					public boolean isEmptyElementTag()
							throws XmlPullParserException {
						// TODO Auto-generated method stub
						return false;
					}
	
					@Override
					public boolean isWhitespace() throws XmlPullParserException {
						// TODO Auto-generated method stub
						return false;
					}
	
					@Override
					public int nextTag() throws XmlPullParserException, IOException {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public String nextText() throws XmlPullParserException,
							IOException {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public int nextToken() throws XmlPullParserException,
							IOException {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public void require(int type, String namespace, String name)
							throws XmlPullParserException, IOException {
						// TODO Auto-generated method stub
						
					}
	
					@Override
					public void setFeature(String name, boolean state)
							throws XmlPullParserException {
						// TODO Auto-generated method stub
						
					}
	
					@Override
					public void setInput(Reader in) throws XmlPullParserException {
						// TODO Auto-generated method stub
						
					}
	
					@Override
					public void setInput(InputStream inputStream,
							String inputEncoding) throws XmlPullParserException {
						// TODO Auto-generated method stub
						
					}
	
					@Override
					public void setProperty(String name, Object value)
							throws XmlPullParserException {
						// TODO Auto-generated method stub
						
					}
	        }, 
	        null, 
	        false
	        );
	    } 
	    catch (InflateException e1) 
	    {
	        // "exit" ignored
	    }
	}
	
	@Override
	protected void onDestroy() 
	{
	    super.onDestroy();
	    // Stop the GA tracker when it is no longer needed.
	    tracker.stopSession();
	    
	    //unregister receiver
        try
        {
        	unregisterReceiver(mReceiver);
        }
        catch(IllegalArgumentException e)
        {
        	Log.w(Debug.TAG, "BUBU::RECEIVER :: EVENTSHOWACTIVITY EXCEPTION " + e.getMessage());
        }
	}
	
	/*
     * Define receiver and action to be taken when event is intercepted
     */
    protected final BroadcastReceiver mReceiver = new BroadcastReceiver() 
    {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
        	//get extras from broadcaster
        	Bundle extras = intent.getExtras();
        	
        	broacastHandler(extras);        	
        }
    };
    
    protected void broacastHandler(Bundle extras)
    {
    	//if new data
    	if(extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)) != null 
    			&& extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)).equals(getResources().getString(R.string.bbCD2MInviteMessage)))
    	{	
    		
    		if(extras.getString(getResources().getString(R.string.bbUpdateEventAction)) != null)
 			{
 				((BubuApp)getApplicationContext()).setInviteNotificationState(extras.getString(getResources().getString(R.string.bbUpdateEventAction)));
 			}
    		
    		displayInviteRefreshDialog();
    		
    		//footprint
    		getProxy().storeEventFootPrint(getApplicationContext(), 3);
    	
    	}
    }
    
    /**
	 * true if options menu is open
	 */
	public boolean optionMenuIsOpen = false;
	
	
	@Override
	public void onOptionsMenuClosed(Menu menu) 
	{
		optionMenuIsOpen = false;
	}
	
	
	protected PrettyRefreshDialog inviteRefreshDialog;
	
	/**
	 * Construct refresh dialog
	 */
	protected void displayInviteRefreshDialog()
	{
		//create dimensions dialog if null
    	if(inviteRefreshDialog == null)
    		inviteRefreshDialog = new PrettyRefreshDialog(BubuBaseActivity.this, R.style.BubuDialogTheme);
    	
    	//display and populate dialog
    	inviteRefreshDialog.show(tf);
    	
    	//event handler for closing dialog
    	//set state with data collected in dialog
    	inviteRefreshDialog.setOnDismissListener(new OnDismissListener() 
    	{
    		@Override
    		public void onDismiss(DialogInterface dialog) 
    		{
    			updateUI();
    			getProxy().storeEventFootPrint(getApplicationContext(), -1);
    			
    		}
    	});
	}
	
	protected void updateUI()
	{
		Intent i = new Intent(this, JointUserFormActivity.class);
		startActivity(i);
	}
	
}
