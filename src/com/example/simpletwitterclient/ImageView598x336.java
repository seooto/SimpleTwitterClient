package com.example.simpletwitterclient;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ImageView598x336 extends ImageView {

	public ImageView598x336(Context context) {
		super(context);
	}

	public ImageView598x336(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ImageView598x336(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = width * 336 / 598;
		setMeasuredDimension(width, height);
	}
}
