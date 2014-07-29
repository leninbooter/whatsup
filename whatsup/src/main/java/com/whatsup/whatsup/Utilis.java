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

    static String getMonthName(Context c, String number) {
        if( number.equals( "01" ) ) return c.getString( R.string.january );
        if( number.equals( "02" ) ) return c.getString( R.string.february );
        if( number.equals( "03" ) ) return c.getString( R.string.march );
        if( number.equals( "04" ) ) return c.getString( R.string.april );
        if( number.equals( "05" ) ) return c.getString( R.string.may );
        if( number.equals( "06" ) ) return c.getString( R.string.june );
        if( number.equals( "07" ) ) return c.getString( R.string.july );
        if( number.equals( "08" ) ) return c.getString( R.string.august );
        if( number.equals( "09" ) ) return c.getString( R.string.septembter );
        if( number.equals( "10" ) ) return c.getString( R.string.october );
        if( number.equals( "11" ) ) return c.getString( R.string.november );
        if( number.equals( "12" ) ) return c.getString( R.string.december );
        return null;
    }
}
