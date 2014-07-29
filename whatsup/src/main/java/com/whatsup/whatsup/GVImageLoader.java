package com.whatsup.whatsup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by alenin on 28/07/2014.
 */
public class GVImageLoader extends AsyncTask<WhatWasHereFragmentGV.ViewHolder, Void, Bitmap> {
    private ImageView iv;
    private WhatWasHereFragmentGV.ViewHolder vh;
    private String addr;
    private int position;

    public GVImageLoader( String pathFile, WhatWasHereFragmentGV.ViewHolder vh, int position ) {
        addr = pathFile;
        //this.iv = iv;
        this.vh  = vh;
        this.position = position;
        execute();
    }

    @Override
    protected Bitmap doInBackground(WhatWasHereFragmentGV.ViewHolder... viewHolders) {
        Log.d("doInBackground", "on GVImageLoader");
        return BitmapFactory.decodeFile(addr);
    }

    @Override
    protected void onPostExecute( Bitmap result ) {
        Log.d("onPostExecute", "on GVImageLoader");
        if( result != null )
            Log.d("result", "null");

        if( this.vh.position != this.position )
            this.vh.icon.setImageBitmap( result );
    }
}
