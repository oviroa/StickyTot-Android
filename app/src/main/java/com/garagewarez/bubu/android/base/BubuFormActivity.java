package com.garagewarez.bubu.android.base;

import java.io.FileNotFoundException;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.garagewarez.bubu.android.BubuApp;
import com.garagewarez.bubu.android.PrettyProgressDialogComplex;
import com.garagewarez.bubu.android.R;
import com.garagewarez.bubu.android.SourcesAdapter;
import com.garagewarez.bubu.android.utils.Debug;
import com.garagewarez.bubu.android.utils.Tools;

public abstract class BubuFormActivity extends BubuBaseActivity 
{
	
	//image view object fro child picture
	protected ImageView pic;
	
	//image uri
	protected Uri currImageURI;
	
	//alert dialog for photo selection
	protected AlertDialog alert;
	
	//imag button for rotation of picture
	protected ImageButton rotationButton;	
	
	//date picker
	protected DatePicker myDP;
	
	//bitmap of child image
	protected Bitmap selectedImage;
	
	//calendar used for date picker
	protected Calendar cal;
	
	//Parent key
	public String parentKey;
	
	
	//Child key
	public String childKey;
	
	//intent
    protected Intent i;
    
    protected String textFieldHack = "";
    protected EditText textFieldHackField;
    
    /**
	 * progress dialog object
	 */
	public PrettyProgressDialog imageProcessDialog;
	
	
	//constants for intent identification
	protected static final int CAMERA_PIC_REQUEST = 1337; 
	protected static final int GALLERY_PIC_REQUEST = 1337337;
	
	
	//save task
	/**
     * AsyncTask flavor, used for deletion of elements in the collection
     */
	public RotationSafeFormAsyncTask saveTask;
	
	public Boolean isActive = false;
	
	/**
	 * Handle image and object data on edit
	 */
	protected abstract void handleEdit();
	
	/**
	 * Prep view elements, set typeface.
	 */
	protected void constructViewElements()
	{
		createImageProcessDialog();
	}
	
	
	//async task used for retrieving events corresponding to a selected child 
	protected AsyncTask<Uri, Integer, Bitmap> imageTask;
	
	/**
	 * Handle image selection (camera, gallery)
	 * @param imageId
	 * @param rotateButtonId
	 * @param context
	 */
	@SuppressLint("NewApi")
	protected void handleImageChooser(int imageId, int rotateButtonId, final Context context)
	{
		   //image, set to default
	       if(pic == null)
	    	   pic = (ImageView)findViewById(imageId);
	       
	       //image rotation button
	       rotationButton = (ImageButton)this.findViewById(rotateButtonId);
	       
	       rotationButton.setOnClickListener(new OnClickListener() 
		   {
		   	    public void onClick(View v)
		   	    {
		   	    	rotatePhoto();
		   	    }
		   });    
	       
		    //make rotation button invisible if images are there
		   	if(((BubuApp)getApplicationContext()).getSelectedImageLarge() != null && ((BubuApp)getApplicationContext()).getSelectedImage() != null)
		   	{
		   		rotationButton.setVisibility(0);
		   	}
		   	
		   //set click handler for loading local image into picture view
	       pic.setOnClickListener(new OnClickListener() 
		   {
		   	    public void onClick(View v) 
		   	    {
		   	    	//run mechanism for choosing an image
		   	    	runImageSelector(context);
		   	    }
		   	});
	}
	
	
	
	
	/**
	 * Create custom date picker, set style, color, font, behavior, limits
	 */
	protected void constructDatePicker(int dateFieldId)
	{
		//date-picker for dob 
		myDP = (DatePicker)findViewById(dateFieldId);
       
		
		try 
		{ 
			//style picker
			for (int i=0; i<3; i++)
			{
				if(currentapiVersion >= 11)//for level deep
				{	
					((ViewGroup) ((ViewGroup) (((ViewGroup) myDP.getChildAt(0)).getChildAt(0))).getChildAt(i)).getChildAt(0).setBackgroundResource(R.drawable.picker_selector_plus);
					((ViewGroup) ((ViewGroup) (((ViewGroup) myDP.getChildAt(0)).getChildAt(0))).getChildAt(i)).getChildAt(1).setBackgroundResource(R.drawable.rounded_corners);
					
					EditText etPicker = (EditText)  ((ViewGroup) ((ViewGroup) (((ViewGroup) myDP.getChildAt(0)).getChildAt(0))).getChildAt(i)).getChildAt(1);
					   
					etPicker.setTextColor(Color.parseColor("#ff14808b"));
					
					etPicker.setTextSize(25);
					
					etPicker.setPadding(25, 25, 25, 25);
					
					etPicker.setWidth(200);
					   
					etPicker.setTypeface(getTypeface(),1); 
					   
					((ViewGroup) ((ViewGroup) (((ViewGroup) myDP.getChildAt(0)).getChildAt(0))).getChildAt(i)).getChildAt(2).setBackgroundResource(R.drawable.picker_selector_minus);
				}	
				else//pre 11, three level deepxs
				{	
					((ViewGroup) ((ViewGroup) myDP.getChildAt(0)).getChildAt(i)).getChildAt(0).setBackgroundResource(R.drawable.picker_selector_plus);
		       
					((ViewGroup) ((ViewGroup) myDP.getChildAt(0)).getChildAt(i)).getChildAt(1).setBackgroundResource(R.drawable.rounded_corners);
		       
					EditText etPicker = (EditText)  ((ViewGroup) ((ViewGroup) myDP.getChildAt(0)).getChildAt(i)).getChildAt(1);
				   
					etPicker.setTextColor(Color.parseColor("#ff14808b"));
					
					etPicker.setTextSize(25);
					
					etPicker.setPadding(25, 25, 25, 25);
					
					etPicker.setWidth(200);
				   
					etPicker.setTypeface(getTypeface(),1); 
				   
					((ViewGroup) ((ViewGroup) myDP.getChildAt(0)).getChildAt(i)).getChildAt(2).setBackgroundResource(R.drawable.picker_selector_minus);
				}	
			}
		} 
		catch (Throwable t) 
		{/* obviously now the view may look like garbage */
			Log.w(Debug.TAG,"GARBIE DOLL");
		}
   
		//disable text entry, limit to touch interaction
		setDisabledTextViews(myDP);
		
	}
	
	/**
	 * Helper method for DatePicker view - disallows text/keyboard entry for dates, leaves only touch option
	 */
    private void setDisabledTextViews(ViewGroup dp) 
    { 
    	View v;
    	
    	for (int x = 0, n = dp.getChildCount(); x < n; x++) 
        { 
                v = dp.getChildAt(x); 
                if (v instanceof TextView) 
                { 
                        v.setEnabled(false);
                        ((TextView) v).setInputType(0);
                } else if (v instanceof ViewGroup) { 
                        setDisabledTextViews((ViewGroup)v); 
                } 
        } 
    } 
	
    
    /**
     * Initializes progress dialog with copy and custom font 
     */
    protected void createImageProcessDialog()
    {
		//if progress dialog is not instantiated, create one and make it invisible 
    	if(imageProcessDialog == null)
    	{	
    		imageProcessDialog = new PrettyProgressDialog(this);
    		imageProcessDialog.show(getResources().getString(R.string.bbImageProcessing), getTypeface());
    		imageProcessDialog.dismiss();
    	}	
    		
    }
	
	/**
	 * Handle progressDialog state and progress to prepare for device rotation
	 */
	protected void handleProgressDialogOnRotation()
	{
		
		//if progress dialog is not instantiated, create one and make it invisible 
    	if(saveTask != null && !saveTask.isCancelled())
    	{	
    		//retrieve from state (memory)
    		progressDialog = ((BubuApp)getApplicationContext()).getProgressDialog();
    		
    		//set progress bar value
    		if(progressDialog.getProgress() == 100)
    			progressDialog.setMessage(getResources().getString(R.string.bbUploadingChildData)); 
    		else if(progressDialog.getProgress() == 0)
    			progressDialog.setMessage(getResources().getString(R.string.bbConnecting)); 
    		else if(progressDialog.getProgress() < 100)
    			progressDialog.setMessage(getResources().getString(R.string.bbUploadingPicture));
    		
    		//display
    		progressDialog.show();
    		
    	}	
	}
	
	/**
     * Hide dialog, clear state (memory)
     */
    public void killProgressDialog()
    {
    	if(progressDialog.isShowing())
    		progressDialog.dismiss();
    	
    	if(saveTask.isCancelled())
    	{	
    		((BubuApp)getApplicationContext()).setProgressDialog(null);
    	}
    }
    
    /**
     * Create dialog, the complex version (with progress bar)
     */
    public void createProgressDialog()
    {
    	progressDialog = new PrettyProgressDialogComplex(this);
    	
    	progressDialog.state = "nice";
    	
	    progressDialog.show(getResources().getString(R.string.bbConnecting), getTypeface());
	    
	    
	    progressDialog.setCancelable(true);
	    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() 
	    	{
	    		@Override
	    		public void onCancel(DialogInterface dialog) 
	    		{
	    			saveTask.cancel(true);
	    		}
	    	});
	    
	    ((BubuApp)getApplicationContext()).setProgressDialog(progressDialog);
    		
    }
	
    
    /**
	 * Run mechanism of selecting an image from either gallery or camera
	 */
	protected void runImageSelector(Context context)
	{
		//create dialog with list of sources
    	final CharSequence[] sources = getResources().getStringArray(R.array.bbSourceArray);
    	
    	//displays options for choosing image
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	
    	
    	//adapter for list of picture sources
    	SourcesAdapter myAdapter = new SourcesAdapter(context, R.layout.sources_list_item, sources, tf);
    	
    	builder.setAdapter 
    	(
    	myAdapter, 
		new DialogInterface.OnClickListener() 
    	{
    	    //list of click handler
			public void onClick(DialogInterface dialog, int item) 
    	    {
				//set state legacy image from internet retrieved image
				//it will be used to handle state refreshes in orientation changes
				if(pic.getDrawable() != null && !((BitmapDrawable)pic.getDrawable()).getBitmap().isRecycled()) 
					((BubuApp)getApplicationContext()).setLegacyImage(((BitmapDrawable)pic.getDrawable()).getBitmap());
				
				switch(item)
    	    	{
    	    	  	//if camera
    	    		case 0:
    	    			textFieldHack = textFieldHackField.getText().toString();
    	    			//define the file-name to save photo taken by Camera activity
    	    			String fileName = "new-photo-name.jpg";
    	    			//create parameters for Intent with filename
    	    			ContentValues values = new ContentValues();
    	    			values.put(MediaStore.Images.Media.TITLE, fileName);
    	    			values.put(MediaStore.Images.Media.DESCRIPTION,"Image capture by camera");
    	    			//imageUri is the current activity attribute, define and save it for later usage
    	    			currImageURI = getContentResolver().insert(
    	    					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    	    			
    	    			//make uri persistent
    	    			((BubuApp)getApplicationContext()).setSelectedImageUri(currImageURI);
    	    			
    	    			//create new Intent
    	    			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	    			cameraIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    	    			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currImageURI);
    	    			cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
    	    			startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    	    			break;
    	    	  
    	    		//if gallery, start gallery activity	
    	    		case 1:
    	    			Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
    	    			galleryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    	    			galleryIntent.setType("image/*");
    	    			startActivityForResult(galleryIntent, GALLERY_PIC_REQUEST);
    	    			break;
    	    	
    	    	}
    	    	
    	    }
    	}
    	);
    	
    	alert = builder.create();
    	alert.show();		
	}
	
	/**
	 * Handles results from chosen activity for image setter
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{  
		if(resultCode == RESULT_OK && requestCode != 1234)
		{	
			switch(requestCode)
			{
				
				//camera, load image with bitmap
				case CAMERA_PIC_REQUEST:
				
					currImageURI = ((BubuApp)getApplicationContext()).getSelectedImageUri();
					//hack
					if(!textFieldHack.equals(""))
						textFieldHackField.setText(textFieldHack);
					
					
					
				break;
				
				//gallery, map image view to URI
				case GALLERY_PIC_REQUEST: 
					
					currImageURI = data.getData();
					
				break;
			
			}
			
			if(currImageURI != null)
			{	
				//initiate and execute async event task
				//show image processing dialog
		    	imageProcessDialog.show();
				imageTask = new ImageTask();
		    	imageTask.execute(currImageURI);
			}	
			
			
		}
		else if(resultCode == RESULT_CANCELED)
			((BubuApp)getApplicationContext()).setLegacyImage(null);
		
		
	}
	
	/**
	 * AsyncTask used to resize and display image
	 * @author oviroa
	 *
	 */
	public class ImageTask extends AsyncTask<Uri, Integer, Bitmap> 
    {
        
       protected void onProgressUpdate(Integer... progress) 
       {
    	   
       }
       
	   
       protected Bitmap doInBackground(Uri... arg0) 
       {
    	   Bitmap thumbnail = null;
    	   
    	   //make large image persistent
			try 
			{
				Bitmap largeBitmap = Tools.scalePicUriSafe(currImageURI, getResources().getInteger(R.integer.bbImageMainSize), getApplicationContext()); 
				
				if(largeBitmap == null)
					return null;
				
				((BubuApp)getApplicationContext()).setSelectedImageLarge(largeBitmap);
				
				//create small image an make persistent
				try 
				{
					thumbnail = Tools.scalePicUriSafe(currImageURI, getResources().getInteger(R.integer.bbImageThumbSize), getApplicationContext());
					
					((BubuApp)getApplicationContext()).setSelectedImage(thumbnail);
					
		    		
				} 
				catch (FileNotFoundException e1) 
				{
					return null;
				} 
				catch (NotFoundException e1) 
				{
					return null;
				}	
	    		
			} 
			catch (FileNotFoundException e) 
			{
				return null;
			} 
			catch (NotFoundException e) 
			{
				return null;
			}
		    		
			return thumbnail;
       }
       
    @SuppressLint("NewApi")
	protected void onPostExecute(Bitmap thumbnail) 
       {
    	   imageProcessDialog.dismiss();
    	   
    	   //nothing got returned, rebuild image 
    	   try
    	   {
    		   if(thumbnail==null)
	    		   handleImage();
	    		else
	    		{	
	    			
	    			//recycle only if image not placeholder since we need that
					if(pic.getDrawable() != null && ((BitmapDrawable)pic.getDrawable()).getBitmap() != null)
						if(!((BitmapDrawable)pic.getDrawable()).getBitmap().sameAs(((BitmapDrawable)getResources().getDrawable(R.drawable.noimage)).getBitmap()))
						{	
							pic.setImageDrawable(null);
							((BitmapDrawable)pic.getDrawable()).getBitmap().recycle();
							pic.setImageBitmap(null);
							((BubuApp)getApplicationContext()).setBitmapWasRecycled(true);
							
						}	
		   		
					
					pic.setImageBitmap(thumbnail);
					rotationButton.setVisibility(0);
					
					
					//kill selected image and legacy image
					((BubuApp)getApplicationContext()).setLegacyImage(null);
					((BubuApp)getApplicationContext()).setSelectedImageUri(null);
					
					//kill current image buffer
					currImageURI = null;
						
				}
    	   }
    	   catch(Exception e)
    	   {
    		   handleImage();
    	   }
    	      
       }
	   	
    } 
    
	/**
	 * handle image display
	 */
	protected void handleImage()
	{
		//retrieve image states
        selectedImage = ((BubuApp)getApplicationContext()).getSelectedImage();
        currImageURI = ((BubuApp)getApplicationContext()).getSelectedImageUri();
        ((BubuApp)getApplicationContext()).setLegacyImage(null);
	}
	
	/**
     * Clears form fields
     */
	@Override
    public void clearForm()
    {
    	//reset state
    	super.clearForm();
		rotationButton.setVisibility(4);
       
    }
    
    /**
	 * Rotates photo by 90
	 */
    protected void rotatePhoto()
    {
    	
    	
    	Bitmap cBitmapLarge = ((BubuApp)getApplicationContext()).getSelectedImageLarge();
    	Bitmap cBitmap = ((BubuApp)getApplicationContext()).getSelectedImage();
    	
    	
    	
    	if(cBitmap != null && cBitmapLarge != null)
    	{
    		
    		//thumb
    		cBitmap = rotateBitmap(cBitmap);
    		pic.setImageBitmap(cBitmap);
    		((BubuApp)getApplicationContext()).setSelectedImage(cBitmap);
    		
    		//large
    		
    		cBitmapLarge = rotateBitmap(cBitmapLarge);
    		((BubuApp)getApplicationContext()).setSelectedImageLarge(cBitmapLarge);
    	}
    	
    }
    
    /**
     * Creates rotated bitmap (90)
     * @param sourceBitmap
     * @return
     */
    protected Bitmap rotateBitmap(Bitmap sourceBitmap)
    {
    	Bitmap resultBitmap = Bitmap.createBitmap((int) (Math.floor(sourceBitmap.getHeight()/2)*2), (int)Math.floor(sourceBitmap.getWidth()/2)*2, Bitmap.Config.ARGB_8888);
		Canvas tempCanvas = new Canvas(resultBitmap); 
		
		int polarizer = sourceBitmap.getWidth() > sourceBitmap.getHeight() ? 1 : -1;
		int difference = polarizer*Math.round(Math.abs(sourceBitmap.getHeight() - sourceBitmap.getWidth())/2);
		
		tempCanvas.rotate(90, sourceBitmap.getWidth()/2, sourceBitmap.getHeight()/2);
		tempCanvas.drawBitmap(sourceBitmap, difference, difference, null);
		
		
		return resultBitmap;
    }
    
    /**
     * Kill event retrieval async task
     */
    private void onCancelImageTask()
    {
    	//if there is a running event retrieving async task
    	if(imageTask != null && imageTask.getStatus() == AsyncTask.Status.RUNNING)
        {
    		//kill it 
    		imageTask.cancel(true);
    		imageTask = null;
    		
    		currImageURI = null;
    		
    		//kill progress dialog if shown 
            if(imageProcessDialog != null)
            {	
            	imageProcessDialog.dismiss();
            }	
        }
    	
    	
    }
    
    /**
     * Removes receivers and kills task
     */
    protected void onStop() 
    {
        super.onStop();
        isActive = false;
        //kill all tasks
        onCancelImageTask();
    }
    
    /**
     * Kills task
     */
    @Override
    protected void onDestroy() 
    {
        super.onDestroy();
        
        onCancelImageTask();
    }
    
    @Override
	protected void onStart()
	{
		super.onStart();
		isActive = true;
		//update progress dialog state on rotation
        handleProgressDialogOnRotation();
	}
    
}
