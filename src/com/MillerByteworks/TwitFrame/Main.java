package com.MillerByteworks.TwitFrame;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.*;
import com.MillerByteworks.TwitFrame.R;

public class Main extends FrameBaseActivity {
    
	private					Button		Authenticate	= null;
	private					Button		Save			= null;
	private					Button		SaveAndContinue	= null;
	private					Spinner		TweetList		= null;
	private					TextView	LoggedInUser	= null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	SharedPreferences 			preferences 	= this.getSharedPreferences(TwitFrameConstants.PREFERENCES, MODE_PRIVATE);
    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //Load all of our views
        this.Authenticate 							= (Button)findViewById(R.id.btnAuthenticate);
        this.TweetList 								= (Spinner)findViewById(R.id.spnTwitterList);
        this.Save 									= (Button)findViewById(R.id.btnSave);
        this.SaveAndContinue 						= (Button)findViewById(R.id.btnSaveAndContinue);
        this.LoggedInUser							= (TextView)findViewById(R.id.tvLoggedInUser);
        
        //Load our Twitter client
        this.Twitter								= new TwitterClient(preferences, this);
        
        //Set up our onClick handlers
        this.Authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { 
            	
            	if(Twitter.IsAuthorized())
            	{
            		new AlertDialog.Builder(v.getContext())
	                    .setIcon(android.R.drawable.ic_dialog_alert)
	                    .setTitle(R.string.really_logout_title)
	                    .setMessage(R.string.really_logout)
	                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	
	                        @Override
	                        public void onClick(DialogInterface dialog, int which) {
	                        	
	                        	SharedPreferences 			preferences 	= getSharedPreferences(TwitFrameConstants.PREFERENCES, Main.MODE_PRIVATE);
	                        	SharedPreferences.Editor 	editor 			= preferences.edit();
	                        	
	                        	//Remove all settings, they've logged out
	                        	editor.clear();
	                        	editor.commit();
	                        	
	                        	ToggleFormEnabled(false);
	                        	
	                        	Main.this.setResult(TwitFrameConstants.RESULT_CLOSE_ALL);
	                        	Main.this.finish();
	                        }
	
	                    })
	                    .setNegativeButton(android.R.string.no, null)
	                    .show();
            	}
            	else
            	{
            		startActivity(Twitter.Authorize(TwitFrameConstants.CALLBACK_URL));
            	}
        	}
        });
        
        this.Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { SaveSettings(); }
        });
        
        this.SaveAndContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) 
            { 
            	SaveSettings();
            	startActivity(new Intent(Main.this, Frame.class));
            	setResult(TwitFrameConstants.RESULT_CLOSE_ALL);
            	finish();
            }
        });
        
        //If we have an active Twitter session, load our settings
        if(this.Twitter.IsAuthorized())
        {       
        	this.InitializeViewFields();        						
			this.ToggleFormEnabled(true);
				
        } else {
        	
        	this.ToggleFormEnabled(false);
        	
        }
    }
    
    public void InitializeViewFields()
    {
    	try {
    		
    		ArrayAdapter<String> adapter;
    		
    		//Set the correct log in/out button text
    		if(this.Twitter != null)
            	this.Authenticate.setText(R.string.twitter_logout);
    		else
    			this.Authenticate.setText(R.string.twitter_authenticate);
    		
    		adapter = new ArrayAdapter<String>(this, R.layout.dropdown_closed, this.Twitter.GetLoggedInUsersTwitterLists());
    		
    		adapter.setDropDownViewResource(R.layout.dropdown_item);
    		
			this.TweetList.setAdapter(adapter);
			
			//Finally, load our settings
			this.LoadSettings();
			
    	} catch (IllegalStateException e) {
			e.printStackTrace();
		}
    }
    
    public void ToggleFormEnabled(boolean enabled)
    {
    	this.TweetList.setEnabled(enabled);
    	this.Save.setEnabled(enabled);
    	this.SaveAndContinue.setEnabled(enabled);
    }
    
    public void SaveSettings()
    {
    	SharedPreferences 			preferences = this.getSharedPreferences(TwitFrameConstants.PREFERENCES, Main.MODE_PRIVATE);
    	SharedPreferences.Editor 	editor 		= preferences.edit();
    	
		editor.putString(TwitFrameConstants.TWEET_LIST, this.TweetList.getSelectedItem().toString());
		
		editor.commit();
		
		Toast.makeText(this, R.string.settings_saved_successfull, Toast.LENGTH_LONG).show();
    }
    
    public void LoadSettings()
    {
    	SharedPreferences 		preferences = this.getSharedPreferences(TwitFrameConstants.PREFERENCES, MODE_PRIVATE);
    	String 					twitterList	= preferences.getString(TwitFrameConstants.TWEET_LIST, null);
    	Boolean 				randomize	= preferences.getBoolean(TwitFrameConstants.RANDOMIZE_TWEETS, false);
    	ArrayAdapter<String>	adapter 	= (ArrayAdapter<String>)this.TweetList.getAdapter();
    	    	
    	this.TweetList.setSelection(adapter.getPosition(twitterList));
    	
    	if(this.Twitter.IsAuthorized())
    		this.LoggedInUser.setText(getString(R.string.logged_in_as) + " " + this.Twitter.ScreenName());
    	else
    		this.LoggedInUser.setText(getString(R.string.blank));
    	
    } 
}	