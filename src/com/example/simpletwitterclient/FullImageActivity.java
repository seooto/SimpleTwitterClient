package com.example.simpletwitterclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

public class FullImageActivity extends Activity {
	
	public static final String KEY_IMAGE_URL = "image_url";
	
	private ImageView598x336 mFullImage;
	private ImageLoader mImageLoader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_full_image);
		mFullImage = (ImageView598x336) findViewById(R.id.img_message);
		
		Bundle bd = getIntent().getExtras();
		String imageUrl = bd.getString(KEY_IMAGE_URL);
		mImageLoader = ImageLoader.getInstance(this);
		mImageLoader.DisplayImage(imageUrl, mFullImage, -1);
		
		mFullImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

}
