package com.MillerByteworks.TwitFrame;

import java.util.Hashtable;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;

import net.htmlparser.jericho.*;

import com.bugsense.trace.BugSenseHandler;

public class Frame extends FrameBaseActivity {

	Hashtable<String, Drawable>	AvatarTable		= new Hashtable<String, Drawable>();
	private ImageView 			Avatar			= null;
	private TextView			TweetText		= null;
	private TextView			TwitterHandle 	= null;
	private TextView			Timestamp		= null;
	private long				FrameRefreshMs	= 11000;
	private boolean				Paused			= false;
	private boolean				IsFirstUpdate	= true;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        
		super.onCreate(savedInstanceState);
        setContentView(R.layout.frame);
        
        //Set up debugging
        BugSenseHandler.setup(this, TwitFrameConstants.BUGSENSE_API_KEY);
                
        //Create our Twitter client
        this.Twitter 					= new TwitterClient(this.getSharedPreferences(TwitFrameConstants.PREFERENCES, MODE_PRIVATE), this);
        
        //Reference our views
        this.Avatar 					= (ImageView)findViewById(R.id.imgAvatar);
        this.TweetText					= (TextView)findViewById(R.id.tvTweetText);
        this.TwitterHandle				= (TextView)findViewById(R.id.tvTwitterHandle);
        this.Timestamp					= (TextView)findViewById(R.id.tvTimestamp);
    }
		
	@Override
	protected void onNewIntent(Intent intent) 
    {
    	Uri 	uri			= intent.getData();
    	String 	verifier;
    	
    	super.onNewIntent(intent);
    			
		if(uri == null)
			return;
		
		verifier = uri.getQueryParameter("oauth_verifier");
		
		if(verifier != null) {
			
			this.Twitter.SetAuthorization(verifier);
			this.StartTicker();
						
		} else {
		
			//If they didn't log in, exit. We can't do anything without their log in credentials
			finish();
		
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
	    MenuInflater inflater = getMenuInflater();
	    
	    inflater.inflate(R.menu.settings_menu, menu);
	    
	    return true;
	    
	}
		
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.settings:

		    	//Clear the status cache in case they switch any settings.
		    	this.Twitter.ResetStatusCache();
		    	
		    	//Stop the tweet processing loop
		    	//this.startActivity(new Intent(this, Main.class));
		    	this.startActivityForResult(new Intent(this, Main.class), TwitFrameConstants.RESULT_SETTINGS);

		    	return true;
		    	
		    case R.id.exit:
		    	
		    	setResult(TwitFrameConstants.RESULT_CLOSE_ALL);
		    	finish();

		    	return true;
		    	
		    case R.id.about:
		    	
		    	//Clear the status cache in case they switch any settings.
		    	this.Twitter.ResetStatusCache();
		    	
		    	//Stop the tweet processing loop
		    	//this.startActivity(new Intent(this, Main.class));
		    	this.startActivity(new Intent(this, About.class));

		    	return true;
		    	
		    default:
		    	
		        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		this.Paused = true;
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		this.Paused = false;
		
		this.TweetText.setText("");
		this.TwitterHandle.setText("");
		this.Timestamp.setText("");
		this.Avatar.setImageResource(R.drawable.twitter_egg);
		
		if(this.Twitter.IsAuthorized())
		{
			this.StartTicker();
		} 
		else
		{
			new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.log_in_to_twitter)
            .setMessage(R.string.go_to_twitter)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                	
                	startActivity(Twitter.Authorize(TwitFrameConstants.CALLBACK_URL));
                	
                }

            })
            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                	
                	//Exit the activity
                	finish();
                	
                }

            })
            .show();
		}
	}
	
	private void StartTicker()
	{				
		//Notify the user that loading may take a second
		Toast.makeText(this, R.string.loading, Toast.LENGTH_LONG).show();
						
		//Start our twitter update processor
		new Thread(new UpdateProcessor()).start();
	}
	
	private void SetLastStatusId(long id)
	{
		SharedPreferences 			preferences = this.getSharedPreferences(TwitFrameConstants.PREFERENCES, Main.MODE_PRIVATE);
    	SharedPreferences.Editor 	editor 		= preferences.edit();
    	
		editor.putLong(TwitFrameConstants.LAST_STATUS, id);
		
		editor.commit();
	}
	
	private long GetLastStatusId()
	{
		SharedPreferences 	preferences = this.getSharedPreferences(TwitFrameConstants.PREFERENCES, Main.MODE_PRIVATE);
		
		return preferences.getLong(TwitFrameConstants.LAST_STATUS, -1);
	}
	
	private void SetLastImageIndex(int index)
	{
		SharedPreferences 			preferences = this.getSharedPreferences(TwitFrameConstants.PREFERENCES, Main.MODE_PRIVATE);
    	SharedPreferences.Editor 	editor 		= preferences.edit();
    	
		editor.putInt(TwitFrameConstants.LAST_IMAGE_INDEX, index);
		
		editor.commit();
	}
	
	private int GetLastImageIndex()
	{
		SharedPreferences 	preferences = this.getSharedPreferences(TwitFrameConstants.PREFERENCES, Main.MODE_PRIVATE);
		
		return preferences.getInt(TwitFrameConstants.LAST_IMAGE_INDEX, -1);
	}
	
	private class UpdateProcessor implements Runnable {

		@Override
		public void run() {
		
			TwitterStatus 	current 		= null;
			Drawable		avatar			= null;
			Intent			imageFrame		= null;	
			int				lastImageIndex	= GetLastImageIndex();
			
			//If the activity has paused, quit the thread. It'll get restarted later.
			if(Paused)
				return;
			
			if(IsFirstUpdate)
			{
				current 		= Twitter.GetNextStatus(GetLastStatusId());
				IsFirstUpdate 	= false;
			} 
			else 
			{
				current = Twitter.GetNextStatus();	
			}			
			
			if(current != null)
			{			
				if(AvatarTable.containsKey(current.ScreenName))
					avatar = AvatarTable.get(current.ScreenName);
				else 
				{
					avatar = LoadDrawableFromURL(current.AvatarURL);
					
					if(avatar != null)
						AvatarTable.put(current.ScreenName, avatar);
				}
				
				if(avatar != null)
					Avatar.post(new FrameImageThread(Avatar, avatar));
				else
					Avatar.post(new FrameImageThread(Avatar, R.drawable.twitter_egg));
				
				TwitterHandle.post(new FrameTextThread(TwitterHandle, "@" + current.ScreenName));
				TweetText.post(new FrameTextThread(TweetText, current.Text));
				Timestamp.post(new FrameTextThread(Timestamp, current.Timestamp));
				
				SetLastStatusId(current.Id);
				
				//If there are attached images, we need to show them after sleeping
				/*if(current.URLs != null && current.URLs.length > 0 && lastImageIndex < current.URLs.length && lastImageIndex >= 0)
				{
					this.Sleep(FrameRefreshMs);
					
					imageFrame = new Intent(Frame.this, FrameImage.class);
					imageFrame.putExtra(TwitFrameConstants.CURRENT_IMAGE_URL, current.URLs[lastImageIndex + 1]);
					
					SetLastImageIndex(lastImageIndex++);
					startActivity(imageFrame);
					
					return;
				} 
				else 
				{
					SetLastImageIndex(-1);
				}
				
				this.Sleep(FrameRefreshMs);*/
				
				
			}
			
			this.Sleep(FrameRefreshMs);
			
			new Thread(new UpdateProcessor()).start();
		}
		
		private void Sleep(long sleepTime)
		{
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};
}
