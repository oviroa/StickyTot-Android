package com.garagewarez.bubu.android.common;

import java.io.Serializable;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;

import com.garagewarez.bubu.android.R;
import com.garagewarez.bubu.android.utils.Validate;


/**
 * 
 * @author oviroa
 * Stores event data that is not indexable (no lists)
 * 
 */
public class EventData implements Serializable, Comparable<EventData>
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
		 * @param encodedKey
		 */
		public void setEncodedKey(String encodedKey)
		{
			this.encodedKey = encodedKey;
		}
			
		private String parentKey; 
		
		/**
		 * 
		 * @return parentKey
		 */
		public String getParentKey()
		{
			return this.parentKey;
		}
		
		/**
		 * 
		 * @param parentKey
		 */
		public void setParentKey(String parentKey)
		{
			this.parentKey = parentKey;
		}
		
		private String childKey; 
		
		/**
		 * 
		 * @return childKey
		 */
		public String getChildKey()
		{
			return this.childKey;
		}
		
		/**
		 * 
		 * @param parentKey
		 */
		public void setChildKey(String childKey)
		{
			this.childKey = childKey;
		}
		
		
		
		
		private static final long serialVersionUID = 1L;
		
		private long date;
		
		/**
		 * 
		 * @return Date when event happened
		 */
		public Date getDate()
		{
			return new Date(this.date);
		}
		
		/**
		 * 
		 * @param date Date when event happened
		 */
		public void setDate(Date date)
		{
			this.date = date.getTime();
		}
		
		
		
		private Measurement measurement;
		
		/**
		 * 
		 * @return Height and Weight at the time of the event
		 */
		public Measurement getMeasurement()
		{
			return measurement;
		}
		
		/**
		 * 
		 * @param measurement Height and Weight at the time of the event
		 */
		public void setMeasurement(Measurement measurement)
		{
			this.measurement = measurement;
		}
		
		
		private Pic pic;
		
		/**
		 * 
		 * @return Event picture
		 */
		public Pic getPic()
		{
			return pic;
		}
		
		/**
		 * 
		 * @param pic Event picture
		 */
		public void setPic(Pic pic)
		{
			this.pic = pic;
		}
		
		private String note;
		
		
		/**
		 * 
		 * @return Note of the event
		 */
		public String getNote()
		{
			return note;
		}
		
		/**
		 * 
		 * @param note Note of the event 
		 */
		public void setNote(String note)
		{
			this.note = note.trim();
		}
		
		private Percentile percentile;
		
		/**
		 * 
		 * @param percentile Percentile values for weight and height
		 */
		public void setPercentile(Percentile percentile)
		{
			this.percentile = percentile;
		}
		
		/**
		 * 
		 * @return Percentile values for weight and height
		 */
		public Percentile getPercentile()
		{
			return percentile;
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
		 * Tool used to sort Events in list by date
		 * @param n Event to compare by date
		 * @return Position of event in list
		 */
		public int compareTo(EventData n) 
		{
	        final Date date = new Date();
			
			int lastCmp = date.compareTo(new Date(n.date));
	        return lastCmp;
	    }
		
		/**
		 * Validates event data
		 * @param dob Child date of birth, used to validate Event date (event can only happen after child's birth date) 
		 * @return Validation status 
		 */
		public RestletResponse validate(Date dob, Context context)
		{
			StringBuffer sb = new StringBuffer();
			
			Boolean dateIsValid = Validate.dateIsValid(new Date(this.date));
			
			sb.append(dateIsValid ? "" :  context.getResources().getString(R.string.bbInvalidDate));
			
			if(dateIsValid)
			{
				sb.append(Validate.requiredTextIsValid(this.note) ? "" : context.getResources().getString(R.string.bbInvalidNote));
				sb.append(Validate.dateIsNotFuture(new Date(this.date)) ? "" :  context.getResources().getString(R.string.bbFutureDate));
				sb.append(Validate.date1AfterDate2(new Date(this.date), dob) ? "" : context.getResources().getString(R.string.bbDateBeforeBirth));				
			}	
			
			if(sb.toString().equals(""))
				return new RestletResponse(context.getResources().getString(R.string.bbOK));
			else
				return new RestletResponse(sb.toString());
		}
		
}
