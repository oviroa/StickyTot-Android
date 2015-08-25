package com.garagewarez.bubu.android;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.garagewarez.bubu.android.base.BubuBaseActivity;
import com.garagewarez.bubu.android.utils.Tools;
import com.garagewarez.bubu.android.utils.Debug;
import com.koushikdutta.ion.Ion;


public class MilestonesActivity extends BubuBaseActivity implements SeekBar.OnSeekBarChangeListener
{
	//view pager slider
	private ViewPager milestonesViewPager;
	
	//seek bar for user pager progress and nav
	private SeekBar seekBarVP;
	
	//currently focused page
	private int focusedPage = 0;
	
	//true if seek bar touched
	private boolean seekTouched = false;
	
	//scrolling indicator arrows
	private ImageView indicatorLeft;
	private ImageView indicatorRight;
	
	private Spinner spinner;
			
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.milestones);
		
		//construct top bar with back button and title
        constructTopBar(R.id.homeButtonMilestones, R.id.textViewMilestones);
        
        //inflate view pager
        milestonesViewPager = (ViewPager)this.findViewById(R.id.bbMilestonesViewPager);
        
        milestonesViewPager.setOffscreenPageLimit(1);
        
        //milestones
        milestonesViewPager.setAdapter(new MilestonesAdapter
				(
					Tools.getMilestones(),
					MilestonesActivity.this
				));
        
        //populate list with child/event data passed via bundle
    	Bundle extras = getIntent().getExtras();
    	
    	//data to be received from event and displayed
    	int ageM = (int)extras.getInt(getResources().getString(R.string.bbChildAge)); 
    	String ageStr = (String)extras.getString(getResources().getString(R.string.bbChildAgeStr)); 
    	String name = (String)extras.getString(getResources().getString(R.string.bbChildName));
    	String thumbUrl = (String)extras.getString(getResources().getString(R.string.bbChildThumbUrl));
    	
    	//set pager to corresponding age
    	if(ageM == 0 || ageM == 1)
    		milestonesViewPager.setCurrentItem(0);
    	else if (ageM >= 60)
    		milestonesViewPager.setCurrentItem(Tools.getMilestones().size()-1);
    	else
    	{	
    		milestonesViewPager.setCurrentItem(Tools.getMilestones().get(Tools.getClosestMilestone(ageM))-1);
    	}	
    	
    	//content field on top
    	TextView nameView = (TextView)findViewById(R.id.childNameInEvent);
    	nameView.setText(new StringBuffer().append(name).append(" @ ").append(ageStr).toString()); 
    	nameView.setTypeface(tf, 1);
    	
    	//child image
    	ImageView thumb = (ImageView)findViewById(R.id.bbChildImage);
    	//add image
    	thumb.setBackgroundResource(R.drawable.rounded_corners_noimage);
    	
    	//icon
    	Ion.with(thumb).
				placeholder(R.drawable.transparent).
				load(thumbUrl);
    	
    	//seek bar
    	seekBarVP = (SeekBar)this.findViewById(R.id.bbSeekBarVP);
        
    	//kill thumb on seekbar
        try
        {
        	seekBarVP.setThumb(null);
        }
        catch(NullPointerException e){}
        
        //set events for seekbar taps
        seekBarVP.setOnSeekBarChangeListener(this);
    	seekBarVP.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_horizontal));
        seekBarVP.setMax(milestonesViewPager.getAdapter().getCount());
        
        milestonesViewPager.setOnPageChangeListener(new MilestonesViewPagerChangeListener()); 
        
        focusedPage = milestonesViewPager.getCurrentItem();
	    
        seekBarVP.setProgress(focusedPage+1);
        
        //indicator arrows
        indicatorLeft = (ImageView)findViewById(R.id.bbIndicatorLeft);
    	indicatorRight = (ImageView)findViewById(R.id.bbIndicatorRight);
    	
    	handleArrows(focusedPage);
    	
    	//spinner
    	spinner = (Spinner) findViewById(R.id.bbMilestonesSelector);
    	
    	//set adapter based on XML item
        ArrayAdapter<CharSequence> selectorAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, this.getResources().getTextArray(R.array.milestones_array)) 
        {

        	public View getView(int position, View convertView, ViewGroup parent) 
        	{
        		View v = super.getView(position, convertView, parent);

        		//set typeface for selected item
        		((TextView) v).setTypeface(getTypeface(),1);

        		return v;
        	}


        	public View getDropDownView(int position,  View convertView,  ViewGroup parent) 
        	{
        		View v =super.getDropDownView(position, convertView, parent);

        		//set typeface for list of options
        		((TextView) v).setTypeface(getTypeface(),1);

        		return v;
        	}
        
        };
    	
    	
    	
        selectorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	// Apply the adapter to the spinner
    	spinner.setAdapter(selectorAdapter);
    	//set
    	spinner.setSelection(focusedPage);
    	
    	//set spinner listener to change current selector page
    	spinner.setOnItemSelectedListener(new OnItemSelectedListener() 
    	{
    	    @Override
    	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) 
    	    {
    	    	 
    	    	milestonesViewPager.setCurrentItem(position);
    	    	
    	    }

    	    @Override
    	    public void onNothingSelected(AdapterView<?> parentView) 
    	    {
    	    	Log.w(Debug.TAG,"Nothing selected");
    	    }

    	});
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) 
	{
		if(seekTouched)
		{	
			milestonesViewPager.setCurrentItem(seekBar.getProgress() - 1);
			
			if(seekBar.getProgress()<1)
				seekBar.setProgress(1);
			
		}
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) 
	{
		seekTouched = true;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) 
	{
		seekTouched = false;
	}

	private class MilestonesViewPagerChangeListener extends ViewPager.SimpleOnPageChangeListener
	{
	    @Override
	    public void onPageSelected(int position) 
	    {
	       handleArrows(position);
	       focusedPage = position;
	       seekBarVP.setProgress(focusedPage+1);
	       spinner.setSelection(focusedPage);
	    }
 
	}  
	
	/**
	 * Handle visibility of legt/right indicator arrows
	 * @param position
	 */
	private void handleArrows(int position)
	{
	   
	   //one page scenario
	   if((milestonesViewPager.getAdapter().getCount()-1) == 0)
	   {	
		   indicatorRight.setVisibility(4);
		   indicatorLeft.setVisibility(4);
	   }   
	   else
	   {
		   //display/hide arrow indicators for scrolling
	       if(position==0 && indicatorLeft.getVisibility()==0)
		   {
			  indicatorLeft.setVisibility(4);
			  indicatorLeft.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out)); 	   
		   }
		   else if(position != 0 && indicatorLeft.getVisibility()==4)
		   {
			   indicatorLeft.setVisibility(0);
			   indicatorLeft.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
		   }
		   
		   if(position == milestonesViewPager.getAdapter().getCount()-1 && indicatorRight.getVisibility() == 0)
		   {
			   indicatorRight.setVisibility(4);
			   indicatorRight.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out));
		   } 
		   else if(indicatorRight.getVisibility() == 4)
		   {
			   if(milestonesViewPager.getAdapter().getCount() != position + 1)
			   {	   
				   indicatorRight.setVisibility(0);
				   indicatorRight.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
			   }	   
		   }
	   }   
	}

}
