package com.MillerByteworks.TwitFrame;

import android.widget.*;

public class FrameTextThread implements Runnable
{
	private TextView 	Label 	= null;
	private String		Text	= null;
	
	public FrameTextThread(TextView view, String text)
	{
		this.Label 	= view;
		this.Text	= text;
	}
	
	public void run()
	{
		this.Label.post(new Runnable() {
			public void run()
			{
				Label.setText(Text);
			}
		});		
	}
}
