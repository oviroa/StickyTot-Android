package com.garagewarez.bubu.android;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.garagewarez.bubu.android.common.EventData;
import com.garagewarez.bubu.android.utils.UrlImageView;

/**
 * Adapter for displaying custom view inside list row
 * @author oviroa
 *
 */
public class EventsAdapter extends ArrayAdapter<EventData> 
{
	 
	    int resource;
	    String response;
	    Context context;
	    Typeface tf;
	    String parentKey;
	    
	    //Initialize adapter
	    public EventsAdapter(Context context, int resource, List<EventData> items, String parentKey) 
	    {
	        super(context, resource, items);
	        this.resource=resource;	 
	        this.context = context;
	        this.parentKey = parentKey;
	    	this.tf = Typeface.createFromAsset(context.getAssets(), "fonts/ExpletusSans-Regular.ttf");
	    }
	    
	    // static to save the reference to the outer class and to avoid access to
		// any members of the containing class
		static class ViewHolder 
		{
			//public WebImageView eventImage;
			
			public ImageView eImage;
			
			public TextView eventNote;
			public TextView eventDate;
			public TextView weightPercentile;
			public TextView heightPercentile;
			
		}
	    
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent)
	    {
	    	final ViewHolder holder;
	    	View rowView = convertView;
	    	//Get the current Account object
	        EventData ed = getItem(position);
	    	
	    	if(rowView == null)
	    	{
	    		rowView = LayoutInflater.from(context).inflate(resource, null);
	    		holder = new ViewHolder();
	    		
	    		//Get the text boxes from the account_list_iten.xml file
		        holder.eventNote =(TextView)rowView.findViewById(R.id.bbEventNote);
		        holder.eventNote.setTypeface(tf);
		        	
		        holder.eventDate =(TextView)rowView.findViewById(R.id.bbEventDate);
		        holder.eventDate.setTypeface(tf,1);
		        
		       // holder.eventImage = (WebImageView)rowView.findViewById(R.id.bbEventImage);	
		        
		        holder.eImage = (ImageView)rowView.findViewById(R.id.bbEventImage);	
		        
		        
		        holder.weightPercentile =(TextView)rowView.findViewById(R.id.bbWeightPercentile);
		        holder.weightPercentile.setTypeface(tf);
		        
		        holder.heightPercentile =(TextView)rowView.findViewById(R.id.bbHeightPercentile);
		        holder.heightPercentile.setTypeface(tf);
		        
		        rowView.setTag(holder);
	    	}
	    	else 
	        {
	 			holder = (ViewHolder) rowView.getTag();
	 		}
	    	
	    	
	    	
	               
	        String imageUrl = ed.getPic().getThumb();
	        
	        
	        //Assign the appropriate data from our alert object above
	        holder.eventNote.setText(ed.getNote());
	        
	        DateFormat formatter = new SimpleDateFormat("MMM/dd/yyyy", Locale.US);
	        holder.eventDate.setText(formatter.format(ed.getDate()));
	        
	        //load image from url
	        if(imageUrl != null)
	        {	
	        	holder.eImage.setBackgroundResource(R.drawable.transparent);
	        	UrlImageView.setBackgroundResource(R.drawable.rounded_corners_noimage);
	        	UrlImageView.setUrlDrawable
        		(
        			holder.eImage, 
        			imageUrl
        		);
	        }	
	       
	        holder.heightPercentile.setText
	        (
	        		Html.fromHtml
	        		(
	        				new StringBuffer().
	                		append(context.getResources().getString(R.string.bbHeightPercentile)).
	                		append(": ").
	                		append("<b>").
	                		append(ed.getPercentile() != null ? Integer.toString(ed.getPercentile().getByHeight()) : context.getResources().getString(R.string.bbNA) ).
	                		append("</b>").
	                		toString()
	        		)
	        );
	        
	        holder.weightPercentile.setText
	        (
	        		Html.fromHtml
	        		(
	        				new StringBuffer().
	                		append(context.getResources().getString(R.string.bbWeightPercentile)).
	                		append(":  ").
	                		append("<b>").
	                		append(ed.getPercentile() != null ? Integer.toString(ed.getPercentile().getByWeight()) : context.getResources().getString(R.string.bbNA) ).
	                		append("</b>").
	                		toString()
	        		)
	        );
	        
	        if(!ed.getParentKey().equals(parentKey))
	        {
	        	//joint user's color
	        	rowView.setBackgroundResource(R.drawable.list_item_join);
	        }
	        else
	        {
	        	rowView.setBackgroundResource(R.drawable.list_selector);
	        }	
	        
	        return rowView;
	    }
	    
	    
}
