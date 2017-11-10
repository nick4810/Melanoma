package com.capstone.nick.melanoma;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

public class ViewProfile extends NavigatingActivity  {
    private boolean loggedIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        findViewById(R.id.saveDets).setBackgroundColor(Color.GREEN);

        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        super.onCreateDrawer(loggedIn);

    }
}
