package com.garagewarez.bubu.android;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.TextView;

import com.garagewarez.bubu.android.base.BubuBaseDialog;

/**
 * Custom dialog used to enter weight and height information for events
 * @author oviroa
 *
 */
public class PrettyRefreshDialog extends BubuBaseDialog implements OnClickListener
{
	//context
	Context context;
	//refresh button
	Button refreshButton;
	TextView refreshText;
	
	//constructor
	public PrettyRefreshDialog(Context context, int theme) 
	{
		super(context, theme);
		this.context = context;
		
		//remove visible window decoration
		this.getWindow().setBackgroundDrawableResource(R.drawable.transparent_padding);
	}
	
	/**
	 * Display styled dialog
	 * @param tf
	 */
	public void show(Typeface tf)
	{
		super.show();
		
		this.tf = tf;
		
		//set view
		setCustomView();
		
		//assign listener to click event for done button
		refreshButton = (Button)findViewById(R.id.bbCloseRefreshDialog);
		refreshButton.setTypeface(tf,1);
		refreshButton.setOnClickListener(this);
		
		refreshText = (TextView)findViewById(R.id.bbTextRefreshDialog);
		refreshText.setTypeface(tf);
		
		
	}
	
	
	/**
	 * Apply xml view and custom fonts
	 */
	private void setCustomView()
	{
		//set view
		this.setContentView(R.layout.refresh_dialog);		
		
	}

	/**
	 * Kill button click
	 */
	@Override
	public void onClick(View v) 
	{
		//kill dialog
        this.cancel();
	}

}
