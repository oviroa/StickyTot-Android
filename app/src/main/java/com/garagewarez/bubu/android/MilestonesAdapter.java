package com.garagewarez.bubu.android;

import java.util.LinkedHashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class MilestonesAdapter extends PagerAdapter 
{
	//milestones hash
	LinkedHashMap<Integer, Integer> milestones;
	
    int count = 1;
    
    //web view and progress bar
    WebView myWebView;
    
    //progress bar
    ProgressBar myPB;
    
    //context
    Context context;
    
    //Initialize adapter
    public MilestonesAdapter(LinkedHashMap<Integer, Integer> list, Context context)
    {
    	super();
    	milestones = list;
    	this.context = context;
    }
	
	@Override
	public int getCount() 
	{
		// TODO Auto-generated method stub
		return milestones.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) 
	{
        return (view==object);
	}
	
	@Override
	public Object instantiateItem(final ViewGroup collection, final int position) 
	{
            
		    //build view from layout
		    View myView = (View) LayoutInflater.from(collection.getContext()).inflate(R.layout.milestone_webview, null);
            
		    //create reference to webview, load, handle onLod
		    myWebView = (WebView) myView.findViewById(R.id.webview);
		    myPB = (ProgressBar) myView.findViewById(R.id.milestones_progress);
		    myWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		    
		    		
		    myWebView.setWebViewClient
		    (
		    		new WebViewClient()
		    		{
		    			@Override
		    			public void onPageStarted(WebView view, String url, Bitmap favicon)
		    			{
		    				//hide web view at start
		    				view.setVisibility(8);
		    			}	
		    			
		    			@Override
		    			public void onPageFinished(WebView view, String url)
		    			{
		    				//show when done
		    				view.setVisibility(0);
		    				view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in));
		    			}
		    			
		    			@Override
		    			public void onReceivedError (WebView view, int errorCode, String description, String failingUrl)
		    			{
		    				View mView = collection.getChildAt(position);
		    				
		    				//hide loader if error, show error page 
		    				if(mView != null)
		    				{	
		    					myPB = (ProgressBar) mView.findViewById(R.id.milestones_progress);
		    				}
		    				
		    				myPB.setVisibility(8);
		    				myPB.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out));
		    				
		    				super.onReceivedError(view, errorCode, description, failingUrl);
		    			}
		    			
		    			@Override
		    		    public boolean shouldOverrideUrlLoading(WebView view, String url) 
		    			{
		    				Intent i = new Intent(Intent.ACTION_VIEW);
		    				i.setData(Uri.parse(url));
		    				context.startActivity(i);
		    				return true;
		    		       
		    		    }
		    			
		    		}
		    );
		    
		    //load url
		    myWebView.loadUrl("https://www.stickytot.com/milestones/" + milestones.keySet().toArray()[position] + ".html");
		    //set backgorund
		    myWebView.setBackgroundColor(0xffdaeef0);
		    
		    collection.addView(myView,0);
            
            return myView;
    }

	@Override
    public void destroyItem(View collection, int position, Object view) 
    {
		((ViewPager) collection).removeView((View) view);
    }
	
	
}
