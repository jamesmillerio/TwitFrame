package com.MillerByteworks.TwitFrame;

import java.util.ArrayList;
import java.util.Hashtable;

import android.content.*;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.*;

public class FrameImage extends FrameBaseActivity 
{
	private	Thread						ImageThread		= null;
	private String						URL				= null;
	private ImageView					CurrentImage	= null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_image);
        
        this.CurrentImage = (ImageView)findViewById(R.id.imgCurrentImage);
	}
	
	@Override
	public void onResume()
	{
		Intent 				intent	= this.getIntent();
		Bundle 				extras 	= intent.getExtras();
	
		super.onResume();
		
		if(extras != null)
        {
        	this.URL = extras.getString(TwitFrameConstants.CURRENT_IMAGE_URL);
        	
        	this.ShowImage();
        	
        } else {
        	finish();
        }
	}
	
	private void ShowImage()
	{					
		this.ImageThread = new Thread(new Runnable() {
			
			@Override
			public void run()
			{
				Drawable drawable = LoadDrawableFromURL(URL);
				
				if(drawable != null)
				{
					CurrentImage.post(new FrameImageThread(CurrentImage, drawable));
					this.Sleep(TwitFrameConstants.IMAGE_DURATION_MS);
					finish();
				}
			}
			
			private void Sleep(long sleepTime)
			{
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		});
		
		this.ImageThread.start();

		
	}
}
