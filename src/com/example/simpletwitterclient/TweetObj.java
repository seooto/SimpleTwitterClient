package com.example.simpletwitterclient;

public class TweetObj {
	
	public static final int TYPE_SINGLE_PHOTO = 0;
	public static final int TYPE_VIDEO = 1;
	public static final int TYPE_MULTIPLE_PHOTO = 2;

	private String channelName;
	private String channelLogo;
	private String detailUrl;
	private String imageUrl;
	private String message;
	private String time;
	private String retweetCount;
	private String likedCount;
	private String playVideoUrl;
	private int type;

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getChannelLogo() {
		return channelLogo;
	}

	public void setChannelLogo(String channelLogo) {
		this.channelLogo = channelLogo;
	}

	public String getDetailUrl() {
		return detailUrl;
	}

	public void setDetailUrl(String detailUrl) {
		this.detailUrl = detailUrl;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getRetweetCount() {
		return retweetCount;
	}

	public void setRetweetCount(String retweetCount) {
		this.retweetCount = retweetCount;
	}

	public String getLikedCount() {
		return likedCount;
	}

	public void setLikedCount(String likedCount) {
		this.likedCount = likedCount;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getPlayVideoUrl() {
		return playVideoUrl;
	}

	public void setPlayVideoUrl(String playVideoUrl) {
		this.playVideoUrl = playVideoUrl;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
