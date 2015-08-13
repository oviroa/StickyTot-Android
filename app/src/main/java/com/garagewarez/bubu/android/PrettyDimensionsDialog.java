package com.garagewarez.bubu.android;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ContextMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.garagewarez.bubu.android.base.BubuBaseDialog;
import com.garagewarez.bubu.android.utils.Convertor;

/**
 * Custom dialog used to enter weight and height information for events
 * @author oviroa
 *
 */
public class PrettyDimensionsDialog extends BubuBaseDialog implements OnClickListener
{
	//context
	Context context;
	//kill button
	Button doneButton;
	
	//constructor
	public PrettyDimensionsDialog(Context context, int theme) 
	{
		super(context, theme);
		this.context = context;
		//show soft keyboard when text field has focus
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
        doneButton = (Button)findViewById(R.id.bbCloseDimensionsDialog);
        doneButton.setTypeface(tf,1);
        doneButton.setOnClickListener(this);
		
	}
	
	/**
	 * Set dimensions for event
	 * @param weight
	 * @param height
	 */
	public void setDimensions(Float weight, Float height)
	{
		//height
		((EditText) findViewById(R.id.bbEventHeightField)).setText(Convertor.trimTrailingZeros(height));
		
		//weight
		((EditText) findViewById(R.id.bbEventWeightField)).setText(Convertor.trimTrailingZeros(weight));
		
	}
	
	/**
	 * Retrieves entered height
	 * @return
	 */
	public Float getHeight()
	{
		try
		{
			return new Float(((EditText) findViewById(R.id.bbEventHeightField)).getText().toString());
		}
		catch(NumberFormatException e)
		{
			return (float) 0;
		}
	}
	
	/**
	 * Retrieves entered height
	 * @return
	 */
	public Float getWeight()
	{
		try
		{
			return new Float(((EditText) findViewById(R.id.bbEventWeightField)).getText().toString());
		}
		catch(NumberFormatException e)
		{
			return (float) 0;
		}
	}
	
	/**
	 * Apply xml view and custom fonts
	 */
	private void setCustomView()
	{
		//set view
		this.setContentView(R.layout.dimensions_dialog);
		
		//set font
		((EditText) findViewById(R.id.bbEventWeightField)).setTypeface(tf);
		((TextView) findViewById(R.id.bbEventWeight)).setTypeface(tf);
		((EditText) findViewById(R.id.bbEventHeightField)).setTypeface(tf);
		((TextView) findViewById(R.id.bbEventHeight)).setTypeface(tf);
		
		registerForContextMenu( ((EditText) findViewById(R.id.bbEventWeightField)));
		registerForContextMenu( ((EditText) findViewById(R.id.bbEventHeightField)));
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
	
	/**
    * create and style context menu
    */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo)
    {
    	
    	//if user long clicks on child name field, handle style of context menu
    	if(view.getId()==R.id.bbEventWeightField || view.getId()==R.id.bbEventHeightField)
           {
    		 //kill header
    		 menu.clearHeader();
    		 try
    		 {
    			 //style
    			 setMenuBackground();    			 
    		 }
    		 catch(Exception e)
    		 {
    			 
    		 }
           }
           else
              super.onCreateContextMenu(menu, view, menuInfo);
    }

}
