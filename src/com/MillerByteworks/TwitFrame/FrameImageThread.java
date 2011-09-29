package com.MillerByteworks.TwitFrame;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class FrameImageThread implements Runnable 
{
	private	Drawable	Drawable	= null;	
	private ImageView 	Image 		= null;
	private int			ResourceId	= 0;
	
	public FrameImageThread(ImageView view, Drawable drawable)
	{
		this.Image 		= view;
		this.Drawable 	= drawable;		
	}
	
	public FrameImageThread(ImageView view, int resId)
	{
		this.Image 		= view;
		this.ResourceId = resId;		
	}
	
	public void run()
	{
		this.Image.post(new Runnable() {
			public void run()
			{
				if(Drawable != null)
					Image.setImageDrawable(Drawable);
				else
					Image.setImageResource(ResourceId);
			}
		});		
	}
	
	/*private Drawable LoadImage(String imageUrl)
	{
		URL 		url	= null;
		InputStream is 	= null;
		
		try {
			
			url = new URL(imageUrl);
			is 	= (InputStream)url.getContent();		
		
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return Drawable.createFromStream(is, "src");
	}
	
	protected Bitmap LoadBitmap(String URL)
	{
		Bitmap 				bitmap = null;
		HttpURLConnection 	conn;
		InputStream 		is;
		URL 				url;
        
        try {
        
        	url 	= new URL(URL);        	
        	conn 	= (HttpURLConnection)url.openConnection();
             
        	conn.setDoInput(true);
            conn.connect();
            
            is 		= conn.getInputStream();
            bitmap 	= BitmapFactory.decodeStream(is);
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
             e.printStackTrace();
        }
        
        return bitmap;
	}*/
	
}
