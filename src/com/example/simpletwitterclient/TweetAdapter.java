package com.example.simpletwitterclient;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TweetAdapter extends BaseAdapter {
	public static final int TYPE_LIST_REPLACED = 1;
	public static final int TYPE_LIST_ADDED = 0;

	private Activity mActivity;
	private LayoutInflater mInflater;
	private List<TweetObj> mListObject = new ArrayList<TweetObj>();
	private ImageLoader mImageLoader;

	public TweetAdapter(Activity activity, List<TweetObj> objects) {
		mActivity = activity;
		mInflater = LayoutInflater.from(activity);
		mImageLoader = ImageLoader.getInstance(activity);
		if (objects != null) {
			mListObject = objects;
		}
	}

	public void notifyListObjectChanged(List<TweetObj> objectsChanged, int type) {
		if (objectsChanged != null) {
			if (type == TYPE_LIST_REPLACED)
				mListObject.clear();
			mListObject.addAll(objectsChanged);
			notifyDataSetChanged();
		}
	}

	public void onDestroy() {

	}

	public void onPause() {

	}

	public void onStop() {

	}

	public void notifyListObjectChanged() {
		notifyDataSetChanged();
	}

	public int getCount() {
		return mListObject.size();
	}

	public Object getItem(int position) {
		return mListObject.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.view_news_row, null);
			holder = new ViewHolder();
			holder.txtChannelName = (TextView) convertView.findViewById(R.id.txt_channel_name);
			holder.txtTime = (TextView) convertView.findViewById(R.id.txt_time);
			holder.txtMessage = (TextView) convertView.findViewById(R.id.txt_message);
			holder.imgChannelLogo = (ImageView) convertView.findViewById(R.id.img_channel_logo);
			holder.imgMessage = (ImageView598x336) convertView.findViewById(R.id.img_message);
			holder.txtRetweetCount = (TextView) convertView.findViewById(R.id.txt_retweet);
			holder.txtLikedCount = (TextView) convertView.findViewById(R.id.txt_liked);
			holder.txtDetailUrl = (TextView) convertView.findViewById(R.id.txt_detail_url);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final TweetObj obj = mListObject.get(position);
		holder.txtChannelName.setText(obj.getChannelName());
		holder.txtTime.setText(obj.getTime());
		holder.txtMessage.setText(obj.getMessage());
		holder.txtDetailUrl.setText(obj.getDetailUrl());
		holder.txtRetweetCount.setText(obj.getRetweetCount());
		holder.txtLikedCount.setText(obj.getLikedCount());

		mImageLoader.DisplayImage(obj.getChannelLogo(), holder.imgChannelLogo,R.drawable.ic_launcher);
		
		if(obj.getType() == TweetObj.TYPE_VIDEO) {
			// no thumbnail for video so using default image
			mImageLoader.DisplayImage(obj.getImageUrl(), holder.imgMessage, R.drawable.bg_video_temp);
		} else {
			mImageLoader.DisplayImage(obj.getImageUrl(), holder.imgMessage, -1);
		}


		holder.txtDetailUrl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + obj.getDetailUrl()));
				mActivity.startActivity(browserIntent);
			}
		});

		holder.imgMessage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (obj.getType() == TweetObj.TYPE_VIDEO) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(obj.getPlayVideoUrl()));
					mActivity.startActivity(browserIntent);
				} else {
					Intent fullImageIntent = new Intent(mActivity, FullImageActivity.class);
					Bundle bd = new Bundle();
					bd.putString(FullImageActivity.KEY_IMAGE_URL, obj.getImageUrl());
					fullImageIntent.putExtras(bd);
					mActivity.startActivity(fullImageIntent);
				}
			}
		});

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		return convertView;
	}

	class ViewHolder {
		TextView txtChannelName;
		ImageView598x336 imgMessage;
		ImageView imgChannelLogo;
		TextView txtTime;
		TextView txtMessage;
		TextView txtDetailUrl;
		TextView txtRetweetCount;
		TextView txtLikedCount;
	}
}
