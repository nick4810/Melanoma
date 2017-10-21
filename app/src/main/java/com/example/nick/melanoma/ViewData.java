package com.example.nick.melanoma;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ViewData extends NavigatingActivity {
    private boolean loggedIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);

        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        super.onCreateDrawer(loggedIn);
    }
}
