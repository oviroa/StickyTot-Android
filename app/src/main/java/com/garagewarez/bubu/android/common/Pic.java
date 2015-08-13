package com.garagewarez.bubu.android.common;

import java.io.Serializable;


/**
 * Holds paths to full size image/media and thumb, handles validation
 * @author oviroa
 *
 */
public class Pic implements Serializable 
{
	
	private static final long serialVersionUID = 1L;
	
	private String main;
	
	/**
	 * 
	 * @param main Path to main media
	 */
	public void setMain(String main)
	{
		this.main = main;
	}
	
	/**
	 * 
	 * @return Path to main media
	 */
	public String getMain()
	{
		return this.main.trim();
	}
	
	private String thumb;
	
	/**
	 * 
	 * @param thumb Path to thumb
	 */
	public void setThumb(String thumb)
	{
		this.thumb = thumb.trim();
	}
	
	/**
	 * 
	 * @return Path to thumb
	 */
	public String getThumb()
	{
		return this.thumb;
	}
	
}
