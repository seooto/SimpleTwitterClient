package com.example.simpletwitterclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.ImageView;

public class ImageLoader {

	private static ImageLoader shareInstance;

	private HashMap<String, Bitmap> cache = new HashMap<String, Bitmap>();

	private File cacheDir;
	private Context context;

	int REQUIRED_SIZE = 96;

	public void setRequiredSize(int value) {
		REQUIRED_SIZE = value;
	}

	public void removeOldCached() {
		int count = 0;
		List<String> keys = new ArrayList<String>();
		Set<Entry<String, Bitmap>> set = cache.entrySet();
		Iterator<Entry<String, Bitmap>> i = set.iterator();
		while (i.hasNext()) {
			try {
				if (count <= 10) {
					count = count + 1;
					Map.Entry<String, Bitmap> entry = (Map.Entry<String, Bitmap>) i
							.next();
					String key = entry.getKey();
					keys.add(key);
				} else {
					break;
				}

			} catch (Exception e) {
				// TODO: handle exception

			}

		}

		for (String key : keys) {
			cache.remove(key);
		}
	}

	// private Bitmap defaultImage;
	public static ImageLoader getInstance(Context context) {
		if (shareInstance == null) {
			shareInstance = new ImageLoader(context);
		}
		return shareInstance;
	}

	private ImageLoader(Context ctx) {
		photoLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
		context = ctx;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(Environment.getExternalStorageDirectory(), "."
					+ context.getPackageName() + "/images/");
		} else {
			cacheDir = context.getCacheDir();
		}

		if (!cacheDir.exists())
			cacheDir.mkdirs();

	}

	public void DisplayImage(String url, ImageView imageView,
			int defaultImageResID) {
		if (url == null)
			url = "";
		imageView.setTag(url);
		String relativeURL = relativeURLof(url);
		if (cache.containsKey(relativeURL)) {
			imageView.setImageBitmap(cache.get(relativeURL));
		} else {
			queuePhoto(url, imageView);
			if (defaultImageResID != -1)
				imageView.setImageResource(defaultImageResID);
		}
	}

	public void getImage(String url, ImageView imageView) {
		Thread dataInitializationThread = new Thread() {
			public void run() {

			}
		};
		dataInitializationThread.start();
	}

	private String relativeURLof(String url) {
		if (url == null || url.trim().length() == 0)
			return "";

		String relativeURL = url.replaceFirst("http://[^/]+/", "");
		return relativeURL;
	}

	private void queuePhoto(String url, ImageView imageView) {
		photosQueue.Clean(imageView);
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		synchronized (photosQueue.photosToLoad) {
			photosQueue.photosToLoad.push(p);
			photosQueue.photosToLoad.notifyAll();
		}

		if (photoLoaderThread.getState() == Thread.State.NEW)
			photoLoaderThread.start();
	}

	public Bitmap getBitmapWithRedirect(String url) {
		if (url == null)
			return null;
		HttpURLConnection connection = null;
		URL myUrl = null;
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);
		Bitmap bitmap;
		bitmap = decodeFile(f);
		if (bitmap != null)
			return bitmap;
		try {
			myUrl = new URL(url);
			connection = (HttpURLConnection) myUrl.openConnection();
			connection.setUseCaches(true);
			InputStream is = connection.getInputStream();
			if (is != null) {
				OutputStream os = new FileOutputStream(f);
				copyStream(is, os);
				os.close();
				bitmap = decodeFile(f);
			}
		} catch (MalformedURLException mue) {
			// TODO: handle exception
			// bitmap = defaultImage;
		} catch (IOException ioe) {
			// TODO: handle exception
			// bitmap = defaultImage;

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return bitmap;
	}

	public Bitmap getBitmap(String url) {
		if (url == null)
			return null;
		url = url.replaceAll(" ", "%20");
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);
		Bitmap bitmap;
		bitmap = decodeFile(f);
		if (bitmap != null)
			return bitmap;
		try {
			URL imgUrl = new URL(url);
			InputStream is = imgUrl.openStream();
			if (is != null) {
				OutputStream os = new FileOutputStream(f);
				copyStream(is, os);
				os.close();
				bitmap = decodeFile(f);
			}
		} catch (MalformedURLException mue) {
			// TODO: handle exception
		} catch (IOException ioe) {
			// TODO: handle exception
		} catch (Exception ex) {
		}
		return bitmap;
	}

	private Bitmap decodeFile(File f) {
		try {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// The new size we want to scale to
			// REQUIRED_SIZE = 64;

			// Find the correct scale value. It should be the power of 2.
			int scale = 1;
			while (o.outWidth / scale / 2 >= REQUIRED_SIZE
					&& o.outHeight / scale / 2 >= REQUIRED_SIZE)
				scale *= 2;

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);

		} catch (FileNotFoundException e) {

		} catch (OutOfMemoryError e) {
			// TODO: handle exception
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	private class PhotoToLoad {
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	PhotosQueue photosQueue = new PhotosQueue();

	public void stopThread() {
		photoLoaderThread.interrupt();
	}

	// stores list of photos to download
	class PhotosQueue {
		private Stack<PhotoToLoad> photosToLoad = new Stack<PhotoToLoad>();

		// removes all instances of this ImageView
		public void Clean(ImageView image) {
			for (int j = 0; j < photosToLoad.size();) {
				try {
					if (photosToLoad.get(j).imageView == image)
						photosToLoad.remove(j);
					else
						++j;
				} catch (Exception e) {
					// TODO: handle exception
					continue;
				}

			}
		}
	}

	class PhotosLoader extends Thread {
		public void run() {
			try {
				while (true) {
					// thread waits until there are any images to load in the
					// queue
					if (photosQueue.photosToLoad.size() == 0)
						synchronized (photosQueue.photosToLoad) {
							photosQueue.photosToLoad.wait();
						}
					if (photosQueue.photosToLoad.size() != 0) {
						PhotoToLoad photoToLoad;
						synchronized (photosQueue.photosToLoad) {
							photoToLoad = photosQueue.photosToLoad.pop();
						}
					
						if (cache.size() > 30)
							removeOldCached();

						Bitmap bmp = null;

						bmp = getBitmap(photoToLoad.url);

						if (bmp != null) {
							cache.put(relativeURLof(photoToLoad.url), bmp);
							Object b = photoToLoad.imageView.getTag();
							// (String) photoToLoad.imageView.getTag()
							String str = b.toString();
							if (str.equals(photoToLoad.url)) {
								BitmapDisplayer bd = new BitmapDisplayer(bmp,
										photoToLoad.imageView);
								Activity a = (Activity) photoToLoad.imageView
										.getContext();
								a.runOnUiThread(bd);
							}
						}

					}
					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {

			} catch (Exception ex) {

			}
		}
	}

	PhotosLoader photoLoaderThread = new PhotosLoader();

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		ImageView imageView;

		public BitmapDisplayer(Bitmap b, ImageView i) {
			bitmap = b;
			imageView = i;
		}

		public void run() {
			if (bitmap != null)
				imageView.setImageBitmap(bitmap);

		}
	}

	public void clearCache() {
		// clear memory cache
		cache.clear();
	}

	public HashMap<String, Bitmap> getHashmap() {
		return this.cache;
	}

	public boolean isAlive() {
		return photoLoaderThread.isAlive();
	}

	public static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}
}
