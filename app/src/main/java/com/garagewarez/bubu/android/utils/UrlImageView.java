package com.garagewarez.bubu.android.utils;

import java.io.File;

import android.content.Context;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class UrlImageView extends UrlImageViewHelper 
{
	/**
	 * Cleans stuff if it expired
	 * @param context
	 */
	public static void cleanup(Context context) 
	{
        if (mHasCleaned)
            return;
        mHasCleaned = true;
        try {
            // purge any *.urlimage files over a week old
            String[] files = context.getFilesDir().list();
            if (files == null)
                return;
            for (String file : files) {
                if (!file.endsWith(".urlimage"))
                    continue;

                File f = new File(context.getFilesDir() +"/"+file );
                if (System.currentTimeMillis() > f.lastModified() + CACHE_DURATION_ONE_WEEK)
                    f.delete();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

	/**
	 * Cleans everything on demand
	 * @param context
	 */
    public static void cleanupAll(Context context)
    {
         try {
             
        	 
        	 
        	 // purge any *.urlimage files
             String[] files = context.getFilesDir().list();
             
             if (files == null)
                 return;
             for (String file : files) 
             {
            	 
            	 if (!file.endsWith(".urlimage"))
                	 continue;

                 File f = new File(context.getFilesDir() +"/"+file );
                 f.delete();
             }
             
            
         }
         catch (Exception e) {
             e.printStackTrace();
         }
    }
}
