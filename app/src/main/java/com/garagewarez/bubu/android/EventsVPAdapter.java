package com.garagewarez.bubu.android;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.garagewarez.bubu.android.common.EventData;
import com.garagewarez.bubu.android.utils.Debug;
import com.garagewarez.bubu.android.utils.ScreenUtil;

public class EventsVPAdapter extends PagerAdapter
{

	
	int resource;
	int currentSelectorPage;
	int eventsPerPage;
    String response;
    Context context;
    Activity activity;
    List<EventData> items;
    int count = 1;
    int screenMultiple = 1;
    EventClickListener listener;
    ListView convertView;
    String parentKey;
    boolean selectedChildIsJoint;

	//Initialize adapter
    public EventsVPAdapter(Activity activity, List<EventData> items, final EventClickListener listener, final int currentSelectorPage, String parentKey, boolean selectedChildIsJoint)
    {
        super();
       	
        this.currentSelectorPage = currentSelectorPage;
        this.context = activity.getApplicationContext();
        this.items = items;
        this.activity = activity;
        this.count = context.getResources().getInteger(R.integer.bbEventsPerPage);
        this.listener = listener;
        this.parentKey = parentKey;
        count = count*ScreenUtil.getMultiplier(context);
        this.eventsPerPage = count * context.getResources().getInteger(R.integer.bbEventsPerInterval);
        this.selectedChildIsJoint = selectedChildIsJoint;
    }
    
    @Override
    public int getCount() 
    {
        return (int)(FloatMath.ceil((float)this.items.size()/(float)count));
    }


	/**
	 * Create the page for the given position.  The adapter is responsible
	 * for adding the view to the container given here, although it only
	 * must ensure this is done by the time it returns from
	 * {@link #finishUpdate()}.
	 *
	 * @param container The containing View in which the page will be shown.
	 * @param position The page position to be instantiated.
	 * @return Returns an Object representing the new page.  This does not
	 * need to be a View, but can be some other container of the page.
	 */
    @Override
    public Object instantiateItem(View collection, final int position) 
    {
    	
    	List<EventData> myItems = items.subList
        							(
        								position*count, 
        								(position+1)*count > items.size() ? items.size() : (position+1)*count
        							);
        
        convertView = new ListView(context);
        
        //set adapter
        int deviceOrientation = context.getResources().getConfiguration().orientation;
	      //if landscape, reduce rowcount by 1  
	       if(deviceOrientation == Configuration.ORIENTATION_LANDSCAPE)
	    	   convertView.setAdapter(new EventsAdapter(activity, R.layout.event_list_item_landscape, myItems, parentKey, selectedChildIsJoint));
	       else
	    	   convertView.setAdapter(new EventsAdapter(activity, R.layout.event_list_item, myItems, parentKey, selectedChildIsJoint));
	       
		//kill black background
		convertView.setCacheColorHint(Color.parseColor("#00000000"));
		//set background
		convertView.setSelector(R.drawable.list_bg_events);
		//style divider
		convertView.setDivider(context.getResources().getDrawable(R.drawable.divider_color));
		convertView.setDividerHeight((int) context.getResources().getDimension(R.dimen.bbDividerHeight));
		
		//remove last divider
		convertView.setFooterDividersEnabled(false);
		//assign context menu
		activity.registerForContextMenu(convertView);
		
		//add to view
        ((ViewPager) collection).addView(convertView,0);
        
        //track position that was clicked on
        convertView.setOnItemClickListener
        (
        		new OnItemClickListener() 
        		{
        		    public void onItemClick(AdapterView<?> parent, View view, int listPosition, long id) 
        		    {
        		    	//calculate position of clicked item in the context of the ViewPager
        		    	listener.click(position * count + listPosition + currentSelectorPage*eventsPerPage);
        		    }
        		  }
        
        );
        
        
        return convertView;
    }
    
    /**
	  * Click interface for listener
	  * @author oviroa
	  *
	  */
	 public static interface EventClickListener
	 {
			void click(int position);
	 }
    
/**
 * Remove a page for the given position.  The adapter is responsible
 * for removing the view from its container, although it only must ensure
 * this is done by the time it returns from {@link #finishUpdate()}.
 *
 * @param container The containing View from which the page will be removed.
 * @param position The page position to be removed.
 * @param object The same object that was returned by
 * {@link #instantiateItem(View, int)}.
 */
    @Override
    public void destroyItem(View collection, int position, Object view) 
    {
        
    	/*
    	ListView lv = (ListView)view;
    	
    	for(int i = 0; i< lv.getAdapter().getCount() ; i++)
    	{
    		
    		LinearLayout ll = (LinearLayout)lv.getChildAt(i);
    		if(ll != null)
    		{	
	    		RelativeLayout rl = (RelativeLayout)ll.getChildAt(0);
	    		WebImageView wiv = (WebImageView)rl.getChildAt(0);
	    		
	    		wiv.reset();
	    		
	    		ImageView iv = (ImageView)(wiv.getChildAt(1)); 
	    		
	    		
	    		BitmapDrawable bd = (BitmapDrawable)iv.getDrawable();
	    		if(bd != null)
	    			bd.getBitmap().recycle();
	    		
	    		iv.setImageDrawable(null);
    		}	
    	}	
    	
    	*/
    	//ImageLoader imageLoader = ImageLoader.getInstance();
    	//imageLoader.stop();
    	((ViewPager) collection).removeView((View) view);
           
    	
    	//System.gc();
           
    }

    
    
    @Override
    public boolean isViewFromObject(View view, Object object) 
    {
            return view==((View)object);
    }

    
/**
 * Called when the a change in the shown pages has been completed.  At this
 * point you must ensure that all of the pages have actually been added or
 * removed from the container as appropriate.
 * @param container The containing View which is displaying this adapter's
 * page views.
 */
    @Override
    public void finishUpdate(View view) 
    { 
    	//Log.w(Debug.TAG,""+view.getClass());
    }
    

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {Log.w(Debug.TAG,"RESTORED");}

    @Override
    public Parcelable saveState() 
    {
        return null;
    }

    @Override
    public void startUpdate(View view) {}
    
    public int getItemPosition(Object object) 
    {
        return POSITION_NONE;
    }

}
