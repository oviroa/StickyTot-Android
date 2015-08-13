package com.garagewarez.bubu.android.utils;

import java.text.DecimalFormat;

/**
 * Used to convert weigth and height to/from imperial
 * @author oviroa
 *
 */
public class Convertor 
{

	/**
	 * Convert weight to/from decimal/imperial
	 * @param weight
	 * @param imperial
	 * @return
	 */
	public static Float weight (Float weight, Boolean imperial)
	{
		
		if(imperial)
		{
			weight = (float) (weight * 2.20462262);
		}
		else
		{
			weight = (float) (weight * 0.45359237);
		}	
		
		
		return weight;
		
	}
	
	
	/**
	 * Convert height to/from imperial/decimal
	 * @param height
	 * @param imperial
	 * @return
	 */
	public static Float height (Float height, Boolean imperial)
	{
		if(imperial)
		{
			height = (float) (height * 0.393700787);
		}
		else
		{
			height = (float) (height * 2.54);
		}	
		
		return height;
		
	}
	
	/**
	 * Keep two decimal positions and remove excessive zeroes
	 * @param number
	 * @return
	 */
	public static String trimTrailingZeros(Float number) 
	{
	    String numberStr = number.toString();
		
		//no decimals, return as is
	    if(!numberStr.contains(".")) 
	    {
	        return numberStr;
	    }

	    //if decimals, keep 2
	    DecimalFormat twoDForm = new DecimalFormat("#.##");
		number =  Float.valueOf(twoDForm.format(number));
	    numberStr = number.toString();
		//remove zeroes and return
	    return numberStr.replaceAll("\\.0*$", "");
	}
	
}
