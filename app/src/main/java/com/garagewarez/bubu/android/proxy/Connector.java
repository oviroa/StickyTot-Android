package com.garagewarez.bubu.android.proxy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;


import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;




/**
 * HTTP connector, used to POST/GET data to/from web services
 * @author oviroa
 *
 */
final public class Connector 
{
	private static final String CONTENT_TYPE = "application/json;charset=UTF-8";
	private static final String CONTENT_TYPE_IMAGE = "image/jpeg";
	private static final String CONTENT_DISPOSITION_MULTIPART = "form-data; name=\"uploadedfile\";filename=\"camera.jpg\"";
	private static final String CONTENT_TYPE_FORM_DATA = "multipart/form-data;boundary=";
	private static final int BUFFER_SIZE = 32;
	
	/**
	 * Process HTTP request
	 * @param urlStr
	 * @param dataStr
	 * @param input
	 * @param output
	 * @param method
	 * @param cookie
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static String process
		(
			String urlStr, 
			String dataStr, 
			Boolean input, 
			Boolean output, 
			String method, 
			String cookie,
			int timeout
		) throws UnsupportedEncodingException, IOException, SocketTimeoutException
	{
		String response = null;
		
		URL url = new URL(urlStr);
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    
		if(conn!=null)
	    {
	 		conn.setConnectTimeout(timeout);
	 		conn.setUseCaches(false);
	 		conn.setRequestMethod(method);
	 		conn.setDoOutput(output);
	 		conn.setDoInput(input);
	 		conn.setReadTimeout(timeout);
	 		
	 		//enrich post object with authentication cookie
			conn.setRequestProperty("Cookie ",cookie);
			
		
			//write stuff
			if(output)
			{	
				conn.setRequestProperty("Content-Type", CONTENT_TYPE);
				OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
				out.write(dataStr);
				out.flush();
			}	
	     
			
			if(conn.getResponseCode()==HttpURLConnection.HTTP_OK)
			{
	    	 	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
	            String line = null;
	            
	            
	            StringBuffer sb = new StringBuffer();
	            
	            while ((line = br.readLine()) != null) 
	            {
	               sb.append(line);
	            }
	            
	            br.close();
	            
	            response = sb.toString();
			}
	     	 
			conn.disconnect();
	     
	    }
		
		return response;
	}
	
	/**
	 * Upload image
	 * @param urlStr
	 * @param image
	 * @param input
	 * @param output
	 * @param method
	 * @param cookie
	 * @param listener
	 * @param timeout
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static String processImage
		(
			String urlStr, 
			Bitmap image, 
			Boolean input, 
			Boolean output, 
			String method, 
			String cookie, 
			ProgressListener listener,
			int timeout
		) throws UnsupportedEncodingException, IOException
	{
		
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;		
		
		String response = null;
		
		URL url = new URL(urlStr);
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    
		
	 	
	    if(conn!=null)
	    {
	     
	 		conn.setConnectTimeout(timeout);
	 		conn.setUseCaches(false);
	 		conn.setRequestMethod(method);
	 		conn.setChunkedStreamingMode(0);
	 		conn.setDoOutput(output);
	 		conn.setDoInput(input);
	 		
	 		
	 		//enrich post object with authentication cookie
			conn.setRequestProperty("Cookie ",cookie);
			
			//compress bitmap
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		image.compress(CompressFormat.JPEG, 100, bos);
    		byte[] data = bos.toByteArray();
    		
    		//create image input stream
    		ByteArrayInputStream fileInputStream = new ByteArrayInputStream(data);
    		
    		//set content types
    		conn.setRequestProperty("Connection", "Keep-Alive");
    		conn.setRequestProperty("Content-Type", CONTENT_TYPE_FORM_DATA + boundary);
	
    		//set output stream, send image data
	        dos = new DataOutputStream( conn.getOutputStream() );
	        dos.writeBytes(twoHyphens + boundary + lineEnd);
	        dos.writeBytes("Content-Disposition: " + CONTENT_DISPOSITION_MULTIPART + lineEnd);
	        dos.writeBytes("Content-Type: " + CONTENT_TYPE_IMAGE  + lineEnd);
	        dos.writeBytes(lineEnd);
	
	        // create a buffer of maximum size
	        bytesAvailable = fileInputStream.available();
	        bufferSize = Math.min(bytesAvailable, BUFFER_SIZE);
	        buffer = new byte[bufferSize];
	
	        // read file and write it into form...
	        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	         
	        while (bytesRead > 0)
	        {
	          dos.write(buffer, 0, bufferSize);
	          bufferSize = Math.min(fileInputStream.available(), BUFFER_SIZE);
	          bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	          
	          //if listener return false, task was canceled, stop everything
	          if(!listener.transferred(bytesAvailable-fileInputStream.available(), bytesAvailable))
	        	  return null;
	          
	        }
	
	         // send multipart form data necesssary after file data...
	
	        dos.writeBytes(lineEnd);
	        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
    		
    		// close streams
    		fileInputStream.close();
    		dos.flush();
    		dos.close();
			
    		if(conn.getResponseCode()==HttpURLConnection.HTTP_OK)
			{
	    	 	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
	            String line = null;
	            
	            
	            StringBuffer sb = new StringBuffer();
	            
	            while ((line = br.readLine()) != null) 
	            {
	               sb.append(line);
	            }
	            
	            br.close();
	            
	            response = sb.toString();	   
	            
			}
	     	 
			conn.disconnect();
	    }
		
		return response;
	}
	
	/**
	 * Retrieve cookie from App engine
	 * @param urlStr
	 * @param timeout
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	public static String processCookie
	(
		String urlStr,
		int timeout
		
	) throws UnsupportedEncodingException, IOException, SocketTimeoutException
	{
	
	String cookieStr = null;
	
	URL url = new URL(urlStr);
	
	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    
 	if(conn!=null)
    {
     
 		conn.setConnectTimeout(timeout);
 		conn.setUseCaches(false);
 		conn.setRequestMethod("GET");
 		conn.setDoOutput(false);
 		conn.setDoInput(true);
 		conn.setReadTimeout(timeout);
 		conn.setInstanceFollowRedirects(false);
 		
 		if(conn.getResponseCode()==HttpURLConnection.HTTP_MOVED_TEMP)
		{
 			cookieStr = conn.getHeaderField("Set-Cookie");
		}
 		else
 		{
 			//
 		}	
 		
 		conn.disconnect();
     
    }
	
	return cookieStr;
}
	
	
	
	public static interface ProgressListener
	{
		boolean transferred(long num, long size);
	}
}
