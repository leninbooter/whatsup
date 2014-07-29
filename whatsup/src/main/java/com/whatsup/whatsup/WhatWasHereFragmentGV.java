package com.whatsup.whatsup;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link android.app.Fragment} subclass.
 * Use the {@link com.whatsup.whatsup.WhatWasHereFragmentGV#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class WhatWasHereFragmentGV extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String EVENT_ID = "event_id";
    private static final String EVENT_NAME = "event_name";
    private static final String EVENT_DATE = "event_date";

    private OnWwhGvsFragmentListener mListener;
    private View mWhatWasHereListView;
    private Params parameters;
    private DownloadTask downloadTask;
    private ListViewLoaderTask listViewLoaderTask;
    private ImageLoaderTask[] imageLoaderTask;
    private String data = null;
    private List<HashMap<String, Object>>  mPictures;
    private JSONObject jObject;
    private GridView[] gridView;
    private ImageAdapter imgAdapter;
    private ExecutorService service;

    public interface OnWwhGvsFragmentListener {
        public void setCurrentFragmentTag(String tag);
        public void setmTitle(String title);
        public void ShowNoConnectionMessage();
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment What_was_here.
     */
    // TODO: Rename and change types and number of parameters
    public static WhatWasHereFragmentGV newInstance(String event_id, String event_name, String event_date) {
        WhatWasHereFragmentGV fragment = new WhatWasHereFragmentGV();
        Bundle args = new Bundle();
        args.putString(EVENT_ID, event_id);
        args.putString(EVENT_NAME, event_name);
        args.putString(EVENT_DATE, event_date);
        fragment.setArguments(args);
        return fragment;
    }
    public WhatWasHereFragmentGV() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnWwhGvsFragmentListener) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.setCurrentFragmentTag("events_pictures");
    }
    @Override
    public void onPause() {
        super.onPause();
        downloadTask.cancel(true);
        /*if( imageLoaderTask != null ) {
            for (int i = 0; i < imageLoaderTask.length; i++) {
                imageLoaderTask[i].cancel(true);
            }
        }*/
        if( service != null)
            service.shutdownNow();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("on", "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("on", "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("on", "onDestroy");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        Log.d("on", "onDetach");
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("WWHFGV onCreate", "entered");
        parameters = new Params();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("WWHFGV onCreateView", "entered");
        mWhatWasHereListView = null;
        if( savedInstanceState == null ) {
            try {
                mWhatWasHereListView = (RelativeLayout) inflater.inflate(R.layout.fragment_what_was_here_gv, container, false);
                String datestr = getArguments().getString(EVENT_DATE);
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date inputDate = fmt.parse(datestr);
                DateFormat.getDateInstance().format(inputDate);
                TextView event_name = (TextView) mWhatWasHereListView.findViewById(R.id.event_name);
                TextView event_date = (TextView) mWhatWasHereListView.findViewById(R.id.event_date);

                event_name.setText(getArguments().getString(EVENT_NAME));
                event_date.setText(DateFormat.getDateInstance().format(inputDate));
            }catch ( ParseException e) {
                Log.d("Exception parsing date", e.getMessage() );
                e.printStackTrace();
            }
        }
        return mWhatWasHereListView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("onActivityCreated", "entré");
        if( savedInstanceState == null ) {
            downloadTask = new DownloadTask();
            String strUrl = parameters.REST_SERVER + "/whatsup/slim/public/index.php/place/getpicturesevents/" + getArguments().getString(EVENT_ID) + "/null/null";
            downloadTask.execute(strUrl);
        }
    }

    public AdapterView.OnItemClickListener getImageClickListener() {
        AdapterView.OnItemClickListener avoicl = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if( imgAdapter.getItem( position) == null )
                    Toast.makeText(getActivity(), "Image on position: " + position + " not yet loaded. Wait", Toast.LENGTH_SHORT).show();
            }
        };
        return avoicl;
    }

    private class PictsJSONparser {

        //Images processing
        public List<HashMap<String,Object>> parseImages (JSONObject jObject) {
            JSONArray jPictures = null;
            try {
                jPictures = jObject.getJSONArray("pictures");
            }catch ( JSONException e ) {
                e.printStackTrace();
            }
            return getPictures(jPictures);
        }

        private List<HashMap<String,Object>> getPictures(JSONArray jPictures) {
            int count = jPictures.length();
            List<HashMap<String, Object>> PictureList = new ArrayList<HashMap<String, Object>>();
            HashMap<String, Object> picture = null;

            for( int i = 0 ; i < count ; i++) {
                try {
                    picture = getPict((JSONObject) jPictures.get(i));
                    PictureList.add(picture);
                }catch ( JSONException e ) {
                    Log.d("Exception from getPictures: ", e.getMessage());
                    e.printStackTrace();
                }
            }
            return PictureList;
        }

        private HashMap<String, Object> getPict(JSONObject jPict) {
            HashMap<String, Object> pict = new HashMap<String, Object>();
            try {
                pict.put("pk_picture_id", jPict.getString("pk_picture_id"));
                pict.put("gp_reference", jPict.getString("gp_reference"));
                pict.put("source", jPict.getString("source"));
                pict.put("gp_html_attributions", jPict.getString("gp_html_attributions"));
            }catch (JSONException e ) {
                e.printStackTrace();
            }
            return pict;
        }
    }

    private class ListViewLoaderTask extends  AsyncTask<String, Void, List<HashMap<String, Object>> > {

        @Override
        protected List<HashMap<String, Object>>  doInBackground(String... strJson) {
            PictsJSONparser pictsJSONparser = new PictsJSONparser();
            mPictures = null;

            try {
                jObject = new JSONObject(strJson[0]);
                mPictures = pictsJSONparser.parseImages(jObject);
            }catch ( Exception e ) {
                Log.d("Exception", e.toString());
            }
            //String[] from = { "title"};
            //int[] to = { R.id.event_title};
            //SimpleAdapter adapter = new SimpleAdapter(getActivity(), events, R.layout.fragment_what_was_here_lv_item, from, to);
            return mPictures;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, Object>>  pictures) {
            getView().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            if( pictures.size() == 0 ) {
                getView().findViewById(R.id.no_info_not_rellay).setVisibility(View.VISIBLE);
            }else {
                imgAdapter = new ImageAdapter( getActivity(),  R.layout.fragment_what_was_here_gv_item, pictures.size() );
                GridView gridview = (GridView) getView().findViewById(R.id.gridview);
                gridview.setAdapter( imgAdapter );
                gridview.setOnItemClickListener( getImageClickListener() );
                gridview.setFastScrollEnabled( true );
                gridview.setFastScrollAlwaysVisible( false );
                imageLoaderTask = new ImageLoaderTask[ pictures.size() ];
                service = Executors.newFixedThreadPool(100);
                for( int i = 0; i < imageLoaderTask.length; i++ ) {
                    Log.d("new thread for image: ", mPictures.get( i ).get( "source" ).toString() );

                    service.submit( new ImageLoaderTask(mPictures.get(i).get("source").toString(),
                                    mPictures.get(i).get("source").toString().substring(1, mPictures.get(i).get("source").toString().length()),
                                    String.valueOf(i)) );
                    /*imageLoaderTask[i] = new ImageLoaderTask();
                    imageLoaderTask[i].executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                            mPictures.get(i).get("source").toString(),
                            mPictures.get(i).get("source").toString().substring(1, mPictures.get(i).get("source").toString().length()),
                            String.valueOf(i)
                    );*/
                }
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... url) {
            try {
                data = Utilis.downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task ", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            if( result != null ) {
                listViewLoaderTask = new ListViewLoaderTask();
                listViewLoaderTask.execute(result);
            }else {
                mListener.ShowNoConnectionMessage();
            }
        }
}
    private class ImageLoaderTask extends Thread {
        String[] urls = new String[3];
        int index;

        public ImageLoaderTask( String...urls ) {
            this.urls[0] = urls[0];
            this.urls[1] = urls[1];
            this.urls[2] = urls[2];
            this.index = Integer.valueOf( urls[2] );
        }

        @Override
        public void run() {
            InputStream iStream = null;
            String imgUrl = Params.CDN + urls[0];
            Log.d("ImageLoaderTask will download: ", imgUrl );
            URL url;
            File tmpFile;
            try {
                File cacheDirectory = getActivity().getCacheDir();
                tmpFile = new File( cacheDirectory.getPath() + "/" + urls[2] + "_" + urls[1] );
                if( !tmpFile.exists() ) {
                    url = new URL(imgUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();
                    if( isInterrupted() ) return;
                    iStream = urlConnection.getInputStream();
                    FileOutputStream fOutStream = new FileOutputStream(tmpFile);
                    Bitmap b = BitmapFactory.decodeStream(iStream);
                    if( isInterrupted() ) return;
                    b.compress(Bitmap.CompressFormat.JPEG, 30, fOutStream);
                    fOutStream.flush();
                    fOutStream.close();
                }
                HashMap<String, Object> item = new HashMap<String, Object>();
                item.put( "source", tmpFile.getPath() );
                mPictures.set ( Integer.valueOf( urls[2] ) , item);
            } catch (Exception e) {
                Log.d("Exception on Imagedownloader task", e.getMessage());
                e.printStackTrace();
            } catch ( OutOfMemoryError ome ) {
                Toast.makeText( getActivity(), getString( R.string.no_memory ), Toast.LENGTH_SHORT ).show();
            }
                synchronized ( imgAdapter ) {
                    imgAdapter.setImage(index, mPictures.get(index).get("source").toString());
                    imgAdapter.notifyDataSetChanged();
                }
        }
    }
    /*
    private class ImageLoaderTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... urls) {
                InputStream iStream = null;
                String imgUrl = Params.CDN + urls[0];
                Log.d("ImageLoaderTask will download: ", imgUrl );
                URL url;
                File tmpFile;
                try {
                    File cacheDirectory = getActivity().getCacheDir();
                    tmpFile = new File( cacheDirectory.getPath() + "/" + urls[2] + "_" + urls[1] );
                    if( !tmpFile.exists() ) {
                        url = new URL(imgUrl);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.connect();
                        iStream = urlConnection.getInputStream();
                        FileOutputStream fOutStream = new FileOutputStream(tmpFile);
                        Bitmap b = BitmapFactory.decodeStream(iStream);
                        b.compress(Bitmap.CompressFormat.JPEG, 30, fOutStream);
                        fOutStream.flush();
                        fOutStream.close();
                    }
                    HashMap<String, Object> item = new HashMap<String, Object>();
                    item.put( "source", tmpFile.getPath() );
                    mPictures.set ( Integer.valueOf( urls[2] ) , item);
                    if (isCancelled()) return null;
                } catch (Exception e) {
                    Log.d("Exception on Imagedownloader task", e.getMessage());
                    e.printStackTrace();
                    return null;
                } catch ( OutOfMemoryError ome ) {
                    Toast.makeText( getActivity(), getString( R.string.no_memory ), Toast.LENGTH_SHORT ).show();
                }
            return Integer.valueOf( urls[2] );
        }

        @Override
        protected void onPostExecute(Integer index) {
            if( index != null ) {
                imgAdapter.setImage( index, mPictures.get( index ).get( "source" ).toString() );
                imgAdapter.notifyDataSetChanged();
            }else {
                Toast.makeText(getActivity(), R.string.lost_connection, Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    private class ImageAdapter extends BaseAdapter{
        private Context mContext;
        private int mResourceId;
        private int mQuantity;
        private int hw;
        private HashMap<Integer, String> mImages = new HashMap<Integer, String>();
        private LruCache<String, Bitmap> mMemoryCache;

        private static final int PROGRESSBARINDEX = 0;
        private static final int IMAGEVIEWINDEX = 1;

        private Handler mHandler;
        private ImageLoader mImageLoader = null;

        public ImageAdapter(Context mContext, int resourceId, int quantity) {
            this.mContext = mContext;
            this.mQuantity = quantity;
            this.mResourceId = resourceId;
            for( int i=0; i<quantity; i++ )
                mImages.put( i, null );

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
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            hw = size.x / 3;
        }

        public int getCount() {
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
            ImageView imageView;
            ViewHolder vh;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                vh = new ViewHolder();
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams( hw, hw ));
                imageView.setImageResource(R.drawable.empty_frame);
                //imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                //imageView.setPadding(0, 0, 0, 0);
                vh.icon = imageView;
            } else {
                //imageView = (ImageView) convertView;
                vh = new ViewHolder();
                vh.icon = (ImageView) convertView;
                vh.icon.setImageResource(R.drawable.empty_frame);
            }
            if( mImages.get(position) == null ) {
                //vh.icon.setImageResource(R.drawable.empty_frame);
            }else {
                //vh.icon.setImageResource( R.drawable.empty_frame );
                //new GVImageLoader( mImages.get( position ), vh, position );
                /*Bitmap bitmap = getBitmapFromMemCache(String.valueOf(position));
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    Log.d("cached", "yes");
                } else {
                    addBitmapToMemoryCache(String.valueOf(position), BitmapFactory.decodeFile(mImages.get(position)));
                    bitmap = getBitmapFromMemCache(String.valueOf(position));
                    imageView.setImageBitmap(bitmap);
                    Log.d("cached", "no");
                }
                //imageView.setImageDrawable( Drawable.createFromPath( mImages.get(position) ) );*/
            }

            return vh.icon;
        }

        public void setImage( int position, String path ) {
            mImages.put(position, path);
            notifyDataSetChanged();
        }

        public Bitmap getBitmapFromMemCache(String key) {
            return mMemoryCache.get(key);
        }

        public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
            if (getBitmapFromMemCache(key) == null) {
                mMemoryCache.put(key, bitmap);
            }
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

    static class ViewHolder {
        int position;
        ImageView icon;
    }
}


