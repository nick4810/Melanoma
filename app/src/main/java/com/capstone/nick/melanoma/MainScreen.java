package com.capstone.nick.melanoma;
//Credit to Ben Jakuben (http://blog.teamtreehouse.com/add-navigation-drawer-android)
//for the basic implementation of a navigation drawer

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainScreen extends NavigatingActivity  {
    private boolean loggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        super.onCreateDrawer(loggedIn);

    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goToLogin:
                goToLogin();
            case R.id.goToCamera:
                goToCamera();
        }
    }

    public void goToLogin() {
        Intent intent = new Intent(this, Login.class);
        intent.putExtra("LOGGEDIN", loggedIn);
        startActivity(intent);

    }

    public void goToCamera() {
        Intent intent = new Intent(this, AddData.class);
        intent.putExtra("LOGGEDIN", loggedIn);
        startActivity(intent);

    }

}
