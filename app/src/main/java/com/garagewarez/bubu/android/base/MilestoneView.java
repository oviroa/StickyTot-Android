package com.garagewarez.bubu.android.base;

import android.content.Context;
import android.graphics.Typeface;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.garagewarez.bubu.android.R;
import com.koushikdutta.ion.Ion;

/**
 * Custom view, displays one clickable milestone (pic and date)
 * @author oviroa
 *
 */
public class MilestoneView extends ScrollView
{

	//view object of milestone
	private View view;
	
	
	/**
	 * Constructor, inflates layout and adds to view
	 * @param context
	 */
	public MilestoneView(Context context) 
	{
		super(context);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    view = inflater.inflate(R.layout.milestone, null);
	    addView(view);
	}
	
	
	 /**
	  * Initializes milestone view, sets image, type, date and click listener
	  * @param imageUrl
	  * @param dateStr
	  * @param tf
	  * @param listener
	  */
	 public void init(String imageUrl, String dateStr, Typeface tf, final MilestoneClickListener listener)
	 {
		 
		
	     //assign and load image   
		 ImageView image = (ImageView) view.findViewById(R.id.bbMilestoneImage);
		//add image to form 
		 image.setBackgroundResource(R.drawable.rounded_corners_noimage);

		 Ion.with(image).
				 placeholder(R.drawable.transparent).
				 load(imageUrl);
	  	   
		 
		 //set text (date)
		 TextView dateTxt = (TextView) view.findViewById(R.id.bbMilestoneDate);
		 dateTxt.setText(dateStr);
		 dateTxt.setTypeface(tf, 1);
		 
		 
		 //inflate button
		 ImageButton imageButton = (ImageButton) view.findViewById(R.id.bbMilestoneButton);
		 
		 //set listener to button
		 imageButton.setOnClickListener(new OnClickListener() 
    	 {
    	    public void onClick(View v) 
    	    {
    	    	listener.click();
    	    }
    	 });
    	 
    	 
	 }


	 /**
	  * Click interface for listener
	  * @author oviroa
	  *
	  */
	 public static interface MilestoneClickListener
	 {
			void click();
	 }



	

}
