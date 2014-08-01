package com.whatsup.whatsup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.util.concurrent.Executor;

/**
 * Created by alenin on 01/08/2014.
 */
public class ImageLoaderAsyncTask extends AsyncTask<Integer, Void, Bitmap> {

    public interface OnImageLoaderAsyncTaskListener {
        public void addBitmapToMemoryCache(String key, Bitmap bitmap);
    }

    OnImageLoaderAsyncTaskListener mCallBback;
    private ViewHolder_GVItem vh;
    private int position;
    private String addr;

    public ImageLoaderAsyncTask( ViewHolder_GVItem vh, int position, String pathFile, Executor pool, ImageLoader callbackclass) {
        this.vh = vh;
        this.position = position;
        this.addr = pathFile;
        mCallBback = (OnImageLoaderAsyncTaskListener) callbackclass;
        execute();
    }

    protected Bitmap doInBackground(Integer... positions) {
        Log.d("doInBackground", "on ImageLoaderAsyncTask");
        return BitmapFactory.decodeFile( this.addr );
    }

    @Override
    protected void onPostExecute( Bitmap result ) {
        Log.d("onPostExecute", "on ImageLoaderAsyncTask");
        if( this.vh.position == this.position ) {
            this.vh.icon.setImageBitmap(result);
            mCallBback.addBitmapToMemoryCache( String.valueOf( vh.position ), result );
        }
    }
}
