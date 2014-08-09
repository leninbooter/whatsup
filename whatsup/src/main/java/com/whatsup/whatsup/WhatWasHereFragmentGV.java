package com.whatsup.whatsup;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
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
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link android.app.Fragment} subclass.
 * Use the {@link com.whatsup.whatsup.WhatWasHereFragmentGV#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class WhatWasHereFragmentGV extends Fragment
                                    implements ImageDownloaderAsyncTask.onImageDownloaderAsyncTaskListener {
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
    private String data = null;
    private List<HashMap<String, Object>>  mPictures;
    private JSONObject jObject;
    private GridView gridview;
    private ImageAdapter imgAdapter;
    private ExecutorService service;
    private ExecutorService mImageLoaderPoolThread;
    private Executor mImageLoaderExecutor;

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
        Log.d("onResume:", "from wwhgv");
        mListener.setCurrentFragmentTag("events_pictures");
    }
    @Override
    public void onPause() {
        super.onPause();
        downloadTask.cancel(true);
        if( service != null) {
            shutdownAndAwaitTermination( service );
        }
        if( mImageLoaderPoolThread != null ) {
            shutdownAndAwaitTermination( mImageLoaderPoolThread );
        }

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
                mWhatWasHereListView = (RelativeLayout) inflater.inflate(R.layout.fragment_what_was_here_gv, container, false);
                TextView event_name = (TextView) mWhatWasHereListView.findViewById(R.id.event_name);
                TextView event_date = (TextView) mWhatWasHereListView.findViewById(R.id.event_date);

                event_name.setText( getArguments().getString(EVENT_NAME) );
                event_date.setText( getArguments().getString(EVENT_DATE) );
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

    void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(30, TimeUnit.SECONDS))
                    Log.d("Pausing WwhfGV:", "Could not stop some threads");
            }

        }catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                pool.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
        }
    }

    public void downLoadImage( String path, String imageName, String index ) {
       service.submit( new ImageDownloaderAsyncTask( getActivity(), imgAdapter ,this , path, imageName, index ) );
    }

    public void PickDownloadedImage( String path ) {
        File file = new File( path );
        String filename = file.getName();
        int index = Integer.valueOf( filename.substring(0, filename.indexOf( "_" ) ) );
        synchronized ( imgAdapter ) {
            imgAdapter.setImage( index, path );
            if( index >= gridview.getFirstVisiblePosition() && index <= gridview.getLastVisiblePosition() ) {
                Log.d("notified", "yes");
                getActivity().runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        imgAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
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
            if( isCancelled() ) return null;
            return mPictures;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, Object>>  pictures) {
            try {
                if (pictures.size() == 0) {
                    getView().findViewById(R.id.no_info_not_rellay).setVisibility(View.VISIBLE);
                } else {
                    imgAdapter = new ImageAdapter(getActivity(), R.layout.fragment_what_was_here_gv_item, pictures.size());
                    gridview = (GridView) mWhatWasHereListView.findViewById(R.id.gridview);
                    getView().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    gridview.setAdapter(imgAdapter);
                    gridview.setOnItemClickListener(getImageClickListener());
                    gridview.setFastScrollEnabled(true);
                    Log.d("adapter", "ready");
                    service = Executors.newFixedThreadPool(2);
                    for (int i = 0; i < pictures.size(); i++) {
                        Log.d("new thread for image: ", mPictures.get(i).get("source").toString());
                        File file = new File( mPictures.get(i).get("source").toString() );
                        downLoadImage( mPictures.get(i).get("source").toString(), file.getName(), String.valueOf( i ) );
                        /*service.submit( new ImageDownloaderAsyncTask( getActivity(),  mPictures.get(i).get("source").toString(),
                                        mPictures.get(i).get("source").toString().substring(1, mPictures.get(i).get("source").toString().length()),
                                        String.valueOf(i))
                                      );*/
                    }
                }
            }catch ( NullPointerException e ) {
               Log.d("onPostExecute WhatWasHereFragmentGV class:","NullPointerException getting back just before drawing display");
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



}


