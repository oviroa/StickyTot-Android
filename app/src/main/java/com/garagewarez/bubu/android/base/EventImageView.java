package com.garagewarez.bubu.android.base;

import com.garagewarez.bubu.android.utils.Debug;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * Gesture enabled WebImageView, used for the display view of events, with two slightly different behavior sets, on pre API version 11,
 * with doubale tap zoom and drag, no pinch zoom, and one for post API version 11 with pinch zoom  
 * @author oviroa
 *
 */
public class EventImageView extends RelativeLayout 
{

	//api version supported by device
	private int currentapiVersion;
	
	//detector for scale event
	private ScaleGestureDetector mScaleDetector;
	
	//detector for scroll and simple touch events
	private GestureDetector mMotionDetector;
	
	//scale factor
	private float mScaleFactor = 1.f;
	
	//state of image, if large, tap zoom brings image back to initial state
	private boolean imageIsLarge = false;
	
	//current position of image
	private float mPosX = 0;
    private float mPosY = 0;
    
    
    //original position of image
    private float oPosX = 0;
    private float oPosY = 0;
    
    //original image dimensions
    private int originalWidth;
    private int originalHeight;
    
    //original padding of image
    private int originalPaddingTop;
    private int originalPaddingBottom;
    private int originalPaddingLeft;
    private int originalPaddingRight;
    
    //state of tap :), set to true after first touch interaction
    private boolean isFirstTap = true;
	
    //layout params used to handle image view
    private RelativeLayout.LayoutParams params;
    
    
    
	/**
	 * Constructor
	 * @param context
	 * @param attributes
	 */
	public EventImageView(Context context, AttributeSet attributes) 
	{
		super(context, attributes);
		
		//set touch detectors
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		mMotionDetector = new GestureDetector(context, new GestureListener());
		currentapiVersion = android.os.Build.VERSION.SDK_INT;
		
	}

	private boolean isLoaded()
	{
		if(((ImageView)getChildAt(1)).getDrawable() != null)
			return true;
		else
			return false;
	}
	
	/**
     * Touch event
     */
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
	    //if Honeycomb or up, enable scaling
		if (currentapiVersion >= 11)
		{	
			try
			{
				mScaleDetector.onTouchEvent(ev);
			}
			catch(Exception e)
			{
				Log.w(Debug.TAG, "BUBU::EVENTIMAGEVIEW :: ONTOUCHEVENT EXCEPTION " + e.getMessage());
			}
			
		}
	    
		//simple touch enabled for all
		mMotionDetector.onTouchEvent(ev);
		
		if(isLoaded())
			((ProgressBar)getChildAt(0)).setVisibility(8);

	    invalidate();
	    
	    return true;
	}
	
	/**
	 * Gesture Listener class, used for simple touch (scroll, souble tap)
	 * @author oviroa
	 *
	 */
	private class GestureListener extends GestureDetector.SimpleOnGestureListener
	{
		
		/**
		 * Handles double tap zoom
		 */
		@Override
	    public boolean onDoubleTap (MotionEvent e)
	    {
			//image finished loadind
			if(isLoaded())
			{	
				
				//if image is not in zoom out mode, zoom out
				if(!imageIsLarge && mScaleFactor <= 2)
				{
					
					//create api apropriate zoom 
					
					if(currentapiVersion >= 11)
					{		
						//use api 11+ scaling
						((ImageView)getChildAt(1)).setScaleX(2);
						((ImageView)getChildAt(1)).setScaleY(2);
					}
					else
					{	
						//use layout params for scaling
						((ImageView)getChildAt(1)).setScaleType(ImageView.ScaleType.CENTER_CROP);
						
						params = new RelativeLayout.LayoutParams( ((ImageView)getChildAt(1)).getWidth()*2, ((ImageView)getChildAt(1)).getHeight()*2);
						((ImageView)getChildAt(1)).setLayoutParams(params);
					}
					
					//save scale factor states
					mScaleFactor = 2;
					imageIsLarge = true;
				}
				else //image is large, double tap brings it back to default
				{
					//reset to original size/position
					if(currentapiVersion >= 11)
					{	
						((ImageView)getChildAt(1)).setScaleX(1);
						((ImageView)getChildAt(1)).setScaleY(1);
						((ImageView)getChildAt(1)).setX(oPosX);
						((ImageView)getChildAt(1)).setY(oPosY);
					}
					else
					{	
						//create original layout settings
						params = new RelativeLayout.LayoutParams( originalWidth, originalHeight);
						//keep image centered
						//params.gravity = Gravity.CENTER;
						((ImageView)getChildAt(1)).setLayoutParams(params);
						//reset scaling
						((ImageView)getChildAt(1)).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
						//reset padding
						((ImageView)getChildAt(1)).setPadding(originalPaddingLeft,originalPaddingTop,originalPaddingRight,originalPaddingBottom);
						mPosX = 0;
						mPosY = 0;
					}
					
					//save scale factor states
					mScaleFactor = 1;
					imageIsLarge = false;
				}	
				
				
				
				invalidate();
				return true;
			}	
			else
				return false;
			
	    }
		
		/**
		 * Collects some info for state purposes after the first interaction
		 */
		@Override
		public boolean onDown(MotionEvent e)
		{
			if(isFirstTap)
			{
				if(currentapiVersion < 11)
				{
					//original size
					originalWidth = ((ImageView)getChildAt(1)).getWidth();
				    originalHeight = ((ImageView)getChildAt(1)).getHeight();
				    
				    //original padding
				    originalPaddingTop = ((ImageView)getChildAt(1)).getPaddingTop();
				    originalPaddingBottom = ((ImageView)getChildAt(1)).getPaddingBottom();
				    originalPaddingLeft = ((ImageView)getChildAt(1)).getPaddingLeft();
				    originalPaddingRight = ((ImageView)getChildAt(1)).getPaddingRight();
				    
				}
				else
				{
					//original position
					oPosX = ((ImageView)getChildAt(1)).getX();
					oPosY = ((ImageView)getChildAt(1)).getY();
				}	
				 
				//check!
			    isFirstTap = false;
			    
			    return true;
					
			}
			
			return false;
			
		}
		
		/**
		 * Event listener for scrolling, handles dragging the image around 
		 */
		@Override
		public boolean onScroll (MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			
			//image finished loading
			if(isLoaded())
			{
				//API post 11
	        	if(currentapiVersion >= 11)
				{
	        		//uses view positioning API
	        		((ImageView)getChildAt(1)).setX(((ImageView)getChildAt(1)).getX() - (int)distanceX);
	        		((ImageView)getChildAt(1)).setY(((ImageView)getChildAt(1)).getY() - (int)distanceY);
	        	}
	        	else if(imageIsLarge)//image does not move unless it is in zoomed in state
	        	{
	        		//ugly crappy hack
	        		//uses positive and negative padding to fake positioning
	        		//bleah
	        		
	        		mPosX = mPosX - distanceX;
		        	mPosY = mPosY - distanceY;
		        	
	        		((ImageView)getChildAt(1)).setPadding((int)mPosX,
	        			(int)mPosY,
	        			(int)(0-mPosX),
	        			(int)(0-mPosY));
	        	}	
	       
	        
	        	invalidate();
				
				
				return true;
	       }
	       else
	    	   return false;
		}
	}

	/**
	 * Scale listener class, used for pinch zoom handling. 
	 * Currently it is enabled for post API 11 only 
	 * @author oviroa
	 *
	 */
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener 
	{
	    @Override
	    public boolean onScale(ScaleGestureDetector detector) 
	    {
	    	//image has finished loading
	    	if(isLoaded())
	        {
	    		//retrieve scale
	    		mScaleFactor *= detector.getScaleFactor();
	    		// Don't let the object get too small or too large.
	    		mScaleFactor = Math.max(1f, Math.min(mScaleFactor, 5.0f));
	    		
	    		if (currentapiVersion >= 11)
	    		{
		    		//uses API 11+ view scaling API
	    			((ImageView)getChildAt(1)).setScaleX(mScaleFactor);
		        	((ImageView)getChildAt(1)).setScaleY(mScaleFactor);
	    		}
	    		//TODO 
	    		//work on pinch zoom issues for pre API 11
	    		/*
	    		else
	    		{
	    			
	    			params = new FrameLayout.LayoutParams
								( 
									(int)(originalWidth*mScaleFactor), 
									(int)(originalWidth*mScaleFactor)
								);
	    			
	    			//((ImageView)getChildAt(1)).setScaleType(ImageView.ScaleType.CENTER_CROP);
	    			//((ImageView)getChildAt(1)).setPadding(originalPaddingLeft,originalPaddingTop,originalPaddingRight,originalPaddingBottom);
	    			
	    			((ImageView)getChildAt(1)).setLayoutParams(params);
	    			
	    		}
	    		*/	
	        }	
	        
	        invalidate();
	        
	        
	        return true;
	    }
	    
	    
	}
    
	
}
