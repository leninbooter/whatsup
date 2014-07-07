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
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
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
import org.w3c.dom.Text;

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
        //String strUrl = parameters.REST_SERVER + "/whatsup/slim/public/index.php/place/getevents/" + getArguments().getString(PLACE_ID) + "/" + getArguments().getString(DATETIME);
        String strUrl = parameters.REST_SERVER + "/whatsup/slim/public/index.php/place/getevents/11051/" + getArguments().getString(DATETIME);
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
                event.put("datetime_from", jEvent.getString("datetime_from"));
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



    private class ListViewLoaderTask extends  AsyncTask<String, Void, List<HashMap<String, Object>> > {

        @Override
        protected List<HashMap<String, Object>>  doInBackground(String... strJson) {
            EventsJSONparser eventsJSONparser = new EventsJSONparser();
            List<HashMap<String, Object>> events = null;

            try {
                jObject = new JSONObject(strJson[0]);
                events = eventsJSONparser.parse(jObject);
            }catch ( Exception e ) {
                Log.d("Exception", e.toString());
            }
            //String[] from = { "title"};
            //int[] to = { R.id.event_title};
            //SimpleAdapter adapter = new SimpleAdapter(getActivity(), events, R.layout.fragment_what_was_here_lv_item, from, to);
            return events;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, Object>>  adapter) {
            ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> hm = new HashMap<String, String>();
            String year, month, year_c, month_c;
            Boolean first = true;

            year = adapter.get(0).get("datetime_from").toString().substring(0,4);
            month = adapter.get(0).get("datetime_from").toString().substring(5,7);
            year_c = adapter.get(0).get("datetime_from").toString().substring(0,4);
            month_c = adapter.get(0).get("datetime_from").toString().substring(5,7);

            hm.put("title", year + " - " + getMonthName(month) );
            hm.put("datetime_from", "" );
            items.add( hm );
            for(int i=0; i < adapter.size() ; i++) {
                Log.d( "i: ", adapter.get(i).get("title").toString() );
                Log.d( "i: ", adapter.get(i).get("datetime_from").toString() );
                year = adapter.get(i).get("datetime_from").toString().substring(0,4);
                month = adapter.get(i).get("datetime_from").toString().substring(5,7);

                if( year.equals(year_c) && month.equals(month_c) ) {
                    hm = new HashMap<String, String>();
                    hm.put("title", adapter.get(i).get("title").toString() );
                    hm.put("datetime_from", adapter.get(i).get("datetime_from").toString() );
                    items.add( hm );
                }else {
                    hm = new HashMap<String, String>();
                    hm.put("title", year + " - " + getMonthName(month) );
                    hm.put("datetime_from", "" );
                    items.add( hm );
                    hm = new HashMap<String, String>();
                    hm.put("title", adapter.get(i).get("title").toString() );
                    hm.put("datetime_from", adapter.get(i).get("datetime_from").toString() );
                    items.add( hm );
                }

                year_c = year;
                month_c = month;
            }
            WhatWasHereListViewAdapter adapter_new = new WhatWasHereListViewAdapter( getActivity(), items );
            getView().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            setListAdapter(adapter_new);

            if( adapter.size() == 0 ) {
                getView().findViewById(R.id.no_info_not_rellay).setVisibility(View.VISIBLE);
            }
        }

        private String getMonthName(String number) {
            if( number.equals("01") )
                return getString(R.string.january);
            else {
                if( number.equals("02") )
                    return getString(R.string.february);
                else {
                    if( number.equals("03" ) )
                        return getString(R.string.march);
                    else {
                        if( number.equals("04" ) )
                            return getString(R.string.april);
                        else {
                            if( number.equals("05" ) )
                                return getString(R.string.may);
                            else {
                                if( number.equals("06" ) )
                                    return getString(R.string.june);
                                else {
                                    if( number.equals("07" ) )
                                        return getString(R.string.july);
                                    else {
                                        if( number.equals("08" ) )
                                            return getString(R.string.august);
                                        else {
                                            if( number.equals("09" ) )
                                                return getString(R.string.septembter);
                                            else {
                                                if( number.equals("10" ) )
                                                    return getString(R.string.october);
                                                else {
                                                    if( number.equals("11" ) )
                                                        return getString(R.string.november);
                                                    else {
                                                        if( number.equals("12" ) )
                                                            return getString(R.string.december);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return null;
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

    private class ImageAdapter extends ArrayAdapter<WwhGvItem> {
        private Context mContext;
        int mResourceId;
        private ArrayList<WwhGvItem> mThumbUris;

        public ImageAdapter(Context mContext, int mResourceId, ArrayList<WwhGvItem> mThumbsUris) {
            super(mContext, mResourceId, mThumbsUris);
            this.mContext = mContext;
            this.mResourceId = mResourceId;
            this.mThumbUris = mThumbsUris;
        }

        public int getCount() {
            Log.d("on getCount", String.valueOf( mThumbUris.size() ) );
            return mThumbUris.size();
        }

        public WwhGvItem getItem(int position) {
            Log.d("on getItem ", "Entered");
            return mThumbUris.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d("on getView ", "Entered");
            GvViewHolder vh;

            if (convertView == null) {  // if it's not recycled, initialize some attributes
                convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(mResourceId, parent, false);
                vh = new GvViewHolder();
                vh.imageItem = (ImageView ) convertView.findViewById(R.id.imgItem);
                convertView.setTag(vh);
            } else {
                vh = (GvViewHolder) convertView.getTag();
            }
            WwhGvItem item = getItem(position);
            Log.d("Number of items on item: ", item.getImage().toString());
            vh.imageItem.setImageDrawable( item.getImage());
            return convertView;
        }

        public void setmThumbUri( int position, WwhGvItem item ) {
            this.mThumbUris.set( position, item );
            notifyDataSetChanged();
        }

    }

    public class ImageAdapter1 extends BaseAdapter {
        private Context mContext;

        public ImageAdapter1(Context c) {
            mContext = c;
        }

        public int getCount() {
            Log.d("on getCount", "entered");
            return 500; //mThumbIds.length;
        }

        public Object getItem(int position) {
            Log.d("on getItem", "entered");
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d("on getView", "entered");
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mThumbIds[0]);
            return imageView;
        }

        // references to our images
        private Integer[] mThumbIds = {
                R.drawable.empty_frame
        };
    }

    static class GvViewHolder {
        ImageView imageItem;
    }

    private class WhatWasHereListViewAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<Integer> mTypes = new ArrayList<Integer>();
        private ArrayList<HashMap<String, String>> mItems;


        public WhatWasHereListViewAdapter( Context c, ArrayList<HashMap<String, String>> mItems) {
            this.mContext = c;
            this.mItems = mItems;
            for( int i = 0; i < mItems.size(); i++ ) {
                Log.d("on WhatWasHereListViewAdapter", mItems.get(i).get("title").toString());
                Log.d("on WhatWasHereListViewAdapter", mItems.get(i).get("datetime_from").toString());
                if( mItems.get(i).get("datetime_from").toString().equals("") )
                    this.mTypes.add(0); //disabled row for dates
                else this.mTypes.add(1); //enabled row for event description
            }
        }

        @Override
        public int getCount() {
            Log.d("on getCount", "in here");
            return mItems.size();
        }

        @Override
        public WwhLvItem getItem( int position ) {
            Log.d("on getItem", "in here");
            return null;
        }

        @Override
        public long getItemId(int position) {
            return mTypes.get(position);
        }

        @Override
        public boolean isEnabled(int position) {
            if( getItemViewType(position) == 0)
                return false;
            else
                return true;
        }

        @Override
        public int getItemViewType(int position ) {
            return mTypes.get( position );
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemViewHolder vh;
            if( convertView == null) {
                vh = new ItemViewHolder();
                if( getItemViewType(position) == 0 ) {
                    convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_what_was_here_lv_item_disabled, parent, false);
                    vh.title = ( TextView ) convertView.findViewById(R.id.year_month);
                }
                if( getItemViewType(position) == 1 ) {
                    convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_what_was_here_lv_item, parent, false);
                    vh.title = (TextView) convertView.findViewById(R.id.event_title);
                    vh.subtitle = (TextView) convertView.findViewById(R.id.date);
                }
                convertView.setTag(vh);
            }else {
                vh = ( ItemViewHolder ) convertView.getTag();
            }

            vh.title.setText( mItems.get(position).get("title") );
            if(vh == null) Log.d("vh", "is null");
            if(vh.subtitle == null) Log.d("vh.subtitle", "is null");
            if( getItemViewType(position) == 1 ) {
                vh.subtitle.setText(mItems.get(position).get("datetime_from"));
            }
            return convertView;
        }
    }

    static class ItemViewHolder {
        TextView title;
        TextView subtitle;
    }

}


