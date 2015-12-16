package com.example.simpletwitterclient;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.example.simpletwitterclient.RefreshableListView.onListLoadMoreListener;
import com.example.simpletwitterclient.RefreshableListView.onListRefreshListener;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class MainActivity extends Activity implements onListRefreshListener,
		onListLoadMoreListener {

	private RefreshableListView mRefreshableListView;
	private TweetAdapter mAdapter;
	private boolean mWannaReload;
	private int mLastTweetPosition;
	private ListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mRefreshableListView = (RefreshableListView) findViewById(R.id.lv_refresh_list);
		mRefreshableListView.setOnListRefreshListener(this);
		mRefreshableListView.setOnListLoadMoreListener(this);

		// set specific attributes
		mRefreshableListView.setDragLength(500);
		// seem load more of RefreshableListView lib working not correctly
		mRefreshableListView.setDistanceFromBottom(10);
		
		mListView = mRefreshableListView.getListView();
		mListView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				int threshold = 1; // if 1 item counted from the end visible then load more
				if (scrollState == SCROLL_STATE_IDLE) {
			        if (mListView.getLastVisiblePosition() >= mListView.getCount() - 1 - threshold) {
			        	mWannaReload = false;
			    		new ParseTweetUsingJsoupTask(mLastTweetPosition).execute();
			        }
			    }
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mWannaReload = true;
		new ParseTweetUsingJsoupTask(0).execute();
	}

	@Override
	public void Refresh(RefreshableListView list) {
		mWannaReload = true;
		new ParseTweetUsingJsoupTask(0).execute();
	}

	@Override
	public void LoadMore(RefreshableListView list) {	
		//mWannaReload = false;
		//new ParseTweetUsingJsoupTask(mLastTweetPosition).execute();
	}

	class ParseTweetUsingJsoupTask extends
			AsyncTask<Integer, Integer, List<TweetObj>> {
		
		private int position;
		public ParseTweetUsingJsoupTask(int position) {
			this.position = position;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mRefreshableListView.showProgressBar();
		}

		@Override
		protected List<TweetObj> doInBackground(Integer... params) {
			List<TweetObj> objs = new ArrayList<TweetObj>();

			try {
				Document doc = Jsoup.connect(Constant.ROOT_URL + Constant.CHANNEL_NAME + "?count=" + (position + Constant.LIMIT)).ignoreContentType(true)
						.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
						.referrer("http://www.google.com").timeout(30000)
						.followRedirects(true).get();
				Elements content = doc.getElementsByClass("content");
				for (int i = position ; i < content.size() ; i ++) {
					try {
						TweetObj obj = new TweetObj();
						Element divElement = content.get(i);
						Element headerElement = divElement.getElementsByClass("stream-item-header").first();
						Element channelLogoElement = headerElement.select("img.avatar.js-action-profile-avatar").first();
						obj.setChannelLogo(channelLogoElement.attr("src"));
						
						Element channelNameElement = headerElement.select("strong.fullname.js-action-profile-name.show-popup-with-id").first();
						obj.setChannelName(channelNameElement.text());
						
						Element rootTimeElement = headerElement.getElementsByClass("time").first();
						Element timeElement = rootTimeElement.select("span.u-hiddenVisually").first();
						obj.setTime(timeElement.text());
						
						Element messageElement = divElement.select("p.TweetTextSize.js-tweet-text.tweet-text").first();					
						Element detailUrlElement = messageElement.select("span.js-display-url").first();
						if(detailUrlElement != null) {
							obj.setDetailUrl(detailUrlElement.text());
						}
						
						// remove all spam text
						Elements invisibleElements = messageElement.getElementsByClass("invisible");
						if(invisibleElements != null)
							invisibleElements.remove();
						detailUrlElement.remove();
						Element hiddenElement = messageElement.select("a.twitter-timeline-link.u-hidden").first();
						if(hiddenElement != null)
							hiddenElement.remove();
						obj.setMessage(messageElement.text());
						
						Element photoElement = divElement.select("div.AdaptiveMedia-singlePhoto").first();
						// single photo
						if(photoElement != null) {
							Element imgElement = photoElement.getElementsByTag("img").first();
							obj.setImageUrl(imgElement.attr("src"));
							obj.setType(TweetObj.TYPE_SINGLE_PHOTO);
						} else {
							// video
							Element videoElement = divElement.select("div.AdaptiveMedia-videoContainer").first();
							if(videoElement != null) {
								Element dataElement = videoElement.getElementsByClass("js-macaw-cards-iframe-container").first();
								if(dataElement != null) {
									obj.setImageUrl(Constant.ROOT_URL + dataElement.attr("data-src")); // this is not real image url
									obj.setPlayVideoUrl(Constant.ROOT_URL + dataElement.attr("data-autoplay-src"));
								} else {
									dataElement = videoElement.select("video.animated-gif").first();
									obj.setImageUrl(dataElement.attr("poster"));
									obj.setPlayVideoUrl(dataElement.getElementsByClass("source-mp4").first().attr("video-src"));
								}
								
								obj.setType(TweetObj.TYPE_VIDEO);
							} else {
								// multiple photo
								photoElement = divElement.select("div.AdaptiveMedia-quadPhoto").first();
								Element imgElement = photoElement.getElementsByTag("img").first();
								obj.setImageUrl(imgElement.attr("src"));
								obj.setType(TweetObj.TYPE_MULTIPLE_PHOTO);
							}
						}
						
						
						Element footerElement = divElement.select("div.stream-item-footer").first();
						Element retweetElement = footerElement.select("span.ProfileTweet-action--retweet.u-hiddenVisually").first().select("span.ProfileTweet-actionCount").first();
						obj.setRetweetCount(retweetElement.attr("data-tweet-stat-count"));
						
						Element likedElement = footerElement.select("span.ProfileTweet-action--favorite.u-hiddenVisually").first().select("span.ProfileTweet-actionCount").first();
						obj.setLikedCount(likedElement.attr("data-tweet-stat-count"));
						
						objs.add(obj);
					} catch (Exception e) {
						// TODO: handle exception
						continue;
					}

				}
				mLastTweetPosition = position + Constant.LIMIT;
				return objs;
			} catch (Exception e) {
				return objs;
			}
		}

		@Override
		protected void onPostExecute(List<TweetObj> result) {
			super.onPostExecute(result);
			mRefreshableListView.finishRefresh();
			mRefreshableListView.finishLoadingMore();
			if (mAdapter == null || mWannaReload) {
				setAdapter(result, TweetAdapter.TYPE_LIST_REPLACED);
			} else {
				setAdapter(result, TweetAdapter.TYPE_LIST_ADDED);
			}
		}
	}

	private void setAdapter(final List<TweetObj> objs, int type) {
		try {

			if (type == TweetAdapter.TYPE_LIST_REPLACED) {
				mAdapter = new TweetAdapter(MainActivity.this, objs);
				mRefreshableListView.setAdapter(mAdapter);
			} else {
				mAdapter.notifyListObjectChanged(objs, type);
			}

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

}
