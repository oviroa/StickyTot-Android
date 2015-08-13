package com.garagewarez.bubu.android.common;


import java.io.Serializable;
import java.util.Date;

import android.graphics.Bitmap;
import android.content.Context;

import com.garagewarez.bubu.android.R;
import com.garagewarez.bubu.android.utils.Validate;



/**
 * Child data object, attached to/child of {@link Child} class
 * @author oviroa
 *
 */
public class ChildData implements Serializable 
{
	private String encodedKey;
	
	/**
	 * Retrieve encoded key
	 * @return String
	 */
	public String getEncodedKey()
	{
		return this.encodedKey;
	}
	
	/**
	 * Set encoded key
	 * @param encodedKey
	 */
	public void setEncodedKey(String encodedKey)
	{
		this.encodedKey = encodedKey;
	}
	
	private String parentKey; 
	
	/**
	 * Retrieve parent key
	 * @return key as {@link String}
	 */
	public String getParentKey()
	{
		return this.parentKey;
	}
	
	/**
	 * Set parent key
	 * @param parentKey
	 */
	public void setParentKey(String parentKey)
	{
		this.parentKey = parentKey;
	}
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	
	/**
	 * Name
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name.trim();
	}
	
	/**
	 * Get name
	 * @return Name
	 */
	public String getName()
	{
		return this.name;
	}
	
	private long dob;
	
	/**
	 * Set date of birth
	 * @param dob
	 */
	public void setDob(Date dob)
	{
		this.dob = dob.getTime();
	}
	
	/**
	 * Get date of birth
	 * @return dob
	 */
	public Date getDob()
	{
		return new Date(this.dob);
	}
	
	/**
	 * Constructor
	 */
	public ChildData()
	{
		
	}
	
	
	
	private Pic pic;
	
	/**
	 * Get Pic
	 * @return Picture object
	 */
	public Pic getPic()
	{
		return this.pic;
	}
	
	/**
	 * Set Pic object
	 * @param pic Picture object
	 */
	public void setPic(Pic pic)
	{
		this.pic = pic;
	}
	
	private String gender;
	
	/**
	 * Retrieve gender
	 * @return Gender
	 */
	public String getGender()
	{
		return this.gender;
	}
	
	/**
	 * Set gender
	 * @param gender Gender
	 */
	public void setGender(String gender)
	{
		this.gender = gender.trim();
	}
	
	/**
	 * Bitmap of image associated to kid;
	 */
	private Bitmap image;
	
	public void setImage(Bitmap image)
	{
		this.image = image;
	}
	
	public Bitmap getImage()
	{
		return this.image;
	}
	
	
	/**
	 * Validates Child data
	 * @return Result of validation
	 */
	public RestletResponse validate( ChildResponse cr, Context context ) 
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append(Validate.requiredStringIsValid(this.getName()) ? "" : context.getResources().getString(R.string.bbInvalidName));
		
		sb.append(Validate.nameIsUnique(this, cr) ? "" : context.getResources().getString(R.string.bbDuplicate)); 
		
		sb.append(Validate.requiredGenderIsValid(this.getGender())  ? "" : context.getResources().getString(R.string.bbInvalidGender)); 
		
		Boolean dateIsValid = Validate.dateIsValid(this.getDob());
		
		sb.append(dateIsValid ? "" : context.getResources().getString(R.string.bbInvalidDate) );
		
		if(dateIsValid)
			sb.append(Validate.dateIsNotFuture(this.getDob()) ? "" : context.getResources().getString(R.string.bbFutureDate)); 
				
		if(sb.toString().equals(""))
			return new RestletResponse( context.getResources().getString(R.string.bbOK));
		else
			return new RestletResponse(sb.toString());
	}
	
}

