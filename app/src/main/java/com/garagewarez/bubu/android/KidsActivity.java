package com.garagewarez.bubu.android;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.lang.ref.WeakReference;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Months;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.garagewarez.bubu.android.base.BubuCollectionActivity;
import com.garagewarez.bubu.android.base.PrettyProgressDialog;
import com.garagewarez.bubu.android.base.RotationSafeCollectionAsyncTask;
import com.garagewarez.bubu.android.base.UpdateDialog;
import com.garagewarez.bubu.android.common.ChildData;
import com.garagewarez.bubu.android.common.ChildResponse;
import com.garagewarez.bubu.android.common.EventResponse;
import com.garagewarez.bubu.android.common.ParentData;
import com.garagewarez.bubu.android.utils.CustomBuilder;
import com.garagewarez.bubu.android.utils.Tools;


/**
 * Displays list of kids corresponding to cuurent parent
 * @author oviroa
 *
 */
public class KidsActivity extends BubuCollectionActivity 
{
	
	//container for collection of kids
	private ListView kidsListView;
	
	//async task used for retrieving events corresponding to a selected child 
	private AsyncTask<ChildData, Integer, Object> eventsTask;
	
	private ChildResponse childResponse;
	
	private UpdateDialog updateDialog;
	
	public PrettyProgressDialog imageProcessDialog;
	
	private Uri sharedImageUri;
	
	private AsyncTask<Uri, Void, Bitmap> imageTask;
	
	private KidsAdapter kAdapter;
	
	//intent used to to send data to next activity
	private Intent intent;

	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
         
    	super.onCreate(savedInstanceState); 
    	
    	intent = getIntent();
	    
        //set main view	
    	setContentView(R.layout.kid_list);
    	
    	//_________assign/handle view elements    	
    	
    	//show messaging
    	//action message
    	TextView viewTitle = (TextView)this.findViewById(R.id.textViewKids);
    	viewTitle.setTypeface(getTypeface(), 1);
    	
    	//add child button inflate
    	addButton = (Button)this.findViewById(R.id.bbAddChildButton);
    	addButton.setTypeface(getTypeface(), 1);
    	addButton.setOnClickListener(new OnClickListener() 
    	{
    	    public void onClick(View v) 
    	    {
    	    	addObject(ChildDetailActivity.class);
    	    }
    	});
    	
    	
        //________handle async task
    	
    	//check if async task is running, kill it if so and remove progress dialog
    	handleRunningAsyncTask((DeleteTask)getLastNonConfigurationInstance());
    	
    	
    	//get stuff from intent
		Bundle extras = intent.getExtras();
        String action = intent.getAction();
    	    	
        //check if something got shared
        if (Intent.ACTION_SEND.equals(action) && extras.containsKey(Intent.EXTRA_STREAM))
        {
            //Get resource path from intent callee
        	sharedImageUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
        }	
    	
    	//_________handle data refresh and events for CD2M
    	handleRefresh();
    	
    	//check if shared image was detected and handle for attachment to event
    	if(sharedImageUri != null)
    		handleSharedImage();
    	
    	//activate top menu button if not sharing
    	if(sharedImageUri == null)
    		constructMenuButton(R.id.bbMenuKidsList);
    	
    	
    	//update text was not shown
    	if(!getProxy().updateTextWasShown(getApplicationContext()))
		{	
    		
    		//show update dialog
    		if(updateDialog == null)
    			updateDialog = new UpdateDialog(KidsActivity.this, R.style.BubuDialogTheme);
    	
    		updateDialog.show(tf);
    		
		}	
    	
    	getProxy().setUpdateTextWasShown(getApplicationContext());
    	
    	if(!getProxy().homeNotificationWasShown(getApplicationContext()))
    	{	
	    	try
	    	{
				//if there is joint user data or message has not been killed, show message at log in
	    		ParentData pd = getProxy().getStoredParentData(getApplicationContext());
				if(pd != null && (pd.getSentUser() != null || pd.getReceivedUser() != null) && !((BubuApp)getApplicationContext()).getHomeNotificationWasShown())
				{
					displayInviteNotification();
				}	
			} 
	    	catch (OptionalDataException e) 
			{
				ErrorHandler.execute
				(
						KidsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"KidsActivity 130: ", 
						e
				);
			} 
			catch (StreamCorruptedException e) 
			{
				ErrorHandler.execute
				(
						KidsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"KidsActivity 140: ", 
						e
				);
				
			} 
			catch (ClassNotFoundException e) 
			{
				ErrorHandler.execute
				(
						KidsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"KidsActivity 150: ", 
						e
				);
				
			} 
			catch (IOException e) 
			{
				ErrorHandler.execute
				(
						KidsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"KidsActivity 160: ", 
						e
				);
				
			}
	    	catch (Exception e)
	    	{
	    		ErrorHandler.execute
				(
						KidsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"KidsActivity 170: ", 
						e
				);
	    	}
    	}
    	
    }
	
	/**
	 * Check for and manipulate/store shared image
	 */
	private void handleSharedImage()
	{
		
		//kill add child button
		addButton.setVisibility(8);
		
		//if no kids entered
		if(childResponse == null || childResponse.getList().size() == 0)
		{
			displayMessageToast(R.string.bbAddChildForSharing);
			finish();
		}	
		else
		//if not local media, make bitmap
		if(sharedImageUri.getHost() != null && !sharedImageUri.getHost().equals("media"))
		{
			//if progress dialog is not instantiated, create one and make it invisible 
        	if(imageProcessDialog == null)
        	{	
        		imageProcessDialog = new PrettyProgressDialog(this);
        		imageProcessDialog.show(getResources().getString(R.string.bbImageProcessing), getTypeface());
        		imageProcessDialog.hide();
        	}	
    		
    		//show dialog
        	imageProcessDialog.show();
        	//manipulate images
        	imageTask = new ImageTask();
        	imageTask.execute(sharedImageUri);
        	Handler handler = new Handler();
        	handler.postDelayed(new Runnable()
        	{
        	  @Override
        	  public void run() 
        	  {
        	      if ( imageTask.getStatus() == AsyncTask.Status.RUNNING )
        	      {
        	    	  onTimeOutImageTask();
        	      }	  
        	  }
        	}, getResources().getInteger(R.integer.bbTimeOutMS)/2 );
		}
		else//local media - show messgae, image will be manipulated when event is created
		{
			//show toast, alerting the user that they need to select a child
			displayMessageToast(R.string.bbSelectAChild);
		}	
        	
        	       
     
    	
	}
	
	
	/**
	 * Displays message prompting the user to chose a child in order to proceed
	 * @return
	 */
	private void displayMessageToast(int message)
	{
		LayoutInflater inflater = getLayoutInflater();
   		View layout = inflater.inflate(R.layout.toast,
   		                               (ViewGroup) findViewById(R.id.bbToastLayout));

   		//manipulate text field, set content, set font
   		TextView text = (TextView) layout.findViewById(R.id.bbToastText);
   		text.setText(message);
   		text.setTypeface(tf, 1);
   		
   		//create and display toast
   		Toast toast = new Toast(getApplicationContext());
   		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 280);
   		toast.setDuration(Toast.LENGTH_LONG);
   		toast.setView(layout);
   		toast.show();
	}
	
	
	/**
	 * AsyncTask used to resize and display shared image
	 * @author oviroa
	 *
	 */
	public class ImageTask extends AsyncTask<Uri, Void, Bitmap> 
    {
        
	   protected Bitmap doInBackground(Uri... arg0) 
       {
    	   Bitmap btmLarge = null;
    	   Bitmap btm = null;
    	   //handle shared image if it exists, build bitmap and store in mem
 		   try 
	   		{   
 			    btmLarge = Tools.scalePic(arg0[0], getResources().getInteger(R.integer.bbImageMainSize), getApplicationContext()); 
 			    btm = Tools.scaleBitmap(btmLarge,getResources().getInteger(R.integer.bbImageThumbSize));
 			    
 			    ((BubuApp)getApplicationContext()).wrSharedPictureBitmapLarge = new WeakReference<Bitmap>(btmLarge);
			    ((BubuApp)getApplicationContext()).wrSharedPictureBitmap = new WeakReference<Bitmap>(btm);
			    
			    if (isCancelled())
			    {
			    	((BubuApp)getApplicationContext()).wrSharedPictureBitmapLarge = new WeakReference<Bitmap>(null);
				    ((BubuApp)getApplicationContext()).wrSharedPictureBitmap = new WeakReference<Bitmap>(null);
			    }	
			} 
		    catch (FileNotFoundException e) 
		    {
		    	ErrorHandler.execute
				(
						KidsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"KidsActivity 297: ", 
						e
				);
			} 
		    catch (NotFoundException e) 
			{
		    	ErrorHandler.execute
				(
						KidsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"KidsActivity 307: ", 
						e
				);
			}
		
 		   return btm;
		    
       }
       
    
	
       protected void onPostExecute(Bitmap thumbnail) 
       {
    	    imageProcessDialog.dismiss();
    	   
	    	//show toast, alerting the user that they need to select a child
    	    displayMessageToast(R.string.bbSelectAChild);
	   		
       }
       	
    }
	
	/**
	 * image task timed out
	 */
	private void onTimeOutImageTask()
    {
    	//if there is a running event retrieving async task
    	if(imageTask != null && imageTask.getStatus() == AsyncTask.Status.RUNNING)
        {
    		//kill it 
    		imageTask.cancel(true);
    		imageTask = null;
    		
    		//kill progress dialog if shown 
            if(imageProcessDialog != null)
            {	
            	imageProcessDialog.dismiss();
            }
            
            
            displayMessageToast(R.string.bbImageProblem);
     	   	finish();
        }
    }	
	
	 /**
     * Kill image retrieval async task
     */
    private void onCancelImageTask()
    {
    	//if there is a running event retrieving async task
    	if(imageTask != null && imageTask.getStatus() == AsyncTask.Status.RUNNING)
        {
    		//kill it 
    		imageTask.cancel(true);
    		imageTask = null;
    		
    		((BubuApp)getApplicationContext()).wrSharedPictureBitmapLarge = new WeakReference<Bitmap>(null);
		    ((BubuApp)getApplicationContext()).wrSharedPictureBitmap = new WeakReference<Bitmap>(null);
    		
    		//kill progress dialog if shown 
            if(imageProcessDialog != null)
            {	
            	imageProcessDialog.dismiss();
            }
           
        }
    	
    	
    }
	
	/**
	 * Handle refresh of collection when new elements are present
	 */
	@Override
	protected void handleRefresh()
	{
		
		loadData();
		
		//if there is content
		if(childResponse != null)
		{
			//populate container with content
			refresh();
			
	    	// Register as a receiver to listen for event/broadcasts
	        IntentFilter filter = new IntentFilter(getResources().getString(R.string.bbUpdateEvent));
	        registerReceiver(mReceiver, filter);
		}  
		else //id there is no content (null), force display starting activity 
		{
			Intent i = new Intent(KidsActivity.this, AccountActivity.class);
			startActivity(i);
		}	
		
	}
	
	@Override
	public void loadData()
	{
		try 
		{
			//get child data (retrieved form local storage) in bundle
			childResponse =  getProxy().getStoredChildResponse(getApplicationContext());
		}
		catch (OptionalDataException e) 
		{
			ErrorHandler.execute
			(
					KidsActivity.this, 
					getResources().getString(R.string.bbProblem),
					"KidsActivity 141: ", 
					e
			);
		} 
		catch (StreamCorruptedException e) 
		{
			ErrorHandler.execute
			(
					KidsActivity.this, 
					getResources().getString(R.string.bbProblem),
					"KidsActivity 151: ", 
					e
			);
			
		} 
		catch (ClassNotFoundException e) 
		{
			ErrorHandler.execute
			(
					KidsActivity.this, 
					getResources().getString(R.string.bbProblem),
					"KidsActivity 162: ", 
					e
			);
			
		} 
		catch (IOException e) 
		{
			ErrorHandler.execute
			(
					KidsActivity.this, 
					getResources().getString(R.string.bbProblem),
					"KidsActivity 173: ", 
					e
			);
			
		}
		catch (Exception e) 
		{
			ErrorHandler.execute
			(
					KidsActivity.this, 
					getResources().getString(R.string.bbProblem),
					"KidsActivity 266: ", 
					e
			);
			
		}
	}
	
	
	/**
	 * Refreshes view
	 * @param response
	 */
	@Override
	public void refresh()
	{
		if(childResponse != null)
		{	
			
			//if the list has not been inflated yet
			if(kidsListView == null)
			{
				//list inflate
		    	kidsListView = (ListView)this.findViewById(R.id.bbKidsListView);
		    	
		    	//empty view, shown when no elements are available
		    	kidsListView.setEmptyView(this.findViewById(R.id.bbEmptyKidsList));
		    	
		    	//set custom typeface
		    	((TextView)this.findViewById(R.id.bbEmptyKidsList)).setTypeface(getTypeface(), 1);
		    	
		    	kidsListView.setScrollingCacheEnabled(false);
		    	
		    	kidsListView.setFastScrollEnabled(true);
		    	
			}	
			
			
			try 
			{
				//get persistent logged in user's parent key
				final String pk = getProxy().getStoredParentData(getApplicationContext()).getEncodedKey();
				
				kAdapter = new KidsAdapter
				(
						this, 
						R.layout.kids_list_item, 
						childResponse.getList(),
						pk,
						(sharedImageUri != null)
				);
				
				//prep adapter, load with children
		    	kidsListView.setAdapter(kAdapter);
		    	
		    	//set (short) click event listener for list
		    	kidsListView.setOnItemClickListener
		        (
		        		new OnItemClickListener() 
		        		{
		        		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
		        		    {
		        		    	
		        		    	if( 
		        		    			sharedImageUri == null ||
		        		    			(
		        		    				sharedImageUri != null &&
		        		    				childResponse.getList().get((int)id).getParentKey().equals(pk)
		        		    			)
		        		    	  )
		        		    	{	
			        		    	//show progress dialog
			        		    	progressDialog.show();
			        		    	
			        		    	//initiate and execute async event task
			        		    	eventsTask = new GetEventsTask();
			        		    	eventsTask.execute(childResponse.getList().get((int)id)); 
		        		    	}
		        		    		
		        		    	
		        		    	
		        		    }
		        		  }
		        
		        );
		    	
				//if not sharing scenario, activate all buttons
		    	if(sharedImageUri == null)
		    	{	
			    	//create context menu for list long-press
			    	registerForContextMenu(kidsListView);
			    	
			    	//overwrite long click listener to only get triggered when non joint user
			    	kidsListView.setOnItemLongClickListener
			    	(
			    			new OnItemLongClickListener() 
			    			{
	
								@Override
								public boolean onItemLongClick(AdapterView<?> adapterView , View v, int position, long id) 
								{
								    //get joint status
									selectedChildIsJoint = childResponse.getList().get((int)id).getIsJoint();
									
									return false;
								}
			    				
			    			}
			    	
			    	);
		    	}	
		    	
			} 
			catch (OptionalDataException e) 
			{
				ErrorHandler.execute
				(
						KidsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"KidsActivity 272: " , 
						e
				);
			} 
			catch (StreamCorruptedException e) 
			{
				ErrorHandler.execute
				(
						KidsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"KidsActivity 282: ", 
						e
				);
			} 
			catch (ClassNotFoundException e) 
			{
				ErrorHandler.execute
				(
						KidsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"KidsActivity 292: ", 
						e
				);
			} 
			catch (IOException e) 
			{
				ErrorHandler.execute
				(
						KidsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"KidsActivity 265: child data is null", 
						null
				);
			}
			catch (Exception e) 
			{
				ErrorHandler.execute
				(
						KidsActivity.this, 
						getResources().getString(R.string.bbProblem),
						"KidsActivity 415: child data is null", 
						null
				);
			}
				
			
		}
		else
		{
			ErrorHandler.execute
			(
					KidsActivity.this, 
					getResources().getString(R.string.bbProblem),
					"KidsActivity 265: child data is null", 
					null
			);
		}	
		
    	
	}
	
	
	/**
	 * Handles menu item selection
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch (item.getItemId())
		{
		  
			case R.id.bbDeleteChildButton:
			
				//delete selected
				onDeleteObject(info.id);
				
				return true;
			
				
			case R.id.bbMilestonesChildButton:
				
				//delete selected
				showMilestones(info.id);
				
				return true;
				
				
			case R.id.bbEditChildButton:
				
				//edit selected
				onEditObject(info.id);
				
				return true;
			     
			default:
			return super.onContextItemSelected(item);
			
		}
	 
	}
	
	
	/**
	 * Handles object edit
	 * @param id
	 */
	@Override
	protected void onEditObject(long id)
	{
		
		super.onEditObject(id);
		
		//intent, prepare to start detail view activity
        Intent i = new Intent(this, ChildDetailActivity.class);
		
        //bundle
        Bundle mB = new Bundle();
        
		//store selected child data (retrieved form local storage) in bundle
		mB.putSerializable(getResources().getString(R.string.bbChildData), childResponse.getList().get((int)id));
		i.putExtras(mB);
		//start detail view activity
		startActivity(i); 
    	
		
	}
	
	/**
	 * Displays milestones correcponding to child age
	 * @param id
	 */
	private void showMilestones(long id)
	{
		Date dob = childResponse.getList().get((int)id).getDob();
		
		int ageM = Months.monthsBetween(new DateTime(dob), new DateTime()).getMonths();
		
		Intent i = new Intent(this, MilestonesActivity.class);
		
		//send age in months
		i.putExtra(getApplicationContext().getResources().getString(R.string.bbChildAge), ageM);
		//send human readable age
		i.putExtra(getApplicationContext().getResources().getString(R.string.bbChildAgeStr), Tools.getAgeStr(dob, new Date(), getApplicationContext()));
		//send name
		i.putExtra(getApplicationContext().getResources().getString(R.string.bbChildName), childResponse.getList().get((int)id).getName());
		//send image
		i.putExtra(getApplicationContext().getResources().getString(R.string.bbChildThumbUrl), childResponse.getList().get((int)id).getPic().getThumb());
		
		
		startActivity(i);
	}
	
	/**
	 * Handles child deletion based on order in list
	 * @param id
	 */
	@Override
	protected void onDeleteObject(long id)
	{
		//retrieve parent key from locally stored object data
		final String cKey = childResponse.getList().get((int)id).getEncodedKey();
		final String pKey = childResponse.getList().get((int)id).getParentKey();
		//initiate end execute background task
    	
		
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
		        	deleteTask = new DeleteTask(KidsActivity.this);
		        	deleteTask.execute(cKey,pKey);
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
	 * Builds context menu
	 *  
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
	  
		super.onCreateContextMenu(menu, v, menuInfo);
	 
	  	//inflate xml (context) menu
	  	MenuInflater inflater = getMenuInflater();
	  
	  	Log.w("BUBU", "MOLA");

	  	
	  	
	  	String pk;
	  	try 
	  	{
		  pk = getProxy().getStoredParentData(getApplicationContext()).getEncodedKey();
		  
		  if(!selectedChildIsJoint)
		  {	  
			  inflater.inflate(R.menu.kids_context_menu, menu);
		  }
		  else
		  {
			  inflater.inflate(R.menu.kids_context_menu_joint, menu);
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
	  	catch (OptionalDataException e1) 
		{
			ErrorHandler.execute
			(
					KidsActivity.this, 
					getResources().getString(R.string.bbProblem),
					"KidsActivity 605:", 
					e1
			);
		} 
		catch (StreamCorruptedException e1) 
		{
			ErrorHandler.execute
			(
					KidsActivity.this, 
					getResources().getString(R.string.bbProblem),
					"KidsActivity 615:", 
					e1
			);
		} 
		catch (ClassNotFoundException e1) 
		{
			ErrorHandler.execute
			(
					KidsActivity.this, 
					getResources().getString(R.string.bbProblem),
					"KidsActivity 625:", 
					e1
			);
		} 
		catch (IOException e1) 
		{
			ErrorHandler.execute
			(
					KidsActivity.this, 
					getResources().getString(R.string.bbProblem),
					"KidsActivity 635:", 
					e1
			);
		}
	
	}
	
	/**
     * Async retrieval of events from model (cascades from local to server)
     * @author oviroa
     *
     */
    private class GetEventsTask extends AsyncTask<ChildData, Integer, Object> 
    {
        
      //data set 
      private ChildData myCD;

      protected void onProgressUpdate(Integer... progress) 
      {
    	   
      }
       
      private Exception carriedException = null;
	   
       protected EventResponse doInBackground(ChildData... arg0) 
       {
    	   myCD = arg0[0];
    	   
    	   try 
           {
               //get kids from server or local storage
   			   return getProxy().getEvents(getApplicationContext(),arg0[0].getParentKey(), arg0[0].getEncodedKey(), KidsActivity.this, false);
    		   
           } 
    	   catch (ClassNotFoundException e) 
    	   {
    		   carriedException = e;
   			
		   }
           catch (IOException e) 
           {
        	   
        	   carriedException = e;
           }
    	   catch (Exception e) 
           {
        	   
        	   carriedException = e;
           }
    	   
           return null;
    	   
       }
       
       protected void onPostExecute(Object result) 
       {
    	   
    	   //kill prpgress dialog
    	   progressDialog.dismiss();
    	   
           if(result != null)
           {	
	            //check for errors from server
            	if(((EventResponse)result).getResponse().getMessage().equals("ok"))
            	{
            		
            		//all ok, display list
            		if(sharedImageUri != null)
            			intent.setClass(KidsActivity.this, EventsActivity.class); 
            		else
            			intent = new Intent(KidsActivity.this, EventsActivity.class);
            		
            		//start bundle of data for events collection activity
            		Bundle mB = new Bundle();
            		//serialize and post events collection
            		mB.putSerializable(getResources().getString(R.string.bbDob), myCD.getDob());

            		//post parent key
            		mB.putString(getResources().getString(R.string.bbParentKey), myCD.getParentKey());
            		//post child key
            		mB.putString(getResources().getString(R.string.bbChildKey), myCD.getEncodedKey());
            		//post name
            		mB.putString(getResources().getString(R.string.bbChildName), myCD.getName());
            		//post url of img thumbnail
            		mB.putString(getResources().getString(R.string.bbChildThumbUrl), myCD.getPic().getThumb());
            		//is joint
					mB.putBoolean(getResources().getString(R.string.bbIsJoint), myCD.getIsJoint());
            		
            		//run post
            		intent.putExtras(mB);
            		startActivity(intent);
            		
            		if(sharedImageUri != null) 
            		{	
            			finish();
            		}
            	}
            	else
            	{
            		//handle server error
            		this.cancel(true);
            		ErrorHandler.execute
        			(
        					KidsActivity.this, 
        					getResources().getString(R.string.bbProblem),
        					"KidsActivity 580: " + ((EventResponse)result).getResponse().getMessage(), 
        					null
        			);
            		
            	}	
           	}
           	else
           	{
           		//show error message if data is null
           		this.cancel(true);
           		if(carriedException != null)
           			ErrorHandler.execute
        			(
        					KidsActivity.this, 
        					getResources().getString(R.string.bbProblem),
        					"KidsActivity 593: " + carriedException.getMessage(), 
        					carriedException
        			);
           		else
           			ErrorHandler.execute
           			(
           					KidsActivity.this, 
           					getResources().getString(R.string.bbErrorNoConnection), 
           					null,
           					null
           			); 
            }   
          
    	   
       }
	   	
    } 
    
    
    /**
     * Creates options menu 
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
	   //inflate menu based on xml menu
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.kids_menu, menu);
	    
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
	    	case R.id.bbLogOutButton:
	    		if(isOnline())//when online, log off
	    			switchAccount();
	    		else//otherwise display error message
	    			ErrorHandler.execute
	    			(
	    					this, 
	    					getResources().getString(R.string.bbErrorNoConnection),
	    					null,
	    					null
	    			);
	    		return true;
	        
	        //add child button
	    	case R.id.bbAddChildButton:
	    		addObject(ChildDetailActivity.class);
	    		return true;
	    	
	    	//add user button
	    	case R.id.bbAddUserdButton:
	    		addObject(JointUserFormActivity.class);
	    		return true;
	    			
	    		
	    	default:
	    		
	    		
	    		
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	
	/**
     * Removes receivers and kills task
     */
    protected void onStop() 
    {
        super.onStop();
        
      //kill all tasks
        onCancelEventsTask();
        onCancelImageTask();
        
       //kill progress dialog if shown 
        if(progressDialog != null)
        	progressDialog.dismiss();
        
        if(alert != null)
        	alert.dismiss();
        
        if(refreshDialog != null)
        	refreshDialog.dismiss();
        
        if(updateDialog != null)
        	updateDialog.dismiss();
       
    }
    
    
    
    /**
     * Registers receivers on resume
     */
    protected void onResume() 
    {
       //if notification killed elsewhere
       if( ((BubuApp)getApplicationContext()).getHomeNotificationWasShown() )
       {	   
    	   Button notificationButton = (Button)this.findViewById(R.id.bbJointUserNotificationHomeButton);
    	   notificationButton.setVisibility(8);
       }	   
        	
    	
    	//if there was a footprint
	   if(getProxy().retrieveEventFootPrint(getApplicationContext()) == 0)
	   {
	 	//reset event footprint
		getProxy().storeEventFootPrint(getApplicationContext(), -1);
	   }
        
       super.onResume();
       
       //reset events adapter set
       ((BubuApp)getApplicationContext()).setPreEditEventSelectorPage(0);
	   ((BubuApp)getApplicationContext()).setPreEditFirstVisibleEvent(0);
       
	   if(sharedImageUri == null && kAdapter != null)
    	   kAdapter.notifyDataSetChanged();
	   
       //if there is fresh content (state boolean variable is true)
   	   if(((BubuApp)getApplicationContext()).getKidListFreshStatus() || ((BubuApp)getApplicationContext()).getBitmapWasRecycled())
       {
   		   if(refreshDialog != null && refreshDialog.isShowing())
          		refreshDialog.dismiss();
    	   else
    	   {	   
    		   //load fresh data into collection view
    		   loadData();
    		   refresh();
    	   }	   
    	   
    	   //reset state boolean variable
    	   ((BubuApp)getApplicationContext()).setKidListFreshStatus(false);
    	   ((BubuApp)getApplicationContext()).setBitmapWasRecycled(false);
       } 
           
       	
    }
    
    /**
     * Kills task
     */
    @Override
    protected void onDestroy() 
    {
        super.onDestroy();
        
        //kill all tasks
        onCancelEventsTask(); 
        onCancelImageTask();
       
    }
    
    /**
     * Kill event retrieval async task
     */
    private void onCancelEventsTask()
    {
    	//if there is a running event retrieving async task
    	if(eventsTask != null && eventsTask.getStatus() == AsyncTask.Status.RUNNING)
        {
    		//kill it 
    		eventsTask.cancel(true);
    		eventsTask = null;
    		//kill progress dialog
    		progressDialog.dismiss();
        }
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
		
		String key;
		
		
		@Override
		protected ChildResponse doInBackground(String... params) 
		{
			
    		//child data
			ChildResponse cr = null;
			
			try
			{
				key = params[0];
				
				//run child deletion, retrieve new child data
				cr = activity.getProxy().deleteChild(activity.getApplicationContext(), params[0], params[1], activity);
			}	
			catch (IOException e)
			{
				carriedException = e;
				return null;					
			} 
			catch (ClassNotFoundException e) 
			{
				carriedException = e;
				return null;
			} 
			catch (Exception e) 
			{
				carriedException = e;
				return null;
			} 
			
			return cr;
		}
		
    	@Override
		protected void onPostExecute (Object cr) 
	    {
			
    		
    		//hide loading bar
			activity.killProgressDialog();
			
			//there is a return data object
			if(cr != null)
			{	
				//get message from response
				String rMessage = ((ChildResponse)cr).getResponse().getMessage();
				
				//if message is ok
				if(rMessage.equals(activity.getResources().getString(R.string.bbOK)))
				{
					//set new list
					activity.getProxy().storeKids(activity.getApplicationContext(), (ChildResponse)cr);
					
					//remove the deleted event list from local storage if it exists
					//get events hash
					try 
					{
						activity.getProxy().removeEventsByChild(activity.getApplicationContext(), key);
					} 
					catch (OptionalDataException e) 
					{
						this.cancel(true);
						ErrorHandler.execute
						(
								activity, 
								activity.getResources().getString(R.string.bbProblem),
								"KidsActivity 702: " + e.getMessage(), 
								e
						);
					} 
					catch (StreamCorruptedException e) 
					{
						this.cancel(true);
						ErrorHandler.execute
						(
								activity, 
								activity.getResources().getString(R.string.bbProblem),
								"KidsActivity 712: " + e.getMessage(), 
								e
						);
					} 
					catch (ClassNotFoundException e) 
					{
						this.cancel(true);
						ErrorHandler.execute
						(
								activity, 
								activity.getResources().getString(R.string.bbProblem),
								"KidsActivity 722: " + e.getMessage(), 
								e
						);
					} 
					catch (IOException e) 
					{
						this.cancel(true);
						ErrorHandler.execute
						(
								activity, 
								activity.getResources().getString(R.string.bbProblem),
								"KidsActivity 732: " + e.getMessage(), 
								e
						);
					}
					catch (Exception e) 
					{
						this.cancel(true);
						ErrorHandler.execute
						(
								activity, 
								activity.getResources().getString(R.string.bbProblem),
								"KidsActivity 990: " + e.getMessage(), 
								e
						);
					}
					
					//populate
					activity.loadData();
					activity.refresh();
				}
				else
				{
					this.cancel(true);
					ErrorHandler.execute
					(
							activity, 
							activity.getResources().getString(R.string.bbProblem),
							"KidsActivity 832: " + rMessage,
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
							"KidsActivity 844: " + carriedException.getMessage(), 
							carriedException
					);
				else
					ErrorHandler.execute
					(
							activity, 
							activity.getResources().getString(R.string.bbErrorNoConnection), 
							null,
							null
					);
			}	
			
			
			detach(); 
	    }
	    	   
    	
    }
    
    
    /**
     * Retrieve running deletion async task, detach from activity
     */
    @Override
	public Object onRetainNonConfigurationInstance() 
    {
    	if(deleteTask != null)
    		deleteTask.detach();
		 
    	return(deleteTask);
	}
    
    
    
    @SuppressLint("InlinedApi")
	@Override
	protected void broacastHandler(Bundle extras)
	{
    	super.broacastHandler(extras);
    	
    	//if new data
    	if(extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)) != null 
    			&& extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)).equals(getResources().getString(R.string.bbCD2MChildMessage)))
    	{	
    		
    		displayRefreshDialog();
    		
    		((BubuApp)getApplicationContext()).setKidListFreshStatus(true);
    		
    		getProxy().storeEventFootPrint(getApplicationContext(), -1);
    	
    	}
    	
    	//child was reset
    	if(extras.getString(getResources().getString(R.string.bbReset)) != null 
    			&& extras.getString(getResources().getString(R.string.bbReset)).equals(getResources().getString(R.string.bbCD2MEventMessage)))
    	{	
    		
    		//show log in/accounts activity
    		Intent i = new Intent(KidsActivity.this, AccountActivity.class);
    		startActivity(i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    	}
	}

    /**
     * KidsActivity specific, will display notification message at start
     */
    protected void displayInviteNotification()
    {
    	Button notificationButton = (Button)this.findViewById(R.id.bbJointUserNotificationHomeButton);
    	notificationButton.setTypeface(getTypeface(), 1);
    	notificationButton.setVisibility(0);
    	notificationButton.setOnClickListener(new OnClickListener() 
    	{
    	    public void onClick(View v) 
    	    {
    	    	updateUI();
    	    	getProxy().setHomeNotificationShown(getApplicationContext());
    			v.setVisibility(8);
    	    	
    	    }
    	});
    }
    
    
	@Override
	public void handleSelector() 
	{
		// TODO Auto-generated method stub
		
	}
    
    
    
}
