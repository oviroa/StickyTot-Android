package com.garagewarez.bubu.android;

import java.io.IOException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.Months;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.garagewarez.bubu.android.base.BubuBaseActivity;
import com.garagewarez.bubu.android.base.EventImageView;
import com.garagewarez.bubu.android.common.EventData;
import com.garagewarez.bubu.android.common.EventResponse;
import com.garagewarez.bubu.android.utils.Convertor;
import com.garagewarez.bubu.android.utils.Tools;
import com.koushikdutta.ion.Ion;

/**
 * Activity used to display read only version of Event
 * @author oviroa
 *
 */
public class EventShowActivity extends BubuBaseActivity 
{
	//model
	private EventData eventData;
	
	//header view
	private RelativeLayout header;
	
	//footer view
	private RelativeLayout footer;
	
	//left toggle button
	private ImageButton leftButton;
	
	//event key
	private String eventDataKey;
	
	//left toggle button
	private ImageButton rightButton;
	
	//left toggle button
	private ImageButton upButton;
	
	//left toggle button
	private ImageButton downButton;
	
	//kid name
	private String name;
		
	//kids dob
	private Date dob;
	
	//child key
	private String childKey;
	
	//child key
	private String parentKey;

	//is joint
	private boolean selectedChildIsJoint;
	
	private int eventDataPosition;
	
	private String thumbUrl;
	
	private Button milestonesButton;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        
        //handles device orientation, displays alternative view for landscape
        int deviceOrientation = getResources().getConfiguration().orientation;
        
        if(deviceOrientation == Configuration.ORIENTATION_PORTRAIT)        
        	setContentView(R.layout.event_display);
        else
        	setContentView(R.layout.event_display_landscape);
        
        
        //get intent
        Intent i = getIntent();
        
        //retrieve child data from intent
        eventDataPosition = (int) i.getIntExtra(getResources().getString(R.string.bbEventData),-1);
        
        eventDataKey = (String)i.getStringExtra(getResources().getString(R.string.bbEventKey));
        
        name = (String)i.getStringExtra(getResources().getString(R.string.bbChildName));
   	 	
   	 	dob = (Date)i.getSerializableExtra(getResources().getString(R.string.bbDob));
     
	 	childKey = (String)i.getStringExtra(getResources().getString(R.string.bbChildKey));
	 	
	 	parentKey = (String)i.getStringExtra(getResources().getString(R.string.bbParentKey));
	 	
	 	thumbUrl = (String)i.getStringExtra(getResources().getString(R.string.bbChildThumbUrl));

		selectedChildIsJoint = (boolean)i.getBooleanExtra(getResources().getString(R.string.bbIsJoint),false);
	 	
	 	// Register as a receiver to listen for event/broadcasts
        IntentFilter filter = new IntentFilter(getResources().getString(R.string.bbUpdateEvent));
        registerReceiver(mReceiver, filter);
	 	
	 	retrieveEventData();
        
	 	if(eventData != null)// an event was found
	 		constructViewElements();
	 	else//no event was found in local storage, so kill activity
	 		finish();
	 	
	 	//activate top menu button 
    	constructMenuButton(R.id.bbMenuEventDisplay);
    	
    	milestonesButton = (Button)findViewById(R.id.display_milestones_button);
    	milestonesButton.setTypeface(tf,1);
    	milestonesButton.setOnClickListener
		(
				new OnClickListener() 
			   	{
			   	    public void onClick(View v) 
			   	    {
			   	    	showMilestones();
			   	    }
			   	}
		);		
    }  
	
	private void showMilestones()
	{
		Date eDate = eventData.getDate();
			
		int ageM = Months.monthsBetween(new DateTime(dob), new DateTime(eDate)).getMonths();
		
		Intent i = new Intent(EventShowActivity.this, MilestonesActivity.class);
		
		//send age in months
		i.putExtra(getApplicationContext().getResources().getString(R.string.bbChildAge), ageM);
		//send human readable age
		i.putExtra(getApplicationContext().getResources().getString(R.string.bbChildAgeStr), Tools.getAgeStr(dob, eDate, getApplicationContext()));
		//send name
		i.putExtra(getApplicationContext().getResources().getString(R.string.bbChildName), name);
		//send image
		i.putExtra(getApplicationContext().getResources().getString(R.string.bbChildThumbUrl), thumbUrl);
		
		
		startActivity(i);
	}
	
	
	/**
	 * Retrieves event data from local storage based on key
	 */
	private void retrieveEventData()
	{
		List<EventData> list = null;
		
		try 
	 	{
			EventResponse eventResponse = getProxy().getStoredEventResponse(getApplicationContext(), childKey);
			
			if(eventResponse == null || (eventResponse == null && eventDataPosition == -1))
				finish();
			
			list = eventResponse.getList();
			
			for(EventData ed : list)
			{
				if(ed.getEncodedKey().equals(eventDataKey))
				{
					eventData = ed;
				}	
			}	
			
			
		} 
	 	catch (OptionalDataException e) 
		{
	 		ErrorHandler.execute
			(
					EventShowActivity.this, 
					getResources().getString(R.string.bbProblem),
					"EventShowActivity 135: ", 
					e
			);
		} 
		catch (StreamCorruptedException e) 
		{
			ErrorHandler.execute
			(
					EventShowActivity.this, 
					getResources().getString(R.string.bbProblem),
					"EventShowActivity 145: ", 
					e
			);
			
		} 
		catch (ClassNotFoundException e) 
		{
			ErrorHandler.execute
			(
					EventShowActivity.this, 
					getResources().getString(R.string.bbProblem),
					"EventShowActivity 156: ", 
					e
			);
			
		} 
		catch (IOException e) 
		{
			ErrorHandler.execute
			(
					EventShowActivity.this, 
					getResources().getString(R.string.bbProblem),
					"EventShowActivity 167: ", 
					e
			);
			
		}


	}
	
	/**
	 * Create and style view elements, populate with data
	 */
	private void constructViewElements()
	{
		
        
        //inflate header
        header = (RelativeLayout)findViewById(R.id.bbEventDisplayHeader);
        
        //inflate footer
        footer = (RelativeLayout)findViewById(R.id.bbEventDisplayFooter);
        
        //construct top bar with back button and title
        constructTopBar(R.id.homeButtonEventDisplay, R.id.textViewEventDisplay);
        
        
        //note
        TextView noteLabel = (TextView)findViewById(R.id.bbEventDisplayNoteLabel);
        noteLabel.setTypeface(tf);
        
        //title for date
        TextView title = (TextView)findViewById(R.id.textViewEventDisplay);
        
        //prep date
        DateFormat formatter = new SimpleDateFormat("MMM/dd/yyyy", Locale.US);
        title.setText(formatter.format(eventData.getDate()));
        title.setTypeface(tf);
        
        //title
        TextView subTitle = (TextView)findViewById(R.id.subTitleEventDisplay);
        subTitle.setText
        (
        		Html.fromHtml
        		(
        				new StringBuffer().
                		append(getResources().getString(R.string.bbFor)).
                		append(" <b>").
                		append(name).
                		append("</b>").
                		toString()
        		)
        ); 
        
        subTitle.setTypeface(tf);
        
        //image
        EventImageView image = (EventImageView) findViewById(R.id.bbEventDisplayImage);	
        
        ((ImageView)(image.getChildAt(1))).setBackgroundResource(R.drawable.transparent);

        if(eventData.getPic().getMain().equals(getResources().getString(R.string.bbDefaultImageURL)))
        {
        	Ion.with((ImageView)(image.getChildAt(1))).
					placeholder(R.drawable.transparent).
					load(new StringBuffer().append(eventData.getPic().getMain()).toString().replace(".png","_large.png"));
        	
        }	
        else
        {
        	Ion.with((ImageView)(image.getChildAt(1))).
					placeholder(R.drawable.transparent).
					load(new StringBuffer().append(eventData.getPic().getMain()).append("&and=1").toString());
        }	
        
        
        //note
        TextView note = (TextView) findViewById(R.id.bbEventDisplayNote);
        note.setText(eventData.getNote());
        note.setTypeface(tf);
        note.setMovementMethod(new ScrollingMovementMethod());
        
        //if tablet
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE)
        {
        	//handles device orientation, displays alternative view for landscape
            int deviceOrientation = getResources().getConfiguration().orientation;
            
            if(deviceOrientation == Configuration.ORIENTATION_PORTRAIT)        
            	note.setLines(getResources().getInteger(R.integer.bbEventNoteLinesTabletPortrait));
            else
            	note.setLines(getResources().getInteger(R.integer.bbEventNoteLinesTabletLandscape));
        }
        //set up buttons
        setupToggleButtons();
        
        //child data fields
        
        TextView weightView = (TextView) findViewById(R.id.bbEventDisplayWeight);
        weightView.setTypeface(tf);
        
        weightView.setText
        (
        		Html.fromHtml
        		(
    				new StringBuffer().
	        		append(getResources().getString(R.string.bbEventWeight)).
	        		append(": ").
	        		append("<b>").
	        		append(Convertor.trimTrailingZeros(Convertor.weight(eventData.getMeasurement().getWeight(), true))).
	        		append("</b>").
	        		toString()
	        	)	
        );
        
        TextView heightView = (TextView) findViewById(R.id.bbEventDisplayHeight);
        heightView.setTypeface(tf);
        
        heightView.setText
        (
        		Html.fromHtml
        		(
    				new StringBuffer().
	        		append(getResources().getString(R.string.bbEventHeight)).
	        		append(": ").
	        		append("<b>").
	        		append(Convertor.trimTrailingZeros(Convertor.height(eventData.getMeasurement().getHeight(),true))).
	        		append("</b>").
	        		toString()
	        	)	
        );
        
        TextView weightViewPercentile = (TextView) findViewById(R.id.bbEventDisplayWeightPercentile);
        weightViewPercentile.setTypeface(tf);
        
        weightViewPercentile.setText
        (
        		Html.fromHtml
        		(
    				new StringBuffer().
	        		append(getResources().getString(R.string.bbWeightPercentile)).
	        		append(": ").
	        		append("<b>").
	        		append(eventData.getPercentile() != null ? eventData.getPercentile().getByWeight() : getResources().getString(R.string.bbNA)).
	        		append("</b>").
	        		toString()
	        	)	
        );
        
        TextView heightViewPercentile = (TextView) findViewById(R.id.bbEventDisplayHeightPercentile);
        heightViewPercentile.setTypeface(tf);
        
        heightViewPercentile.setText
        (
        		Html.fromHtml
        		(
    				new StringBuffer().
	        		append(getResources().getString(R.string.bbHeightPercentile)).
	        		append(": ").
	        		append("<b>").
	        		append(eventData.getPercentile() != null ? eventData.getPercentile().getByHeight() : getResources().getString(R.string.bbNA)).
	        		append("</b>").
	        		toString()
	        	)	
        );
	}
	

	/**
	 * Creates events for toggle buttons
	 */
	private void setupToggleButtons()
	{
		
		
		
		
		//button used to hide header
		leftButton = (ImageButton) findViewById(R.id.bbHideTitleButton);
		//button used to hide header
		rightButton = (ImageButton) findViewById(R.id.bbShowTitleButton);
		
		//button used to show footer
		upButton = (ImageButton) findViewById(R.id.bbShowFooterButton);
		
		//button used to hide footer
		downButton = (ImageButton) findViewById(R.id.bbHideFooterButton);
		
		
		//button delegate containers
		final View hideHorizontalParent = findViewById(R.id.bbHideTitleButtonContainer); 
		final View showHorizontalParent = findViewById(R.id.bbShowTitleButtonContainer);
		final View hideVerticalParent = findViewById(R.id.bbHideFooterButtonContainer); 
		final View showVerticalParent = findViewById(R.id.bbShowFooterButtonContainer);
		
		
		//set visibility for footer and buttons
		if(((BubuApp)getApplicationContext()).getFooterVisibility())
		{
			footer.setVisibility(0);
			hideVerticalParent.setVisibility(0);
			showVerticalParent.setVisibility(4);
		}
		else
		{
			footer.setVisibility(4);
			hideVerticalParent.setVisibility(4);
			showVerticalParent.setVisibility(0);
		}	
		
		if(((BubuApp)getApplicationContext()).getHeaderVisibility())
		{
			header.setVisibility(0);
			hideHorizontalParent.setVisibility(0);
			showHorizontalParent.setVisibility(4);
		}
		else
		{
			header.setVisibility(4);
			hideHorizontalParent.setVisibility(4);
			showHorizontalParent.setVisibility(0);
		}	
		
		
		hideHorizontalParent.post
		(
				new Runnable() 
				{ 
			      @Override 
			      public void run() 
			      { 
				        Rect delegateAreaLeft = new Rect(0,0,hideHorizontalParent.getWidth(),hideHorizontalParent.getHeight()); 
				        
				        //Rect delegateAreaRight = new Rect();
				        hideHorizontalParent.setTouchDelegate(new TouchDelegate(delegateAreaLeft, leftButton));
				        
			      } 
				}
		); 
		 
		
		//hide header
		leftButton.setOnClickListener
		(
				new OnClickListener() 
			   	{
			   	    public void onClick(View v) 
			   	    {
			   	    	if(hideHorizontalParent.getVisibility()==0)
			   	    	{
				   	    	header.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.toggle_out_vertical));
				   	    	header.setVisibility(4);
				   	    	((BubuApp)getApplicationContext()).setHeaderVisibility(false);
				   	    	
				   	    	hideHorizontalParent.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.toggle_button_left_out_vertical));
				   	    	hideHorizontalParent.setVisibility(4);
				   	    	
				   	    	showHorizontalParent.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.toggle_button_right_in_vertical));
				   	    	showHorizontalParent.setVisibility(0);
			   	    	}	
			   	    }
			   	}    
			   	 
		);
		
		
		showHorizontalParent.post
		(
				new Runnable() 
				{ 
			      @Override 
			      public void run() 
			      { 
				        Rect delegateAreaRight = new Rect(0,0,showHorizontalParent.getWidth(),showHorizontalParent.getHeight()); 
				        
				        //Rect delegateAreaRight = new Rect();
				        showHorizontalParent.setTouchDelegate(new TouchDelegate(delegateAreaRight, rightButton));
				        
			      } 
				}
		);
		
		
		//show header
		rightButton.setOnClickListener
		(
				new OnClickListener() 
			   	{
			   	    public void onClick(View v) 
			   	    {
			   	    	
			   	    	if(showHorizontalParent.getVisibility()==0)
			   	    	{
				   	    	header.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.toggle_in_vertical));
				   	    	header.setVisibility(0);
				   	    	((BubuApp)getApplicationContext()).setHeaderVisibility(true);
				   	    	
				   	    	hideHorizontalParent.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.toggle_button_left_in_vertical));
				   	    	hideHorizontalParent.setVisibility(0);
				   	    	
				   	    	showHorizontalParent.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.toggle_button_right_out_vertical));
				   	    	showHorizontalParent.setVisibility(4);
			   	    	}
			   	    }
			   	}    
			   	 
		);
		
		
		hideVerticalParent.post
		(
				new Runnable() 
				{ 
			      @Override 
			      public void run() 
			      { 
				        Rect delegateAreaDown = new Rect(0,0,hideVerticalParent.getWidth(),hideVerticalParent.getHeight()); 
				        
				        //Rect delegateAreaRight = new Rect();
				        hideVerticalParent.setTouchDelegate(new TouchDelegate(delegateAreaDown, downButton));
				        
			      } 
				}
		); 
		 
		
		//hide header
		downButton.setOnClickListener
		(
				new OnClickListener() 
			   	{
			   	    public void onClick(View v) 
			   	    {
			   	    	if(hideVerticalParent.getVisibility()==0)
			   	    	{
				   	    	footer.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.down_out));
				   	    	footer.setVisibility(4);
				   	    	((BubuApp)getApplicationContext()).setFooterVisibility(false);
				   	    	
				   	    	hideVerticalParent.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.toggle_button_right_out_vertical));
				   	    	hideVerticalParent.setVisibility(4);
				   	    	
				   	    	showVerticalParent.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.toggle_button_right_in_vertical));
				   	    	showVerticalParent.setVisibility(0);
			   	    	}	
			   	    }
			   	}    
			   	 
		);
		
		
		showVerticalParent.post
		(
				new Runnable() 
				{ 
			      @Override 
			      public void run() 
			      { 
				        Rect delegateAreaUp = new Rect(0,0,showVerticalParent.getWidth(),showVerticalParent.getHeight()); 
				        
				        //Rect delegateAreaRight = new Rect();
				        showVerticalParent.setTouchDelegate(new TouchDelegate(delegateAreaUp, upButton));
				        
			      } 
				}
		); 
		 
		
		//hide header
		upButton.setOnClickListener
		(
				new OnClickListener() 
			   	{
			   	    public void onClick(View v) 
			   	    {
			   	    	if(showVerticalParent.getVisibility()==0)
			   	    	{
				   	    	footer.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.up_in));
				   	    	footer.setVisibility(0);
				   	    	((BubuApp)getApplicationContext()).setFooterVisibility(true);
				   	    	
				   	    	showVerticalParent.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.toggle_button_right_out_vertical));
				   	    	showVerticalParent.setVisibility(4);
				   	    	
				   	    	hideVerticalParent.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.toggle_button_right_in_vertical));
				   	    	hideVerticalParent.setVisibility(0);
			   	    	}	
			   	    }
			   	}    
			   	 
		);
		
		
	}
	
	/**
     * Creates options menu 
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
	    //inflate menu based on xml menu
		MenuInflater inflater = getMenuInflater();
	    

		if(!selectedChildIsJoint)
			inflater.inflate(R.menu.events_menu, menu);
		else
			inflater.inflate(R.menu.events_menu_readonly, menu);

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
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) 
	    {
	    	//edit button
	    	case R.id.bbEditEventDisplayButton:
	    		onEditEvent();
	    		return true;
	    	
	    	case R.id.bbShareEventDisplayButton:
	    		onShareEvent();
	    		return true;
	    		
	    	case R.id.bbMilestonesEventDisplayButton:
	    		showMilestones();
	    		return true;
	    		
	    	default:
	    		
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	/**
	 * Edits event in new view/activity
	 * @param id
	 */
	private void onEditEvent()
	{
		
		//clear persistent data
		((BubuApp)getApplicationContext()).setSelectedImageLarge(null);
    	((BubuApp)getApplicationContext()).setSelectedImage(null);
        ((BubuApp)getApplicationContext()).setSelectedImageUri(null);
		
		// Perform action on clicks
        Intent i = new Intent(this, EventDetailActivity.class);
		
        Bundle mB = new Bundle();
	
        mB.putSerializable(getResources().getString(R.string.bbEventData), eventData);
		mB.putSerializable(getResources().getString(R.string.bbDob), dob);
		mB.putString(getResources().getString(R.string.bbChildKey),childKey);
		mB.putString(getResources().getString(R.string.bbParentKey),parentKey);
		mB.putString(getResources().getString(R.string.bbChildThumbUrl),thumbUrl);
		mB.putString(getResources().getString(R.string.bbChildName),name);
		
		i.putExtras(mB);
	        
	    startActivity(i);
		
	}
	
	/**
	 * Share event link via existing/compatible apps
	 */
	private void onShareEvent()
	{
		
		String extraText = new StringBuffer()
							.append(getResources().getString(R.string.bbEventLinkPrefix))
							.append(eventData.getEncodedKey())
							.toString(); 
			
		Intent sendIntent = new Intent();
		sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, extraText);
		sendIntent.setType("text/plain");
		startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.bbShareLinkTo)));		
	}
	
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		
		
		if(((BubuApp)getApplicationContext()).getEventListFreshStatus())
		{
			retrieveEventData();
			constructViewElements();
		}
		
	}
	
	
	@SuppressLint("InlinedApi")
	@Override
	protected void broacastHandler(Bundle extras)
	{
		
		super.broacastHandler(extras);
		
		if(extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)) != null 
				&& extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)).equals(getResources().getString(R.string.bbInviteRejectionMessage)) 
				&& extras.getString(getResources().getString(R.string.bbKey)).equals(childKey))
		{	
			finish();
		}
		
		if(extras.getString(getResources().getString(R.string.bbReset)) != null 
				&& extras.getString(getResources().getString(R.string.bbReset)).equals(getResources().getString(R.string.bbCD2MEventMessage)))
		{	
			Intent i = new Intent(EventShowActivity.this, AccountActivity.class);
			startActivity(i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
			finish();
		}
		
		if(extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)) != null 
				&& extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)).equals(getResources().getString(R.string.bbCD2MChildMessage)))
		{
			finish();
		}
	}	
	
}
