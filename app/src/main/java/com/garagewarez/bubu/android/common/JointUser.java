package com.garagewarez.bubu.android.common;

import java.io.Serializable;

/**
 * Characteristics of user to be given access to view kids/events
 * @author oviroa
 *
 */
public class JointUser implements Serializable 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String email;
	
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) 
	{
		this.email = email;
	}


	/**
	 * @return the email
	 */
	public String getEmail() 
	{
		return email;
	}
	
	//Joint user id
	private String parentId;
	
	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(String parentId) 
	{
		this.parentId = parentId;
	}


	/**
	 * @return the parentId
	 */
	public String getParentId() 
	{
		return parentId;
	}


	/**
	 * @param isApproved the isApproved to set
	 */
	public void setIsApproved(Boolean isApproved) 
	{
		this.isApproved = isApproved;
	}


	/**
	 * @return the isApproved
	 */
	public Boolean getIsApproved() 
	{
		return isApproved;
	}

	
	//true if user accepts invite to view
	private Boolean isApproved = false;
	
	private RestletResponse response;
	
	/**
	 * 
	 * @param response Messaging
	 */
	public void setResponse(RestletResponse response)
	{
		this.response = response;
	}
	
	/**
	 * 
	 * @return Messaging
	 */
	public RestletResponse getResponse()
	{
		return this.response;
	}
	
}
