package com.garagewarez.bubu.android.utils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;

import android.content.Context;

import com.garagewarez.bubu.android.R;
import com.garagewarez.bubu.android.common.ChildData;
import com.garagewarez.bubu.android.common.ChildResponse;

/**
 * Validates user submitted data
 * @author oviroa
 *
 */
public class Validate 
{
	
	/**
	 * Checks if string is valid and not null
	 * @param rString
	 * @return valid state
	 */
	public static boolean requiredStringIsValid(String rString)
	{
		
		Pattern reqSPattern = Pattern.compile("((\\%3C)|<)[^\n]+((\\%3E)|>)");
	    Matcher reqSMatcher = reqSPattern.matcher(rString);
	    if (reqSMatcher.find() || rString.equals(""))
	    {
	    	return false;
	    }
		
		return true;
	}
	
	/**
	 * Checks is gender is valid and not null (boy or girl)
	 * @param rString
	 * @return valid state
	 */
	public static boolean requiredGenderIsValid(String rString)
	{
		return(rString.equals("boy") || rString.equals("girl") ? true : false);
	}
	
	
	/**
	 * Checks if multi-row text is valid, looks for XSS attack patterns  
	 * @param rString
	 * @return valid state
	 */
	public static boolean requiredTextIsValid(String rString)
	{
		
		 Pattern reqSPattern = Pattern.compile("((%3C)|<)[^\n]+((%3E)|>)");
		 Matcher reqSMatcher = reqSPattern.matcher(rString);
		 
		 return(rString != null && !rString.equals("") && !reqSMatcher.find());
	}
	
	
	/**
	 * Checks if date is valid
	 * @param mDate
	 * @return valid state
	 */
	public static boolean dateIsValid(Date mDate)
	{
		if (mDate == null)
		      return false;
		else
			return true;

	}
	
	/**
	 * Check if date is in the past or present
	 * @param mDate
	 * @return valid state
	 */
	 
	public static boolean dateIsNotFuture(Date mDate)
	{
		
		DateTime mDt = new DateTime(mDate);
		return (!mDt.isAfterNow());
		
	}
	
	
	
	public static boolean imageExtensionIsValid(String rString)
	{
		
		Pattern reqSPattern = Pattern.compile(".\\.(?:(jpg|gif|png|JPG|GIF|PNG|jpeg))");
	    Matcher reqSMatcher = reqSPattern.matcher(rString);
	    if (reqSMatcher.find() && rString !="")
	    {
	    	return true;
	    }
		
		return false;
	}
	
	
	public static boolean emailIsValid(String rString)
	{
		
		Pattern reqSPattern = Pattern.compile("^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$");
	    Matcher reqSMatcher = reqSPattern.matcher(rString);
	    if (reqSMatcher.find() && rString !="")
	    {
	    	return true;
	    }
		
		return false;
	}
	
	
	/**
	 * 
	 * @param typeStr
	 * @return
	 */
	public static boolean fileTypeIsValid(String typeStr, Context context)
	{
		List<String> typeList = Arrays.asList(context.getResources().getStringArray(R.array.bbImageTypes));
			
		return(typeList.contains(typeStr) ? true : false);		 
	}
	
	/**
	 * Chacks if date1 is after or on date2
	 * @param date1
	 * @param date2
	 * @return valid state
	 */
	public static boolean date1AfterDate2(Date date1, Date date2)
	{
		
		DateTime mDt1 = new DateTime(date1);
		DateTime mDt2 = new DateTime(date2);
	
		return (mDt1.isEqual(mDt2) || mDt1.isAfter(mDt2));
	}
	
	/**
	 * Checks if the child's name is unique
	 * @param child
	 * @param cr
	 * @return
	 */
	public static boolean nameIsUnique(ChildData child, ChildResponse cr)
	{
		Boolean isUnique = true;
		
		for(ChildData myC : cr.getList())
		{
			//go through native children, exclude children from joint user
			if(child.getName().equals(myC.getName()) && child.getParentKey().equals(myC.getParentKey()))
			{
				if(child.getEncodedKey() == null)
					isUnique = false;
				else if(!child.getEncodedKey().equals(myC.getEncodedKey()))
					isUnique = false;
			}	
		}
		
		return isUnique;
	}
}
