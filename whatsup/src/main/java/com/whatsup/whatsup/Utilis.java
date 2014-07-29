/**
 * Created by alenin on 19/06/2014.
 */

package com.whatsup.whatsup;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class Utilis{

    public static void getRouteFromMaps(Context currentContext, String lat, String lon) {
        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?&daddr=%s,%s", lat, lon);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        try
        {
            currentContext.startActivity(intent);
        }
        catch(ActivityNotFoundException ex)
        {
            try
            {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                currentContext.startActivity(unrestrictedIntent);
            }
            catch(ActivityNotFoundException innerEx)
            {
                Toast.makeText(currentContext, R.string.no_maps, Toast.LENGTH_LONG).show();
            }
        }
    }

    public static String downloadUrl( String strURL ) throws IOException {
        String data = "";
        InputStream iStream = null;
        try {
            URL url = new URL(strURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(20000);
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader( new InputStreamReader( iStream ) );
            StringBuffer sb = new StringBuffer();
            String line = "";
            while( (line = br.readLine()) != null ) {
                sb.append(line);
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
}
