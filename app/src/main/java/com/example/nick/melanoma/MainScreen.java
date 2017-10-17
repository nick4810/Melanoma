package com.example.nick.melanoma;
//Credit to Ben Jakuben (http://blog.teamtreehouse.com/add-navigation-drawer-android)
//for the basic implementation of this navigation drawer

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainScreen extends NavigatingActivity  {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        super.onCreateDrawer();


    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goToLogin:
                goToLogin();
                break;
        }
    }

    public void goToLogin() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);

    }

}
