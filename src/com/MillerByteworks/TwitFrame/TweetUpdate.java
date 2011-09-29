package com.MillerByteworks.TwitFrame;

import android.graphics.drawable.Drawable;

public class TweetUpdate {

	private Drawable 	avatar 		= null;
	private String		name		= null;
	private	String		tweet		= null;
	private String		timestamp	= null;
	
	public TweetUpdate(Drawable avatar, String name, String tweet, String timestamp)
	{
		this.avatar 	= avatar;
		this.name 		= name;
		this.tweet 		= tweet;
		this.timestamp 	= timestamp;
	}
	
	public	Drawable	GetAvatar() 	{ return this.avatar; }
	public	String		GetScreenName() { return this.name; }
	public 	String		GetTweet()		{ return this.tweet; }
	public 	String		GetTimestamp()	{ return this.timestamp; }
	
}
