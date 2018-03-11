package com.capstone.nick.melanoma;
//Credit to Ben Jakuben (http://blog.teamtreehouse.com/add-navigation-drawer-android)
//for the basic implementation of a navigation drawer

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


class NavigatingActivity extends AppCompatActivity {
    private ListView mDrawerList;
    private String[] listOps;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    private boolean loggedIn;
    private String email;

    protected void onCreateDrawer(boolean userLogged, String userEmail) {
        loggedIn = userLogged;
        email = userEmail;

        if(loggedIn){
            listOps = getResources().getStringArray(R.array.loggedInList);

        } else {
            listOps = getResources().getStringArray(R.array.loggedOutList);
        }

        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        addDrawerItems();
        setupDrawer();

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

    }

    private void addDrawerItems() {
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listOps);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MainScreen.this, "Good Choice", Toast.LENGTH_SHORT).show();
                goToActivity(position);
            }
        });
    }

    private void goToActivity(int numChosen) {
        Intent intent;
        if(loggedIn) {//logged in
            if (numChosen == 0) {//home
                intent = new Intent(this, MainScreen.class);
            } else if (numChosen == 1) {//profile
                intent = new Intent(this, ViewProfile.class);
            } else if (numChosen == 2) {//data
                intent = new Intent(this, ViewData.class);
            } else if (numChosen == 3) {//settings
                intent = new Intent(this, SettingsActivity.class);
            } else {//log out
                intent = new Intent(this, MainScreen.class);
                intent.putExtra("LOGMEOUT", true);
            }
        } else {//not logged in
            if (numChosen == 0) {//home
                intent = new Intent(this, MainScreen.class);
            } else {//login
                intent = new Intent(this, NewUser.class);
            }
        }
        intent.putExtra("LOGGEDIN", loggedIn);
        intent.putExtra("EMAIL", email);
        startActivity(intent);
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Menu");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
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
