package com.garagewarez.bubu.android.common;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;



public class ParentData implements Serializable 
{
	
    private String encodedKey;
	
	/**
	 * 
	 * @return encodedKey
	 */
	public String getEncodedKey()
	{
		return this.encodedKey;
	}
	
	/**
	 * 
	 * @param encodedKey encodedKey
	 */
	public void setEncodedKey(String encodedKey)
	{
		this.encodedKey = encodedKey;
	}
	
	
	private static final long serialVersionUID = 1L;
	
	
	private String email;
	
	/**
	 * 
	 * @param email Parent email (unique ID)
	 */
	public void setEmail(String email)
	{
		this.email = email;
	}
	
	/**
	 * 
	 * @return Parent email (unique ID)
	 */
	public String getEmail()
	{
		return this.email;
	}
	
	
	
	private String nickName;
	
	/**
	 * 
	 * @param nickName Parent nick (unique ID)
	 */
	public void setNickName(String nickName)
	{
		this.nickName = nickName;
	}
	
	/**
	 * 
	 * @return Parent nick (unique ID)
	 */
	public String getNickName()
	{
		return this.nickName;
	}
	
	private RestletResponse response;
	
	/**
	 * 
	 * @param response Messaging
	 */
	public void setResponse(RestletResponse response)
	{
		this.response = response;
	}
	
	private JointUser sentUser = null;
    
    /**
	 * @param sentUser the sentUser to set
	 */
	public void setSentUser(JointUser sentUser) 
	{
		this.sentUser = sentUser;
	}

	/**
	 * @return the sentUser
	 */
	public JointUser getSentUser() 
	{
		return sentUser;
	}

	/**
	 * User that allowed this parent to view its info
	 */
	private JointUser receivedUser = null;
	
	/**
	 * @param receivedUser the receivedUser to set
	 */
	public void setReceivedUser(JointUser receivedUser) 
	{
		this.receivedUser = receivedUser;
	}

	/**
	 * @return the receivedUser
	 */
	public JointUser getReceivedUser() 
	{
		return receivedUser;
	}
	
	
	/**
	 * 
	 * @return Messaging
	 */
	public RestletResponse getResponse()
	{
		return this.response;
	}
	
	
	public void setTimestamp(Date timestamp) 
	{
		this.timestamp = timestamp;
	}

	public Date getTimestamp() 
	{
		return timestamp;
	}

	private Date timestamp = Calendar.getInstance().getTime();
}

