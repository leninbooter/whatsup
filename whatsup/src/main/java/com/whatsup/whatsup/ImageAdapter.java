package com.whatsup.whatsup;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import java.util.HashMap;

/**
 * Created by alenin on 09/08/2014.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private int mResourceId;
    private int mQuantity;
    private int hw;
    private HashMap<Integer, String> mImages = new HashMap<Integer, String>();

    private static final int PROGRESSBARINDEX = 0;
    private static final int IMAGEVIEWINDEX = 1;

    private Handler mHandler;
    private ImageLoader mImageLoader = null;

    public ImageAdapter(Context mContext, int resourceId, int quantity) {
        this.mContext = mContext;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mQuantity = quantity;
        this.mResourceId = resourceId;
        for( int i=0; i<quantity; i++ )
            mImages.put( i, null );


        Display display = ( (Activity) this.mContext ).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        hw = size.x / 3;
        mImageLoader = new ImageLoader( R.drawable.empty_frame );

    }

    public int getCount() {
        Log.d("getCount:", String.valueOf(mImages.size()));
        return mImages.size();
    }

    public String getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("getView", "yes");
        ImageView imageView;
        ViewHolder_GVItem vh;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            convertView = mInflater.inflate( R.layout.fragment_what_was_here_gv_item, parent, false );
            vh = new ViewHolder_GVItem();
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams( hw, hw ));
            imageView.setImageResource(R.drawable.empty_frame);
            //imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            //imageView.setPadding(0, 0, 0, 0);
            vh.icon = (ImageView) convertView.findViewById( R.id.imageview );
            vh.icon.getLayoutParams().height = hw;
            vh.icon.getLayoutParams().width = hw;
            vh.icon.setImageResource(R.drawable.empty_frame);
            vh.position = position;
            convertView.setTag( vh );
        } else {
            vh = (ViewHolder_GVItem) convertView.getTag();
            //vh = new ViewHolder();
            vh.icon.setImageResource( R.drawable.empty_frame);
            vh.position = position;
        }
        mImageLoader.getImage( mImages.get( position ), position, vh, null );
        return convertView;
    }

    public void setImage( int position, String path ) {
        mImages.put(position, path);
        Log.d("setImage" , "yes");
    }


    public void handleImageLoaded(
            final ViewSwitcher aViewSwitcher,
            final ImageView aImageView,
            final Bitmap aBitmap) {

        // The enqueue the following in the UI thread
        mHandler.post(new Runnable() {
            public void run() {

                // set the bitmap in the ImageView
                aImageView.setImageBitmap(aBitmap);

                // explicitly tell the view switcher to show the second view
                aViewSwitcher.setDisplayedChild(IMAGEVIEWINDEX);
            }
        });

    }
}