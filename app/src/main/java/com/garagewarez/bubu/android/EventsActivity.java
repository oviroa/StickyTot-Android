package com.garagewarez.bubu.android;

import java.io.IOException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.client.ClientProtocolException;
import org.joda.time.DateTime;
import org.joda.time.Months;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.garagewarez.bubu.android.EventsVPAdapter.EventClickListener;
import com.garagewarez.bubu.android.base.BubuCollectionActivity;
import com.garagewarez.bubu.android.base.MilestoneView;
import com.garagewarez.bubu.android.base.MilestoneView.MilestoneClickListener;
import com.garagewarez.bubu.android.base.RotationSafeCollectionAsyncTask;
import com.garagewarez.bubu.android.common.EventData;
import com.garagewarez.bubu.android.common.EventResponse;
import com.garagewarez.bubu.android.common.Measurement;
import com.garagewarez.bubu.android.proxy.Proxy;
import com.garagewarez.bubu.android.utils.CustomBuilder;
import com.garagewarez.bubu.android.utils.Debug;
import com.garagewarez.bubu.android.utils.ScreenUtil;
import com.garagewarez.bubu.android.utils.Tools;
import com.garagewarez.bubu.android.utils.UrlImageView;

/**
 * Activity showing list of events
 * @author oviroa
 *
 */
public class EventsActivity extends BubuCollectionActivity implements SeekBar.OnSeekBarChangeListener
{
	
	private Date dob;
	private String name;
	private Measurement newestMeasurement;
	private String thumbUrl;
	
	private ViewPager eventsViewPager;
	private SeekBar seekBarVP;
	private TextView empty;
	private RelativeLayout mainContainer;
	private int multiplier;
	private int focusedPage = 0;
	private TextView currentDate;
	private boolean seekTouched = false;
	
	//scrolling indicator arrows
	private ImageView indicatorLeft;
	private ImageView indicatorRight;
	
	//event data
	private EventResponse eventResponse;
	
	//hint overlay
	private RelativeLayout hintLayout;
	
	//current page in selector
	private int currentSelectorPage = 0;
	private double eventsPerPage;
	
	private boolean isSavedInstance = false;
	
	//intent used to receive data from the previous activity and to send data to next activity
	private Intent intent;
	
	private Uri sharedImageUri;
	
	//event interval selector
	Spinner intervalSelector;
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        
    	super.onCreate(savedInstanceState); 
    	
    	//handles device orientation, displays alternative view for landscape
        int deviceOrientation = getResources().getConfiguration().orientation;
        
        if(deviceOrientation == Configuration.ORIENTATION_PORTRAIT)        
        	setContentView(R.layout.event_list);
        else
        	setContentView(R.layout.event_list_landscape);
    	
    	//number of events per screen
    	multiplier = getResources().getInteger(R.integer.bbEventsPerPage) * ScreenUtil.getMultiplier(getApplicationContext());
    	
    	eventsPerPage = multiplier * getResources().getInteger(R.integer.bbEventsPerInterval);
    	
//_______handle data from previous view
    	
    	intent = getIntent();
    	
    	//populate list with EventResponse object passed via bundle
    	Bundle extras = intent.getExtras();
    	
    	dob = (Date)extras.getSerializable(getResources().getString(R.string.bbDob));
    	childKey = (String)extras.getString(getResources().getString(R.string.bbChildKey));
    	parentKey = (String)extras.getString(getResources().getString(R.string.bbParentKey));
		selectedChildIsJoint = (boolean)extras.getBoolean(getResources().getString(R.string.bbIsJoint));
    	name = (String)extras.getString(getResources().getString(R.string.bbChildName));
    	thumbUrl = (String)extras.getString(getResources().getString(R.string.bbChildThumbUrl));
    	
    	
//_____assign/handle view elements
    	
    	//current date
    	currentDate = (TextView)findViewById(R.id.textCurrentEventDate);
    	currentDate.setTypeface(tf, 1);
    	
    	indicatorLeft = (ImageView)findViewById(R.id.bbIndicatorLeft);
    	indicatorRight = (ImageView)findViewById(R.id.bbIndicatorRight);
    	
    	//interval selector
    	intervalSelector = (Spinner)findViewById(R.id.bbEventIntevalSelector);
    	
    	
    	
    	//set spinner listener to change current selector page and run a refresh
    	intervalSelector.setOnItemSelectedListener(new OnItemSelectedListener() 
    	{
    	    @Override
    	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) 
    	    {
    	    	 
    	    	 isSavedInstance = false;
    	    	 currentSelectorPage = position;
    	    	 refresh();
    	    	
    	    }

    	    @Override
    	    public void onNothingSelected(AdapterView<?> parentView) 
    	    {
    	    	Log.w(Debug.TAG,"Nothing selected");
    	    }

    	});
    	
    	//construct top bar with back button and title
        constructTopBar(R.id.homeButtonEventList, R.id.textViewEventList);

        //replacement view for when no data is present
        empty =  (TextView)findViewById(R.id.bbEmptyEventList);
    	
        //container of data view
        mainContainer = (RelativeLayout)findViewById(R.id.bbEventsContainer);
        
        //title
        TextView title = (TextView)findViewById(R.id.childNameInEvent);
        title.setText
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
        
        title.setTypeface(tf);
        
    	//add event button inflate
    	Button addEventButton = (Button)this.findViewById(R.id.bbAddEventButton);
    	addEventButton.setTypeface(tf, 1);


		//if not events from joint user
		if(!selectedChildIsJoint)
		{
			addEventButton.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					addEvent();
				}
			});
		}
		else//otherwise, kill add button
			addEventButton.setVisibility(View.GONE);
	    	
    	//child image
    	ImageView thumb = (ImageView)findViewById(R.id.bbChildImage);
    	//add image
    	thumb.setBackgroundResource(R.drawable.transparent);
    	UrlImageView.setBackgroundResource(R.drawable.rounded_corners_noimage);
    	UrlImageView.setUrlDrawable
		(
			thumb, 
			thumbUrl
		);
    	
//________handle async task
    	
    	//check if async task is running, kill it if so and remove progress dialog
    	handleRunningAsyncTask((DeleteTask)getLastNonConfigurationInstance());
    	
    	
        //_________handle data refresh and events for CD2M
		
        handleRefresh();
    	
        
        
        //if image was shared
        //get stuff from intent
      	String action = intent.getAction();
          	
	    //check if something got shared
	    if (Intent.ACTION_SEND.equals(action) && extras.containsKey(Intent.EXTRA_STREAM))
        {
            //Get resource path from intent caller
            sharedImageUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
            
            //if shared, move to event detail acyivity
            if(sharedImageUri != null)
            {
            	addEventShared();
            }
        }
	    	
        
    }
	
	@Override
    public void onRestoreInstanceState(Bundle outState) 
    {
        super.onSaveInstanceState(outState);
        
        
        //manipulate visible screen at rotation
        //create flag, if true, at refresh, position slider and selector correctly, if not, start at default
        isSavedInstance = true;
    }
	
	/**
	 * Handle refresh of collection when new elements are present
	 */
	@Override
	protected void handleRefresh()
	{
		
		//inflate view pager
		eventsViewPager = (ViewPager)this.findViewById(R.id.bbEventsViewPager);
		
		loadData();
		
		if(eventResponse != null)
		{	
		
			// Register as a receiver to listen for event/broadcasts
	        IntentFilter filter = new IntentFilter(getResources().getString(R.string.bbUpdateEvent));
	        registerReceiver(mReceiver, filter);
	        
	        seekBarVP = (SeekBar)this.findViewById(R.id.bbSeekBarVP);
	        
	        try
	        {
	        	seekBarVP.setThumb(null);
	        }
	        catch(NullPointerException e){}
	        
	        seekBarVP.setOnSeekBarChangeListener(this);
	    	
	        seekBarVP.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_horizontal));
	        
	        //prepare dropdown
	        handleSelector();
	       
		}
		else
		{
			finish();
		}
	}
	
	@Override
	public void loadData()
	{
		try 
    	{
			eventResponse = getProxy().getStoredEventResponse(getApplicationContext(), childKey);				
		} 
    	
    	catch (OptionalDataException e) 
		{
    		ErrorHandler.execute
			(
					EventsActivity.this, 
					getResources().getString(R.string.bbProblem),
					"EventsActivity 248: ", 
					e
			);
		} 
		catch (StreamCorruptedException e) 
		{
			ErrorHandler.execute
			(
					EventsActivity.this, 
					getResources().getString(R.string.bbProblem),
					"EventsActivity 258: ", 
					e
			);
			
		} 
		catch (ClassNotFoundException e) 
		{
			ErrorHandler.execute
			(
					EventsActivity.this, 
					getResources().getString(R.string.bbProblem),
					"EventsActivity 269: ", 
					e
			);
			
		} 
		catch (IOException e) 
		{
			ErrorHandler.execute
			(
					EventsActivity.this, 
					getResources().getString(R.string.bbProblem),
					"EventsActivity 280: ", 
					e
			);
			
		}
		
	}
	
	
	/**
	 * Handle selector refresh and visibility
	 */
	public void handleSelector()
	{
		//we have a larger number of events, so build selector 
		if(eventResponse.getList().size() > eventsPerPage)
			constructSelector();
		else
		{	
			isSavedInstance = true;
			refresh();
		}	
	}
	
	
	/**
	 * 
	 * @param er
	 */
	@Override
	public void refresh()
	{
		if(eventResponse != null)
		{	
		
			List<EventData> smallList;
	        
	        if(eventResponse.getList().size() <= eventsPerPage)
	        {
	        	smallList= eventResponse.getList();
	        }	
	        else
	        {	
	        	
	        	smallList= eventResponse.getList().subList
	        				(
	        					(int)eventsPerPage*currentSelectorPage,
	        					(int)eventsPerPage*(currentSelectorPage+1) > eventResponse.getList().size()-1
									? eventResponse.getList().size()
									: (int)eventsPerPage*(currentSelectorPage+1)
	        				);
	        }
			
	        //get persistent logged in user's parent key
			String pk;
			try 
			{
				pk = getProxy().getStoredParentData(getApplicationContext()).getEncodedKey();
			
	        
				//we have a smaller number of events, 
				eventsViewPager.setAdapter(new EventsVPAdapter
						(
								this, 
								smallList,
								new EventClickListener()
								{
									@Override
									public void click(int position) 
									{
										displayEvent(position);															
									}
								},
								currentSelectorPage,
								pk,
								selectedChildIsJoint
								
						));
				
				eventsViewPager.setOnPageChangeListener(new EventsViewPagerChangeListener()); 
				
				//if true, at refresh, position slider and selector correctly, if not, start at default
				if(isSavedInstance)
				{	
					focusedPage = (((BubuApp)getApplicationContext()).getPreEditFirstVisibleEvent() % (int)eventsPerPage)/multiplier;
					eventsViewPager.setCurrentItem(focusedPage);
					isSavedInstance = false;
				}	
				else
				{	
					eventsViewPager.setCurrentItem(0);
					focusedPage = 0;
				} 
				
				
				seekBarVP.setMax(eventsViewPager.getAdapter().getCount());
				
				//if the list has elements
				if(smallList.size() > 0)
				{	
					newestMeasurement = eventResponse.getList().get(0).getMeasurement();
		    	    seekBarVP.setVisibility(0);
		    	    empty.setVisibility(8);
					mainContainer.setVisibility(0);
					
					//no seek bar, no milestones for just one screen
					if(eventsViewPager.getAdapter().getCount() == 1)
					{	
						seekBarVP.setVisibility(4);
						((LinearLayout) findViewById(R.id.bbEventsMilestones)).setVisibility(8);
					}
					else
					{
						buildMilestones();
					}
					
					
					handleArrows(focusedPage);
					
				}
				else
				{	
					seekBarVP.setVisibility(4);
					empty.setVisibility(0);
					mainContainer.setVisibility(8);
					
				}	
				
				
				//check to see if edits reduced the number of sliders, set to last if it did
				if(eventsViewPager.getAdapter().getCount() < focusedPage+1)
				{	
					eventsViewPager.setCurrentItem(eventsViewPager.getAdapter().getCount()-1);
				}	
				else 
				{
					int buffer = focusedPage +1;
					seekBarVP.setProgress(0);
					seekBarVP.setProgress(buffer);
					
				}
				
				handleArrows(focusedPage);
			}
			catch (OptionalDataException e) 
			{
				ErrorHandler.execute
				(
						EventsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"EventsActivity 363: event data is null", 
						null
				);
			} 
			catch (StreamCorruptedException e) 
			{
				ErrorHandler.execute
				(
						EventsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"EventsActivity 483: event data is null", 
						e
				);
			} 
			catch (ClassNotFoundException e) 
			{
				ErrorHandler.execute
				(
						EventsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"EventsActivity 493: event data is null", 
						e
				);
			} 
			catch (IOException e) 
			{
				ErrorHandler.execute
				(
						EventsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"EventsActivity 503: event data is null", 
						e
				);
			}
		}
		else
		{
			ErrorHandler.execute
			(
					EventsActivity.this, 
					getResources().getString(R.string.bbProblem),
					"EventsActivity 363: event data is null", 
					null
			);
		}
		
		eventsViewPager.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fast_fade_in));
	}
	
	
	/**
	 * Build date interval selector spinner, set content, style, fonts
	 */
	private void constructSelector()
	{
		
		 //list size
        int size = eventResponse.getList().size();
        
        if(size > eventsPerPage)
        {	
        	intervalSelector.setVisibility(0);
        	
			//create spinner base on xml array
	        List<String> items = new ArrayList<String>();
	        
	        
	        //selector size
	        double selectorSize = Math.ceil(size/eventsPerPage);
	        
	        //go through the events and create labels
	        for(int i = 0; i < (int)selectorSize; i++ )
	        {
	        	String label = "";
	        	DateFormat formatter = new SimpleDateFormat("MMM dd yyyy", Locale.US);
	        	
	        	if((i+1)*(int)eventsPerPage > size)
	        	{	
	        		label = formatter.format(eventResponse.getList().get(size-1).getDate()) + " - " + //start
	        		formatter.format(eventResponse.getList().get(i*(int)eventsPerPage).getDate()); //end
	        	}
	        	else
	        	{
	        		label = formatter.format(eventResponse.getList().get((i+1)*(int)eventsPerPage-1).getDate()) + " - " + //start
	        		formatter.format(eventResponse.getList().get(i*(int)eventsPerPage).getDate()); //end
	        	}	
	        	
	        	items.add(label);
	        }	
	        	
	        
	        //set adapter based on XML item
	        ArrayAdapter<String> selectorAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) 
	        {
	
	        	public View getView(int position, View convertView, ViewGroup parent) 
	        	{
	        		View v = super.getView(position, convertView, parent);
	
	        		//set typeface for selected item
	        		((TextView) v).setTypeface(getTypeface(),1);
	
	        		return v;
	        	}
	
	
	        	public View getDropDownView(int position,  View convertView,  ViewGroup parent) 
	        	{
	        		View v =super.getDropDownView(position, convertView, parent);
	
	        		//set typeface for list of options
	        		((TextView) v).setTypeface(getTypeface(),1);
	
	        		return v;
	        	}
	        
	        };
	        
	        selectorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        intervalSelector.setAdapter(selectorAdapter);	        
	       
        }
        else
        {
        	intervalSelector.setVisibility(8);
        }	
	}
	
	/**
	 * Display read only view for event at position (parameter)
	 * @param eventPosition
	 */
	private void displayEvent(int eventPosition)
	{
		
		// Perform action on clicks
        Intent i = new Intent(this, EventShowActivity.class);
		Bundle mB = new Bundle();
        
		mB.putInt
		(
				
				getResources().getString(R.string.bbEventData), 
				(int)eventPosition
		);
		
		mB.putString(getResources().getString(R.string.bbChildName), name);
		mB.putSerializable(getResources().getString(R.string.bbDob), dob);
		mB.putString(getResources().getString(R.string.bbChildKey), childKey);
		mB.putString(getResources().getString(R.string.bbParentKey),parentKey);
		mB.putString(getResources().getString(R.string.bbEventKey),eventResponse.getList().get(eventPosition).getEncodedKey());
		mB.putString(getResources().getString(R.string.bbChildThumbUrl),thumbUrl);
		mB.putBoolean(getResources().getString(R.string.bbIsJoint), selectedChildIsJoint);
		
		i.putExtras(mB);
        
		startActivity(i);	
		
	}
	
	
	
	
	/**
	 * Create string of milestone buttons underneath the main list view
	 * @param er
	 */
	private void buildMilestones()
	{
		//number of milestones
        int count = ScreenUtil.getMilestoneCount(getApplicationContext());
        List<EventData> smallList;
        
        if(eventResponse.getList().size() <= eventsPerPage)
        {
        	smallList= eventResponse.getList();
        }	
        else
        {	
        	
        	smallList= eventResponse.getList().subList
        				(
        					(int)eventsPerPage*currentSelectorPage,
        					(int)eventsPerPage*(currentSelectorPage+1) > eventResponse.getList().size()-1
								? eventResponse.getList().size()
								: (int)eventsPerPage*(currentSelectorPage+1)
        				);
        }
        
		if(count != 0)
		{
			
			//layout for milestones   	
	    	LinearLayout milestoneContainer = (LinearLayout) findViewById(R.id.bbEventsMilestones);
	    	milestoneContainer.setVisibility(0);
	    	milestoneContainer.removeAllViews();
	    	
	    	DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
	        MilestoneView mView;
	    	
	    	//if there are more milestones supported than number of screens, limit milestones
	        if(count > eventsViewPager.getAdapter().getCount())
	    		count = eventsViewPager.getAdapter().getCount();
	    	
	        //number of events per milestone
	    	final int eventInterval = smallList.size()/(count-1);
	    	
	    	//used to distribute
	    	int remainder = smallList.size() % (count-1);
	    	
	    	
	    	//screens to used for extra distribution of remainder
	    	int delta = count-remainder;
	    	
	    	
	    	//position of event in milestone
	    	int eventId = -eventInterval;
	    	
	    	//construct milestones
	    	for(int i=0; i < count; i++)
	    	{
	    		
	    		//if the last screens of sequence
	    		if(remainder > 0 && i >= delta)
	    		{	
	    			//current event used for milestone
	    			remainder--;
	    			eventId += eventInterval + 1;
	    		}
	    		else
	    			eventId += eventInterval;
	    		
	    		//if last milestone, set last event in series as milestone
	    		if(i == count-1)
	    		{
	    			eventId = smallList.size() -1;
	    		}	
	    		
	    		
	    		//get id of screen that contains milestone
	    		final int finalScreenId = eventId/multiplier;
	    		
	    		//build milestones view
	    		mView = new MilestoneView(this);
	        	mView.init(smallList.get(eventId).getPic().getThumb(), formatter.format(smallList.get(eventId).getDate()),tf, 
	        			
	        			new MilestoneClickListener()
	    				{
	    					@Override
	    					public void click()
	    					{
	    						eventsViewPager.setCurrentItem(finalScreenId);
	    					}
	    				}
	        	);
	        	
	        	//if last milestone, make it wrap content for width
	    		if(i == count-1)
	    			mView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	    		else
	    			mView.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
	    		
	        	milestoneContainer.addView(mView);
	    	}
		}	
        
		
    	
	}
	
	
	private class EventsViewPagerChangeListener extends ViewPager.SimpleOnPageChangeListener
	{
	    @Override
	    public void onPageSelected(int position) 
	    {
	       
	       handleArrows(position);
	   
	       focusedPage = position;
	       seekBarVP.setProgress(focusedPage+1); 
	       
	       ((BubuApp)getApplicationContext()).setFirstEventOnScreen( position * multiplier );
	       
	    }
 
	}                            
	
	/**
	 * Handle visibility of legt/right indicator arrows
	 * @param position
	 */
	private void handleArrows(int position)
	{
	   
	   //one page scenario
	   if((eventsViewPager.getAdapter().getCount()-1) == 0)
	   {	
		   indicatorRight.setVisibility(4);
		   indicatorLeft.setVisibility(4);
	   }   
	   else
	   {
		   //display/hide arrow indicators for scrolling
	       if(position==0 && indicatorLeft.getVisibility()==0)
		   {
			  indicatorLeft.setVisibility(4);
			  indicatorLeft.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out)); 	   
		   }
		   else if(position != 0 && indicatorLeft.getVisibility()==4)
		   {
			   indicatorLeft.setVisibility(0);
			   indicatorLeft.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
		   }
		   
		   if(position == eventsViewPager.getAdapter().getCount()-1 && indicatorRight.getVisibility() == 0)
		   {
			   indicatorRight.setVisibility(4);
			   indicatorRight.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out));
		   } 
		   else if(indicatorRight.getVisibility() == 4)
		   {
			   if(eventsViewPager.getAdapter().getCount() != position + 1)
			   {	   
				   indicatorRight.setVisibility(0);
				   indicatorRight.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
			   }	   
		   }
	   }   
	}
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		int count = getResources().getInteger(R.integer.bbEventsPerPage);
		
		count = count * ScreenUtil.getMultiplier(getApplicationContext());
		
		switch (item.getItemId())
		{
		  
			case R.id.bbDeleteEventButton:
				
				onDeleteObject(info.id + focusedPage*count + currentSelectorPage*(int)eventsPerPage);
				
				return true;
			
			case R.id.bbEditEventButton:
				
				onEditObject(info.id + focusedPage*count + currentSelectorPage*(int)eventsPerPage);
				
				return true;
				
			case R.id.bbShareEventButton:
				
				onShareEvent(info.id + focusedPage*count + currentSelectorPage*(int)eventsPerPage);
				
				return true;
				
			case R.id.bbMilestonesEventButton:
				
				onMilestonesEvent(info.id + focusedPage*count + currentSelectorPage*(int)eventsPerPage);
				
				return true;		
			     
			default:
			return super.onContextItemSelected(item);
			
		}
	 
	}
	
	
	/**
	 * Share event link via existing/compatible apps
	 */
	private void onShareEvent(long id)
	{
		
		String extraText = new StringBuffer()
							.append(getResources().getString(R.string.bbEventLinkPrefix))
							.append(eventResponse.getList().get((int)id).getEncodedKey())
							.toString(); 
			
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, extraText);
		sendIntent.setType("text/plain");
		startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.bbShareLinkTo)));		
	}
	
	
	/**
	 * Display activity with milestone content depending on kids age
	 * @param id
	 */
	private void onMilestonesEvent(long id)
	{
		Date eDate = eventResponse.getList().get((int)id).getDate();
		
		int ageM = Months.monthsBetween(new DateTime(dob), new DateTime(eDate)).getMonths();
		
		Intent i = new Intent(this, MilestonesActivity.class);
		
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
	 * Edits event in new view/activity
	 * @param id
	 */
	@Override
	public void onEditObject(long id)
	{
		
		super.onEditObject(id);
		
		// Perform action on clicks
        Intent i = new Intent(this, EventDetailActivity.class);
		
        Bundle mB = new Bundle();
		
       
		mB.putSerializable(getResources().getString(R.string.bbEventData), eventResponse.getList().get((int)id));
		mB.putSerializable(getResources().getString(R.string.bbDob), dob);
		mB.putString(getResources().getString(R.string.bbParentKey),parentKey);
		mB.putString(getResources().getString(R.string.bbChildKey),childKey);
		mB.putString(getResources().getString(R.string.bbChildName),name);
		mB.putString(getResources().getString(R.string.bbChildThumbUrl),thumbUrl);
		i.putExtras(mB);
        
		startActivity(i);
		
	}
	
	/**
	 * Handles event deletion based on order in list
	 * @param id
	 */
	protected void onDeleteObject(final long id)
	{
		CustomBuilder builder = new CustomBuilder(this);
		
		dialogClickListener = new DialogInterface.OnClickListener() 
		{
		    @Override
		    public void onClick(DialogInterface dialog, int which) 
		    {
		        switch (which)
		        {
		        case DialogInterface.BUTTON_POSITIVE:
		            //Yes button clicked
		        	//get event data
		    		try 
		    		{
		    			//initiate end execute background task
		    	    	deleteTask = new DeleteTask(EventsActivity.this);
		    	    	EventData ed = getProxy().getStoredEventResponse(getApplicationContext(), childKey).getList().get((int)id);
		    	    	
		    	    	deleteTask.execute
		    	    		(
		    	    				
		    	    				ed.getParentKey(),
		    	    				ed.getChildKey(),
		    	    				ed.getEncodedKey()
		    	    		);
		    		} 
		    		catch (OptionalDataException e) 
		    		{
		    			ErrorHandler.execute
		    			(
		    					EventsActivity.this, 
		    					getResources().getString(R.string.bbProblem),
		    					"EventsActivity 793: ", 
		    					e
		    			);
		    		} 
		    		catch (StreamCorruptedException e) 
		    		{
		    			ErrorHandler.execute
		    			(
		    					EventsActivity.this, 
		    					getResources().getString(R.string.bbProblem),
		    					"EventsActivity 803: ", 
		    					e
		    			);
		    			
		    		} 
		    		catch (ClassNotFoundException e) 
		    		{
		    			ErrorHandler.execute
		    			(
		    					EventsActivity.this, 
		    					getResources().getString(R.string.bbProblem),
		    					"EventsActivity 814: ", 
		    					e
		    			);
		    			
		    		} 
		    		catch (IOException e) 
		    		{
		    			ErrorHandler.execute
		    			(
		    					EventsActivity.this, 
		    					getResources().getString(R.string.bbProblem),
		    					"EventsActivity 825: ", 
		    					e
		    			);
		    			
		    		}
		        	
		        	break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		            break;
		        }
		    }
		};

		alert = builder
				.setMessage(getResources().getString(R.string.bbAreYouSure))
				.setPositiveButton(getResources().getString(R.string.bbYes), dialogClickListener)
				.setNegativeButton(getResources().getString(R.string.bbNo), dialogClickListener)
				.show();
		builder.setDialogInstance(alert);
		
	}
	
	
	/**
     * Background task used to delete child
     * @author oviroa
     *
     */
	private static class DeleteTask extends RotationSafeCollectionAsyncTask
    {

		public DeleteTask(BubuCollectionActivity activity) 
		{
			super(activity);			
		}
		
		private Exception carriedException = null;
		
    	@Override
		protected EventResponse doInBackground(String... params) 
		{
			
    		EventResponse er = null;
			
			Proxy myProxy = Proxy.getInstance();
			
			try 
			{
				er = myProxy.deleteEvent(activity.getApplicationContext(), params[0], params[1], params[2], activity);
			} 
			catch (ClientProtocolException e) 
			{
				carriedException = e;
			} 
			catch (IOException e) 
			{
				carriedException = e;
				return null;
			}
			
			
			return er;
		}
		
		protected void onPostExecute (Object er) 
	    {
			//hide loading bar
			activity.killProgressDialog();
			
			if(er != null)
			{	
				String rMessage = ((EventResponse)er).getResponse().getMessage();
				
				if(rMessage.equals(activity.getResources().getString(R.string.bbOK)))
				{
					//clear events state
					 ((BubuApp)activity.getApplicationContext()).setPreEditEventSelectorPage(0);
					 ((BubuApp)activity.getApplicationContext()).setPreEditFirstVisibleEvent(0);
					
					
					//set new list
					Proxy myProxy = Proxy.getInstance();
					
					//store retrieved event list locally
	                HashMap<String, EventResponse> eMap = ((BubuApp)activity.getApplicationContext()).getEventMap();
	                
	                if(eMap == null)
	                {
	                	eMap = new HashMap<String, EventResponse>();
	                }	
	                
	                eMap.put(activity.childKey, (EventResponse)er);
	                
	                myProxy.storeEvents(activity.getApplicationContext(), eMap);
					
	                activity.loadData();
	                //prepare dropdown
	    	        //we have a larger number of events, so build selector 
	    			activity.handleSelector();
	                //activity.refresh();
	                	
				}
				else
				{
					this.cancel(true);
					ErrorHandler.execute
					(
							activity, 
							activity.getResources().getString(R.string.bbProblem),
							"EventsActivity 910: " + rMessage, 
							null
					);					
					
				}
			}
			else
			{
				this.cancel(true);
				if(carriedException != null)
					ErrorHandler.execute
					(
							activity, 
							activity.getResources().getString(R.string.bbProblem),
							"EventsActivity 923: " + carriedException.getMessage(), 
							carriedException
					);
				else	
					ErrorHandler.execute
					(
							activity, 
							activity.getResources().getString(R.string.bbErrorNoData),
							"EventsActivity 931: " + carriedException.getMessage(), 
							null
					);
			}	
			
			detach(); 
	    }
	    	   
    	
    }
    
    
    @Override
	public Object onRetainNonConfigurationInstance() 
    {
    	if(deleteTask != null)
    		deleteTask.detach();
		 
    	return(deleteTask);
	}
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		  
		//get persistent logged in user's parent key
		MenuInflater inflater = getMenuInflater();

		if(!selectedChildIsJoint)
		{
			inflater.inflate(R.menu.events_context_menu, menu);
		}
		else
		{
			inflater.inflate(R.menu.events_context_menu_joint, menu);
		}

		//styling hack
		try
		{
			setMenuBackground();
		}
		catch(Exception e)
		{
			//hack did not work, ignore
		}
	}
	
	
	 /**
     * Starts new activity with form view for data input
     */
    private void addEvent()
    {
    	// Perform action on clicks
    	
     	//clear persistent data
    	clearForm();
    	
    	if(sharedImageUri != null)
    		intent.setClass(this, EventDetailActivity.class);
    	else
    		intent = new Intent(this, EventDetailActivity.class);
    	
    	Bundle mB = new Bundle();
		mB.putSerializable(getResources().getString(R.string.bbDob), dob);
		mB.putSerializable(getResources().getString(R.string.bbNewestMeasurement), newestMeasurement);
		mB.putString(getResources().getString(R.string.bbParentKey), parentKey);
		mB.putString(getResources().getString(R.string.bbChildKey), childKey);
		mB.putString(getResources().getString(R.string.bbChildName),name);
		mB.putString(getResources().getString(R.string.bbChildThumbUrl),thumbUrl);
		intent.putExtras(mB);
		startActivity(intent);
    } 
    
    
    /**
     * Move to the event detail activity
     */
    private void addEventShared()
    {
    	addEvent();
    	//Finish EventsActivity (thus when user navigates back they go straight to the activity before that (gallery)
    	finish();
    }
    
    /**
     * Registers receivers on resume
     */
    protected void onResume() 
    {
       
       super.onResume();
       
       if(((BubuApp)getApplicationContext()).getBitmapWasRecycled())
       {
    	   eventsViewPager.getAdapter().notifyDataSetChanged();
    	   if(eventsViewPager.getAdapter().getCount() > 1)
    		   buildMilestones();
    	   ((BubuApp)getApplicationContext()).setBitmapWasRecycled(false);
       }   
       
       
       if(((BubuApp)getApplicationContext()).getEventListFreshStatus())
       {
    	  
    	   if(refreshDialog != null && refreshDialog.isShowing())
         		refreshDialog.dismiss();
	   	   else
	   	   {	   
	   		   //load fresh data into collection view
	   		   loadData();
	   		   
	   		   handleSelector();
	   		   //refresh(); 
	   		   
	   	   }	
    	   
    	  ((BubuApp)getApplicationContext()).setEventListFreshStatus(false);
       }    
       
       
       //intervalSelector.setSelection(((BubuApp)getApplicationContext()).getPreEditEventSelectorPage());
       intervalSelector.setSelection(((BubuApp)getApplicationContext()).getPreEditFirstVisibleEvent() / (int)eventsPerPage);
       isSavedInstance = true;
    }
    
    
    /**
     * Removes receivers and kills task
     */
    @Override
    protected void onStop() 
    {
        super.onStop();
        
       //kill progress dialog if shown 
        if(progressDialog != null)
        	progressDialog.dismiss();
        
        if(refreshDialog != null)
    		refreshDialog.dismiss();
    }
    
    @Override
    protected void onPause() 
    {
        super.onPause();
    
        ((BubuApp)getApplicationContext()).setPreEditEventSelectorPage(intervalSelector.getSelectedItemPosition());
        ((BubuApp)getApplicationContext()).setPreEditFirstVisibleEvent
        	(
        			(int)(intervalSelector.getSelectedItemPosition()*eventsPerPage + 
        	        focusedPage * multiplier)
        	);
    }    
    
    /**
     * Handler for scrubbing the bar
     */
    @Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) 
	{
		if(seekTouched)
		{	
			eventsViewPager.setCurrentItem(seekBar.getProgress() - 1);
			if(seekBar.getProgress()<1)
				seekBar.setProgress(1);
			
			//Display date overlay when scrubbing
			showDateOverlay();
		   
			
		}
	}
	
	
	/**
	 * Display date overlay
	 */
	public void showDateOverlay()
	{
			if(currentDate.getVisibility() != 0)
	   			currentDate.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
	   		currentDate.setVisibility(0);
		    
			DateFormat formatter = new SimpleDateFormat("MMM/dd/yyyy", Locale.US);
		    
			List<EventData> smallList;
	        
	        if(eventResponse.getList().size() <= eventsPerPage)
	        {
	        	smallList= eventResponse.getList();
	        }	
	        else
	        {	
	        	
	        	smallList= eventResponse.getList().subList
	        				(
	        					(int)eventsPerPage*currentSelectorPage,
	        					(int)eventsPerPage*(currentSelectorPage+1) > eventResponse.getList().size()-1
									? eventResponse.getList().size()
									: (int)eventsPerPage*(currentSelectorPage+1)
	        				);
	        }
			currentDate.setText(formatter.format(smallList.get(focusedPage*multiplier).getDate()));
		    
	}
	

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) 
	{
		seekTouched = true;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) 
	{
		seekTouched = false;
		currentDate.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out));
		currentDate.setVisibility(8);
	}
	
	
	@Override
	protected void broacastHandler(Bundle extras)
	{
		
		super.broacastHandler(extras);
		
		if(extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)) != null 
				&& extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)).equals(getResources().getString(R.string.bbInviteRejectionMessage)) 
				&& extras.getString(getResources().getString(R.string.bbKey)).equals(childKey))
		{	
			displayRefreshDialog();
			((BubuApp)getApplicationContext()).setEventListFreshStatus(true);        		
		}
		
		if(extras.getString(getResources().getString(R.string.bbReset)) != null 
				&& extras.getString(getResources().getString(R.string.bbReset)).equals(getResources().getString(R.string.bbCD2MEventMessage)))
		{	
			Intent i = new Intent(EventsActivity.this, AccountActivity.class);
			startActivity(i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
			finish();
		}
		
		if(extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)) != null 
    			&& extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)).equals(getResources().getString(R.string.bbCD2MChildMessage)))
    	{
			finish();
    	}
		
	}
	
	/**
	 * Displays animated UI hints
	 */
	public void onWindowFocusChanged(boolean hasFocus)
	{
		if(eventsViewPager != null && seekBarVP != null && seekBarVP.isShown())
		{	
			if(!getProxy().hintWasShown(getApplicationContext()))
			{	
			
				//transparent layover
				hintLayout = (RelativeLayout)this.findViewById(R.id.bbHintContainer);
				
				hintLayout.setVisibility(RelativeLayout.VISIBLE);
				
				//swipe hint for fragment
				
				//retrieves fragment location
				int[] vpLocation = new int[3];
				eventsViewPager.getLocationOnScreen(vpLocation);
				
				
				ImageView handSwipe = (ImageView) findViewById(R.id.bbEventsViewSwipeHint);
		        Animation swipeAnimation = AnimationUtils.loadAnimation(this, R.anim.hand_swipe);
		        handSwipe.startAnimation(swipeAnimation);
		        
		        RelativeLayout.LayoutParams nparams = new RelativeLayout.LayoutParams(100, 100);
		        nparams.leftMargin = 0;
		        nparams.topMargin = vpLocation[1]+eventsViewPager.getHeight()/4;
		        handSwipe.setLayoutParams(nparams);
		        
		        //seek bar hint
		        
		        //retrieves seek location
		        
		        int[] sbLocation = new int[3];
		        seekBarVP.getLocationOnScreen(sbLocation);
				
		        
		        ImageView handSeek = (ImageView) findViewById(R.id.bbEventsViewSeekHint);
		        Animation seekAnimation = AnimationUtils.loadAnimation(this, R.anim.hand_seek);
		        handSeek.startAnimation(seekAnimation);
		        
		        //handSeek.setY(sbLocation[1] - seekBarVP.getHeight()/2);
		        nparams = new RelativeLayout.LayoutParams(100, 100);
		        nparams.leftMargin = 0;
		        
		        int extraSpace = 0;
		        
		        if(intervalSelector.getVisibility() == 8)
		        {
		        	extraSpace = intervalSelector.getHeight() + intervalSelector.getPaddingBottom()*2;		        	
		        }	
		        
		        nparams.topMargin = sbLocation[1] - seekBarVP.getHeight()*2 + extraSpace*5;
		        handSeek.setLayoutParams(nparams);
		        
		      //kill when clicked
				hintLayout.setOnClickListener(new OnClickListener() 
		    	{
		    	    public void onClick(View v) 
		    	    {
		    	    	hintLayout.setVisibility(RelativeLayout.GONE);
		    	    }
		    	});
		        
		        getProxy().setHintWasShown(getApplicationContext());
		        
			}   
		}	
	}	
	
}
