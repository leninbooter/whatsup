package com.whatsup.whatsup;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;


import com.whatsup.whatsup.dummy.DummyContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class SpecialsFragment extends ListFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PLACE_ID = "place_id";
    private static final String PLACE_NAME = "place_name";
    private static final String DATETIME = "datetime";

    private OnFragmentInteractionListener mListener;
    private View mSpecialsListView;
    private Params parameters;
    private DownloadTask downloadTask;
    private ListViewLoaderTask listViewLoaderTask;
    private OnFragmentWhatItIsHotFragmentListener mCallBack;

    public interface OnFragmentWhatItIsHotFragmentListener {
        public void ShowNoConnectionMessage();
    }

    // TODO: Rename and change types of parameters
    public static SpecialsFragment newInstance(String place_id, String place_name, String datetime) {
        SpecialsFragment fragment = new SpecialsFragment();
        Bundle args = new Bundle();
        args.putString(PLACE_ID, place_id);
        args.putString(PLACE_NAME, place_name);
        args.putString(DATETIME, datetime);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SpecialsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parameters = new Params();
        downloadTask = new DownloadTask();
        String strUrl = parameters.REST_SERVER + "/whatsup/slim/public/index.php/specials/allofplacefor/" + getArguments().getString(PLACE_ID) + "/" + getArguments().getString(DATETIME);
        downloadTask.execute(strUrl);
        Log.d("Params: ", getArguments().getString(PLACE_ID) + " " + getArguments().getString(DATETIME));

        /*/ TODO: Change Adapter to display your content
        setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS));
        */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        mSpecialsListView = (RelativeLayout) inflater.inflate(R.layout.fragment_specials, container, false);
        TextView place_name = (TextView)mSpecialsListView.findViewById(R.id.place_name);
        place_name.setText( getString(R.string.what_is_at) + " " + getArguments().getString(PLACE_NAME) + "?");
        return mSpecialsListView;
    }

    /*
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }*/

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }


    private class DownloadTask extends AsyncTask<String, Integer, String> {
        String data = null;

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
                mCallBack.ShowNoConnectionMessage();
            }
        }
    }

    private class ListViewLoaderTask extends  AsyncTask<String, Void, SimpleAdapter> {
        JSONObject jObject;

        @Override
        protected SimpleAdapter doInBackground(String... strJson) {
            SpecialsJSONparser specialsJSONparser = new SpecialsJSONparser();
            List<HashMap<String, Object>> specials = null;

            try {
                jObject = new JSONObject(strJson[0]);
                specials = specialsJSONparser.parse(jObject);
            }catch ( Exception e ) {
                Log.d("Exception", e.toString());
            }
            String[] from = { "title", "price_currency", "details"};
            int[] to = { R.id.title, R.id.price_currency, R.id.concurrence_det};
            SimpleAdapter adapter = new SimpleAdapter(getActivity(), specials, R.layout.specials_list_view_item, from, to);
            return adapter;
        }

        @Override
        protected void onPostExecute(SimpleAdapter adapter) {
            setListAdapter(adapter);
        }

    }

    private class SpecialsJSONparser {

        public List<HashMap<String,Object>> parse (JSONObject jObject) {
            JSONArray jPlaces = null;

            try {
                jPlaces = jObject.getJSONArray("specials");
                Log.d("Gotten specials: ", String.valueOf(jPlaces.length()));
            }catch ( JSONException e ) {
                e.printStackTrace();
            }
            return getSpecials(jPlaces);
        }

        private List<HashMap<String,Object>> getSpecials(JSONArray jSpecials) {
            int specialCount = jSpecials.length();
            List<HashMap<String, Object>> SpecialList = new ArrayList<HashMap<String, Object>>();
            HashMap<String, Object> special = null;

            for( int i = 0 ; i < specialCount ; i++) {
                try {
                    special = getSpecial((JSONObject) jSpecials.get(i));
                    SpecialList.add(special);
                }catch ( JSONException e ) {
                    Log.d("Exception from getplaces: ", e.getMessage());
                    e.printStackTrace();
                }
            }
            return SpecialList;
        }

        private HashMap<String, Object> getSpecial(JSONObject jSpecial) {
            HashMap<String, Object> special = new HashMap<String, Object>();
            String[] days_of_week;
            String details = "";

            try {
                special.put("title", jSpecial.getString("title"));
                special.put("price_currency", jSpecial.getString("price") + " " + jSpecial.getString("currency_simbol"));
                details = jSpecial.getString("from_date") + " - " + jSpecial.getString("to_date") + "\n";
                days_of_week = jSpecial.getString("days_of_week").split(",");
                for( int i=0; i<days_of_week.length; i++ ) {
                    if ( days_of_week[i].equals("1") )
                        details = details + getString(R.string.sunday);
                    else {
                        if ( days_of_week[i].equals("2") )
                            details = details + getString(R.string.monday);
                        else {
                            if ( days_of_week[i].equals("3") )
                                details = details + getString(R.string.tuesday);
                            else {
                                if ( days_of_week[i].equals("4") )
                                    details = details + getString(R.string.wednsday);
                                else {
                                    if ( days_of_week[i].equals("5") )
                                        details = details + getString(R.string.thursday);
                                    else {
                                        if ( days_of_week[i].equals("6") )
                                            details = details + getString(R.string.friday);
                                        else {
                                            if ( days_of_week[i].equals("7") )
                                                details = details + getString(R.string.saturday);
                                        }
                                    }
                                }
                            }

                        }
                    }
                    if (i <= days_of_week.length - 2)
                        details = details + " - ";
                }
                special.put("details", details);
            }catch (JSONException e ) {
                e.printStackTrace();
            }
            return special;
        }

    }
}

