package com.garagewarez.bubu.android.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;

import android.util.Base64InputStream;
import android.util.Base64OutputStream;

/**
 * Helper class, used to serialize (end encode) classes to strings and deserialize them
 * @author oviroa
 *
 */
public class Serializer 
{
	/**
	 * Serialized serializable class to encoded string
	 * @param object
	 * @return
	 */
	public static String objectToString(Object object) 
	{
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    
	    try 
	    {
	        new ObjectOutputStream(out).writeObject(object);
	        byte[] data = out.toByteArray();
	        out.close();

	        out = new ByteArrayOutputStream();
	        Base64OutputStream b64 = new Base64OutputStream(out,0);
	        b64.write(data);
	        b64.close();
	        out.close();

	        return new String(out.toByteArray());
	    } 
	    catch (IOException e) 
	    {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	/**
	 * Deserializes serializable class from encoded string
	 * @param encodedObject
	 * @return
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws StreamCorruptedException 
	 * @throws OptionalDataException 
	 */
	@SuppressWarnings("resource")
	public static Object stringToObject(String encodedObject) throws OptionalDataException, StreamCorruptedException, ClassNotFoundException, IOException 
	{
	        return new ObjectInputStream(new Base64InputStream(
	                new ByteArrayInputStream(encodedObject.getBytes()),0)).readObject();	    
	    
	}
}
