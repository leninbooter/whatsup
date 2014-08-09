package com.whatsup.whatsup;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.widget.BaseAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Executor;

/**
 * Created by alenin on 08/08/2014.
 */
/*public class ImageDownloaderAsyncTask extends AsyncTask<String, Void, String> {
    Activity activity;

    private onImageDownloaderAsyncTaskListener mListener;

    public interface onImageDownloaderAsyncTaskListener {
        public void PickDownloadedImage(String path);
    }

    public ImageDownloaderAsyncTask(Activity activity, WhatWasHereFragmentGV whatWasHereFragmentGV, Executor executor, String... urls) {
        this.activity = activity;
        mListener = whatWasHereFragmentGV;
        //execute(urls);
        executeOnExecutor( executor, urls);
    }

    protected String doInBackground(String... urls) {
        InputStream iStream = null;
        String imgUrl = Params.CDN + urls[0];
        Log.d("ImageLoaderTask will download: ", imgUrl);
        URL url;
        File tmpFile = null;
        try {
            File cacheDirectory = activity.getCacheDir();
            tmpFile = new File( cacheDirectory.getPath() + "/" + urls[1] );
            if( true ) {
                Log.d("Image was on cache", "no");
                url = new URL(imgUrl);
                Bitmap b;
                int hw;
                Display display = activity.getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                FileOutputStream fOutStream = new FileOutputStream(tmpFile);
                hw = size.x / 3;
                b = BitmapFactory.decodeStream(iStream);
                b = Bitmap.createScaledBitmap(b, hw, hw, false);
                b.compress(Bitmap.CompressFormat.JPEG, 90, fOutStream);
                fOutStream.flush();
                fOutStream.close();
            }else {
                Log.d("Image was on cache", "yes");
            }
        } catch (Exception e) {
            Log.d("Exception on Imagedownloader task", e.getMessage());
            e.printStackTrace();
        } catch ( OutOfMemoryError ome ) {
            return null;
        }
        return tmpFile.getPath();
    }

    protected void onPostExecute(String result) {
        if( result != null )
            mListener.PickDownloadedImage( result );
        else {
            Toast.makeText(activity, activity.getString(R.string.no_memory), Toast.LENGTH_SHORT).show();
        }
    }
}*/

public class ImageDownloaderAsyncTask extends Thread {
    Context mContext;
    BaseAdapter mBaseAdapter;
    String[] urls = new String[3];
    int index;

    private onImageDownloaderAsyncTaskListener mListener;

    public interface onImageDownloaderAsyncTaskListener {
        public void PickDownloadedImage(String path);
    }

    public ImageDownloaderAsyncTask( Context context, BaseAdapter baseAdapter, WhatWasHereFragmentGV whatWasHereFragmentGV, String...urls ) {
        mContext = context;
        mBaseAdapter = baseAdapter;
        mListener = whatWasHereFragmentGV;
        this.urls[0] = urls[0]; //full url
        this.urls[1] = urls[1]; //image name
        this.urls[2] = urls[2]; //index of image on grid
        this.index = Integer.valueOf( urls[2] );
    }

    @Override
    public void run() {
        InputStream iStream = null;
        String imgUrl = Params.CDN + urls[0];
        Log.d("ImageLoaderTask will download: ", imgUrl );
        URL url;
        File tmpFile = null;
        try {
            File cacheDirectory = ( (Activity) mContext).getCacheDir();
            tmpFile = new File( cacheDirectory.getPath() + "/" + urls[2] + "_" + urls[1] );
            if( true ) {
                Log.d("Image was on cache", "no");
                url = new URL(imgUrl);
                Bitmap b;
                int hw;
                Display display = ( (Activity) mContext).getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                if( isInterrupted() ) return;
                iStream = urlConnection.getInputStream();
                FileOutputStream fOutStream = new FileOutputStream(tmpFile);
                hw = size.x / 3;
                b = BitmapFactory.decodeStream( iStream );
                b = Bitmap.createScaledBitmap(b, hw, hw, false);
                if( isInterrupted() ) return;
                b.compress(Bitmap.CompressFormat.JPEG, 90, fOutStream);
                fOutStream.flush();
                fOutStream.close();
            }else {
                Log.d("Image was on cache", "yes");
            }
            HashMap<String, Object> item = new HashMap<String, Object>();
            mListener.PickDownloadedImage( tmpFile.getPath() );
        } catch (Exception e) {
            Log.d("Exception on Imagedownloader task", e.getMessage());
            e.printStackTrace();
        } catch ( OutOfMemoryError ome ) {
            Log.d("Exception on Imagedownloader task", "OutOfMemoryError");
        }

    }
}