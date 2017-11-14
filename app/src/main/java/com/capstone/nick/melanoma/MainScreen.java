package com.capstone.nick.melanoma;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainScreen extends NavigatingActivity  {
    private boolean loggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        if(getIntent().getExtras() !=null)
            loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        super.onCreateDrawer(loggedIn);

    }


    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

}
