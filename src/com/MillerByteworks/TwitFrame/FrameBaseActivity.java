package com.MillerByteworks.TwitFrame;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

public class FrameBaseActivity extends Activity {

	TwitterClient	Twitter				= null;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	    switch(resultCode)
	    {
	    	case TwitFrameConstants.RESULT_CLOSE_ALL:
	    		setResult(TwitFrameConstants.RESULT_CLOSE_ALL);
	    		finish();
	    }
	    
	    super.onActivityResult(requestCode, resultCode, data);
	    
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
	}
	
	protected Drawable LoadDrawableFromURL(String imageUrl)
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
	
	private String GetImageURLFromWebPage(String URL)
	{
		Source 			source;
		List<StartTag>	links;
				
		try {
			
			source	= new Source(new URL(URL));
			links 	= source.getAllStartTags("link");
			
			for(int index = 0; index < links.size(); index++)
			{
				StartTag 	tag	= links.get(index);
				String 		rel	= tag.getAttributeValue("rel");
				
				if(rel != null && rel.length() > 0 && rel.toLowerCase().equals("image_src"))
					return tag.getAttributeValue("href");
				
			}
			
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
             e.printStackTrace();
        }
        
        return null;
	}
	
}
