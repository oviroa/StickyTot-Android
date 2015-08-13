package com.garagewarez.bubu.android.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.garagewarez.bubu.android.R;

public class CustomBuilder extends Builder
{
	private TextView message = null;
	private Button positiveButton = null;
	private Button negativeButton = null;
	
	private Typeface tf;

	private AlertDialog alert;
	
	public void setDialogInstance(AlertDialog alert)
	{
		this.alert = alert;
	}
	
	public CustomBuilder( Context context )
	{
		super( context );
		
		tf = Typeface.createFromAsset(context.getApplicationContext().getAssets(), "fonts/ExpletusSans-Regular.ttf");
		
		View customMessage = View.inflate( context, R.layout.alert_dialog_message, null );
		message = (TextView) customMessage.findViewById( R.id.message );
		message.setTypeface(tf, 1);
		
		positiveButton = (Button) customMessage.findViewById( R.id.button1 );
		positiveButton.setTypeface(tf, 1);
		
		negativeButton = (Button) customMessage.findViewById( R.id.button2 );
		negativeButton.setTypeface(tf,1);
		
		setInverseBackgroundForced(true);
		
		setView( customMessage );
	}

	
	@Override
	public CustomBuilder setPositiveButton(int textId, final DialogInterface.OnClickListener listener)
	{
		positiveButton.setText(textId);
		positiveButton.setOnClickListener(new android.view.View.OnClickListener() 
    	{
			public void onClick(View v) 
    	    {
    	    	alert.dismiss();
				listener.onClick(null,DialogInterface.BUTTON_POSITIVE);
    	    }
    	});
		return this;
	}
	
	@Override
	public CustomBuilder setPositiveButton(CharSequence text, final DialogInterface.OnClickListener listener)
	{
		positiveButton.setText(text);
		positiveButton.setOnClickListener(new android.view.View.OnClickListener() 
    	{
			public void onClick(View v) 
    	    {
    	    	alert.dismiss();
				listener.onClick(null,DialogInterface.BUTTON_POSITIVE);
    	    }
    	});
		
		return this;
	}
	
	
	
	@Override
	public CustomBuilder setNegativeButton(int textId, final DialogInterface.OnClickListener listener)
	{
		negativeButton.setText(textId);
		negativeButton.setOnClickListener(new android.view.View.OnClickListener() 
    	{
			public void onClick(View v) 
    	    {
    	    	alert.dismiss();
				listener.onClick(null,DialogInterface.BUTTON_NEGATIVE);
    	    }
    	});
		return this;
	}
	
	@Override
	public CustomBuilder setNegativeButton(CharSequence text, final DialogInterface.OnClickListener listener)
	{
		negativeButton.setText(text);
		negativeButton.setOnClickListener(new android.view.View.OnClickListener() 
    	{
			public void onClick(View v) 
    	    {
    	    	alert.dismiss();
				listener.onClick(null,DialogInterface.BUTTON_NEGATIVE);
    	    }
    	});
		return this;
	}
	

	@Override
	public CustomBuilder setMessage( int textResId )
	{
		message.setText( textResId );
		return this;
	}

	@Override
	public CustomBuilder setMessage( CharSequence text )
	{
		message.setText( text );
		return this;
	}

}
