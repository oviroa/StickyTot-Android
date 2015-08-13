package com.garagewarez.bubu.android.base;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.garagewarez.bubu.android.R;

public class UpdateDialog extends BubuBaseDialog implements OnClickListener
{
	//context
	Context context;
	//kill button
	Button doneButton;
	
	//constructor
	public UpdateDialog(Context context, int theme) 
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
        doneButton = (Button)findViewById(R.id.bbCloseUpdateDialog);
        doneButton.setTypeface(tf,1);
        doneButton.setOnClickListener(this);
		
	}
	

	/**
	 * Apply xml view and custom fonts
	 */
	private void setCustomView()
	{
		//set view
		this.setContentView(R.layout.update_dialog);
		
		//title
		((TextView) findViewById(R.id.bbUpdateTitle)).setTypeface(tf,1);
		//text
		((TextView) findViewById(R.id.bbUpdateText)).setTypeface(tf,0);
		
		
	}

	/**
	 * Kill button click
	 */
	@Override
	public void onClick(View v) 
	{
		//hide soft key
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(doneButton.getApplicationWindowToken(), 0);
		//kill dialog
        this.cancel();
	}  
}
