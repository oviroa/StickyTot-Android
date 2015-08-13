package com.garagewarez.bubu.android;

import java.io.IOException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.Calendar;

import org.acra.ACRA;
import org.apache.http.client.ClientProtocolException;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.garagewarez.bubu.android.base.BubuFormActivity;
import com.garagewarez.bubu.android.base.RotationSafeFormAsyncTask;
import com.garagewarez.bubu.android.common.ChildData;
import com.garagewarez.bubu.android.common.ChildResponse;
import com.garagewarez.bubu.android.common.Pic;
import com.garagewarez.bubu.android.proxy.Connector.ProgressListener;
import com.garagewarez.bubu.android.utils.UrlImageView;

/**
 * Activity invoked when a new child is added or an existing child is being edited
 * @author oviroa
 *
 */
public class ChildDetailActivity extends BubuFormActivity 
{
	
	static final int DATE_DIALOG_ID = 0;
	
	//data instance
	private ChildData childData;
	
	//gender dropdown
	private Spinner genderSpinner;
	
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        //Register as a receiver to listen for event/broadcasts
        IntentFilter filter = new IntentFilter(getResources().getString(R.string.bbUpdateEvent));
        registerReceiver(mReceiver, filter);        
        
        //handles device orientation, displays alternative view for landscape
        int deviceOrientation = getResources().getConfiguration().orientation;
        
        if(deviceOrientation == Configuration.ORIENTATION_PORTRAIT)        
        	setContentView(R.layout.kids_detail);
        else
        	setContentView(R.layout.kids_detail_landscape);
        
    	
       //instantiated async task
       saveTask = (SaveTask)getLastNonConfigurationInstance();

       //handles running async task on device rotation or re-draws after other interruptions
       if (saveTask != null) 
       {
    	   saveTask.attach(this);    	  
       }
        
       //hack
       textFieldHackField = ((EditText) findViewById(R.id.bbChildNameField));
       
        registerForContextMenu(((EditText) findViewById(R.id.bbChildNameField)));
       
        //construct top bar with back button and title
        constructTopBar(R.id.homeButtonChildDetails, R.id.textViewChildDetails);
       
        //construct view elements, set content for text, fonts etc
        constructViewElements();
   		
   		//select image from internal resources
        handleImageChooser(R.id.bbChildImage,R.id.bbRotateChildButton, ChildDetailActivity.this);
       
        //populate with existing values, handle updates of fields
        handleEdit();
       
    }	
	
	
	/**
	 * Construct view elements, widgets, set content, fonts etc.
	 */
	@Override
	protected void constructViewElements()
	{
		
        super.constructViewElements();
		
		//set typeface for text fields
   		//child name label
   		((TextView) findViewById(R.id.bbChildName)).setTypeface(getTypeface());
   		//child name field
   		((EditText) findViewById(R.id.bbChildNameField)).setTypeface(getTypeface());
   		//gender label
   		((TextView) findViewById(R.id.bbChildGender)).setTypeface(getTypeface());
   		//dob label
   		((TextView) findViewById(R.id.bbChildDOB)).setTypeface(getTypeface());
   		
   		//assign listener to click event for save button
        Button saveButton = (Button)findViewById(R.id.bbSaveChildData);
        saveButton.setTypeface(getTypeface(),1);
        saveButton.setOnClickListener(onClickSave);
   		
   		//build gender spinner
   		constructGenderSpinner();
   		
   		//construct datepicker
   		constructDatePicker(R.id.bbChildDOBField);
   		
   		
   	}
	
	
	/**
	 * Build gender spinner, set content, style, fonts
	 */
	private void constructGenderSpinner()
	{
		//set spinner data for gender
        genderSpinner = (Spinner) findViewById(R.id.bbChildGenderField);
        
                
        //create spinner base on xml array
        String [] items = getResources().getStringArray(R.array.bbGenderArray);
       
        
        //set adapter based on XML item
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) 
        {

        	public View getView(int position, View convertView, ViewGroup parent) 
        	{
        		View v = super.getView(position, convertView, parent);

        		//set typeface for selected item
        		((TextView) v).setTypeface(getTypeface());

        		return v;
        	}


        	public View getDropDownView(int position,  View convertView,  ViewGroup parent) 
        	{
        		View v =super.getDropDownView(position, convertView, parent);

        		//set typeface for list of options
        		((TextView) v).setTypeface(getTypeface());

        		return v;
        	}
        
        };
        
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(spinnerAdapter);
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
	 * Handle image and object data on edit
	 */
	@Override
	protected void handleEdit()
	{
		   //get intent
	       i = getIntent();
	       //if this is an update...
	       
	       
	       handleImage();
	       
	       //load form with intent data, if existent
	       if(i.getSerializableExtra(getResources().getString(R.string.bbChildData)) != null)
	       {
	    	   viewTitle.setText(getResources().getString(R.string.bbChildEdit));
	      		
	    	   childData = (ChildData) i.getSerializableExtra(getResources().getString(R.string.bbChildData));
	    	   
	    	   //populate form
	    	   ((EditText) findViewById(R.id.bbChildNameField)).setText(childData.getName());
	  	       ((Spinner) findViewById(R.id.bbChildGenderField)).setSelection(childData.getGender().equals(getResources().getStringArray(R.array.bbGenderArray)[0]) ? 0 : 1);
	  	       
	  	       cal.setTime(childData.getDob());
	  	       //cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR)+1);
	  	       myDP.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), mDateChangedListener);
	  	       
	  	   }
	       else
	       {
	    	   viewTitle.setText(getResources().getString(R.string.bbChildNew));
	       }	   
	   
	}
	
	
	@Override
	protected void handleImage()
	{
		 super.handleImage();
		 
		  //something was selected, so show it
	       if( selectedImage != null)
	       {
	    	  pic.setImageBitmap(selectedImage);
	    	  
	       }
	       //otherwise, if there is an intent, populate image with 
	       else if(i.getSerializableExtra(getResources().getString(R.string.bbChildData)) != null)
	       {
	    	   
	    	   //retrieve child data from intent
	    	   childData = (ChildData) i.getSerializableExtra(getResources().getString(R.string.bbChildData));
	    	   
	    	   //if there is no legacy image, retrieve image from the internets using the provided url 
	    	   if( ((BubuApp)getApplicationContext()).getLegacyImage() == null)
	           {	   
	    		   if(!childData.getPic().getThumb().equals(getResources().getString(R.string.bbDefaultImageURL)))
	    		   {	   

		    		   //add image to form 
		    		   pic.setBackgroundResource(R.drawable.transparent);
		    		   UrlImageView.setBackgroundResource(R.drawable.rounded_corners_noimage);
		    		   
		    		   UrlImageView.setUrlDrawable
		        		(
		        			pic, 
		        			childData.getPic().getThumb()
		        		);
		        		
	    		   }  
	        	   
	           }
	    	   else//otherwise, use the legacy image
	    	   {
	    		   pic.setImageBitmap(((BubuApp)getApplicationContext()).getLegacyImage());    		   
	    	   }	   
	    	   
	  	   }
	}
	
	
	/**
	 * Listener for DatePicker. Set max limit to today's date
	 */
	private DatePicker.OnDateChangedListener mDateChangedListener = new DatePicker.OnDateChangedListener() 
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
			
			//if selected date is past today, set today as the selected date
			if(cal.before(calSelected))
			{
				view.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
				
				//show message
				Toast toast = Toast.makeText(getApplicationContext(), R.string.bbDatePastLimit, Toast.LENGTH_SHORT);
		    	toast.show();
			} 
			
		}
	};
	
	//listener for save button
	private OnClickListener onClickSave = new OnClickListener() 
   	{
   	    public void onClick(View v) 
   	    {
   	    	
   	    	Bitmap cBitmap = ((BubuApp)getApplicationContext()).getSelectedImageLarge();
   	    	
   	    	//load value object
   	    	if(childData == null)
   	    	{	
	   	    	childData = new ChildData();
	   	    	if(cBitmap != null)
	   	    		childData.setImage(cBitmap);
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
	   	    		childData.setPic(dPic);
	   	    	}	
   	    	}
   	    	else
   	    	{
   	    		if(cBitmap != null)
	   	    		childData.setImage(cBitmap);
   	    	}
   	    	
   	    	childData.setName(((EditText) findViewById(R.id.bbChildNameField)).getText().toString());
   	    	childData.setGender(((Spinner) findViewById(R.id.bbChildGenderField)).getSelectedItem().toString());
   	    	DatePicker dp = ((DatePicker)findViewById(R.id.bbChildDOBField));
   	    	Calendar cal = Calendar.getInstance();
   	    	cal.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
   	    	cal.set(Calendar.HOUR_OF_DAY, 0);
   	    	cal.set(Calendar.MINUTE, 0);
   	    	cal.set(Calendar.SECOND, 0);
   	    	cal.set(Calendar.MILLISECOND, 0);
   	    	childData.setDob(cal.getTime());
   	    	
   	    	//validate form child data
   	    	ChildResponse cr;
			
   	    	try 
			{
				cr = getProxy().getStoredChildResponse(getApplicationContext());
				
				if(childData.getEncodedKey() == null)
					childData.setParentKey(getProxy().getStoredParentData(getApplicationContext()).getEncodedKey());
				
				String validationResponse = childData.validate(cr, getApplicationContext()).getMessage();
	   	    	
	   	    	if(validationResponse.equals(getResources().getString(R.string.bbOK)))
	   	    	{
	   	    		//initiate end execute background task
	   		    	saveTask = new SaveTask(ChildDetailActivity.this);
	   	   	    	saveTask.execute(childData);
	   	   	    	
	   	    	}	
	   	    	else
	   	    	{
	   	    		
	   	    		//validation
	   	    		ErrorHandler.execute
	   	    		(
	   	    				ChildDetailActivity.this, 
	   	    				validationResponse.replace("/","\n"),
	   	    				null,
	   	    				null
	   	    		);
	   	    	}
				
			} 
			catch (OptionalDataException e) 
			{
				ErrorHandler.execute
				(
						ChildDetailActivity.this, 
						getResources().getString(R.string.bbProblem), 
       					"ChildDetailActivity 373: ", 
						e
				);
			} 
			catch (StreamCorruptedException e) 
			{
				ErrorHandler.execute
				(
						ChildDetailActivity.this, 
						getResources().getString(R.string.bbProblem), 
       					"ChildDetailActivity 383: ", 
						e
				);
			} 
			catch (ClassNotFoundException e) 
			{
				ErrorHandler.execute
				(
						ChildDetailActivity.this, 
						getResources().getString(R.string.bbProblem), 
       					"ChildDetailActivity 393: ", 
						e
				);
			} 
			catch (IOException e) 
			{
				ErrorHandler.execute
				(
						ChildDetailActivity.this, 
						getResources().getString(R.string.bbProblem), 
       					"ChildDetailActivity 393: ", 
						e
				);
			}
   	    	catch (Exception e) 
			{
				ErrorHandler.execute
				(
						ChildDetailActivity.this, 
						getResources().getString(R.string.bbProblem), 
       					"ChildDetailActivity 431: ", 
						e
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

    	public SaveTask(BubuFormActivity activity) 
		{
			super(activity);			
		}
    	
    	private Exception carriedException = null;
    	private Boolean updaterCrashed = false;
		
		@Override
		protected void onPreExecute()
		{
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
					//send to bug tracker
					ACRA.getErrorReporter().putCustomData("BBMessage", "ChildDetailActivity 477 :: progress :: errorMessage: " + e.getMessage() );
					ACRA.getErrorReporter().handleSilentException(null);
					
				}	
				
				
				//exit
				return;
			}	
			
			
		}
		
    	@Override
		protected ChildResponse doInBackground(Object... params) 
		{
			
    		ChildResponse cr = null;
			
    		if(!isCancelled())
				try 
				{
					
					cr = activity.getProxy().putChild(activity.getApplicationContext(), (ChildData) params[0], activity,
								
								new ProgressListener()
								{
									@Override
									public boolean transferred(long num, long size)
									{
										publishProgress((int) ((num / (float) size) * 100));
										return true;
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
						
						cr = activity.getProxy().putChild(activity.getApplicationContext(), (ChildData) params[0], activity,
									
									new ProgressListener()
									{
										@Override
										public boolean transferred(long num, long size)
										{
											publishProgress((int) ((num / (float) size) * 100));
											return true;
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
				}
				
			return cr;
		}
		
    	protected void onPostExecute (Object cr) 
	    {
			//hide loading bar
    		if(activity.isActive)
    			activity.killProgressDialog();
			
			
			if(isCancelled())
			{	
				detach();
				return;
			}	
			
			if(cr != null && ((ChildResponse) cr).getResponse() != null)
			{	
				String rMessage = ((ChildResponse) cr).getResponse().getMessage();
				
				
				if(rMessage != null && rMessage.equals(activity.getResources().getString(R.string.bbOK)))
				{
					//store kids
	                activity.getProxy().storeKids(activity.getApplicationContext(), (ChildResponse) cr);
					
					//make new kid list persistent
					((BubuApp)activity.getApplicationContext()).setKidListFreshStatus(true);
					((BubuApp)activity.getApplicationContext()).setBitmapWasRecycled(false);
					
					//reset form
					activity.clearForm();
					
					//kill activity
					activity.finish();
				}
				else
				{
					this.cancel(true);
					ErrorHandler.execute
					(
							activity,
							activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
           					"ChildDetailActivity 534: " + rMessage,							
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
							activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
							"ChildDetailActivity 546: " + carriedException.getMessage(), 
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
     * Hide dialog
     */
    @Override
    public void killProgressDialog()
    {
    	super.killProgressDialog();
    	
    	saveTask = null;
    }
    
    
    @Override
    protected void onStop() 
    {
        super.onStop();
        
        if(progressDialog != null)
        	progressDialog.dismiss();
        
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
	
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo)
    {
    	
    	//if user long clicks on child name field, handle style of context menu
    	if(view.getId()==R.id.bbChildNameField)
           {
    		 menu.clearHeader();
    		 try
    		 {
    			 setMenuBackground();
    		 }
    		 catch(Exception e)
    		 {
    			 
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
