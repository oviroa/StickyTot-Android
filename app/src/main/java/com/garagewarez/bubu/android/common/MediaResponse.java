package com.garagewarez.bubu.android.common;

import java.util.List;

public class MediaResponse 
{
	private RestletResponse response;
	private List<String> imageIdList;
	
	/**
	 * 
	 * @param response
	 */
	public void setResponse( RestletResponse response)
	{
		this.response = response;
 	}
		
	/**
	 * 
	 * @return response
	 */
	public RestletResponse getResponse()
	{
		return response;
	}

	
	public void setImageIdList(List<String> imageIdList)
	{
		this.imageIdList = imageIdList;
	}
	
	public List<String> getImageIdList()
	{
		return this.imageIdList;
	}
}
