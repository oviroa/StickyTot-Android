package com.garagewarez.bubu.android.common;

import java.io.Serializable;


/**
 * Holds percentile values for weight/height
 * @author oviroa
 *
 */
public class Percentile implements Serializable 
{
	
	private static final long serialVersionUID = 1L;
	
	private int byWeight;
	
	/**
	 * 
	 * @return Percentile value by weight
	 */
	public int getByWeight()
	{
		return byWeight;
	}
	
	/**
	 * 
	 * @param byWeight Percentile value by weight
	 */
	public void setByWeight(int byWeight)
	{
		this.byWeight = byWeight;
	}
	
	private int byHeight;
	
	/**
	 * 
	 * @return Percentile value by height
	 */
	public int getByHeight()
	{
		return byHeight;
	}
	
	/**
	 * 
	 * @param byHeight Percentile value by height
	 */
	public void setByHeight(int byHeight)
	{
		this.byHeight = byHeight;
	}
}
