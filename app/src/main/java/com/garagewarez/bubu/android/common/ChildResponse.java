package com.garagewarez.bubu.android.common;

import java.io.Serializable;
import java.util.List;


/**
 * 
 * @author oviroa
 * Container response for {@link RestletResponse} object and list of {@link ChildData} instances. Used to push/pull data to/from client.
 *
 */
public class ChildResponse implements Serializable 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3914324254988495332L;
	
	private RestletResponse response;
	private List<ChildData> list;
	
	/**
	 * Set {@link RestletResponse} object
	 * @param response
	 */
	public void setResponse( RestletResponse response)
	{
		this.response = response;
 	}
	
	/**
	 * Set list of {@link ChildData} objects
	 * @param list
	 */
	public void setList(List<ChildData> list)
	{
		this.list = list;
	}
	
	/**
	 * Get list of {@link ChildData} objects
	 * @return list
	 */
	public List<ChildData> getList()
	{
		return list;
	}
	
	/**
	 * Get {@link RestletResponse} object
	 * @return response
	 */
	public RestletResponse getResponse()
	{
		return response;
	}
}
