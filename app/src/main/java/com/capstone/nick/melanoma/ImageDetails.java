package com.capstone.nick.melanoma;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class ImageDetails extends NavigatingActivity {
    private boolean loggedIn;
    private String userEmail;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        userEmail = getIntent().getExtras().getString("EMAIL");
        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        location = getIntent().getExtras().getString("LOCATION");
        super.onCreateDrawer(loggedIn, userEmail);

        Spinner mySpinner = (Spinner)findViewById(R.id.img_location);
        mySpinner.setSelection(((ArrayAdapter)mySpinner.getAdapter()).getPosition(location));
    }
}
