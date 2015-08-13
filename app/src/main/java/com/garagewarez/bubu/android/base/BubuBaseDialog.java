package com.garagewarez.bubu.android.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.garagewarez.bubu.android.R;

/**
 * Base dialog class
 * Contains context menu setup methods
 * @author oviroa
 *
 */
public class BubuBaseDialog extends Dialog 
{
	
	protected Typeface tf;
	
	public BubuBaseDialog(Context context, int theme) 
	{
		super(context, theme);		
	}
	
	/**
	 * Hack to style the options menu
	 * @throws Exception
	 */
	protected void setMenuBackground() throws Exception
	{
	    getLayoutInflater().setFactory
	    (
	    new Factory() 
	    {

	        @Override
	        public View onCreateView(final String name, final Context context,
	                final AttributeSet attrs) 
	        {
	        	
	        	if (name.equalsIgnoreCase("com.android.internal.view.menu.ListMenuItemView")) 
	            {	
            		try 
	                { // Ask our inflater to create the view
	                    final LayoutInflater f = getLayoutInflater();
	                    final View[] view = new View[1];
	                    try 
	                    {
	                        view[0] = f.createView(name, null, attrs); 
	                    } 
	                    catch (InflateException e) 
	                    {
	                         hackAndroid23(name, attrs, f, view);
	                    }
	                    // Kind of apply our own background
	                    new Handler().post
	                    (
	                    new Runnable() 
	                    {
	                        public void run() 
	                        {
	                            try
	                            {
	                            	((TextView)((ViewGroup)((ViewGroup)view[0]).getChildAt(0)).getChildAt(0)).setTypeface(tf);
	                            	((TextView)((ViewGroup)((ViewGroup)view[0]).getChildAt(0)).getChildAt(0)).setTextColor(context.getResources().getColor(R.color.bbMainTextColor));
	                            }
	                            catch (Exception e) 
	        	                {
	        	                	//style stays as default
	        	                }
	                            
	                        }
	                    }
	                    );
	                    return view[0];
	                } 
	                catch (InflateException e) 
	                {
	                } 
	                catch (ClassNotFoundException e) 
	                {
	                }
	                
	            }
	            
	            return null;
	        }
	    }
	    );
	    
	    //getLayoutInflater().setFactory(null);
	}
	
	/**
	 * Extra menu layout hack for 2.3 (G'bread)
	 * @param name
	 * @param attrs
	 * @param f
	 * @param view
	 */
	static void hackAndroid23(final String name,  final android.util.AttributeSet attrs, final LayoutInflater f, final View[] view) 
	{
	    // mConstructorArgs[0] is only non-null during a running call to
	    // inflate()
	    // so we make a call to inflate() and inside that call our dully
	    // XmlPullParser get's called
	    // and inside that it will work to call
	    // "f.createView( name, null, attrs );"!
	    try {
	        	f.inflate
	        	(
	        	new XmlPullParser() 
	        	{
		            @Override
		            public int next() throws XmlPullParserException, IOException 
		            {
		                try 
		                {
		                   
		                	view[0] =  f.createView(name, null, attrs);
		                   
		                } 
		                catch (InflateException e) 
		                {
		                } 
		                catch (ClassNotFoundException e) 
		                {
		                }
		                throw new XmlPullParserException("exit");
		            }

					@Override
					public void defineEntityReplacementText(String entityName,
							String replacementText) throws XmlPullParserException 
					{
						// TODO Auto-generated method stub
						
					}

					@Override
					public int getAttributeCount() {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public String getAttributeName(int index) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getAttributeNamespace(int index) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getAttributePrefix(int index) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getAttributeType(int index) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getAttributeValue(int index) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getAttributeValue(String namespace, String name) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public int getColumnNumber() {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public int getDepth() {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public int getEventType() throws XmlPullParserException {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public boolean getFeature(String name) {
						// TODO Auto-generated method stub
						return false;
					}
	
					@Override
					public String getInputEncoding() {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public int getLineNumber() {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public String getName() {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getNamespace() {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getNamespace(String prefix) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public int getNamespaceCount(int depth)
							throws XmlPullParserException {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public String getNamespacePrefix(int pos)
							throws XmlPullParserException {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getNamespaceUri(int pos)
							throws XmlPullParserException {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getPositionDescription() {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getPrefix() {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public Object getProperty(String name) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public String getText() {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public char[] getTextCharacters(int[] holderForStartAndLength) {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public boolean isAttributeDefault(int index) {
						// TODO Auto-generated method stub
						return false;
					}
	
					@Override
					public boolean isEmptyElementTag()
							throws XmlPullParserException {
						// TODO Auto-generated method stub
						return false;
					}
	
					@Override
					public boolean isWhitespace() throws XmlPullParserException {
						// TODO Auto-generated method stub
						return false;
					}
	
					@Override
					public int nextTag() throws XmlPullParserException, IOException {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public String nextText() throws XmlPullParserException,
							IOException {
						// TODO Auto-generated method stub
						return null;
					}
	
					@Override
					public int nextToken() throws XmlPullParserException,
							IOException {
						// TODO Auto-generated method stub
						return 0;
					}
	
					@Override
					public void require(int type, String namespace, String name)
							throws XmlPullParserException, IOException {
						// TODO Auto-generated method stub
						
					}
	
					@Override
					public void setFeature(String name, boolean state)
							throws XmlPullParserException {
						// TODO Auto-generated method stub
						
					}
	
					@Override
					public void setInput(Reader in) throws XmlPullParserException {
						// TODO Auto-generated method stub
						
					}
	
					@Override
					public void setInput(InputStream inputStream,
							String inputEncoding) throws XmlPullParserException {
						// TODO Auto-generated method stub
						
					}
	
					@Override
					public void setProperty(String name, Object value)
							throws XmlPullParserException {
						// TODO Auto-generated method stub
						
					}
	        }, 
	        null, 
	        false
	        );
	    } 
	    catch (InflateException e1) 
	    {
	        // "exit" ignored
	    }
	}

}
