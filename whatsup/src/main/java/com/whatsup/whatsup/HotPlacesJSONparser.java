package com.whatsup.whatsup;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alenin on 04/06/2014.
 */
public class HotPlacesJSONparser {

    public List<HashMap<String,Object>> parse (JSONObject jObject) {
        JSONArray jPlaces = null;

        try {
            jPlaces = jObject.getJSONArray("places");
            Log.d("Gotten places: ", String.valueOf(jPlaces.length()));
        }catch ( JSONException e ) {
            e.printStackTrace();
        }
        return getPlaces(jPlaces);
    }

    private List<HashMap<String,Object>> getPlaces(JSONArray jPlaces) {
        int placeCount = jPlaces.length();
        List<HashMap<String, Object>> placeList = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> place = null;

        for( int i = 0 ; i < placeCount ; i++) {
            try {
                place = getPlace((JSONObject)jPlaces.get(i));
                placeList.add(place);
            }catch ( JSONException e ) {
                Log.d("Exception from getplaces: ", e.getMessage());
                e.printStackTrace();
            }
        }
        return placeList;
    }

    private HashMap<String, Object> getPlace(JSONObject jPlace) {
        Log.d("Hot place parser getPlace: ","entrada");
        HashMap<String, Object> place = new HashMap<String, Object>();

        try {
            place.put("name", jPlace.getString("name"));
            place.put("gp_formatted_address", jPlace.getString("gp_formatted_address"));
            place.put("gp_icon", R.drawable.blank );
            place.put("gp_icon_path", jPlace.getString("gp_icon"));
            place.put("fullness", jPlace.getString("fullness"));
            place.put("capacity", jPlace.getString("capacity"));
        }catch (JSONException e ) {
            e.printStackTrace();
        }
        return place;
    }

}
