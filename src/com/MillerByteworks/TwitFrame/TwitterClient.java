package com.MillerByteworks.TwitFrame;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.content.*;
import android.content.res.*;
import android.net.Uri;
import twitter4j.*;
import twitter4j.auth.*;

public class TwitterClient {
 
	private 				Twitter 				Twitter			= null;
	private					RequestToken 			RequestToken	= null;
	private					SharedPreferences		Preferences		= null;
	private					ResponseList<Status> 	Statuses		= null;
	private					SimpleDateFormat 		DateFormat 		= new SimpleDateFormat("MMMM d, y");
	private					SimpleDateFormat		TimeFormat		= new SimpleDateFormat("h:mm a");
	private					Context 				Context			= null;
	private					boolean					StreamStarted	= false;
	private					long					LatestStatusId	= -1;
	
	public TwitterClient(SharedPreferences preferences, Context context)
	{
		String token;
		String secret;
				
		try {
			
			this.Twitter 		= new TwitterFactory().getInstance();
			this.Preferences 	= preferences;
			this.Context		= context;
			
			this.Twitter.setOAuthConsumer(TwitFrameConstants.TWITTER_CONSUMER_KEY, TwitFrameConstants.TWITTER_CONSUMER_SECRET);
			
			this.RequestToken 	= this.Twitter.getOAuthRequestToken(TwitFrameConstants.CALLBACK_URL);
			token 				= preferences.getString(TwitFrameConstants.AUTH_TOKEN, "");
			secret				= preferences.getString(TwitFrameConstants.AUTH_SECRET, "");
			
			if(this.IsAuthorized())
				this.Twitter.setOAuthAccessToken(new AccessToken(token, secret));
			
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}
	
	public Intent Authorize(String callbackUrl)
	{
		Intent intent = null;
		String authUrl;
		
		try {
			
			if(this.RequestToken == null)
				this.RequestToken 	= this.Twitter.getOAuthRequestToken(callbackUrl);
			
			authUrl				= this.RequestToken.getAuthenticationURL();
			intent 				= new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return intent;
	}
	
	public void LogOut()
	{
		SharedPreferences.Editor editor;
		
		if(!this.IsAuthorized() || this.Preferences == null)
			return;
		
		editor 	= this.Preferences.edit();
		
		editor.putString(TwitFrameConstants.AUTH_TOKEN, null);
		editor.putString(TwitFrameConstants.AUTH_SECRET, null);
		
		editor.commit();
	}
	
	public void SetAuthorization(String verifier)
	{
		AccessToken					accessToken;
		SharedPreferences.Editor 	editor;
		String 						token;
		String 						secret;
		
		try {
			
			accessToken = this.Twitter.getOAuthAccessToken(this.RequestToken, verifier);
			token 		= accessToken.getToken();
			secret 		= accessToken.getTokenSecret();
			
			//Make sure we got a token and secret, if we did, save them
			if(token != null && secret != null && token.length() > 0 && secret.length() > 0)
			{
				editor 	= this.Preferences.edit();
				
				editor.putString(TwitFrameConstants.AUTH_TOKEN, token);
				editor.putString(TwitFrameConstants.AUTH_SECRET, secret);
				editor.putString(TwitFrameConstants.AUTH_USERNAME, this.Twitter.getScreenName());
				
				editor.commit();				
			}
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean IsAuthorized()
	{
		String token 	= this.Preferences.getString(TwitFrameConstants.AUTH_TOKEN, null);
		String secret 	= this.Preferences.getString(TwitFrameConstants.AUTH_SECRET, null);
		
		return (token != null && secret != null && token.length() > 0 && secret.length() > 0);
	}
	
	public String TwitterList()
	{
		return this.Preferences.getString(TwitFrameConstants.TWEET_LIST, this.Context.getResources().getString(R.string.everyone));
	}
	
	public int TwitterListId() throws IllegalStateException, TwitterException
	{
		ResponseList<UserList> lists = this.Twitter.getAllUserLists(this.Twitter.getId());
		
		if(this.TwitterList().equals(this.Context.getResources().getString(R.string.everyone)))
			return -1;
		
		for(int i = 0; i < lists.size(); i++)
		{
			UserList list = lists.get(i);
			
			if(list.getName().equals(this.TwitterList()))
				return list.getId();
		}
		
		return -1;
	}
	
	public String ScreenName()
	{
		return this.Preferences.getString(TwitFrameConstants.AUTH_USERNAME, "");
	}
		
	private void UpdateStatusList(long sinceId) throws TwitterException
	{
		ResponseList<Status> current = null;
		
		/* If we haven't gotten statuses yet, get a fresh 
		 * list from their home timeline. If we have, append 
		 * the newest to the front of the list. */
		if(this.TwitterList().equals(this.Context.getResources().getString(R.string.everyone)))
		{
			//Get our statuses from their main timeline
			if(sinceId <= 0)
				current = this.Twitter.getHomeTimeline();
			else
				current = this.Twitter.getHomeTimeline(new Paging(sinceId));	
		} 
		else 
		{		
			if(sinceId <= 0)
				current = this.Twitter.getUserListStatuses(this.TwitterListId(), new Paging());
			else
				current = this.Twitter.getUserListStatuses(this.TwitterListId(), new Paging(sinceId));
		}
		
		if(this.Statuses == null)
			this.Statuses = current;
		else
			this.Statuses.addAll(0, current);
	}
	
	public TwitterStatus GetNextStatus()
	{
		return GetNextStatus(-1);
	}
	
	public TwitterStatus GetNextStatus(long sinceId)
	{
		Status 					topStatus;
		TwitterStatus			status 		= null;
		
		try {
		
			if(!this.IsAuthorized())
				return null;
		
			//Grab new statuses if we haven't already, or we've run out of them.
			if(this.Statuses == null || this.Statuses.size() <= 0)
			{
				if(this.LatestStatusId <= 0)
					this.UpdateStatusList(sinceId);
				else
					this.UpdateStatusList(this.LatestStatusId);
			}
							
			//If after getting statuses, we have none, return null
			if(this.Statuses == null || this.Statuses.size() <= 0)
				return null;
			
			//Set the newest update as our latest so we know where to start once we grab statuses again
			this.LatestStatusId = this.Statuses.get(0).getId();
			
			//Retrieve the last status in our list since we're working towards the most recent
			topStatus 			= this.Statuses.get(this.Statuses.size() - 1);
			
			//Remove the status we just retrieved
			this.Statuses.remove(this.Statuses.size() - 1);
			
			if(topStatus != null)
				status = this.ConvertTwitterStatus(topStatus);
		
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
		return status;
	}
	
	public void ResetStatusCache()
	{
		if(this.Statuses == null)
			return;
		
		this.Statuses.clear();
		this.Statuses = null;
	}
	
	public String[] GetLoggedInUsersTwitterLists()
	{
		ResponseList<UserList> 		twitterLists	= null;
    	String[]					lists			= null;
    	Resources					resources		= this.Context.getResources();
    	
		try {
			
			twitterLists 	= this.Twitter.getAllUserLists(this.Twitter.getId());
			
			lists 			= new String[twitterLists.size() + 1];
			
			//Add the default option
			lists[0] = resources.getString(R.string.everyone);
			
			for(int index = 1; index <= twitterLists.size(); index++)
				lists[index] = twitterLists.get(index - 1).getName();
						
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
		return lists;
	}
	
	private List<TwitterStatus> ConvertResponseList(ResponseList<Status> list)
	{
		List<TwitterStatus> newList = new ArrayList<TwitterStatus>();
		
		if(list == null)
			return null;
		
		for(int i = 0; i < list.size(); i++)
			newList.add(ConvertTwitterStatus(list.get(i)));
		
		return newList;
	}
	
	public TwitterStatus ConvertTwitterStatus(Status original)
	{
		TwitterStatus 	status 		= new TwitterStatus();
		Date			date		= original.getCreatedAt();
		URLEntity[]		entities	= original.getURLEntities();
		String[]		urls		= null;
		
		if(entities != null && entities.length > 0)
		{
			urls	= new String[entities.length];
			
			for(int j = 0; j < entities.length; j++)
				urls[j] = entities[j].getURL().toString();
		}
		
		status.Id			= original.getId();
		status.URLs			= urls;			
		status.ScreenName 	= original.getUser().getScreenName();
		status.Text 		= original.getText();
		status.AvatarURL	= original.getUser().getProfileImageURL().toString();
		status.Name			= original.getUser().getName();
		status.Timestamp	= DateFormat.format(date) + " at " + TimeFormat.format(date);
		status.UserId		= original.getUser().getId();
		
		return status;
	}
		
	public void StartStatusStream(UserStreamListener listener)
	{
		TwitterStreamFactory 	factory 	= null;
		TwitterStream 			stream 		= null;
		FilterQuery				filter		= new FilterQuery();
		
		if(this.StreamStarted)
			return;
		
		factory = new TwitterStreamFactory(this.Twitter.getConfiguration());
		stream 	= factory.getInstance(this.Twitter.getAuthorization());

		if(listener != null)
			stream.addListener(listener);
		
		stream.filter(filter);
		stream.user();
		this.StreamStarted = true;
	}
		
	public boolean FollowMe()
	{
		try {
			
			User james = this.Twitter.showUser(TwitFrameConstants.JAMES_TWITTER);
			
			//If we can't find James, return...
			if(james == null)
				return false;
		
			return (this.Twitter.createFriendship(james.getId()) != null);
			
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
		return false;
	}
		
}
