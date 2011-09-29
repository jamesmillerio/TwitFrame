package com.MillerByteworks.TwitFrame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class About extends FrameBaseActivity {

	private Button 		FollowMe 			= null;
	private TextView 	MillerByteWorksURL	= null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	        
	    	super.onCreate(savedInstanceState);
	        setContentView(R.layout.about);
	        
	        //Set our views
	        this.FollowMe 			= (Button)findViewById(R.id.btnFollowMe);
	        this.MillerByteWorksURL = (TextView)findViewById(R.id.tvMillerByteworksURL);
	        
	        //Create our Twitter client
	        this.Twitter 					= new TwitterClient(this.getSharedPreferences(TwitFrameConstants.PREFERENCES, MODE_PRIVATE), this);
	        
	        //Set our link text to respond to clicks
	        this.MillerByteWorksURL.setMovementMethod(LinkMovementMethod.getInstance());
	        
	        //Set up our link
	        /*this.MillerByteWorksURL.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TwitFrameConstants.MILLER_BYTEWORKS_URL))); }
	        });*/
	        
	        //Set our buttons onClick listener
	        this.FollowMe.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) { 
	            
	            	if(!Twitter.IsAuthorized())
	            	{
	            		Toast.makeText(About.this, R.string.log_in_first, Toast.LENGTH_LONG).show();
	            		return;
	            	}
	            	
	            	if(Twitter.FollowMe())
	            		Toast.makeText(About.this, R.string.successfully_following, Toast.LENGTH_LONG).show();
	            	else
	            		Toast.makeText(About.this, R.string.error_following, Toast.LENGTH_LONG).show();
	            	
	        	}
        });
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		finish();
	}
}
