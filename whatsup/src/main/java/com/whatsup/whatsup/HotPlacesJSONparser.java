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
        Float fullness, capacity;

        try {
            place.put("name", jPlace.getString("name"));
            place.put("gp_formatted_address", jPlace.getString("gp_formatted_address"));
            place.put("gp_icon", R.drawable.blank );
            place.put("gp_icon_path", jPlace.getString("gp_icon"));
            fullness = Float.valueOf( jPlace.getString("fullness") );
            capacity = Float.valueOf( jPlace.getString("capacity") );
            fullness = (fullness / capacity) * 100;
            Log.d("Fullness: ", String.valueOf(fullness));
            if( fullness >= 0.0 && fullness <= 10.0 ) {
                place.put("fullness", R.drawable.ten);
            }else {
                if( fullness >= 11.0 && fullness <= 20.0 ) {
                    place.put("fullness", R.drawable.twenty);
                }else {
                    if( fullness >= 21.0 && fullness <= 30.0 ) {
                        place.put("fullness", R.drawable.thirty);
                    }else {
                        if( fullness >= 31.0 && fullness <= 40.0 ) {
                            place.put("fullness", R.drawable.fourty);
                        }else {
                            if( fullness >= 41.0 && fullness <= 50.0 ) {
                                place.put("fullness", R.drawable.fifty);
                            }else {
                                if( fullness >= 51.0 && fullness <= 60.0 ) {
                                    place.put("fullness", R.drawable.sixty);
                                }else {
                                    if( fullness >= 61.0 && fullness <= 70.0 ) {
                                        place.put("fullness", R.drawable.seventy);
                                    }else {
                                        if( fullness >= 71.0 && fullness <= 80.0 ) {
                                            place.put("fullness", R.drawable.eighty);
                                        }else {
                                            if( fullness >= 81.0 && fullness <= 90.0 ) {
                                                place.put("fullness", R.drawable.ninety);
                                            }else {
                                                place.put("fullness", R.drawable.hundred);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }catch (JSONException e ) {
            e.printStackTrace();
        }
        return place;
    }

}
