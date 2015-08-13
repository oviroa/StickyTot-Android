package com.garagewarez.bubu.android;

import android.accounts.Account;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Adapter for accounts list. Used to display list of user accounts that have been registered with the phone.
 * Pulls account name from {@link android.accounts.Account} class and displays as list item
 * @author oviroa
 *
 */
public class AccountsAdapter extends ArrayAdapter<Account> 
{
 
    int resource;
    String response;
    Context context;
    Typeface adapterTf;
    
    //Initialize adapter
    public AccountsAdapter(Context context, int resource, Account[] items, Typeface tf) 
    {
        super(context, resource, items);
        this.resource=resource;
        this.adapterTf = tf;
 
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout accountView;
        
        //Get the current Account object
        Account account = getItem(position);
 
        //Inflate the view
        if(convertView==null)
        {
        	accountView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi;
            vi = (LayoutInflater)getContext().getSystemService(inflater);
            vi.inflate(resource, accountView, true);
        }
        else
        {
        	accountView = (LinearLayout) convertView;
        }
        
        //Get the text boxes from the account_list_iten.xml file
        TextView accountName =(TextView)accountView.findViewById(R.id.bbAccountName);
 
        //Assign the appropriate data from our alert object above
        accountName.setText(account.name);        
        accountName.setTypeface(adapterTf);
        return accountView;
    }
 
}
