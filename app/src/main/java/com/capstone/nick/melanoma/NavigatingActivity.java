package com.capstone.nick.melanoma;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

/**
 * Class representing the navigation drawer. Opens/closes drawer, adds options to drawer, etc.
 */
class NavigatingActivity extends AppCompatActivity {
    private NavigationView mDrawerList;
    private String[] listOps;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    private boolean loggedIn;
    private String email;

    /**
     * Show the list of options based on whether user is logged in or not.
     * @param userLogged status of log-in
     * @param userEmail email address of user
     */
    protected void onCreateDrawer(boolean userLogged, String userEmail) {
        loggedIn = userLogged;
        email = userEmail;

        mDrawerList = (NavigationView)findViewById(R.id.navList);
        mDrawerList.getMenu().clear();
        if(loggedIn){
            mDrawerList.inflateMenu(R.menu.navmenu_loggedin);
            mDrawerList.inflateHeaderView(R.layout.drawer_header);
        } else {
            mDrawerList.inflateMenu(R.menu.navmenu_loggedout);
            //mDrawerList.inflateHeaderView(R.layout.drawer_header);
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
                        intent.putExtra("LOGGEDIN", loggedIn);
                        intent.putExtra("EMAIL", email);
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
}
