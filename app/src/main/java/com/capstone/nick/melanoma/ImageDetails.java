package com.capstone.nick.melanoma;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;

public class ImageDetails extends NavigatingActivity {
    private boolean loggedIn;
    private String userEmail;
    private String location;

    private String filename;
    private String path;
    private String date;
    private String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        userEmail = getIntent().getExtras().getString("EMAIL");
        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        location = getIntent().getExtras().getString("LOCATION");

        filename = getIntent().getExtras().getString("FILE");
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/JPEG Images/";
        String tmpFile ="JPEG";
        tmpFile+=filename.substring(3);
        tmpFile+=".jpg";
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path+tmpFile), 150, 200);
        ImageView img = (ImageView)findViewById(R.id.image_taken);
        img.setImageBitmap(thumbnail);

        filename+=".txt";
        date = getIntent().getExtras().getString("DATE");
        time = getIntent().getExtras().getString("TIME");
        super.onCreateDrawer(loggedIn, userEmail);

        Spinner mySpinner = (Spinner)findViewById(R.id.img_location);
        mySpinner.setSelection(((ArrayAdapter)mySpinner.getAdapter()).getPosition(location));

        EditText setDet = (EditText)findViewById(R.id.img_date);
        setDet.setText(date, TextView.BufferType.EDITABLE);
        setDet = (EditText)findViewById(R.id.img_time);
        setDet.setText(time, TextView.BufferType.EDITABLE);

    }

    public void onClick(View v) {
        saveFile();

        Intent intent = new Intent(this, ViewData.class);
        intent.putExtra("LOGGEDIN", loggedIn);
        intent.putExtra("EMAIL", userEmail);

        startActivity(intent);
    }

    private void saveFile() {
        FileHandler saver = new FileHandler();
        String data = "";
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/Raw Images/";

        data+="Date: ";
        EditText dataSource = (EditText)findViewById(R.id.img_date);
        data+= dataSource.getText().toString();
        data+="\nTime: ";
        dataSource = (EditText)findViewById(R.id.img_time);
        data+= dataSource.getText().toString();
        data+="\nLocation: ";
        Spinner locSource = (Spinner)findViewById(R.id.img_location);
        data+= locSource.getSelectedItem().toString();
        data+="\nNotes:\n";
        dataSource = (EditText)findViewById(R.id.img_notes);
        data+= dataSource.getText().toString();

        if(!saver.saveToFile(path, filename, data)) {
            System.out.println("Error saving file");
        }
    }

}
