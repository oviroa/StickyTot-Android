package com.garagewarez.bubu.android.utils;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Utility class used to hold methods for screen size/orientation related tasks
 * @author oviroa
 *
 */
public class ScreenUtil 
{

	/**
	 * Returns number of events per screen based on device screen type and orientation
	 * @param context
	 * @return
	 */
	static public int getMultiplier(Context context)
	{
		
		
		//set multiple of count based on device size
        if ((context.getResources().getConfiguration().screenLayout & 
        	    Configuration.SCREENLAYOUT_SIZE_MASK) == 
        	        Configuration.SCREENLAYOUT_SIZE_LARGE) 
        {
        	    // on a large screen device ... (7" tablet)
        		
        	if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)        
        		return 3;
        }
        else if ((context.getResources().getConfiguration().screenLayout & 
        	    Configuration.SCREENLAYOUT_SIZE_MASK) == 
        	        Configuration.SCREENLAYOUT_SIZE_XLARGE) //tablet 10.1"
        {
        	if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)        
        		return  2;
        	else if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        		return 4;
        }
        
        return 1;
	}
	
	
	static public int getMilestoneCount(Context context)
	{
		//set multiple of count based on device size
        if ((context.getResources().getConfiguration().screenLayout & 
        	    Configuration.SCREENLAYOUT_SIZE_MASK) == 
        	        Configuration.SCREENLAYOUT_SIZE_LARGE) 
        {
        	    // on a large screen device ... (7" tablet)
        		
        	if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)        
        		return 5;
        	else if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)    
        		return 7;
        }
        else if ((context.getResources().getConfiguration().screenLayout & 
        	    Configuration.SCREENLAYOUT_SIZE_MASK) == 
        	        Configuration.SCREENLAYOUT_SIZE_XLARGE) //tablet 10.1"
        {
        	if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)        
        		return  13;
        	else if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        		return 9;
        }
        else
        {
        	if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)        
        		return  0;
        }
        
        return 3;
	}
	
}
