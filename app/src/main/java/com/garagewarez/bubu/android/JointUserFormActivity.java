/**
 * 
 */
package com.garagewarez.bubu.android;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.garagewarez.bubu.android.base.BubuBaseActivity;
import com.garagewarez.bubu.android.base.PrettyProgressDialog;
import com.garagewarez.bubu.android.common.ChildResponse;
import com.garagewarez.bubu.android.common.JointUser;
import com.garagewarez.bubu.android.common.ParentData;
import com.garagewarez.bubu.android.common.RestletResponse;
import com.garagewarez.bubu.android.proxy.Proxy;

/**
 * @author oviroa
 *
 */
public class JointUserFormActivity extends BubuBaseActivity 
{

	//email field
	private EditText email;
	
	//inviter email text field
	private TextView inviter;
	
	//notification bar
	private LinearLayout notificationLayout;
	
	/**
     * AsyncTask flavor, used for deletion of elements in the collection
     */
	protected RotationSafeAsyncTask jointUserTask;
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        //set main view	
    	setContentView(R.layout.joint_user_form);
    	
    	updateUI();

    	//top menu
    	constructTopBar(R.id.homeButtonJointUser, R.id.textViewJointUser);
    	
    	//store
    	jointUserTask =  (RotationSafeAsyncTask)getLastNonConfigurationInstance();

    	// Register as a receiver to listen for event/broadcasts
        IntentFilter filter = new IntentFilter(getResources().getString(R.string.bbUpdateEvent));
        registerReceiver(mReceiver, filter);
    	
        //handles running async task on device rotation or re-draws after other interruptions
        if (jointUserTask != null) 
        {
        	jointUserTask.attach(this);    	  
        }
        
        if(!getProxy().homeNotificationWasShown(getApplicationContext()))
        	getProxy().setHomeNotificationShown(getApplicationContext());
    }	

	@Override
	protected void updateUI()
	{
		//data proxy
		Proxy myProxy = Proxy.getInstance();
		
		//in case of removal or rejection, display notification bar
		
		TextView notificationText = (TextView)this.findViewById(R.id.bbJointUserNotification);
		notificationLayout = (LinearLayout)this.findViewById(R.id.bbJointUserNotificationLayout);
		ImageButton  notificationButton = (ImageButton)this.findViewById(R.id.bbJointUserNotificationButton);
		notificationButton.setOnClickListener(onClickHideNotification);
		
		if(((BubuApp)getApplicationContext()).getInviteNotificationState() != null)
		{
			//removal
			if(((BubuApp)getApplicationContext()).getInviteNotificationState().equals(getResources().getString(R.string.bbInviteRemovalMessage)))
			{
				notificationText.setText(getApplicationContext().getResources().getString(R.string.bbInviteRemovedText));
			}	
			else if(((BubuApp)getApplicationContext()).getInviteNotificationState().equals(getResources().getString(R.string.bbInviteRejectionMessage)))//rejection
			{
				notificationText.setText(getApplicationContext().getResources().getString(R.string.bbInviteRejectedText));
			}
			else if(((BubuApp)getApplicationContext()).getInviteNotificationState().equals(getResources().getString(R.string.bbInviteReceivedMessage)))//Invite received
			{
				notificationText.setText(getApplicationContext().getResources().getString(R.string.bbInviteReceivedText));
			}
			else if(((BubuApp)getApplicationContext()).getInviteNotificationState().equals(getResources().getString(R.string.bbInviteApprovedMessage)))//Invite Approved
			{
				notificationText.setText(getApplicationContext().getResources().getString(R.string.bbInviteApprovedText));
			}
			
			notificationText.setTypeface(getTypeface(), 1);
			
			notificationLayout.setVisibility(0);
			notificationLayout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
			
			((BubuApp)getApplicationContext()).setInviteNotificationState(null);
		}
		else
		{
			notificationLayout.setVisibility(8);
		}	
		
		
		
		
		//check if there is invite history
    	try 
    	{
			//get parent data from local storage
    		ParentData pd = myProxy.getStoredParentData(getApplicationContext());
			
    		if(pd != null)
    		{
    			
    		
	    		//no invite sent
				if(pd.getSentUser() == null)
				{
					//draw form
					setSentUIState(0);
				}	
				else//sent invite found 
				{
					//populate email field
	    	    	email = (EditText)this.findViewById(R.id.bbJointUserEmail);
	    	    	email.setText(pd.getSentUser().getEmail());
	    	    	email.setTypeface(getTypeface(), 0);
	    	    	
	    	    	//set ui elements (invite pending)
	    	    	setSentUIState(1);
	    	    	
	    	    	//set ui elements (invite approved)
	    	    	if (pd.getSentUser().getIsApproved())
	    	    		setSentUIState(2);
				}	
				
				//text field to display email of inviter user
				inviter = (TextView)this.findViewById(R.id.bbReceivedInviteEmail);
				
				//no invite
				if(pd.getReceivedUser() == null)
				{
					//hide everything in the inviter area
					setReceivedUIState(0);
				}	
				else
				{
					//set email field value for inviter
					inviter.setText(pd.getReceivedUser().getEmail());
					inviter.setTypeface(getTypeface(), 1);
	
					//display inviter area
					setReceivedUIState(1);
					
					//approved state
					if(pd.getReceivedUser().getIsApproved())
						setReceivedUIState(2);
				}
    		}
    		else
    		{
    			ErrorHandler.execute
				(
						this,
						"parent is null", 
       					"JointUserFormActivity 152: parent is null",
						null
				);
    		}	
		} 
    	catch (SocketTimeoutException e) 
		{
			ErrorHandler.execute
			(
					this,
					getApplicationContext().getResources().getString(R.string.bbProblem), 
   					"JointUserFormActivity 91: " + e.getMessage(),
					null
			);
		} 
		catch (IOException e) 
		{
			ErrorHandler.execute
			(
					this,
					getApplicationContext().getResources().getString(R.string.bbProblem), 
					"JointUserFormActivity 101: " + e.getMessage(),
					null
			);
		} 
		catch (ClassNotFoundException e) 
		{
			ErrorHandler.execute
			(
					this,
					getApplicationContext().getResources().getString(R.string.bbProblem), 
					"JointUserFormActivity 111: " + e.getMessage(),
					null
			);
		}
		
	}
	
	//listener for notification close button
	private OnClickListener onClickHideNotification = new OnClickListener() 
   	{
   	    public void onClick(View v) 
   	    {
   	    	notificationLayout.setVisibility(8);
   	    	((BubuApp)getApplicationContext()).setInviteNotificationState(null);
   	    }
   	};  
	
	/**
	 * Reloads async task on restart
	 */
	@Override
	public Object onRetainNonConfigurationInstance() 
    {
    	if(jointUserTask != null)
    		jointUserTask.detach();
    	return(jointUserTask);
	}
	
	/**
     * Hide dialog, clear state (memory)
     */
    public void killProgressDialog()
    {
    	if(progressDialog.isShowing())
    		progressDialog.dismiss();
    	
    	if(jointUserTask.isCancelled())
    	{	
    		((BubuApp)getApplicationContext()).setProgressDialog(null);
    	}
    	
    	jointUserTask = null;
    }
	
	
	//listener for "add joint user" button
	private OnClickListener onClickSend = new OnClickListener() 
   	{
   	    public void onClick(View v) 
   	    {
   	    	notificationLayout.setVisibility(8);
   	    	((BubuApp)getApplicationContext()).setInviteNotificationState(null);
   	    	
   	    	try
   	    	{
   	    		//validate and send email
   	    		handleEmail(email.getText().toString());
   	    	}
   	    	catch(SocketTimeoutException e)
   	    	{
   	    		ErrorHandler.execute
				(
						JointUserFormActivity.this,
						getApplicationContext().getResources().getString(R.string.bbProblem), 
       					"EventDetailActivity 114: " + e.getMessage(),
						null
				);
   	    	}
   	    	catch(NotFoundException e)
   	    	{
   	    		ErrorHandler.execute
				(
						JointUserFormActivity.this,
						getApplicationContext().getResources().getString(R.string.bbProblem), 
       					"EventDetailActivity 124: " + e.getMessage(),
						null
				);
   	    	}
   	    	catch(IOException e)
   	    	{
   	    		ErrorHandler.execute
				(
						JointUserFormActivity.this,
						getApplicationContext().getResources().getString(R.string.bbProblem), 
       					"EventDetailActivity 134: " + e.getMessage(),
						null
				);
   	    	}
   	    	catch(ClassNotFoundException e)
   	    	{
   	    		ErrorHandler.execute
				(
						JointUserFormActivity.this,
						getApplicationContext().getResources().getString(R.string.bbProblem), 
       					"EventDetailActivity 144: " + e.getMessage(),
						null
				);
   	    	}
   	    	catch(Exception e)
   	    	{
   	    		ErrorHandler.execute
				(
						JointUserFormActivity.this,
						getApplicationContext().getResources().getString(R.string.bbProblem), 
       					"EventDetailActivity 336: " + e.getMessage(),
						e
				);
   	    	}
   	    }
   	};   
   	 
   	//listener for "remove joint user" button
	private OnClickListener onClickRemove = new OnClickListener() 
   	{
   	    public void onClick(View v) 
   	    {
   	    	notificationLayout.setVisibility(8);
   	    	((BubuApp)getApplicationContext()).setInviteNotificationState(null);
   	    	
   	    	//instantiate and execute async task
   	    	//data proxy
   			jointUserTask = new RotationSafeAsyncTask(JointUserFormActivity.this);
   			jointUserTask.setTaskType(1);
   			jointUserTask.execute();
   	    	
   	    }
   	}; 
   	
   	//listener for "approve invite" button
	private OnClickListener onClickApprove = new OnClickListener() 
   	{
   	    public void onClick(View v) 
   	    {
   	    	notificationLayout.setVisibility(8);
   	    	((BubuApp)getApplicationContext()).setInviteNotificationState(null);
   	    	
   	    	//instantiate and execute async task
   	    	//data proxy
   			jointUserTask = new RotationSafeAsyncTask(JointUserFormActivity.this);
   			jointUserTask.setTaskType(2);
   			jointUserTask.execute();
   	    	
   	    }
   	};
   	
	//listener for "approve invite" button
	private OnClickListener onClickReject = new OnClickListener() 
   	{
   	    public void onClick(View v) 
   	    {
   	    	notificationLayout.setVisibility(8);
   	    	((BubuApp)getApplicationContext()).setInviteNotificationState(null);
   	    	
   	    	//instantiate and execute async task
   	    	//data proxy
   			jointUserTask = new RotationSafeAsyncTask(JointUserFormActivity.this);
   			jointUserTask.setTaskType(3);
   			jointUserTask.execute();
   	    	
   	    }
   	};
   	
   	/**
   	 * Handles email address input collection, sends if valid, brings up error view if not
   	 * @param emailString
   	 * @throws ClassNotFoundException 
   	 * @throws IOException 
   	 * @throws NotFoundException 
   	 * @throws SocketTimeoutException 
   	 */
   	private void handleEmail(String emailString) throws SocketTimeoutException, NotFoundException, IOException, ClassNotFoundException, Exception
   	{
   		//check if email is valid
   		Pattern pattern = Patterns.EMAIL_ADDRESS;
   		boolean emailIsValid = pattern.matcher(emailString).matches();
   		
   		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
   		imm.hideSoftInputFromWindow(email.getWindowToken(), 0);
   		
   		//non valid email, show
   		if(!emailIsValid)
   		{
   			ErrorHandler.execute
			(
					JointUserFormActivity.this, 
					getApplicationContext().getResources().getString(R.string.bbInvalidEmailShort), 
					null, 
					null
			);
   		}
   		else if (emailString.equals(getProxy().getStoredParentData(getApplicationContext()).getEmail()))
   		{
   			ErrorHandler.execute
			(
					JointUserFormActivity.this, 
					getApplicationContext().getResources().getString(R.string.bbInvalidEmailYours), 
					null, 
					null
			);
   		}
   		else//all good
   		{
   			//instantiate and execute async task
   			jointUserTask = new RotationSafeAsyncTask(JointUserFormActivity.this);
   			jointUserTask.setTaskType(0);
   			jointUserTask.execute(emailString);  
   			
   		}	
   	}
   	
   	
    /**
     * Initializes progress dialog with copy and custom font 
     */
    protected void createProgressDialog()
    {
		progressDialog = new PrettyProgressDialog(this);
		progressDialog.show(getResources().getString(R.string.bbLoading), getTypeface());
    }
    
    /**
     * Changes the state of the Received UI section depending on changes to data
     * @param stateId
     */
    public void setReceivedUIState(int stateId)
    {
    	
    	//separator
    	View separator = (View)this.findViewById(R.id.bbReceivedInvitesSeparator);
    	//info
    	TextView info = (TextView)this.findViewById(R.id.bbReceivedInviteText);
    	//buttons
    	Button approveButton = (Button)this.findViewById(R.id.bbApproveInviteButton);
    	Button rejectButton = (Button)this.findViewById(R.id.bbRejectInviteButton);
    	
    	switch(stateId)
    	{
    		//initial state, empty form
    		case 0:
    			separator.setVisibility(8);
    			info.setVisibility(8);
    			inviter.setVisibility(8);
    			approveButton.setVisibility(8);
    			rejectButton.setVisibility(8);
    			approveButton.setEnabled(true);
    		break;	
    		
    		//invite received, not approved, approve button active
    		case 1:
    			
    			separator.setVisibility(0);
    			
    			info.setVisibility(0);
    			info.setText(getResources().getString(R.string.bbReceivedInvitesText));
    			info.setTypeface(getTypeface(), 0);
    			
    			inviter.setVisibility(0);
    			
    			approveButton.setVisibility(0);
    			approveButton.setTypeface(getTypeface(), 1);
    			approveButton.setOnClickListener(onClickApprove);
    			
    			rejectButton.setVisibility(0);
    			rejectButton.setTypeface(getTypeface(), 1);
    			rejectButton.setOnClickListener(onClickReject);
    			
    			break;
    		
    			//invite approved, de-activate approve button
    		case 2:
    			
    			approveButton.setEnabled(false);
    			
    			
    			break;
    	}		
    			
    }
    
    /**
     * Changes the state of the Sent UI section depending on changes to data
     * @param stateId
     */
    public void setSentUIState(int stateId)
    {
    	
    	//email label
    	TextView label = (TextView)this.findViewById(R.id.bbJointUserEmailLabel);
    	//lead text
    	TextView lead = (TextView)this.findViewById(R.id.bbJointUserEmailLead);
    	//instruction text
    	TextView instructions = (TextView)this.findViewById(R.id.bbJointUserEmailText);
    	//joint user button inflate
    	Button jointUserButton = (Button)this.findViewById(R.id.bbAddJointUserButton);
    	
    	Drawable icon;
    	
    	switch(stateId)
    	{
    		//initial state, empty form
    		case 0:
    			//email label
    	    	label.setText(getResources().getString(R.string.bbJointUserEmailLabel));
    	    	label.setTypeface(getTypeface(), 1);
    	    	
    	    	//email field
    	    	email = (EditText)this.findViewById(R.id.bbJointUserEmail);
    	    	email.setText("");
    	    	email.setEnabled(true);
    	    	email.setTypeface(getTypeface(), 0);
    	    	
    	    	//lead text
    	    	lead.setText(getResources().getString(R.string.bbJointUserEmailLead));
    	    	lead.setTypeface(getTypeface(), 0);
    	    	
    	    	//instruction text
    	    	instructions.setText(getResources().getString(R.string.bbJointUserEmailText));
    	    	instructions.setTypeface(getTypeface(), 0);
    	    	
    	    	
    	    	//joint user button inflate
    	    	jointUserButton.setText(getResources().getString(R.string.bbAddJointUser));
    	    	jointUserButton.setTypeface(getTypeface(), 1);
    	    	icon = getResources().getDrawable( R.drawable.plus_icon );
    	    	jointUserButton.setCompoundDrawablesWithIntrinsicBounds( icon, null, null, null );
    	    	jointUserButton.setOnClickListener(onClickSend);
    	    	
    	    	//done listener
    	    	email.setOnEditorActionListener
    	    	(
    				new TextView.OnEditorActionListener() 
    				{
    					@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) 
    					{
    						notificationLayout.setVisibility(8);
    			   	    	((BubuApp)getApplicationContext()).setInviteNotificationState(null);
    			   	    	
    						if (actionId == EditorInfo.IME_ACTION_DONE) 
    						{
    							try 
    							{
    								handleEmail(email.getText().toString());
    								return true;
    							} 
    				   	    	catch(SocketTimeoutException e)
    				   	    	{
    				   	    		ErrorHandler.execute
    								(
    										JointUserFormActivity.this,
    										getApplicationContext().getResources().getString(R.string.bbProblem), 
    				       					"EventDetailActivity 114: " + e.getMessage(),
    										null
    								);
    				   	    	}
    				   	    	catch(NotFoundException e)
    				   	    	{
    				   	    		ErrorHandler.execute
    								(
    										JointUserFormActivity.this,
    										getApplicationContext().getResources().getString(R.string.bbProblem), 
    				       					"EventDetailActivity 124: " + e.getMessage(),
    										null
    								);
    				   	    	}
    				   	    	catch(IOException e)
    				   	    	{
    				   	    		ErrorHandler.execute
    								(
    										JointUserFormActivity.this,
    										getApplicationContext().getResources().getString(R.string.bbProblem), 
    				       					"EventDetailActivity 134: " + e.getMessage(),
    										null
    								);
    				   	    	}
    				   	    	catch(ClassNotFoundException e)
    				   	    	{
    				   	    		ErrorHandler.execute
    								(
    										JointUserFormActivity.this,
    										getApplicationContext().getResources().getString(R.string.bbProblem), 
    				       					"EventDetailActivity 144: " + e.getMessage(),
    										null
    								);
    				   	    	}
    							catch(Exception e)
    				   	    	{
    				   	    		ErrorHandler.execute
    								(
    										JointUserFormActivity.this,
    										getApplicationContext().getResources().getString(R.string.bbProblem), 
    				       					"EventDetailActivity 622: " + e.getMessage(),
    										e
    								);
    				   	    	}
    							
    						}
    						
    						return false;
    					}
    				}
    	    	);
    		break;
    		
    		//invite sent, awaiting approval
    		case 1:
    			//email label
    	    	label.setText(getResources().getString(R.string.bbJointUserEmailLabelPending));
    	    	label.setTypeface(getTypeface(), 1);
    			
    			//lead text
    	    	lead.setText(getResources().getString(R.string.bbJointUserEmailLeadPending));
    	    	lead.setTypeface(getTypeface(), 0);
    	    	
    	    	//disable input field
    	    	email.setEnabled(false);
    	    	
    	    	
    	    	//instruction text
    	    	instructions.setText(getResources().getString(R.string.bbJointUserEmailTextPending));
    	    	instructions.setTypeface(getTypeface(), 0);
    	    	
    	    	//joint user button inflate
    	    	jointUserButton.setText(getResources().getString(R.string.bbRemoveJointUser));
    	    	jointUserButton.setTypeface(getTypeface(), 1);
    	    	icon = getResources().getDrawable( R.drawable.minus_icon ); 
    	    	jointUserButton.setCompoundDrawablesWithIntrinsicBounds( icon, null, null, null );
    	    	jointUserButton.setOnClickListener(onClickRemove);
    	    	
        	break;
        		
        	
        	//invite sent and approved
    		case 2:
    			lead.setText(getResources().getString(R.string.bbJointUserEmailLabelApproved));
    			lead.setTypeface(getTypeface(), 0);
    	    	
    	    	instructions.setText(getResources().getString(R.string.bbJointUserEmailTextApproved));
    	    	instructions.setTypeface(getTypeface(), 0);
    	    	
    		break;	
    	}
    
    }
   	
   	
   	/**
   	 * Custom AsyncTask base class, used for registering joint user
   	 * @author oviroa
   	 *
   	 */
   	public class RotationSafeAsyncTask extends AsyncTask<String, Void, Object> 
   	{

   		//activity attached to the task
   		public JointUserFormActivity activity =  null;
   		private Exception carriedException = null;
   		
   		/**
   		 * Task type, 0 - invite, 1 - delete invite, 2 - accept invite, 3 - reject invite
		 * @param taskType the taskType to set
		 */
		public void setTaskType(int taskType) 
		{
			this.taskType = taskType;
		}

		/**
		 * @return the taskType
		 */
		public int getTaskType() 
		{
			return taskType;
		}
		
   		private int taskType;
   		
   		
   		/**
   		 * Constructor, attaches activity
   		 * @param activity
   		 */
   		public RotationSafeAsyncTask(JointUserFormActivity activity) 
   		{
   			attach(activity);
   		}
   		
   		/**
   		 * Clears activity
   		 */
   		public void detach() 
   		{
   			activity=null;
   		}

   		@Override
   		protected void onPreExecute()
   		{
   			//show dialog
   			activity.createProgressDialog();			
   		}
   		
   		@Override
   		protected Object doInBackground(String... params) 
   		{
   			//data proxy
	    	Proxy myProxy = Proxy.getInstance();
	    	RestletResponse rr = null;
	    	ChildResponse cr = null;
	    	
	    	switch(taskType)
   			{
   			//sends invitation	
   			case 0:
   					
   					JointUser response = null;
   		    		
   		    		//create and load invite instance 
   		    		JointUser invite = new JointUser();
   		    		invite.setEmail(params[0]);
   		    		invite.setIsApproved(false);
   		    		invite.setResponse(null);
   		    		
   		    		
   		    		try 
   		    		{
   		    			//send to server
   		    			response = myProxy.inviteJointUser(activity.getApplicationContext(), activity, invite);
   					} 
   		    		catch (ClientProtocolException e) 
   		    		{
   		    			carriedException = e;
   					} 
   		    		catch (IOException e) 
   		    		{
   		    			carriedException = e;
   					} 
   		    		catch (ClassNotFoundException e) 
   		    		{
   		    			carriedException = e;
   					}
   		    		
   		    		return response;
   				
   		    	//revokes sent invite
   				case 1:
   					
   					try 
   		    		{
   		    			//send to server
   		    			rr = myProxy.deleteJointUser(activity.getApplicationContext(), activity);
   					} 
   		    		catch (ClientProtocolException e) 
   		    		{
   		    			carriedException = e;
   					} 
   		    		catch (IOException e) 
   		    		{
   		    			carriedException = e;
   					} 
   		    		catch (ClassNotFoundException e) 
   		    		{
   		    			carriedException = e;
   					}
   					
   					return rr;
   				
   				//approve received invite
   				case 2:
   					
   					try 
   		    		{
   		    			//send to server
   		    			cr = myProxy.approveJointUserInvite(activity.getApplicationContext(), activity);
   					} 
   		    		catch (ClientProtocolException e) 
   		    		{
   		    			carriedException = e;
   					} 
   		    		catch (IOException e) 
   		    		{
   		    			carriedException = e;
   					} 
   		    		catch (ClassNotFoundException e) 
   		    		{
   		    			carriedException = e;
   					}
   					
   					return cr;
   					
   				//rejects received invite
   				case 3:
   					
   					try 
   		    		{
   		    			//send to server
   		    			cr = myProxy.rejectJointUserInvite(activity.getApplicationContext(), activity);
   					} 
   		    		catch (ClientProtocolException e) 
   		    		{
   		    			carriedException = e;
   					} 
   		    		catch (IOException e) 
   		    		{
   		    			carriedException = e;
   					} 
   		    		catch (ClassNotFoundException e) 
   		    		{
   		    			carriedException = e;
   					}
   		    		
   		    		return cr;
   			
   			}
			
   			return null;
   			
   			
   		}
   		
   		protected void onPostExecute (Object jr) 
	    {
   			//kill dialog
   			activity.killProgressDialog();
   			
   			Proxy myProxy = Proxy.getInstance();
   			
   			String rMessage;
   			
   			//response not null
			if(jr != null )
			{	
				switch(taskType)
				{
					//invite sent handles response
					case 0:
						
						if(((JointUser)jr).getResponse() != null)
						{
							rMessage = ((JointUser)jr).getResponse().getMessage();
							
							//ok message
							if(rMessage != null && rMessage.equals(activity.getResources().getString(R.string.bbOK)))
							{
								//store invite by updating parent data
								try 
								{
									//get parent from state
									ParentData pd = myProxy.getStoredParentData(activity.getApplicationContext());
									
									if(pd != null)
									{
										//populate with sent user
										pd.setSentUser((JointUser)jr);
										//store parent
										myProxy.storeParent(activity.getApplicationContext(), pd);
										
										//toast notification
										Toast.makeText
											(
												activity.getApplicationContext(), 
												getResources().getString(R.string.bbJointUserInvited), 
												Toast.LENGTH_SHORT
											).show();
										
										//change ui
										activity.setSentUIState(1);
									}
									else
									{
										ErrorHandler.execute
										(
												activity,
												"parent is null", 
					           					"JointUserFormActivity 788: parent is null",
												null
										);
									}	
								} 
								catch (SocketTimeoutException e) 
								{
									ErrorHandler.execute
									(
											activity,
											activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
				           					"JointUserFormActivity 439: " + rMessage,
											null
									);
								} 
								catch (IOException e) 
								{
									ErrorHandler.execute
									(
											activity,
											activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
				           					"JointUserFormActivity 449: " + rMessage,
											null
									);
								} 
								catch (ClassNotFoundException e) 
								{
									ErrorHandler.execute
									(
											activity,
											activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
				           					"JointUserFormActivity 460: " + rMessage,
											null
									);
								}
								
								
							}
							else
							{
								if(rMessage != null)
								{	
									if(rMessage.equals(activity.getResources().getString(R.string.bbInvalid)))
									{
										ErrorHandler.execute
										(
												activity,
												activity.getApplicationContext().getResources().getString(R.string.bbInviteInvalid), 
					           					"JointUserFormActivity 427: " + rMessage,
												null
										);
									}
									else if(rMessage.equals(activity.getResources().getString(R.string.bbRedundant)))
									{
										ErrorHandler.execute
										(
												activity,
												activity.getApplicationContext().getResources().getString(R.string.bbInviteRedundant), 
					           					"JointUserFormActivity 437: " + rMessage, 
												null
										);
									}	
									else
										ErrorHandler.execute
										(
												activity,
												activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
					           					"JointUserFormActivity 423: " + rMessage,
												null 
										);
											
								}
								else
									ErrorHandler.execute
									(
											activity,
											activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
				           					"JointUserFormActivity 423: " + rMessage,
											null
									);
							}
						}
						else
						{
							
							if(carriedException != null)
								ErrorHandler.execute
								(
										activity, 
										activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
										"EventDetailActivity 667: " + carriedException.getMessage(), 
										carriedException
								);
							else	
								ErrorHandler.execute
								(
										activity, 
										activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
										null, 
										null
								);
						}
						break;
					//invite revoked, handles response	
					case 1:
						rMessage = ((RestletResponse)jr).getMessage();
						
						//ok message
						if(rMessage != null && rMessage.equals(activity.getResources().getString(R.string.bbOK)))
						{
							
							//store invite by updating parent data
							try 
							{
								//get parent from state
								ParentData pd = myProxy.getStoredParentData(activity.getApplicationContext());
								
								if(pd != null)
								{
									//populate with sent user
									pd.setSentUser(null);
									//store parent
									myProxy.storeParent(activity.getApplicationContext(), pd);
									
									//toast notification
									Toast.makeText
										(
											activity.getApplicationContext(), 
											getResources().getString(R.string.bbJointUserRemoved), 
											Toast.LENGTH_SHORT
										).show();
									
									//change ui
									activity.setSentUIState(0);
								}
								else
								{
									ErrorHandler.execute
									(
											activity,
											"parent is null", 
				           					"JointUserFormActivity 932: parent is null",
											null
									);
								}	
							} 
							catch (SocketTimeoutException e) 
							{
								ErrorHandler.execute
								(
										activity,
										activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
			           					"JointUserFormActivity 739: " + rMessage,
										null
								);
							} 
							catch (IOException e) 
							{
								ErrorHandler.execute
								(
										activity,
										activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
			           					"JointUserFormActivity 749: " + rMessage,
										null
								);
							} 
							catch (ClassNotFoundException e) 
							{
								ErrorHandler.execute
								(
										activity,
										activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
			           					"JointUserFormActivity 760: " + rMessage,
										null
								);
							}
						}
						else
						{
							ErrorHandler.execute
							(
									activity,
									activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
		           					"JointUserFormActivity 812: " + rMessage,
									null
							);
							
						}	
						
						break;
					//invite approval sent, handles response	
					case 2:
						rMessage = ((ChildResponse)jr).getResponse().getMessage();
						
						//ok message
						if(rMessage != null && rMessage.equals(activity.getResources().getString(R.string.bbOK)))
						{
							
							//store invite by updating parent data
							try 
							{
								//get parent from state
								ParentData pd = myProxy.getStoredParentData(activity.getApplicationContext());
								//populate with sent user
								pd.getReceivedUser().setIsApproved(true);
								//store parent
								myProxy.storeParent(activity.getApplicationContext(), pd);
								
								//store child list
								myProxy.storeKids(activity.getApplicationContext(), (ChildResponse)jr);
								
								((BubuApp)activity.getApplicationContext()).setKidListFreshStatus(true);
								
								//toast notification
								Toast.makeText
									(
										activity.getApplicationContext(), 
										getResources().getString(R.string.bbJointUserInviteApproved), 
										Toast.LENGTH_SHORT
									).show();
								
								//change ui
								activity.setReceivedUIState(2);
							} 
							catch (SocketTimeoutException e) 
							{
								ErrorHandler.execute
								(
										activity,
										activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
			           					"JointUserFormActivity 739: " + rMessage,
										null
								);
							} 
							catch (IOException e) 
							{
								ErrorHandler.execute
								(
										activity,
										activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
			           					"JointUserFormActivity 749: " + rMessage,
										null
								);
							} 
							catch (ClassNotFoundException e) 
							{
								ErrorHandler.execute
								(
										activity,
										activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
			           					"JointUserFormActivity 760: " + rMessage,
										null
								);
							}
						}
						else
						{
							ErrorHandler.execute
							(
									activity,
									activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
		           					"JointUserFormActivity 812: " + rMessage,
									null
							);
							
						}
	   					break;
	   				
	   				//invite rejection sent, handles response
	   				case 3:
	   					rMessage = ((ChildResponse)jr).getResponse().getMessage();
						
						//ok message
						if(rMessage != null && rMessage.equals(activity.getResources().getString(R.string.bbOK)))
						{
							
							//store invite by updating parent data
							try 
							{
								//get parent from state
								ParentData pd = myProxy.getStoredParentData(activity.getApplicationContext());
								//populate with sent user
								pd.setReceivedUser(null);
								//store parent
								myProxy.storeParent(activity.getApplicationContext(), pd);
								
								//store child list
								myProxy.storeKids(activity.getApplicationContext(), (ChildResponse)jr);
								
								((BubuApp)activity.getApplicationContext()).setKidListFreshStatus(true);
								
								//toast notification
								Toast.makeText
									(
										activity.getApplicationContext(), 
										getResources().getString(R.string.bbJointUserInviteRejected), 
										Toast.LENGTH_SHORT
									).show();
								
								//change ui
								activity.setReceivedUIState(0);
							} 
							catch (SocketTimeoutException e) 
							{
								ErrorHandler.execute
								(
										activity,
										activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
			           					"JointUserFormActivity 739: " + rMessage,
										null
								);
							} 
							catch (IOException e) 
							{
								ErrorHandler.execute
								(
										activity,
										activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
			           					"JointUserFormActivity 749: " + rMessage,
										null
								);
							} 
							catch (ClassNotFoundException e) 
							{
								ErrorHandler.execute
								(
										activity,
										activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
			           					"JointUserFormActivity 760: " + rMessage,
										null
								);
							}
						}
						else
						{
							ErrorHandler.execute
							(
									activity,
									activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
		           					"JointUserFormActivity 812: " + rMessage,
									null
							);
							
						}
	   					break;	
				}
				
				
			}
			else
			{
				
				if(carriedException != null)
					ErrorHandler.execute
					(
							activity, 
							activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
							"EventDetailActivity 667: " + carriedException.getMessage(), 
							carriedException
					);
				else	
					ErrorHandler.execute
					(
							activity, 
							activity.getApplicationContext().getResources().getString(R.string.bbProblem), 
							null, 
							null
					);
			}	
			
			//detach activity from async task
			detach(); 
			
	    }

   		/**
   		 * Attaches activity to task
   		 * @param activity
   		 */
   		public void attach(JointUserFormActivity activity) 
   		{
   			this.activity=activity;		
   		}

   		
   	}
   	
  //activity is stopped, kill dialogs
	@Override
    protected void onStop() 
    {
        super.onStop();
        
        if(progressDialog != null)
        	progressDialog.dismiss();
        
    } 
	
	
   	@Override
	protected void onStart()
	{
		super.onStart();
		//update progress dialog state on rotation
        handleProgressDialogOnRotation();
	}
    
   	/**
	 * Handle progressDialog state and progress to prepare for device rotation
	 */
	protected void handleProgressDialogOnRotation()
	{
		
		//if progress dialog is not instantiated, create one and make it invisible 
    	if(jointUserTask != null && !jointUserTask.isCancelled())
    	{	
    		createProgressDialog();
    	}	
	}
	
	@Override
	protected void broacastHandler(Bundle extras)
	{
	
		super.broacastHandler(extras);
    	
		//child was reset
    	if(extras.getString(getResources().getString(R.string.bbReset)) != null 
    			&& extras.getString(getResources().getString(R.string.bbReset)).equals(getResources().getString(R.string.bbCD2MEventMessage)))
    	{	
    		
    		//show log in/accounts activity
    		Intent i = new Intent(JointUserFormActivity.this, AccountActivity.class);
    		startActivity(i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    	}
	}
	
}
