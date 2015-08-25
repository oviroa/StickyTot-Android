package com.garagewarez.bubu.android;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.joda.time.DateTime;
import org.joda.time.Days;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.garagewarez.bubu.android.base.BubuFormActivity;
import com.garagewarez.bubu.android.base.RotationSafeFormAsyncTask;
import com.garagewarez.bubu.android.common.EventData;
import com.garagewarez.bubu.android.common.EventResponse;
import com.garagewarez.bubu.android.common.Measurement;
import com.garagewarez.bubu.android.common.Pic;
import com.garagewarez.bubu.android.proxy.Connector.ProgressListener;
import com.garagewarez.bubu.android.proxy.Proxy;
import com.garagewarez.bubu.android.utils.Convertor;
import com.koushikdutta.ion.Ion;

/**
 * Event detail view
 * @author oviroa
 *
 */
public class EventDetailActivity extends BubuFormActivity
{
	//Child birthday
	private Date dob;
	
	//calendar instace before chnage
	private Calendar preChangeCal = Calendar.getInstance();
	
	//instance of model
	private EventData eventData;
	
	//most recent measurement object, used to pre-populate empty form 
	private Measurement newestMeasurement;
	
	//button used to trigger dimensions view
	private Button dimensionButton;
	
	//dimensions view as a dialog
	private PrettyDimensionsDialog pdDialog;
	
	//code used to identify intent for voice recognition
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	
	//kid dimensions
	private Float height = (float) 0;
	private Float weight = (float) 0;


	//kids name
	private String name;
	
	//kids thumb
	private String thumbUrl; 
	
	//uri of shared image
	private Uri sharedImageUri;
	
	//intent used to receive data from the previous activity
	private Intent intent;
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // Register as a receiver to listen for event/broadcasts
        IntentFilter filter = new IntentFilter(getResources().getString(R.string.bbUpdateEvent));
        registerReceiver(mReceiver, filter);
        
        //handles device orientation, displays alternative view for landscape
        int deviceOrientation = getResources().getConfiguration().orientation;
        
        if(deviceOrientation == Configuration.ORIENTATION_PORTRAIT)        
        	setContentView(R.layout.event_detail);
        else
        	setContentView(R.layout.event_detail_landscape);
        
        //hack
        textFieldHackField = ((EditText) findViewById(R.id.bbEventNoteField));
        registerForContextMenu( ((EditText) findViewById(R.id.bbEventNoteField)));
        
        //construct top bar with back button and title
        constructTopBar(R.id.homeButtonEventDetails, R.id.textViewEventDetails);
        
        intent = getIntent();
        
        //data transferred from child
        Bundle extras = intent.getExtras();
    	
    	dob = (Date)extras.getSerializable(getResources().getString(R.string.bbDob));
        parentKey = (String)extras.getString(getResources().getString(R.string.bbParentKey));
        childKey = (String)extras.getString(getResources().getString(R.string.bbChildKey));
        newestMeasurement = (Measurement)extras.getSerializable(getResources().getString(R.string.bbNewestMeasurement));
        name = (String)extras.getString(getResources().getString(R.string.bbChildName));
        thumbUrl = (String)extras.getString(getResources().getString(R.string.bbChildThumbUrl));
        
        //child image
    	ImageView thumb = (ImageView)findViewById(R.id.bbChildImage);
    	//add image 
    	thumb.setBackgroundResource(R.drawable.transparent);
    	Ion.with(thumb).
				placeholder(R.drawable.rounded_corners_noimage).
				load(thumbUrl);
        
        
        //set default measurement
        if(newestMeasurement != null)
        {
        	weight = Convertor.weight(newestMeasurement.getWeight(), true);
        	
        	height = Convertor.height(newestMeasurement.getHeight(), true);        	
        }	
        
        
        //construct view elements, set content for text, fonts etc
        constructViewElements();
        
        
        //select image from internal resources
        handleImageChooser(R.id.bbEventImage,R.id.bbRotateEventButton, EventDetailActivity.this);
        
       
        //handle data transfer to populate interface
        handleEdit();
        
        
        //store
        saveTask = (SaveTask)getLastNonConfigurationInstance();

        //handles running async task on device rotation or re-draws after other interruptions
        if (saveTask != null) 
        {
     	   saveTask.attach(this);    	  
        }
        
        //get stuff from intent
  		String action = intent.getAction();
  	
  		//check if something got shared
  		if (Intent.ACTION_SEND.equals(action) && extras.containsKey(Intent.EXTRA_STREAM))
		{
          //Get resource path from intent callee
			sharedImageUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
      		
			//there is a sharing URI
			if(sharedImageUri != null)
			{	
				Bitmap selectedImageLarge = null;
				
				//if images have been constructed, attempt to retrieve them 
				if(((BubuApp)getApplicationContext()).wrSharedPictureBitmap != null 
						&& ((BubuApp)getApplicationContext()).wrSharedPictureBitmapLarge != null )
				{	
					//image got constructed
					selectedImage = ((BubuApp)getApplicationContext()).wrSharedPictureBitmap.get();
					selectedImageLarge = ((BubuApp)getApplicationContext()).wrSharedPictureBitmapLarge.get();
				}	
				
				//if retrieved images are not null, use them
				if(selectedImage != null && selectedImageLarge != null)
		    	{
		    		
		    		((BubuApp)getApplicationContext()).setSelectedImage(selectedImage);
					((BubuApp)getApplicationContext()).wrSharedPictureBitmapLarge  = new WeakReference<Bitmap>(null);
					((BubuApp)getApplicationContext()).setSelectedImageLarge(selectedImageLarge);
					((BubuApp)getApplicationContext()).wrSharedPictureBitmapLarge = new WeakReference<Bitmap>(null);
					
					pic.setImageBitmap(selectedImage);
					
					rotationButton.setVisibility(0);  						
					
		    	}
	    		else//no image constructed, use URI to retrieve local image and build bitmap
	    		{	
				
	    			//populate image slot with shared pic (based on its uri)
  					currImageURI = sharedImageUri;
  					imageProcessDialog.show();
  					imageTask = new ImageTask();
  					imageTask.execute(currImageURI);
	    		}	
			}	
	    	
  		}
        
    }
	
	/**
	 * Handle image and object data on edit
	 */
	@Override
	protected void handleEdit()
	{
		
		i = getIntent();
		
        handleImage();
        
        
        
        if(i.getSerializableExtra(getResources().getString(R.string.bbEventData)) != null)//there is data (edit)
        {	
     	   
        	//populate from with data
        	viewTitle.setText(getResources().getString(R.string.bbEventEdit));
        	
        	eventData = (EventData) i.getSerializableExtra(getResources().getString(R.string.bbEventData));
        	
        	((EditText) findViewById(R.id.bbEventNoteField)).setText(eventData.getNote());
   	       
     	    //set calendar
        	Calendar cal=Calendar.getInstance();
     	   	cal.setTime(eventData.getDate());
     	   	cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR));
     	   	myDP.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), mDateChangedListener);
   	       
     	   	//set dimensions
     	   	if(eventData.getMeasurement().getWeight() != 0)
     	   	{
			    weight = Convertor.weight(eventData.getMeasurement().getWeight(), true);
			    
		    }  
		   
		    if(eventData.getMeasurement().getHeight() != 0)
		    {
		     	height = Convertor.height(eventData.getMeasurement().getHeight(), true);
		    } 
   	    
        }
        else//no data, new event, clean form
        {
        	viewTitle.setText(getResources().getString(R.string.bbEventNew));
        }	
        
        constructDimensionsButton();
		
	}
	
	
	@Override
	protected  void handleImage()
	{
		
        super.handleImage();
        
        //something was selected, so show it
        if( selectedImage != null )
        {
     	   pic.setImageBitmap(selectedImage);
     	   
        }
        //otherwise, if there is an intent, populate image with 
        else if(i.getSerializableExtra(getResources().getString(R.string.bbEventData)) != null)
        {
     	     
        	 //retrieve child data from intent
        	 eventData = (EventData) i.getSerializableExtra(getResources().getString(R.string.bbEventData));
     	   
     	  
        	//if there is no legacy image, retrieve image from the internets using the provided url 
	     	 if( ((BubuApp)getApplicationContext()).getLegacyImage() == null )
	         {
	     		  
	     		if(!eventData.getPic().getThumb().equals(getResources().getString(R.string.bbDefaultImageURL)))
	     		{	
	     		 //add image to form
	     		  //add image to form 
	     		  pic.setBackgroundResource(R.drawable.rounded_corners_noimage);
	     		  Ion.with(pic).
							placeholder(R.drawable.transparent).
							load(eventData.getPic().getThumb());
	     		}
	     		
	         }
	     	 else//otherwise, use the legacy image
	     	 {
	 		   pic.setImageBitmap(((BubuApp)getApplicationContext()).getLegacyImage());    		   
	     	 }
        }
	}
	
	/**
	 * Create custom date picker, set style, color, font, behavior, limits
	 */
	@Override
	protected void constructDatePicker(int dateFieldId)
	{
		
		super.constructDatePicker(dateFieldId);
		
		//disallow choosing a date past today's date
		cal = Calendar.getInstance();
		myDP.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), mDateChangedListener);
	}
	
	
	/**
	 * Set view elements
	 */
	@Override
	protected void constructViewElements() 
	{
		
		super.constructViewElements();
		
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
		
		//set typeface (custom font)
		((TextView) findViewById(R.id.bbEventNote)).setTypeface(tf);
		
		EditText note = ((EditText) findViewById(R.id.bbEventNoteField));
		note.setTypeface(tf);
		
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
		
		
		((TextView) findViewById(R.id.bbEventDate)).setTypeface(tf);
		
		
		//assign listener to click event for save button
        Button saveButton = (Button)findViewById(R.id.bbSaveEventData);
        saveButton.setTypeface(getTypeface(),1);
        saveButton.setOnClickListener(onClickSave);
        
        
        //speak button
        ImageButton speakButton = (ImageButton)findViewById(R.id.bbSpeakButton);
        
        // Check to see if a voice recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) 
        {
        	speakButton.setOnClickListener(onClickSpeak);
        } 
        else 
        {
            speakButton.setEnabled(false);
        }
        

        //construct datepicker
   		constructDatePicker(R.id.bbEventDateField);
		
	}
	
	/**
	 * Construct button with dimension values
	 */
	private void constructDimensionsButton()
	{
		//assign listener to click event for dimensions button
        dimensionButton = (Button)findViewById(R.id.bbWHButton);
        //style
        dimensionButton.setTypeface(getTypeface(),1);
        dimensionButton.setOnClickListener(onClickShowDimensions);
        //set button text
        dimensionButton.setText
        (
        		Html.fromHtml
        		(
    				new StringBuffer().
	        		append(getResources().getString(R.string.bbWeight)).
	        		append(weight !=0 ? ": " : "").
	        		append(weight !=0 ? Convertor.trimTrailingZeros(weight) : "").
	        		append(" / ").
	        		append(getResources().getString(R.string.bbHeight)).
	        		append(height !=0 ? ": " : "").
	        		append(height !=0 ? Convertor.trimTrailingZeros(height) : "").
	        		toString()
	        	)	
        );
	}
	
	//speak button clicked event
	private OnClickListener onClickSpeak = new OnClickListener() 
   	{
   	    public void onClick(View v) 
   	    {
   	    	//start activity used to collect voice message
   	    	startVoiceRecognitionActivity();
   	    }
   	};    
	
   	/**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() 
    {
        //create voice reco intent
    	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.bbSpeakMessage));

        // Given an hint to the recognizer about what the user is going to say
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Specify how many results you want to receive. The results will be sorted
        // where the first result is the one with higher confidence.
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }
    
    /**
     * Handle the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) 
        {
            //note fiels
        	EditText note = (EditText) findViewById(R.id.bbEventNoteField);
        	
            int start = note.getSelectionStart();
            
            //insert the first found option at cursor separated by spaces
            note.getText().insert
            (
            		start,
            		new StringBuffer()
					.append(" ")
            		.append(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0).toString())
            		.append(" ").toString()
            );		
            
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
	
	//listeners for showing  dimensions dialog
	private OnClickListener onClickShowDimensions = new OnClickListener() 
   	{
   	    public void onClick(View v) 
   	    {
   	    	//create dimensions dialog if null
   	    	if(pdDialog == null)
   	    		pdDialog = new PrettyDimensionsDialog(EventDetailActivity.this, R.style.BubuDialogTheme);
   	    	
   	    	//display and populate dialog
   	    	pdDialog.show(tf);
   	    	pdDialog.setDimensions(weight,height);
   	    	
   	    	//event handler for closing dialog
   	    	//set state with data collected in dialog
   	    	pdDialog.setOnDismissListener(new OnDismissListener() 
   	    	{
   	    		@Override
   	    		public void onDismiss(DialogInterface dialog) 
   	    		{
   	    			height = pdDialog.getHeight();
   	    			weight = pdDialog.getWeight();
   	    			//set button
   	    			constructDimensionsButton();
   	    		}
   	    	});   	    	
   	    	
   	    }
   	};    
	
	
	//listener for save event button
	private OnClickListener onClickSave = new OnClickListener() 
   	{
   	   
		public void onClick(View v) 
   	    {
   	    	//image
   	    	Bitmap cBitmap = ((BubuApp)getApplicationContext()).getSelectedImageLarge();
   	    	
   	    	//load value object
   	    	if(eventData == null)
   	    	{	
   	    		eventData = new EventData();
	   	    	if(cBitmap != null)
	   	    		eventData.setImage(cBitmap);
	   	    	else//no image got chosen, set default
	   	    	{
	   	    		String defaultImagePath = new StringBuffer()
	   	    									.append(getResources().getString(R.string.bbProtocol))
	   	    									.append(getResources().getString(R.string.bbDomain))
	   	    									.append(getResources().getString(R.string.bbDefaultImage))
	   	    									.toString();
	   	    		Pic dPic = new Pic();
	   	    		dPic.setMain(defaultImagePath);
	   	    		dPic.setThumb(defaultImagePath);
	   	    		eventData.setPic(dPic);
	   	    	}	
   	    	}
   	    	else
   	    	{
   	    		if(cBitmap != null)
   	    			eventData.setImage(cBitmap);
   	    	}
   	    	
   	    	//Note
   	    	eventData.setNote(((EditText) findViewById(R.id.bbEventNoteField)).getText().toString());
   	    	
   	    	//set date
   	    	DatePicker dp = ((DatePicker)findViewById(R.id.bbEventDateField));
   	    	Calendar cal = Calendar.getInstance();
   	    	cal.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
   	    	cal.set(Calendar.HOUR_OF_DAY, 0);
   	    	cal.set(Calendar.MINUTE, 0);
   	    	cal.set(Calendar.SECOND, 0);
   	    	cal.set(Calendar.MILLISECOND, 0);
   	    	eventData.setDate(cal.getTime());
   	    	
   	    	//retrieve weight and height from form, handle and set measurement object
   	    	Measurement measurement = new Measurement();
   	    	
   	    	//set measurement with state
    		measurement.setHeight
    		(
    				Convertor.height(height, false)
    		);
   	    	
   	    	
   	    	measurement.setWeight
   	    	(
   	    			Convertor.weight(weight, false)
   	    	);
   	    	
   	    	
   	    	eventData.setMeasurement(measurement);
   	    	
   	    	
   	    	//clear percentile data
   	    	//eventData.setPercentile(null);
   	    	
   	    	//validate form event data
   	    	String validationResponse = eventData.validate(dob, getApplicationContext()).getMessage();
   	    	
   	    	//all ok, run async task
   	    	if(validationResponse.equals(getResources().getString(R.string.bbOK)))
   	    	{
   	    		
   	    		if(eventData.getParentKey() == null)
   	    			eventData.setParentKey(parentKey);
   	    		
   	    		if(eventData.getChildKey() == null)
   	    			eventData.setChildKey(childKey);

   	    		
   	    		//initiate/execute background task
   	    		saveTask = new SaveTask(EventDetailActivity.this);
   	    		saveTask.execute(eventData);  	   
   	   	    	
   	    	}	
   	    	else//not ok, show error message
   	    	{
   	    		ErrorHandler.execute
   	    		(
   	    				EventDetailActivity.this,
   	    				validationResponse.replace("/","\n"),
   	    				null,
   	    				null
   	    		);
   	    	}
   	    	
   	    }
   	};
	
   	
   	 /**
     * Background task used to post image and information
     * @author oviroa
     *
     */
    private static class SaveTask extends RotationSafeFormAsyncTask
    {
    	private Exception carriedException = null;
    	private Boolean updaterCrashed = false;
    	
    	//constructor
    	public SaveTask(BubuFormActivity activity) 
		{
			super(activity);			
		}
    	
    	@Override
		protected void onPreExecute()
		{
			//show dialog
    		activity.createProgressDialog();			
		}
    	
		
		@Override
		protected void onProgressUpdate(Integer... progress)
		{
			if(isCancelled())
			{	
				if(activity != null)
				{
					activity.progressDialog.hide();
					activity.progressDialog.dismiss();
			    	((BubuApp)activity.getApplicationContext()).setProgressDialog(null);
				}	
				else
					detach();
				return;
			}	
			
			//update progress dialog view
			try
			{
				activity.progressDialog.setProgress((int) (progress[0]));
					
				if(progress[0] == 0)
				{
					activity.progressDialog.setMessage(activity.getResources().getString(R.string.bbConnecting));
					activity.progressDialog.hideBar();
				}
				else if(progress[0] < 100)
				{
					activity.progressDialog.setMessage(activity.getResources().getString(R.string.bbUploadingPicture));
					activity.progressDialog.hide();
					activity.progressDialog.showBar();
					if(activity.isActive)
						activity.progressDialog.show(); 
				}
				else if(progress[0] == 100)
				{
					activity.progressDialog.setMessage(activity.getResources().getString(R.string.bbUploadingChildData));
					activity.progressDialog.hideBar();
				}	
			
			}
			catch(Exception e)
			{
				
				if(!updaterCrashed)
				{	
					//log error once
					updaterCrashed = true;
				}
				
				//exit
				return;
			}
		}
		
		
    	@Override
		protected EventResponse doInBackground(Object... params) 
		{
			
    		EventResponse er = null;
    		
    		//data proxy
    		Proxy myProxy = Proxy.getInstance();
			
    		
    		if(!isCancelled())
				try 
				{
					//submit event data
					er = myProxy.putEvent(activity.getApplicationContext(), (EventData)params[0], activity,
							
							new ProgressListener()//listener used to retrieve image upload data in real time
							{
								@Override
								public boolean transferred(long num, long size)
								{
									if(isCancelled())
									{	
										return false;
									}	
									else
									{		
										int current = (int) ((num / (float) size) * 100);
										
										 //update dialog
										publishProgress(current);
								       
										return true;
									}	
								}
							}
					
					);
				} 
				catch (ClientProtocolException e) 
				{
					carriedException = e;
				}
				catch (ClassNotFoundException e) 
				{
					carriedException = e;
				}
				catch (IOException e) 
				{
					try 
					{
						//submit event data
						er = myProxy.putEvent(activity.getApplicationContext(), (EventData)params[0], activity,
								
								new ProgressListener()//listener used to retrieve image upload data in real time
								{
									@Override
									public boolean transferred(long num, long size)
									{
										if(isCancelled())
										{	
											return false;
										}	
										else
										{		
											//update dialog
											publishProgress((int) ((num / (float) size) * 100));
											return true;
										}	
									}
								}
						
						);
					} 
					catch (ClientProtocolException e1) 
					{
						carriedException = e1;
					}
					catch (ClassNotFoundException e1) 
					{
						carriedException = e1;
					}
					catch (IOException e1) 
					{
						carriedException = e1;
					}
		    		catch (Exception e1) 
					{
						carriedException = e1;
					}
					
					
				}
	    		catch (Exception e) 
				{
					carriedException = e;
					Log.w("BUBU ERROR :: ","EventDetailActivity 780: "+e.getMessage());
					e.printStackTrace();
				}
			
			return er;
		}
		
		protected void onPostExecute (Object er) 
	    {
			//hide loading bar
			if(activity.isActive)
				activity.killProgressDialog();
			
			if(isCancelled())
			{	
				detach();
				return;
			}
			
			//response not null
			if(er != null && ((EventResponse)er).getResponse() != null)
			{	
				
				
				String rMessage = ((EventResponse)er).getResponse().getMessage();
				
				
				//ok message
				if(rMessage != null && rMessage.equals(activity.getResources().getString(R.string.bbOK)))
				{
					
					Proxy myProxy = Proxy.getInstance();
						
					//get locally stored event map
	                HashMap<String, EventResponse> eMap = ((BubuApp)activity.getApplicationContext()).getEventMap();
	                
	                if(eMap == null)
	                {
	                	eMap = new HashMap<String, EventResponse>();
	                }	
	                
	                eMap.put(activity.childKey, (EventResponse)er);
	                
	                myProxy.storeEvents(activity.getApplicationContext(), eMap);

	                //flag that there is a fresher version of this event list
	                ((BubuApp)activity.getApplicationContext()).setEventListFreshStatus(true);
	                ((BubuApp)activity.getApplicationContext()).setBitmapWasRecycled(false);
	                
	                //reset form
					activity.clearForm();
					
					//if not edit (new event)
					if(activity.getIntent().getSerializableExtra(activity.getResources().getString(R.string.bbEventData)) == null)
					{
						//reset events adapter set
				   		((BubuApp)activity.getApplicationContext()).setPreEditEventSelectorPage(0);
				   		((BubuApp)activity.getApplicationContext()).setPreEditFirstVisibleEvent(0);
					}	
					
					//kill activity
					activity.finish();
				}
				else
				{
					ErrorHandler.execute
					(
							activity,
							activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
           					"EventDetailActivity 655: " + rMessage,
							new Exception(rMessage)
					);
				}
			}
			else
			{
				
				if(carriedException != null)
					ErrorHandler.execute
					(
							activity, 
							activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
							"EventDetailActivity 667: " + carriedException.getMessage(), 
							carriedException
					);
				else	
					ErrorHandler.execute
					(
							activity, 
							activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
							"EventDetailActivity 860", 
							new Exception("EventDetailActivity 860")
					);
			}	
			
			//detach activity from async task
			detach(); 
	    }
	    	   
    	
    }
    
    
    /**
	 * Listener for DatePicker. Set max limit to today's date, min limit to child's birth date
	 */
	private  DatePicker.OnDateChangedListener mDateChangedListener = new DatePicker.OnDateChangedListener() 
	{
		
		@Override
		public void onDateChanged(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) 
		{
			
			//today
			Calendar cal=Calendar.getInstance();
			//selected
			Calendar calSelected=Calendar.getInstance();
			calSelected.set(year, monthOfYear, dayOfMonth);
			
			//dob
			Calendar calDob = Calendar.getInstance();
			calDob.setTime(dob);
			calDob.set(Calendar.DAY_OF_YEAR, calDob.get(Calendar.DAY_OF_YEAR));
			calDob.set(Calendar.HOUR_OF_DAY, 0);
			calDob.set(Calendar.MINUTE, 0);
			calDob.set(Calendar.SECOND, 0);
			
			//conditional for allowing a 31 day grace period between chosen dates in order to make date chooser more flexible for dates close to birth date
			if(Math.abs(Days.daysBetween(new DateTime(dob), new DateTime(calSelected.getTime())).getDays()) > 31)
			{	
				//if selected date is past today, set today as the selected date
				if(cal.before(calSelected))
				{
					view.updateDate(preChangeCal.get(Calendar.YEAR), preChangeCal.get(Calendar.MONTH), preChangeCal.get(Calendar.DAY_OF_MONTH));
					//show message
					Toast toast = Toast.makeText(getApplicationContext(), R.string.bbDatePastLimit, Toast.LENGTH_SHORT);
			    	toast.show();
				}
				else if(calSelected.before(calDob))
				{
					view.updateDate(preChangeCal.get(Calendar.YEAR), preChangeCal.get(Calendar.MONTH), preChangeCal.get(Calendar.DAY_OF_MONTH));
					//show message
					Toast toast = Toast.makeText(getApplicationContext(), R.string.bbDateBeforeLimit, Toast.LENGTH_SHORT);
			    	toast.show();
				}	
				else
					preChangeCal.set(year, monthOfYear, dayOfMonth); //date, pre-chage, used to reverso to it when bounds are hit
			}	
		}
	};
	
	//activity is resumed, set calendar
	@Override
    protected void onResume() 
    {
        super.onResume();
        preChangeCal.set(myDP.getYear(), myDP.getMonth(), myDP.getDayOfMonth());
    }
	
	
    //activity is stopped, kill dialogs
	@Override
    protected void onStop() 
    {
        super.onStop();
        
        if(progressDialog != null)
        	progressDialog.dismiss();
        
        if(pdDialog != null)
        	pdDialog.dismiss();
        
        if(alert != null)
        	alert.dismiss();
    }      
    
   @Override
	public Object onRetainNonConfigurationInstance() 
    {
    	if(saveTask != null)
    		saveTask.detach();
    	return(saveTask);
	}

   /**
    * Hide dialog
    */
   @Override
   public void killProgressDialog()
   {
   	super.killProgressDialog();
   	
   	saveTask = null;
   }
   
   /**
    * create and style context menu
    */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo)
    {
    	
    	//if user long clicks on event note field, handle style of context menu
    	if(view.getId()==R.id.bbEventNoteField)
           {
    		 //kill header
    		 menu.clearHeader();
    		 try
    		 {
    			 //style
    			 setMenuBackground();
    		 }
    		 catch(Exception e)
    		 {
    			 //ignore
    		 }
           }
           else
              super.onCreateContextMenu(menu, view, menuInfo);
    }
	
    @Override
	protected void broacastHandler(Bundle extras)
	{
    	super.broacastHandler(extras);
    	
    	if(extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)) != null 
    			&& extras.getString(getResources().getString(R.string.bbUpdateEventUpdated)).equals(getResources().getString(R.string.bbCD2MChildMessage)))
    	{
			finish();
    	}
    	
	}
    
	
}
