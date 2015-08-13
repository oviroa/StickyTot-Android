package com.garagewarez.bubu.android.common;

import java.io.Serializable;


/**
 * Stores kids measurements (weight/height)
 * @author oviroa
 *
 */
public class Measurement implements Serializable 
{

	private static final long serialVersionUID = 1L;

	private float weight;
	
	/**
	 * 
	 * @return Child's weight
	 */
	public float getWeight()
	{
		return weight;
	}
	
	/**
	 * 
	 * @param weight Child's weight
	 */
	public void setWeight(float weight)
	{
		this.weight = weight;
	}
	
	
	
	private float height;
	
	/**
	 * 
	 * @return Child's height
	 */
	public float getHeight()
	{
		return height;
	}
	
	/**
	 *
	 * @param height  Child's height
	 */
	public void setHeight(float height)
	{
		this.height = height;
	}
	
	/**
	 * Constructor
	 */
	public Measurement()
	{
		
	}
}	
