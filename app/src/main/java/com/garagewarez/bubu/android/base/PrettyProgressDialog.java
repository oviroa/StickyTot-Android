package com.garagewarez.bubu.android.base;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import com.garagewarez.bubu.android.R;

/**
 * Custom progress dialog
 * @author oviroa
 *
 */
public class PrettyProgressDialog extends AlertDialog 
{
	Context context;
	
    /**
	 * Constructor
	 * @param context
	 */
	public PrettyProgressDialog(Context context) 
	{
		super(context);
		this.context = context;
		this.setCancelable(false);
		this.setCanceledOnTouchOutside(false);
	}
	
	public String state = "";
	
	/**
	 * Show dialog, set text and type font
	 * @param text
	 * @param tf
	 */
	public void show(String text, Typeface tf)
	{
		super.show();
		setCustomView();
		setTitleContent(text, tf);
	}
	
	
	/**
	 * Assign layout to view
	 */
	private void setCustomView()
	{
		//set view
		this.setContentView(R.layout.dialog_simple);
	}
	
	/**
	 * Apply title content (text, type font)
	 * @param title
	 */
	private void setTitleContent(String title, Typeface tf)
	{
		
		TextView titleView = (TextView)findViewById(R.id.bbProgressBarTitle);
		titleView.setTypeface(tf);
		titleView.setText(title); 
		
	}
	
	/**
	 * Set message
	 * @param text
	 */
	public void setMessage(String text)
	{
		
	}
	
	/**
	 * Set progress
	 * @param progress
	 */
	public void setProgress(int progress)
	{
		
	}
	
	/**
	 * Retrieve progress
	 * @return
	 */
	public int getProgress()
	{
		return 0;
	}
	
	/**
	 * Show bar
	 */
	public void showBar()
	{
		
	}
	
	/**
	 * Hide bar
	 */
	public void hideBar()
	{
			
	}

}
