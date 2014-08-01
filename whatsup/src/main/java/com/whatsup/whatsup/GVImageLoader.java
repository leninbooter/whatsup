package com.whatsup.whatsup;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by alenin on 28/07/2014.
 */

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
                /*this.mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });*/
            }
    }
}
/*
public class GVImageLoader extends AsyncTask<ViewHolder_GVItem, Void, Bitmap> {
    private ImageView iv;
    private ViewHolder_GVItem vh;
    private String addr;
    private int position;

    public GVImageLoader( String pathFile, ViewHolder_GVItem vh, int position ) {
        addr = pathFile;
        //this.iv = iv;
        this.vh  = vh;
        this.position = position;
        execute();
    }

    @Override
    protected Bitmap doInBackground(ViewHolder_GVItem... viewHolders) {
        Log.d("doInBackground", "on GVImageLoader");
        return BitmapFactory.decodeFile(addr);
    }

    @Override
    protected void onPostExecute( Bitmap result ) {
        Log.d("onPostExecute", "on GVImageLoader");

        if( this.vh.position == this.position )
            this.vh.icon.setImageBitmap( result );
    }
}*/

