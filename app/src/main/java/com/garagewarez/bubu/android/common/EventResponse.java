package com.garagewarez.bubu.android.common;

import java.io.Serializable;
import java.util.List;


/**
 * 
 * @author oviroa
 * Container response for {@link RestletResponse} object and list of {@link EventData} instances. Used to push/pull data to/from client.
 */
public class EventResponse implements Serializable 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5009614583744822120L;
	private RestletResponse response;
	private List<EventData> list;
	
	/**
	 * Set response object
	 * @param response response
	 */
	public void setResponse( RestletResponse response)
	{
		this.response = response;
 	}
	
	/**
	 * Set list of {@link EventData} instances
	 * @param list list
	 */
	public void setList(List<EventData> list)
	{
		this.list = list;
	}
	
	/**
	 * 
	 * @return list
	 */
	public List<EventData> getList()
	{
		return list;
	}
	
	/**
	 * 
	 * @return response
	 */
	public RestletResponse getResponse()
	{
		return response;
	}
	
	/**
	 * @param eventCount the eventCount to set
	 */
	public void setEventCount(int eventCount) 
	{
		this.eventCount = eventCount;
	}

	/**
	 * @return the eventCount
	 */
	public int getEventCount() 
	{
		return eventCount;
	}

	private int eventCount;
}

