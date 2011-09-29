package com.MillerByteworks.TwitFrame;

import android.graphics.drawable.Drawable;

public class AvatarUpdate {

	private Drawable 	drawable 	= null;
	private String 		screenName 	= null;
	
	public AvatarUpdate(Drawable asset, String name)
	{
		this.SetDrawable(asset);
		this.SetScreenName(name);
	}
	
	public 	Drawable	GetDrawable() 					{ return this.drawable; }
	public 	String		GetScreenName() 				{ return this.screenName; }
	public	void		SetDrawable(Drawable asset) 	{ this.drawable = asset; }
	public	void		SetScreenName(String name) 		{ this.screenName = name; }
	
}
