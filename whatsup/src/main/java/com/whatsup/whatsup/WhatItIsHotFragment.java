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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;


/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WhatItIsHotFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WhatItIsHotFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class WhatItIsHotFragment extends ListFragment {

    private Params parameters;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //private OnFragmentInteractionListener mListener;

    /*
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WhatItIsHotFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WhatItIsHotFragment newInstance() {
        WhatItIsHotFragment fragment = new WhatItIsHotFragment();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        return fragment;
    }
    public WhatItIsHotFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parameters = new Params();

        String strUrl = parameters.REST_SERVER + "/whatsup/slim/public/index.php/hotplaces/1";
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(strUrl);

        //placesListView = (ListView) getActivity().findViewById(R.id.lv_hot_places);
    }

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        /*if(placesListView == null) {
            Toast.makeText(getActivity().getBaseContext(), "Null", Toast.LENGTH_SHORT).show();
        }
        return placesListView;
    }*/
    /*public void setAdapter(SimpleAdapter adapter) {
        placesListView.setAdapter(adapter);
    }*/
    /*
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

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
    /*public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }*/
    private String downloadUrl( String strURL ) throws IOException {
        String data = "";
        InputStream iStream = null;
        try {
            URL url = new URL(strURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader( new InputStreamReader( iStream ) );
            StringBuffer sb = new StringBuffer();
            String line = "";
            while( (line = br.readLine()) != null ) {
                sb.append(line);
                //Toast.makeText(getActivity().getBaseContext(), line, Toast.LENGTH_SHORT).show();
            }
            data = sb.toString();
            br.close();
        }catch ( Exception e ) {
            Log.d("Exception while downloading url", e.toString());
        }finally {
            iStream.close();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {
        String data = null;

        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            }catch( Exception e ) {
                Log.d("Background Task ", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            ListViewLoaderTask listViewLoaderTask = new ListViewLoaderTask();
            listViewLoaderTask.execute(result);
        }
    }
    private class ListViewLoaderTask extends  AsyncTask<String, Void, SimpleAdapter> {
        JSONObject jObject;

        @Override
        protected SimpleAdapter doInBackground(String... strJson) {
            try {
                jObject = new JSONObject(strJson[0]);
                HotPlacesJSONparser placeJsonParser = new HotPlacesJSONparser();
                placeJsonParser.parse(jObject);
            }catch( Exception e ) {
                Log.d("JSON Exception1", e.toString());
            }

            HotPlacesJSONparser hotPlacesJsonParser = new HotPlacesJSONparser();
            List<HashMap<String, Object>> places = null;

            try {
                places = hotPlacesJsonParser.parse(jObject);
            }catch ( Exception e ) {
                Log.d("Exception", e.toString());
            }
            String[] from = { "name", "gp_formatted_address", "gp_icon", "fullness" };
            int[] to = { R.id.place_name, R.id.place_address, R.id.place_logo, R.id.place_fullness };
            SimpleAdapter adapter = new SimpleAdapter(getActivity(), places, R.layout.hot_places_list_view_item, from, to);

            return adapter;
        }

        @Override
        protected void onPostExecute(SimpleAdapter adapter) {
            setListAdapter(adapter);
            for(int i=0; i<adapter.getCount(); i++) {
                HashMap<String,Object> hm = (HashMap<String, Object>) adapter.getItem(i);
                String imgURL = (String) hm.get("gp_icon_path");
                ImageLoaderTask imageLoaderTask = new ImageLoaderTask();

                HashMap<String, Object> hmDownload = new HashMap<String, Object>();
                hm.put("gp_icon_path", imgURL);
                hm.put("position", i);

                imageLoaderTask.execute(hm);
            }
        }

    }

    private class ImageLoaderTask extends AsyncTask<HashMap<String, Object>, Void, HashMap<String, Object>> {

        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String,Object>... hm) {
            Log.d("IMage downloader tastk doInBackground: ","entro");
            InputStream iStream = null;
            String imgUrl = (String) hm[0].get("gp_icon_path");
            int position = (Integer) hm[0].get("position");
            URL url;
            try {
                url = new URL(imgUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                Log.d("gp_icon_path: ", imgUrl);
                File cacheDirectory = getActivity().getCacheDir();
                File tmpFile = new File(cacheDirectory.getPath() + "/wpa_" + position + ".png");
                FileOutputStream fOutStream = new FileOutputStream(tmpFile);
                Bitmap b = BitmapFactory.decodeStream(iStream);
                b.compress(Bitmap.CompressFormat.PNG,100,fOutStream);
                fOutStream.flush();
                fOutStream.close();
                HashMap<String, Object>  hmBitmap = new HashMap<String, Object>();
                hmBitmap.put("place_logo", tmpFile.getPath());
                hmBitmap.put("position", position);
                Log.d("Supuestamente descargó: ", tmpFile.getPath());
                Log.d("IMage downloader tastk doInBackground: ","salida");
                return hmBitmap;
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> result) {
            Log.d("IMage downloader tastk onPostExecute: ","entrada");
            String path = (String) result.get("place_logo");
            int position = (Integer) result.get("position");
            SimpleAdapter adapter = (SimpleAdapter) getListAdapter();
            HashMap<String, Object> hm = (HashMap<String, Object>) adapter.getItem(position);
            Log.d("Coger imagen desde: ", path + " position: " + String.valueOf(position) +  " Adapter tiene: " + String.valueOf(adapter.getCount()) );
            hm.put("gp_icon", path);
            //adapter.notifyDataSetChanged();
            Log.d("IMage downloader tastk onPostExecute: ","salida");
        }

    }
}
