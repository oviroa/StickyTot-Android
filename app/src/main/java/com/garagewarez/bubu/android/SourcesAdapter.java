package com.garagewarez.bubu.android;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Adapter for displaying custom view inside list row
 * @author oviroa
 *
 */
public class SourcesAdapter extends ArrayAdapter<CharSequence> 
{
	 
	    int resource;
	    String response;
	    Context context;
	    Typeface tf;
	    
	    //Initialize adapter
	    public SourcesAdapter(Context context, int resource, CharSequence[] items, Typeface tf) 
	    {
	        super(context, resource, items);
	        this.resource = resource;	 
	        this.context = context;
	        this.tf = tf;
	    }
	    
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent)
	    {
	        //Get the current Account object
	    	CharSequence ed = getItem(position);
	 
	        //Inflate the view
	        if (convertView == null) 
	        {
	            convertView = LayoutInflater.from(context).inflate(resource, null);
	        }
	        
	        //Get the text boxes from the account_list_item.xml file
	        TextView sourceField = (TextView) convertView.findViewById(R.id.bbSource);
	        
	        //Assign the appropriate data from our alert object above
	        sourceField.setText(ed);
	        sourceField.setTypeface(tf);
	       
	       
	        return convertView;
	    }
	    
	    
}
