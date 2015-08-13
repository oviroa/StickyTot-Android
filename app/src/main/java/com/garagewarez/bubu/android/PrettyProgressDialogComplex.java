package com.garagewarez.bubu.android;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.garagewarez.bubu.android.base.PrettyProgressDialog;


/**
 * Custom 2 tier (content + progress bar) progress dialog
 * @author oviroa
 *
 */
public class PrettyProgressDialogComplex extends PrettyProgressDialog 
{
	//context
	private Context context;
	//bar
	private ProgressBar progressBar;
	//text message
	private TextView textView;
	
	/**
	 * Constructir
	 * @param context
	 */
	public PrettyProgressDialogComplex(Context context) 
	{
		super(context);
		this.context = context;		
	}

	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	}
	
	
	/**
	 * Display dialog with custom content and style
	 * @param text
	 * @param tf
	 */
	public void show(String text, Typeface tf)
	{
		super.show();
		setCustomView();
		setTitleContent(text, tf);
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		@SuppressWarnings("deprecation")
		int width = display.getWidth();
		textView.setWidth((int) (width * .7));
	}
	
	/**
	 * Display simple
	 */
	@Override
	public void show()
	{
		super.show();
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		@SuppressWarnings("deprecation")
		int width = display.getWidth();
		textView.setWidth((int) (width * .7));
	}
	
	/**
	 * Uses custom xml layout for view, inflate for handling
	 */
	private void setCustomView()
	{
		//set view
		this.setContentView(R.layout.dialog_complex);
		progressBar = (ProgressBar)this.findViewById(R.id.bbProgressBarComplex);
		progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_horizontal));	
		
	}
	
	/**
	 * Sets content for title bar
	 * @param text
	 * @param tf
	 */
	private void setTitleContent(String text, Typeface tf)
	{
		textView = (TextView)this.findViewById(R.id.bbProgressBarTitleComplex);
		textView.setText(text);
		textView.setTypeface(tf);
		
		
	}
	
	/**
	 * Set text message
	 */
	@Override
	public void setMessage(String text)
	{
		textView = (TextView)this.findViewById(R.id.bbProgressBarTitleComplex);
		textView.setText(text);
	}
	
	/**
	 * Set progress for bar
	 */
	@Override
	public void setProgress(int progress)
	{
		progressBar.setProgress(progress);
		
	}
	
	/**
	 * Get progress from bar
	 */
	@Override
	public int getProgress()
	{
		return progressBar.getProgress();
	}
	
	
	/**
	 * Dispaly progress bar
	 */
	@Override
	public void showBar()
	{
		if(progressBar.getVisibility() != 0)
		{	
			progressBar.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));
			progressBar.setVisibility(0);
		}	
	}
	
	/**
	 * Hide progress bar
	 */
	@Override
	public void hideBar()
	{
		if(progressBar.getVisibility() != 8)
		{	
			progressBar.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out));
			progressBar.setVisibility(8);
		}	
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            // DO SOMETHING
        	this.cancel();
        }

        // Call super code so we dont limit default interaction
        super.onKeyDown(keyCode, event);

        return true;
    }
	
}
