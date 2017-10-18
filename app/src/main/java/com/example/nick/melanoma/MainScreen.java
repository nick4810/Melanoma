package com.example.nick.melanoma;
//Credit to Ben Jakuben (http://blog.teamtreehouse.com/add-navigation-drawer-android)
//for the basic implementation of a navigation drawer

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
