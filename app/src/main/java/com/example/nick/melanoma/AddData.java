package com.example.nick.melanoma;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AddData extends AppCompatActivity  {
    private boolean loggedIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data);

        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");


    }
}
