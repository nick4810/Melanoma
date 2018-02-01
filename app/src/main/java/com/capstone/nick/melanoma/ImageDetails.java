package com.capstone.nick.melanoma;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ImageDetails extends NavigatingActivity {
    private boolean loggedIn;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        userEmail = getIntent().getExtras().getString("EMAIL");
        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        super.onCreateDrawer(loggedIn, userEmail);
    }
}
