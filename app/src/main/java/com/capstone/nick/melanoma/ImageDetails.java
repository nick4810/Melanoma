package com.capstone.nick.melanoma;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ImageDetails extends NavigatingActivity {
    private boolean loggedIn;
    private String userEmail;
    private String location;
    private String date;
    private String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        userEmail = getIntent().getExtras().getString("EMAIL");
        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        location = getIntent().getExtras().getString("LOCATION");
        date = getIntent().getExtras().getString("DATE");
        time = getIntent().getExtras().getString("TIME");
        super.onCreateDrawer(loggedIn, userEmail);

        Spinner mySpinner = (Spinner)findViewById(R.id.img_location);
        mySpinner.setSelection(((ArrayAdapter)mySpinner.getAdapter()).getPosition(location));

        EditText setDet = (EditText)findViewById(R.id.img_date);
        setDet.setText(date, TextView.BufferType.EDITABLE);
        setDet = (EditText)findViewById(R.id.img_time);
        setDet.setText(time, TextView.BufferType.EDITABLE);

        //FileHandler tmp = new FileHandler();
        //tmp.readFile("/nicks/", "tmp.txt");
    }

    public void onClick(View v) {
        Intent intent = new Intent(this, ViewData.class);
        intent.putExtra("LOGGEDIN", loggedIn);
        intent.putExtra("EMAIL", userEmail);

        startActivity(intent);
    }

}
