package com.garagewarez.bubu.android.common;

import java.io.Serializable;


public class RestletResponse implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6341388885448402651L;
	
	private String message;
	
	/**
	 * 
	 * @return Message
	 */
	public String getMessage ()
	{
		return this.message;
	}
	
	/**
	 * 
	 * @param message Message
	 */
	public void setMessage (String message)
	{
		this.message = message;
	}

	/**
	 * Constructor
	 * @param message
	 */
	public RestletResponse ( String message )
	{
		super();
		this.message = message;
	}
	
	public RestletResponse ()
	{
		
	}
	
	private String errorType;
	
	public void setErrorType(String errorType)
	{
		this.errorType = errorType;
	}
	
	public String getErrorType()
	{
		return this.errorType;
	}
	
}

