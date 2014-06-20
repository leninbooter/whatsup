/**
 * Created by alenin on 19/06/2014.
 */

package com.whatsup.whatsup;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

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
}
