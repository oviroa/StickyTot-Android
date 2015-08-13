package com.garagewarez.bubu.android.utils;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.LinkedHashMap;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.Weeks;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.garagewarez.bubu.android.R;


/**
 * Constants and other static data
 * @author oviroa
 *
 */
public class Tools 
{
	
	private static LinkedHashMap<Integer, Integer> milestones;
	
	/**
	 * Milestones hash map
	 * @return
	 */
	public static LinkedHashMap<Integer, Integer> getMilestones()
	{
		if(milestones == null)
		{
			milestones = new LinkedHashMap<Integer, Integer>();
	        
	        milestones.put(2, 1);
	        milestones.put(4, 2);
	        milestones.put(6, 3);
	        milestones.put(9, 4);
	        milestones.put(12, 5);
	        milestones.put(18, 6);
	        milestones.put(24, 7);
	        milestones.put(36, 8);
	        milestones.put(48, 9);
	        milestones.put(60, 10);
		}
		
		return milestones;
				
	}
	
	/**
	 * Return closest lower age milestone
	 * @param ageM
	 * @return
	 */
	public static int getClosestMilestone(int ageM)
	{
		int closestAge = 0;
		
		for (int key : getMilestones().keySet())
		{
		    if(key <= ageM)
		    	closestAge = key;
		    else
		    	return closestAge;
		}
		
		return ageM;
	}
	
	/**
     * Calculate age
     * @param dob
     * @return
     */
    public static String getAgeStr(Date dob, Date doe, Context context)
    {
    	String as = new StringBuffer().append(context.getResources().getString(R.string.bbChildAge)).append(": ").toString();
    	
    	Months ageM = Months.monthsBetween(new DateTime(dob), new DateTime(doe));
    	
    	if(ageM.getMonths() == 0)
    	{
    		Weeks ageW = Weeks.weeksBetween(new DateTime(dob), new DateTime(doe));
    		
    		if (ageW.getWeeks() == 0)
    			as = new StringBuffer().append(as).append(context.getResources().getString(R.string.bbChildBrandNew)).toString();
    		else
    			as = new StringBuffer().append(as).append( ageW.getWeeks() ).append(context.getResources().getString(R.string.bbChildWeeks)).toString();
    	}	
    	else if(ageM.getMonths() < 24)
    	{
    		as = new StringBuffer().append(as).append( ageM.getMonths() ).append(context.getResources().getString(R.string.bbChildMonths)).toString();
    	}
    	else
    	{
    		int years = ageM.getMonths()/12;
    		int months = ageM.getMonths() - years*12;
    		
    		as = new StringBuffer().
    				append(as).
    				append(Integer.toString(years)).
    				append(context.getResources().getString(R.string.bbChildYears)).
    				append((months == 0) ? 
    						"" : 
    						new StringBuffer().
    							append(" , ").
    							append(Integer.toString(months)).
    							append(context.getResources().getString(R.string.bbChildMonths)).toString()).toString();
    	}	
    	
    	return as;
    }
    
    /**
     * Scale picture retrieved from URI so the smallest side is of maxsize
     * @param uri
     * @param maxSize
     * @param context
     * @return
     * @throws FileNotFoundException
     */
    public static Bitmap scalePic(Uri uri, int maxSize, Context context) throws FileNotFoundException
	{
		//Bitmap
		Bitmap btm = null;
		//orientation
		int orientation = 0;
		
		//bitmap creation options
		BitmapFactory.Options options = new BitmapFactory.Options();
		
		//retrieve picture orientation
		String[] columns = {MediaStore.Images.Media.ORIENTATION};
			
		//retrieve information about picture orientation
		Cursor cursor = context.getContentResolver().query(uri, columns, null, null, null);
		if (cursor != null) 
		{
			cursor.moveToFirst();
			
			int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
			orientation = cursor.getInt(orientationColumnIndex);
		}
		
		float desiredScale = 0;
		
		// Decode with inSampleSize
		options.inJustDecodeBounds = false;
		options.inDither = false;
		options.inSampleSize = 4;
		options.inScaled = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		btm = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
		
		//the source image's dimensions
		int srcWidth = options.outWidth;
		int srcHeight = options.outHeight;
		
		if (srcWidth < srcHeight)
		{	
			//only scale if the source is big enough. This code is just trying to fit a image into a certain width.
			if(maxSize > srcWidth)
				maxSize = srcWidth;
			
			// Calculate the correct inSampleSize/scale value. This helps reduce memory use. It should be a power of 2
			
			while(srcWidth / 2 > maxSize)
			{
			    srcWidth /= 2;
			    srcHeight /= 2;
			}
			
			desiredScale = (float) maxSize / srcWidth;
		}
		else
		{
			//only scale if the source is big enough. This code is just trying to fit a image into a certain width.
			if(maxSize > srcHeight)
				maxSize = srcHeight;
			
			// Calculate the correct inSampleSize/scale value. This helps reduce memory use. It should be a power of 2
			
			while(srcHeight / 2 > maxSize)
			{
			    srcHeight /= 2;
			    srcWidth /= 2;
			}
			
			desiredScale = (float) maxSize / srcHeight;
		}	
		
		// Resize
		Matrix matrix = new Matrix();
		matrix.postScale(desiredScale, desiredScale);
		
		if(orientation != 0)
			matrix.postRotate(orientation);
		
		btm = Bitmap.createBitmap(btm, 0, 0, btm.getWidth(), btm.getHeight(), matrix, true);			
		
		return btm;
	}
    
    /**
	 * Scale bitmap to have smallest edge at maxSize
	 * @param source
	 * @param maxSize
	 * @return
	 */
	static public Bitmap scaleBitmap(Bitmap source, int maxSize)
	{
		
		Bitmap btm;
		float desiredScale = 0;
		
		//the source image's dimensions
		int srcWidth = source.getWidth();
		int srcHeight = source.getHeight();
		
		if (srcWidth < srcHeight)
		{	
			//only scale if the source is big enough. This code is just trying to fit a image into a certain width.
			if(maxSize > srcWidth)
				maxSize = srcWidth;
			
			// Calculate the correct inSampleSize/scale value. This helps reduce memory use. It should be a power of 2
			
			while(srcWidth / 2 > maxSize)
			{
			    srcWidth /= 2;
			    srcHeight /= 2;
			}
			
			desiredScale = (float) maxSize / srcWidth;
		}
		else
		{
			//only scale if the source is big enough. This code is just trying to fit a image into a certain width.
			if(maxSize > srcHeight)
				maxSize = srcHeight;
			
			// Calculate the correct inSampleSize/scale value. This helps reduce memory use. It should be a power of 2
			
			while(srcHeight / 2 > maxSize)
			{
			    srcHeight /= 2;
			    srcWidth /= 2;
			}
			
			desiredScale = (float) maxSize / srcHeight;
		}	
		
		
		btm = Bitmap.createScaledBitmap(source, (int)(srcWidth*desiredScale), (int)(srcHeight*desiredScale), false);		
		
		return btm;
	}
	
	/**
	 * Scale bitmap
	 * @param uri
	 * @param maxSize
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static Bitmap scalePicUriSafe(Uri uri, int maxSize, Context context) throws FileNotFoundException
	{
		//Bitmap
		Bitmap btm = null;
		//orientation
		int orientation = 0;
		
		//bitmap creation options
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		
		//retrieve picture orientation
		String[] columns = {MediaStore.Images.Media.ORIENTATION};
			
		//retrieve information about picture orientation
		Cursor cursor = context.getContentResolver().query(uri, columns, null, null, null);
		if (cursor != null) 
		{
			cursor.moveToFirst();
			
			int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
			orientation = cursor.getInt(orientationColumnIndex);
		}
		
		
		//retrieve bitmap from URI
		btm = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
		
		
		
		//the source image's dimensions
		int srcWidth = options.outWidth;
		int srcHeight = options.outHeight;
		
		if(srcWidth == -1 || srcHeight == -1)
		{
			return null;
		}	
		
		
		int inSampleSize = 1;
		float desiredScale = 0;
		
		if (srcWidth < srcHeight)
		{	
			//only scale if the source is big enough. This code is just trying to fit a image into a certain width.
			if(maxSize > srcWidth)
				maxSize = srcWidth;
			
			// Calculate the correct inSampleSize/scale value. This helps reduce memory use. It should be a power of 2
			
			while(srcWidth / 2 > maxSize)
			{
			    srcWidth /= 2;
			    srcHeight /= 2;
			    inSampleSize *= 2;
			}
			
			desiredScale = (float) maxSize / srcWidth;
		}
		else
		{
			//only scale if the source is big enough. This code is just trying to fit a image into a certain width.
			if(maxSize > srcHeight)
				maxSize = srcHeight;
			
			// Calculate the correct inSampleSize/scale value. This helps reduce memory use. It should be a power of 2
			
			while(srcHeight / 2 > maxSize)
			{
			    srcHeight /= 2;
			    srcWidth /= 2;
			    inSampleSize *= 2;
			}
			
			desiredScale = (float) maxSize / srcHeight;
		}	
		
		// Decode with inSampleSize
		options.inJustDecodeBounds = false;
		options.inDither = false;
		options.inSampleSize = inSampleSize;
		options.inScaled = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		btm = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
		
		// Resize
		Matrix matrix = new Matrix();
		matrix.postScale(desiredScale, desiredScale);
		
		if(orientation != 0)
			matrix.postRotate(orientation);
		
		btm = Bitmap.createBitmap(btm, 0, 0, btm.getWidth(), btm.getHeight(), matrix, true);			
			
		return btm;
	}
}
