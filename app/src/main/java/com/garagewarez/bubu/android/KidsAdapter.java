package com.garagewarez.bubu.android;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.garagewarez.bubu.android.common.ChildData;
import com.garagewarez.bubu.android.utils.Tools;
import com.garagewarez.bubu.android.utils.UrlImageView;

/**
 * Creates custom view for data rows, populates with content from value objects
 *  
 * @author oviroa
 *
 */
public class KidsAdapter extends ArrayAdapter<ChildData> 
{
 
    int resource;
    String response;
    String parentKey;
    Context context;
    Typeface tf;
    Boolean isShared = false;
    
    //Initialize adapter
    public KidsAdapter(Context context, int resource, List<ChildData> items, String parentKey, Boolean isShared) 
    {
        super(context, resource, items);
        this.resource = resource;
        this.context = context;
        this.parentKey = parentKey;
        this.tf = Typeface.createFromAsset(context.getAssets(), "fonts/ExpletusSans-Regular.ttf");  
        this.isShared= isShared;
    }
    
    // static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder 
	{
		public ImageView childImage;
		public TextView childName;
		public TextView childDob;
		public TextView childGender;
		public TextView childAge;
	}
	
	@SuppressLint("NewApi")
	@Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
    	// ViewHolder will buffer the assess to the individual fields of the row
		// layout
    	final ViewHolder holder;
		// Recycle existing view if passed as parameter
		// This will save memory and time on Android
		// This only works if the base layout for all classes are the same
		View rowView = convertView;
    	
    	//Get the current Account object
        ChildData cd = getItem(position);
        
        //Inflate the view
        if (rowView == null) 
        {
        	rowView = LayoutInflater.from(context).inflate(resource, null);
        	holder = new ViewHolder();
        	
        	holder.childName =(TextView)rowView.findViewById(R.id.bbChildName);
			holder.childName.setTypeface(tf,1);
			
			holder.childDob =(TextView)rowView.findViewById(R.id.bbChildDob);
			holder.childDob.setTypeface(tf);
			
			holder.childGender =(TextView)rowView.findViewById(R.id.bbChildGender);
			holder.childGender.setTypeface(tf,1);
			
			holder.childAge =(TextView)rowView.findViewById(R.id.bbChildAge);
			holder.childAge.setTypeface(tf,1);
			
			holder.childImage = (ImageView)rowView.findViewById(R.id.bbChildImage);
			
			rowView.setTag(holder);
			
			
        }
        else 
        {
			holder = (ViewHolder) rowView.getTag();
		}
        
    	String imageUrl = cd.getPic().getThumb();
        
        //Assign the appropriate data from our alert object above
        //user's data
        holder.childName.setText(cd.getName());
        	
        
        
        holder.childDob.setText
        (
        		new StringBuffer().
        		append(context.getResources().getString(R.string.bbChildDOB)).
        		append(": ").        		
        		append(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).format(cd.getDob())).toString()
        );
        
        holder.childGender.setText(cd.getGender());
        
        String age = Tools.getAgeStr(cd.getDob(), new Date(), context);
       
        holder.childAge.setText
        (
        		new StringBuffer().
        		append(age).toString() 
        );
        
        if(cd.getIsJoint())
        {
        	
        	if(isShared)
        	{	
        		if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB)
        			rowView.setAlpha(.4f);
        		
        		rowView.setBackgroundResource(R.drawable.disabled_pink);
        	}	
        	else
        	{	
        		//joint user's color
            	rowView.setBackgroundResource(R.drawable.list_item_join);
            	if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB)
            		rowView.setAlpha(1f);
        	}	
        }
        else
        {
        	rowView.setBackgroundResource(R.drawable.list_selector);
        	if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB)
        		rowView.setAlpha(1f);
        }	
            
       if(imageUrl != null)
       {	
    	   holder.childImage.setBackgroundResource(R.drawable.transparent);
    	   UrlImageView.setBackgroundResource(R.drawable.rounded_corners_noimage);
    	   UrlImageView.setUrlDrawable
	   		(
	   			holder.childImage, 
	   			imageUrl
	   		);	  
    		  
    	}	  
    	
        return rowView;
    }
    
 
}