package com.garagewarez.bubu.android;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.joda.time.DateTime;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;

import com.garagewarez.bubu.android.base.PrettyProgressDialog;
import com.garagewarez.bubu.android.common.ChildResponse;
import com.garagewarez.bubu.android.common.EventResponse;
import com.garagewarez.bubu.android.common.ParentData;


/**
 * Application singleton
 * @author oviroa
 *
 */


@ReportsCrashes(	
					formUri = "http://www.bugsense.com/api/acra?api_key=601c5690",
					//formKey="dHVmTUV6azFsYkkxZTBhLUJHMEo5U3c6MQ",
					formKey=""
			   )
				
public class BubuApp extends Application 
{
	
	
	@Override
    public void onCreate() 
	{
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        
        super.onCreate();
        
        //ImageLoader imageLoader = ImageLoader.getInstance();
    	
        //mageLoader.init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
    }
	
	private PrettyProgressDialog progressDialog;
	
	public void setProgressDialog(PrettyProgressDialog progressDialog)
	{
		this.progressDialog = progressDialog;
	}

	public PrettyProgressDialog getProgressDialog()
	{
		return progressDialog;
	}
	
	
	//current child data
	private ChildResponse childResponse = null;
	
	public ChildResponse getChildResponse()
	{
		return this.childResponse;
	}
	
	public void setChildResponse(ChildResponse childResponse)
	{
		this.childResponse = childResponse;
	}
	
	
	private Bitmap selectedImage;
	
	public Bitmap getSelectedImage()
	{
		return this.selectedImage;
	}
	
	public void setSelectedImage(Bitmap selectedImage)
	{
		this.selectedImage = selectedImage;
	}
	
	private Bitmap legacyImage;
	
	public Bitmap getLegacyImage()
	{
		return this.legacyImage;
	}
	
	public void setLegacyImage(Bitmap legacyImage)
	{
		this.legacyImage = legacyImage;
	}
	
	private Bitmap selectedImageLarge;
	
	public Bitmap getSelectedImageLarge()
	{
		return this.selectedImageLarge;
	}
	
	public void setSelectedImageLarge(Bitmap selectedImageLarge)
	{
		this.selectedImageLarge = selectedImageLarge;
	}
	
	private Uri selectedImageUri;
	
	public Uri getSelectedImageUri()
	{
		return this.selectedImageUri;
	}
	
	public void setSelectedImageUri(Uri selectedImageUri)
	{
		this.selectedImageUri = selectedImageUri;
	}	
	
	//current parent data
	private ParentData parentData = null;
	
	public ParentData getParentData()
	{
		return this.parentData;
	}
	
	public void setParentData(ParentData parentData)
	{
		this.parentData = parentData;
	}
	
	//current event data, contains all loaded event responses
	private HashMap<String, EventResponse> eventMap = null;
	
	public HashMap<String, EventResponse> getEventMap()
	{
		return this.eventMap;
	}
	
	public void setEventMap(HashMap<String, EventResponse> eventMap)
	{
		this.eventMap = eventMap;
	}
	
	//selected account name 
	private String accountName = null;
	
	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
	}
	
	public String getAccountName()
	{
		return this.accountName;
	}
	
	
	//account token used to authenticate with app engine server app
	private String accountToken = null;
	
	public void setAccountToken(String accountToken)
	{
		this.accountToken = accountToken;
	}
	
	public String getAccountToken()
	{
		return this.accountToken;
	}
	
	/*
	 * true if fresh kid stuff is available
	 */
	private Boolean kidListFreshStatus = false;
	
	public Boolean getKidListFreshStatus()
	{
		return this.kidListFreshStatus;
	}
	
	public void setKidListFreshStatus(Boolean kidListFreshStatus)
	{
		this.kidListFreshStatus = kidListFreshStatus;
	}
	
	/*
	 * true if fresh kid stuff is available
	 */
	private Boolean eventListFreshStatus = false;
	
	public Boolean getEventListFreshStatus()
	{
		return this.eventListFreshStatus;
	}
	
	public void setEventListFreshStatus(Boolean eventListFreshStatus)
	{
		this.eventListFreshStatus = eventListFreshStatus;
	}
	
	
	/**
	 * @param bitmapWasRecycled the bitmapWasRecycled to set
	 */
	public void setBitmapWasRecycled(Boolean bitmapWasRecycled) 
	{
		this.bitmapWasRecycled = bitmapWasRecycled;
	}

	/**
	 * @return the bitmapWasRecycled
	 */
	public Boolean getBitmapWasRecycled() 
	{
		return bitmapWasRecycled;
	}

	/**
	 * true if thumb bitmap was recycled
	 */
	
	private Boolean bitmapWasRecycled = false;
	
	/**
	 * first event in the current screen
	 */
	private int firstEventOnScreen;
	
	public void setFirstEventOnScreen(int firstEventOnScreen)
	{
		this.firstEventOnScreen = firstEventOnScreen;
	}
	
	public int getFirstEventOnScreen()
	{
		return this.firstEventOnScreen;
	}
	
	
	/**
	 * state of visibility for header in the Event Display view
	 */
	private boolean headerIsVisible = true;
	
	public void setHeaderVisibility(boolean headerIsVisible)
	{
		this.headerIsVisible = headerIsVisible;
	}
	
	public boolean getHeaderVisibility()
	{
		return this.headerIsVisible;
	}
	
	
	/**
	 * state of visibility for foter in the Event Display view
	 */
	private boolean footerIsVisible = false;
	
	public void setFooterVisibility(boolean footerIsVisible)
	{
		this.footerIsVisible = footerIsVisible;
	}
	
	public boolean getFooterVisibility()
	{
		return this.footerIsVisible;
	}
	
	
	//pre edit first visible event
	private int preEditFirstVisibleEvent = 0;
	
	/**
	 * @param preEditFirstVisibleEvent the preEditFirstVisibleEvent to set
	 */
	public void setPreEditFirstVisibleEvent(int preEditFirstVisibleEvent) 
	{
		this.preEditFirstVisibleEvent = preEditFirstVisibleEvent;
	}

	/**
	 * @return the preEditFirstVisibleEvent
	 */
	public int getPreEditFirstVisibleEvent() 
	{
		return preEditFirstVisibleEvent;
	}

	
	//pre edit interval
	private int preEditEventSelectorPage = 0;
	
	/**
	 * @param preEditEventSelectorPage the preEditEventSelectorPage to set
	 */
	public void setPreEditEventSelectorPage(int preEditEventSelectorPage) 
	{
		this.preEditEventSelectorPage = preEditEventSelectorPage;
	}

	/**
	 * @return the preEditEventSelectorPage
	 */
	public int getPreEditEventSelectorPage() 
	{
		return preEditEventSelectorPage;
	}

	//pre edit screen
	private int preEditEventViewPagerItem = 0;
	
	/**
	 * @param preEditEventViewPagerItem the preEditEventViewPagerItem to set
	 */
	public void setPreEditEventViewPagerItem(int preEditEventViewPagerItem) 
	{
		this.preEditEventViewPagerItem = preEditEventViewPagerItem;
	}

	/**
	 * @return the preEditEventViewPagerItem
	 */
	public int getPreEditEventViewPagerItem() {
		return preEditEventViewPagerItem;
	}

	/**
	 * @param hintWasShown the hintWasShown to set
	 */
	public void setHintWasShown(boolean hintWasShown) 
	{
		this.hintWasShown = hintWasShown;
	}

	/**
	 * @return the hintWasShown
	 */
	public boolean isHintWasShown() 
	{
		return hintWasShown;
	}

	private boolean hintWasShown;
	
	private boolean updateTextWasShown;
	
	/**
	 * @param updateTextWasShown the updateTextWasShown to set
	 */
	public void setUpdateTextWasShown(boolean updateTextWasShown) 
	{
		this.updateTextWasShown = updateTextWasShown;
	}

	/**
	 * @return the updateTextWasShown
	 */
	public boolean isUpdateTextWasShown() 
	{
		return updateTextWasShown;
	}
	
	private String inviteNotificationState;
	
	/**
	 * @param inviteNotificationState the inviteNotificationState to set
	 */
	public void setInviteNotificationState(String inviteNotificationState) 
	{
		this.inviteNotificationState = inviteNotificationState;
	}

	/**
	 * @return the inviteNotificationState
	 */
	public String getInviteNotificationState() 
	{
		return inviteNotificationState;
	}

	
	/**
	 * @param homeNotificationWasShown the homeNotificationWasShown to set
	 */
	public void setHomeNotificationWasShown(Boolean homeNotificationWasShown) 
	{
		this.homeNotificationWasShown = homeNotificationWasShown;
	}

	/**
	 * @return the homeNotificationWasShown
	 */
	public Boolean getHomeNotificationWasShown() 
	{
		return homeNotificationWasShown;
	}

	

	/**
	 * home notification was shown
	 */
	
	private Boolean homeNotificationWasShown = false;
	
	public WeakReference<Bitmap> wrSharedPictureBitmapLarge = null;
	public WeakReference<Bitmap> wrSharedPictureBitmap = null;
	
	
	/*
	 * Time when last warmup was initiated
	 */
	private DateTime warmupTime = null;
	
	/**
	 * @return the warmupTime
	 */
	public DateTime getWarmupTime() 
	{
		return warmupTime;
	}

	/**
	 * @param warmupTime the warmupTime to set
	 */
	public void setWarmupTime(DateTime warmupTime) 
	{
		this.warmupTime = warmupTime;
	}

	
}
