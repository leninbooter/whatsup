package com.whatsup.whatsup;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends Activity
        implements  NavigationDrawerFragment.NavigationDrawerCallbacks,
                    WhatItIsHotFragment.OnFragmentWhatItIsHotFragmentListener,
                    SpecialsFragment.OnFragmentSpecialsFragmentListener {

    private static String NO_CONNECTION = "noconnection";
    private static String HOT_PLACES = "hotplaces";
    private static String WHAT_IS_HERE = "whatishere";
    private static String FOR_TODAY = "fortoday";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Control of loaded fragments
     *
     * NoConnectionFragment: 0 at loadedFragments
     * HotPlacesFragment: 1 at loadedFragments
     * whatItIsUpToday: 2 at loadedFragments
     * private boolean[] loadedFragments = new boolean[3];
     */


    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private String currentFragmentTag = "";

    private String previousFragmentTag = null;

    public void setCurrentFragmentTag(String tag) {
        currentFragmentTag = tag;
    }

    public void ShowNoConnectionMessage() {
        previousFragmentTag = currentFragmentTag;
        setCurrentFragmentTag(NO_CONNECTION);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, NoConnectionFragment.newInstance(), currentFragmentTag)
                .commit();
    }

    public void loadWhatItIsHotFragment() {
        if( currentFragmentTag.equals(HOT_PLACES)) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove( getFragmentManager().findFragmentByTag(currentFragmentTag) );
            fragmentTransaction.commit();
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, WhatItIsHotFragment.newInstance(), HOT_PLACES);
        if( !currentFragmentTag.equals(NO_CONNECTION) && !currentFragmentTag.equals(HOT_PLACES))
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        previousFragmentTag = null;
    }

    public void loadSpecialsFragment(String place_id, String place_name) {
        Calendar rightNow = Calendar.getInstance();
        String datetime = String.valueOf(rightNow.get(rightNow.YEAR)) + "-" + String.valueOf(rightNow.get(rightNow.MONTH) + 1) + "-" + String.valueOf(rightNow.get(rightNow.DAY_OF_MONTH)) + " " + String.valueOf(rightNow.get(rightNow.HOUR_OF_DAY)) + ":" + String.valueOf(rightNow.get(rightNow.MINUTE)) + ":" + String.valueOf(rightNow.get(rightNow.SECOND));

        if( place_id == null && place_name == null ) {
            place_id = getFragmentManager().findFragmentByTag(currentFragmentTag).getArguments().getString("place_id");
            place_name = getFragmentManager().findFragmentByTag(currentFragmentTag).getArguments().getString("place_name");
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove( getFragmentManager().findFragmentByTag(currentFragmentTag) );
            fragmentTransaction.commit();
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, SpecialsFragment.newInstance(place_id, place_name, datetime), WHAT_IS_HERE);
        if( !currentFragmentTag.equals(NO_CONNECTION) && !currentFragmentTag.equals(WHAT_IS_HERE))
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        previousFragmentTag = null;
    }

    public void loadWhatItIsUpTodayFragment() {
        if( currentFragmentTag.equals(FOR_TODAY)) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove( getFragmentManager().findFragmentByTag(currentFragmentTag) );
            fragmentTransaction.commit();
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, WhatItIsUpToday.newInstance(), FOR_TODAY);
        if( !currentFragmentTag.equals(NO_CONNECTION) && currentFragmentTag.equals(FOR_TODAY) )
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        setCurrentFragmentTag(FOR_TODAY);
        previousFragmentTag = null;
    }

    private void refreshFragment(String fragmentTag) {
            if( fragmentTag.equals(HOT_PLACES) )
                loadWhatItIsHotFragment();
            else {
                if( fragmentTag.equals(WHAT_IS_HERE) )
                    loadSpecialsFragment(null, null);
                else {
                    if( fragmentTag.equals(FOR_TODAY) )
                        loadWhatItIsUpTodayFragment();
                    else {
                        return;
                    }
                }
            }
        Toast.makeText(getBaseContext(), currentFragmentTag, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {

        switch (position) {
            case 0:
                if ( isOnline() ) {
                    loadWhatItIsHotFragment();
                }else {
                    Toast.makeText( getBaseContext(), R.string.cannot_connect, Toast.LENGTH_LONG).show();
                }
                break;
            case 1:
                loadWhatItIsUpTodayFragment();
                break;
            case 2:

                break;
        }

    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.whats_hot);
                break;
            case 2:
                mTitle = getString(R.string.whats_up_today);
                break;
            case 3:
                mTitle = getString(R.string.whats_free);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_refresh:
                // Reload current fragment
                if( previousFragmentTag == null)
                    refreshFragment(currentFragmentTag);
                else
                    refreshFragment(previousFragmentTag);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove( getFragmentManager().findFragmentByTag(currentFragmentTag) );
        fragmentTransaction.commit();
        super.onBackPressed();
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    public void setmTitle(String title) {
        mTitle = title;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
