package com.whatsup.whatsup;


import android.app.Activity;
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
import android.widget.ListView;
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
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private OnFragmentWwhFragmentListener mListener;
    private View mWhatWasHereListView;
    private Params parameters;
    private DownloadTask downloadTask;
    private ListViewLoaderTask listViewLoaderTask;
    private String data = null;
    private List<List<HashMap<String,Object>>> pictures = new ArrayList<List<HashMap<String, Object>>>();
    private ArrayList<String> picturesLocaleUris = new ArrayList<String>();
    private JSONObject jObject;
    private HashMap<Integer, HashMap<String, String>> mEvents = new HashMap<Integer, HashMap<String, String>>();
    private WhatWasHereListViewAdapter whatWasHereListViewAdapter;
    private Boolean mPaused = false;

    public interface OnFragmentWwhFragmentListener {
        public void ShowNoConnectionMessage();
        public void setmTitle(String title);
        public void setCurrentFragmentTag( String tag );
        public void LoadEventsPicturesFragment( String event_id, String event_name, String event_date );
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnFragmentWwhFragmentListener) activity;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate", "entered");
        if( savedInstanceState == null ) {
            parameters = new Params();
            downloadTask = new DownloadTask();
            //String strUrl = parameters.REST_SERVER + "/whatsup/slim/public/index.php/place/getevents/" + getArguments().getString(PLACE_ID) + "/" + getArguments().getString(DATETIME);
            String strUrl = parameters.REST_SERVER + "/whatsup/slim/public/index.php/place/getevents/11051/" + getArguments().getString(DATETIME);
            downloadTask.execute(strUrl);
        }else {
            Log.d("Fragment wwh:", "restored onCreate");
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        Log.d("Fragment wwh:", "onCreateView");
        mWhatWasHereListView = (RelativeLayout) inflater.inflate( R.layout.fragment_what_was_here, container, false );
        TextView place_name = (TextView) mWhatWasHereListView.findViewById(R.id.place_name);
        place_name.setText(getString(R.string.what_was_at) + " " + getArguments().getString(PLACE_NAME) + "?");
        return mWhatWasHereListView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.setCurrentFragmentTag("events");
        mListener.setmTitle( getArguments().getString(PLACE_NAME) );
        if( mPaused ) {
            Log.d("Fragment wwh:", "restored onResume");
            getView().findViewById( R.id.progress_bar ).setVisibility(View.GONE);
            mPaused = false;
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.d("on", "onPause");
        mPaused = true;
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
        Log.d("on", "onDetach");
        mListener = null;

    }
    public void onListItemClick(ListView l, View v, int position, long id) {
        mListener.LoadEventsPicturesFragment(
                                                    whatWasHereListViewAdapter.getItem( position ).get( "id" ),
                                                    whatWasHereListViewAdapter.getItem( position ).get( "title" ),
                                                    whatWasHereListViewAdapter.getItem( position ).get( "datetime_from" )
                                                );

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
                event.put("id", jEvent.getString("pk_event_id"));
                event.put("title", jEvent.getString("title"));
                event.put("datetime_from", jEvent.getString("datetime_from"));
            }catch (JSONException e ) {
                e.printStackTrace();
            }
            return event;
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
            return events;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, Object>>  adapter) {
            ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> hm = new HashMap<String, String>();
            String year, month, year_c, month_c;
            Boolean first = true;

            String datestr = adapter.get(0).get("datetime_from").toString();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            try {
                calendar.setTime( fmt.parse(datestr) );
            }catch ( ParseException e) {

            }
            year_c = String.valueOf(calendar.get(Calendar.YEAR));
            fmt = new SimpleDateFormat("MM");
            month_c = fmt.format(calendar.getTime());
            fmt = new SimpleDateFormat("yyyy / MMMM");
            hm.put("title", String.valueOf(fmt.format(calendar.getTime())) );
            hm.put("datetime_from", "" );
            items.add( hm );
            for(int i=0; i < adapter.size() ; i++) {
                datestr = adapter.get( i ).get("datetime_from").toString();
                fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                calendar = Calendar.getInstance();
                try {
                    calendar.setTime( fmt.parse(datestr) );
                }catch ( ParseException e) {

                }
                year = String.valueOf(calendar.get(Calendar.YEAR));
                fmt = new SimpleDateFormat("MM");
                month = String.valueOf(fmt.format(calendar.getTime()));

                if( year.equals(year_c) && month.equals(month_c) ) {
                    hm = new HashMap<String, String>();
                    hm.put("id", adapter.get(i).get("id").toString() );
                    hm.put("title", adapter.get(i).get("title").toString() );
                    hm.put("datetime_from", adapter.get(i).get("datetime_from").toString() );
                    items.add( hm );
                }else {
                    hm = new HashMap<String, String>();
                    hm.put("id", adapter.get(i).get("id").toString() );
                    fmt = new SimpleDateFormat("yyyy / MMMM");
                    hm.put("title", String.valueOf(fmt.format(calendar.getTime())) );
                    hm.put("datetime_from", "" );
                    items.add( hm );
                    hm = new HashMap<String, String>();
                    hm.put("id", adapter.get(i).get("id").toString() );
                    hm.put("title", adapter.get(i).get("title").toString() );
                    hm.put("datetime_from", adapter.get(i).get("datetime_from").toString() );
                    items.add( hm );
                }

                year_c = year;
                month_c = month;
            }
            whatWasHereListViewAdapter = new WhatWasHereListViewAdapter( getActivity(), items );
            try {
                if (adapter.size() == 0) {
                    getView().findViewById(R.id.no_info_not_rellay).setVisibility(View.VISIBLE);
                } else {
                    getView().findViewById(R.id.progress_bar).setVisibility(View.GONE);
                    setListAdapter(whatWasHereListViewAdapter);
                }
            }catch ( NullPointerException e ) {
                Log.e( "NullPointerException onPostExecute from ListViewLoaderTask", "getView is null.");
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

    private class WhatWasHereListViewAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<Integer> mTypes = new ArrayList<Integer>();
        private ArrayList<HashMap<String, String>> mItems;
        private LayoutInflater mInflater;

        public WhatWasHereListViewAdapter( Context c, ArrayList<HashMap<String, String>> mItems) {
            this.mContext = c;
            this.mItems = mItems;
            this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for( int i = 0; i < mItems.size(); i++ ) {
                if( mItems.get(i).get("datetime_from").toString().equals("") ) {
                    mItems.get(i).put("type", "0"); //disabled row for dates
                }
                else{
                    mItems.get(i).put("type", "1"); //enabled row for event description
                    String datestr = mItems.get(i).get("datetime_from");
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date inputDate = fmt.parse(datestr);
                        mItems.get(i).put("datetime_from", DateFormat.getDateInstance().format(inputDate));
                    }catch ( ParseException e) {
                        Log.d("Exception parsing date", e.getMessage() );
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public int getCount() {
            Log.d("on getCount", "in here");
            return mItems.size();
        }

        @Override
        public HashMap<String, String> getItem( int position ) {
            Log.d("on getItem", "in here");
            return mItems.get( position );
        }

        @Override
        public long getItemId(int position) {
            return 0;
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
            return Integer.valueOf( mItems.get( position ).get( "type" ) );
        }

        @Override
        public int getViewTypeCount() {
            Log.d("getViewTypeCount:", "entered");
            return 2;
        }
        // create a new view for each item referenced by the Adapter
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ItemViewHolder vh;
            int type = getItemViewType( position );

            if( convertView == null) {
                vh = new ItemViewHolder();
                switch ( type ) {
                    case 0:
                        convertView = mInflater.inflate(R.layout.fragment_what_was_here_lv_item_disabled, parent, false);
                        vh.title = ( TextView ) convertView.findViewById(R.id.year_month);
                        break;
                    case 1:
                        convertView = mInflater.inflate(R.layout.fragment_what_was_here_lv_item, parent, false);
                        vh.title = (TextView) convertView.findViewById(R.id.event_title);
                        vh.subtitle = (TextView) convertView.findViewById(R.id.date);
                        break;
                }
                convertView.setTag( vh );
            }
            else {
                vh = (ItemViewHolder) convertView.getTag();
            }

            switch ( type ) {
                case 0:
                    vh.title.setText( mItems.get(position).get("title") );
                    break;
                case 1:
                    vh.title.setText( mItems.get(position).get("title") );
                    vh.subtitle.setText( mItems.get(position).get("datetime_from") );
                    break;
            }
            return convertView;
        }
    }

    static class ItemViewHolder {
        TextView title;
        TextView subtitle;
    }
}
