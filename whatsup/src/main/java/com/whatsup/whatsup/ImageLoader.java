package com.whatsup.whatsup;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by alenin on 28/07/2014.
 */
/*
public class GVImageLoader extends Thread {
    private ViewHolder_GVItem mVh;
    private String mAddr;
    private int mPosition;
    private Activity mContext;

    public GVImageLoader(Activity context, String pathFile, ViewHolder_GVItem vh, int position ) {
        this.mContext = context;
        this.mAddr = pathFile;
        this.mVh  = vh;
        this.mPosition = position;
        run();
    }

    @Override
    public void run() {
            if (this.mVh.position == this.mPosition) {
                    mVh.icon.setImageBitmap(BitmapFactory.decodeFile(mAddr));

            }
    }
}*/

public class ImageLoader implements ImageLoaderAsyncTask.OnImageLoaderAsyncTaskListener {
    private LruCache<String, Bitmap> mMemoryCache;
    private Executor mImageLoaderExecutor;
    private List< ImageLoaderAsyncTask > mAsyncTask = new ArrayList<ImageLoaderAsyncTask>();
    private int mNoLoadedImage;

    public ImageLoader(int mNoLoadedImage_in) {
        mImageLoaderExecutor = Executors.newFixedThreadPool(6);
        this.mNoLoadedImage = mNoLoadedImage_in;
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 2;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public void getImage( String pathFile_in, int position_in, ViewHolder_GVItem vh_in, Executor pool ) {
        Bitmap bitmap = null;

        bitmap = getBitmapFromMemCache(String.valueOf(position_in));
        if (bitmap != null) {
            Log.d("cached", "yes");
            vh_in.icon.setImageBitmap( bitmap );
        } else {
            Log.d("cached", "no");
            vh_in.icon.setImageResource( mNoLoadedImage );
            if( pathFile_in != null) {
                new ImageLoaderAsyncTask(vh_in, position_in, pathFile_in, mImageLoaderExecutor, this);
            }
        }
    }
}

