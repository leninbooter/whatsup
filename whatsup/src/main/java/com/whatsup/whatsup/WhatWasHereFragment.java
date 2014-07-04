package com.whatsup.whatsup;



import android.app.ListFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WhatWasHereFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class WhatWasHereFragment extends ListFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PLACE_ID = "place_id";
    private static final String PLACE_NAME = "place_name";
    private static final String DATETIME = "datetime";

    private OnFragmentSpecialsFragmentListener mListener;
    private View mWhatWasHereListView;
    private Params parameters;
    private DownloadTask downloadTask;
    private ListViewLoaderTask listViewLoaderTask;
    private ImageLoaderTask[] imageLoaderTask;
    private String data = null;
    private List<List<HashMap<String,Object>>> pictures = new ArrayList<List<HashMap<String, Object>>>();
    private ArrayList<String> picturesLocaleUris = new ArrayList<String>();
    private JSONObject jObject;
    private GridView[] gridView;
    private ImageAdapter[] imageAdapter;

    public interface OnFragmentSpecialsFragmentListener {
        public void ShowNoConnectionMessage();
        public void setCurrentFragmentTag(String tag);
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
    public static WhatWasHereFragment newInstance(String place_id, String place_name, String datetime) {
        WhatWasHereFragment fragment = new WhatWasHereFragment();
        Bundle args = new Bundle();
        args.putString(PLACE_ID, place_id);
        args.putString(PLACE_NAME, place_name);
        args.putString(DATETIME, datetime);
        fragment.setArguments(args);
        return fragment;
    }
    public WhatWasHereFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate", "entered");
        parameters = new Params();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("onCreateView from what was here", "entered");
        mWhatWasHereListView = (RelativeLayout) inflater.inflate(R.layout.fragment_what_was_here, container, false);
        TextView place_name = (TextView)mWhatWasHereListView.findViewById(R.id.place_name);
        place_name.setText( getString(R.string.what_was_at) + " " + getArguments().getString(PLACE_NAME) + "?");
        return mWhatWasHereListView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("onActivityCreated", "entré");
        downloadTask = new DownloadTask();
        String strUrl = parameters.REST_SERVER + "/whatsup/slim/public/index.php/place/getevents/" + getArguments().getString(PLACE_ID) + "/" + getArguments().getString(DATETIME);
        downloadTask.execute(strUrl);
    }

    private class EventsJSONparser {

        public List<HashMap<String,Object>> parse (JSONObject jObject) {
            JSONArray jEvents = null;

            try {
                jEvents = jObject.getJSONArray("events");
            }catch ( JSONException e ) {
                e.printStackTrace();
            }
            return getEvents(jEvents);
        }

        private List<HashMap<String,Object>> getEvents(JSONArray jEvents) {
            int specialCount = jEvents.length();
            List<HashMap<String, Object>> EventList = new ArrayList<HashMap<String, Object>>();
            HashMap<String, Object> event = null;

            for( int i = 0 ; i < specialCount ; i++) {
                try {
                    event = getEvent((JSONObject) jEvents.get(i));
                    EventList.add(event);
                }catch ( JSONException e ) {
                    Log.d("Exception from getEvents: ", e.getMessage());
                    e.printStackTrace();
                }
            }
            return EventList;
        }

        private HashMap<String, Object> getEvent(JSONObject jEvent) {
            HashMap<String, Object> event = new HashMap<String, Object>();

            try {
                event.put("title", jEvent.getString("title"));
                Log.d("title: ",jEvent.getString("title"));
            }catch (JSONException e ) {
                e.printStackTrace();
            }
            return event;
        }
        //Images processing
        public List<HashMap<String,Object>> parseImages (JSONObject jObject, int index) {
            JSONArray jPictures = null;

            try {
                jPictures = jObject.getJSONArray("events").getJSONObject(index).getJSONArray("pictures");
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
                pict.put("source", jPict.getString("source"));
                //pict.put("gp_html_attributions", jPict.getString("gp_html_attributions"));
                Log.d("source", jPict.getString("source"));
                //Log.d("gp_html_attributions",jPict.getString("gp_html_attributions"));
            }catch (JSONException e ) {
                e.printStackTrace();
            }
            return pict;
        }
    }



    private class ListViewLoaderTask extends  AsyncTask<String, Void, SimpleAdapter> {

        @Override
        protected SimpleAdapter doInBackground(String... strJson) {
            EventsJSONparser eventsJSONparser = new EventsJSONparser();
            List<HashMap<String, Object>> events = null;

            try {
                jObject = new JSONObject(strJson[0]);
                events = eventsJSONparser.parse(jObject);
            }catch ( Exception e ) {
                Log.d("Exception", e.toString());
            }
            String[] from = { "title"};
            int[] to = { R.id.event_title};
            SimpleAdapter adapter = new SimpleAdapter(getActivity(), events, R.layout.fragment_what_was_here_lv_item, from, to);
            return adapter;
        }

        @Override
        protected void onPostExecute(SimpleAdapter adapter) {
            Log.d("onPostExecute", "Pediré una vista ");
            getView().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            ArrayList<String> headers = new ArrayList<String>();
            headers.add("lunes");headers.add("lunes");
            headers.add("martes");headers.add("martes");
            headers.add("miercoles");headers.add("miercoles");
            headers.add("jueves");headers.add("jueves");
            headers.add("viernes");headers.add("viernes");
            headers.add("sabado");headers.add("sabado");

            WhatWasHereListView wwhlv = new WhatWasHereListView(getActivity(), 10, headers, null);
            setListAdapter(wwhlv);
            /*for(int i=0; i<adapter.getCount(); i++) {
                View listAdapterItemView = adapter.getView(i, null, null);
                ViewGroup vg = (ViewGroup) listAdapterItemView.findViewById(R.id.containerRelativeLayout);
                NonScrollableGridView nsgv = new NonScrollableGridView(getActivity(), null);
                nsgv.setColumnWidth(100);
                nsgv.setNumColumns(3);
                nsgv.setVerticalSpacing(20);
                nsgv.setHorizontalSpacing(20);
                nsgv.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
                nsgv.setGravity(Gravity.CENTER);
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                rlp.addRule(RelativeLayout.BELOW, R.id.event_title);
                nsgv.setAdapter(new ImageAdapter(getActivity(), 10 ));
                vg.addView(nsgv,rlp);
                setListAdapter(adapter);
                adapter.notifyDataSetChanged();

            }*/
            if( adapter.getCount() == 0 ) {
                getView().findViewById(R.id.no_info_not_rellay).setVisibility(View.VISIBLE);
            }else {
                /*imageLoaderTask = new ImageLoaderTask[adapter.getCount()];
                for(int i=0; i<adapter.getCount(); i++) {
                    picturesLocaleUris = new ArrayList<String>();
                    EventsJSONparser pictsjsonparser = new EventsJSONparser();
                    Log.d("i", String.valueOf(i));
                    List<HashMap<String, Object>> pictsParsed = pictsjsonparser.parseImages(jObject, i);
                    Log.d("Images parsiadas: ", String.valueOf(pictsParsed.size()));
                    pictures.add(pictsParsed);
                    List<String> picl = new ArrayList<String>();
                    for(int j=0; j<pictures.get(i).size(); j++) {
                        picl.add(pictures.get(i).get(j).get("source").toString());
                        Log.d("pictures.get(i).get(j).get(\"source\").toString()", pictures.get(i).get(j).get("source").toString());
                    }
                    imageLoaderTask[i] = new ImageLoaderTask();
                    try {
                        Time now = new Time();
                        now.setToNow();
                        Log.d("hora entrada: ", now.toString());
                        Log.d("inicio hilo con # de imagenes ", String.valueOf(picl.size()));
                        imageLoaderTask[i].execute(picl).get();
                        now.setToNow();
                        Log.d("hora salida: ", now.toString() );
                        SimpleAdapter listAdapter = (SimpleAdapter) getListAdapter();
                        View listAdapterItemView = listAdapter.getView(i, null, null);
                        GridView gv = (GridView) listAdapterItemView.findViewById(R.id.gridview);
                        Log.d("picturesLocaleUris.size()", String.valueOf(picturesLocaleUris.size()));
                        ImageAdapter gv_adapter = new ImageAdapter(getActivity(), picturesLocaleUris.size(), picturesLocaleUris);
                        gv.setAdapter(gv_adapter);
                        gv_adapter.notifyDataSetChanged();

                    }catch (java.lang.InterruptedException e) {
                        Log.d("java.lang.InterruptedException", e.getMessage());
                    }catch (java.util.concurrent.ExecutionException e) {
                        Log.d("java.util.concurrent.ExecutionException", e.getMessage());
                    }
                }*/
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

private class ImageLoaderTask extends AsyncTask<List<String>, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(List<String>... urls) {
            for(int i=0; i<urls[0].size(); i++) {
                InputStream iStream = null;
                String imgUrl = Params.CDN + urls[0].get(i);
                URL url;
                try {
                    url = new URL(imgUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();
                    iStream = urlConnection.getInputStream();
                    File cacheDirectory = getActivity().getCacheDir();
                    File tmpFile = new File(cacheDirectory.getPath() + "/gv_" + String.valueOf(picturesLocaleUris.size()) + "_.png");
                    FileOutputStream fOutStream = new FileOutputStream(tmpFile);
                    Bitmap b = BitmapFactory.decodeStream(iStream);
                    b.compress(Bitmap.CompressFormat.PNG, 100, fOutStream);
                    fOutStream.flush();
                    fOutStream.close();
                    Log.d("tmpFile.getPath()", tmpFile.getPath());
                    picturesLocaleUris.add(tmpFile.getPath());
                    if (isCancelled()) return null;
                } catch (Exception e) {
                    Log.d("Exception on Imagedownloader task", e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
            return picturesLocaleUris;
        }

        @Override
        protected void onPostExecute(ArrayList<String> localUrls) {
            if( localUrls != null ) {
                //nothing to do yet
            }else {
                Toast.makeText(getActivity(), R.string.lost_connection, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private class ImageAdapter extends BaseAdapter {
        private Context mContext;

        // references to our images
        private ArrayList<String> mThumbUris;

        public ImageAdapter(Context c, int numOfImages) { //, ArrayList<String> imagesLocalUris) {
            mContext = c;
            //mThumbUris = imagesLocalUris;
        }

        public int getCount() {
            return 10;//mThumbUris.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            //imageView.setImageURI(Uri.fromFile(new File(mThumbUris.get(position))));
            imageView.setImageResource(R.drawable.hundred);
            return imageView;
        }



    }

    private class WhatWasHereListView extends BaseAdapter {
        private Context mContext;

        // references to our images
        private ArrayList<String> mThumbUris;
        private ArrayList<String> headers;
        private List<List<HashMap<String,Object>>> pictures;
        private LayoutInflater mInflater = null;

        public WhatWasHereListView(Context c, int numOfImages, ArrayList<String> headers, List<List<HashMap<String,Object>>> pictures ) { //, ArrayList<String> imagesLocalUris) {
            this.mContext = c;
            this.mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.headers = headers;
            this.pictures = pictures;
            //mThumbUris = imagesLocalUris;
        }

        public int getCount() {
            return 5;//mThumbUris.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if( convertView == null) {
                convertView = mInflater.inflate(R.layout.fragment_what_was_here_lv_item, parent, false);
            }

            TextView tv = (TextView) convertView.findViewById(R.id.event_title);
            tv.setText(headers.get(position));
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            rlp.addRule(RelativeLayout.BELOW, R.id.event_title);
            NonScrollableGridView gv = new NonScrollableGridView(getActivity(),null);
            gv.setColumnWidth(100);
            gv.setNumColumns(3);
            gv.setVerticalSpacing(20);
            gv.setHorizontalSpacing(20);
            gv.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            gv.setGravity(Gravity.CENTER);
            gv.setFastScrollEnabled(true);
            gv.setScrollingCacheEnabled(true);
            ViewGroup vg = (ViewGroup) convertView.findViewById(R.id.containerRelativeLayout);
            gv.setAdapter(new ImageAdapter(mContext, 10));
            vg.addView(gv, rlp);

            return convertView;
        }



    }

}


