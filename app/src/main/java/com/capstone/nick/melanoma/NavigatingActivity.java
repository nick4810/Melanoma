package com.capstone.nick.melanoma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

/**
 * Class representing the navigation drawer. Opens/closes drawer, adds options to drawer, etc.
 */
class NavigatingActivity extends AppCompatActivity {
    private NavigationView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    private boolean loggedIn;
    private String email;
    private String username;

    /**
     * Show the list of options based on whether user is logged in or not.
     */
    protected void onCreateDrawer() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        email = prefs.getString("USEREMAIL", "");
        username = prefs.getString("USERNAME", "");
        loggedIn = !email.equals("");

        mDrawerList = (NavigationView)findViewById(R.id.navList);
        mDrawerList.getMenu().clear();

        if(loggedIn){
            mDrawerList.inflateMenu(R.menu.navmenu_loggedin);

            View hView =  mDrawerList.inflateHeaderView(R.layout.drawer_header);
            LinearLayout drawer_lay = (LinearLayout)hView.findViewById(R.id.header_linLay);
            ImageView prof_img = (ImageView)hView.findViewById(R.id.img_profile);
            TextView nameText = (TextView)hView.findViewById(R.id.drawer_name);
            TextView emailText = (TextView)hView.findViewById(R.id.drawer_email);

            //change the profile picture/header if user has them set
            String profPath =android.os.Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + "/" + email + "/profile.jpg";
            String headPath =android.os.Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + "/" + email + "/header.jpg";
            File chk_prof = new File(profPath);
            File chk_head = new File(headPath);

            if(chk_prof.exists()) {
                prof_img.setImageBitmap(decodeSampledBitmapFromResource(profPath, 75, 75));
            }
            if(chk_head.exists()) {
                Drawable hImg = new BitmapDrawable(getResources(), decodeSampledBitmapFromResource(headPath, 215, 145));
                drawer_lay.setBackground(hImg);
                //drawer_lay.setImageBitmap(decodeSampledBitmapFromResource(headPath, 75, 75));
            }

            try {
                nameText.setText(username);//set the user's name in header
                emailText.setText(email);//set the user's email in header
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mDrawerList.inflateMenu(R.menu.navmenu_loggedout);
        }

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        addDrawerItems();
        setupDrawer();

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

    }

    /**
     * Start intent based on the user's selection.
     */
    private void addDrawerItems() {
        mDrawerList.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        Intent intent = new Intent();
                        if(loggedIn) {
                            if(menuItem.getItemId() == R.id.nav_home) {
                                intent = new Intent(NavigatingActivity.this, MainScreen.class);
                            } else if(menuItem.getItemId() == R.id.nav_profile) {
                                intent = new Intent(NavigatingActivity.this, ViewProfile.class);
                            } else if(menuItem.getItemId() == R.id.nav_myData) {
                                intent = new Intent(NavigatingActivity.this, ViewData.class);
                            } else if(menuItem.getItemId() == R.id.nav_allPatients) {
                                intent = new Intent(NavigatingActivity.this, AllPatients.class);
                            } else if(menuItem.getItemId() == R.id.nav_settings) {
                                intent = new Intent(NavigatingActivity.this, SettingsActivity.class);
                            } else if(menuItem.getItemId() == R.id.nav_logOut) {
                                intent = new Intent(NavigatingActivity.this, MainScreen.class);
                                intent.putExtra("LOGMEOUT", true);
                            }
                        } else {
                            if(menuItem.getItemId() == R.id.nav_home) {
                                intent = new Intent(NavigatingActivity.this, MainScreen.class);
                            } else if(menuItem.getItemId() == R.id.nav_signUp) {
                                intent = new Intent(NavigatingActivity.this, NewUser.class);
                            }
                        }
                        startActivity(intent);

                        return true;
                    }
                });

    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Menu");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Activate the navigation drawer toggle
        return (mDrawerToggle.onOptionsItemSelected(item));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Used to create thumbnails for images
     * @param path Image file directory
     * @param reqWidth Requested width of thumbnail
     * @param reqHeight Requested height of thumbnail
     * @return Bitmap for thumbnail
     */
    private Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * Finds the largest number to divide by while still keeping requested width/height
     * @param options Options for creating Bitmap
     * @param reqWidth Requested width of thumbnail
     * @param reqHeight Requested height of thumbnail
     * @return Largest value for creating smallest thumbnail
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
