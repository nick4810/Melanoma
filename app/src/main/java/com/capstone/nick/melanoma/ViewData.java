package com.capstone.nick.melanoma;
//Credit to https://www.androidauthority.com/how-to-build-an-image-gallery-app-718976/
//for implementing a gallery image view
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ViewData extends NavigatingActivity {
    private boolean loggedIn;

    private MyAdapter adapter;

    private final String image_titles[] = {
            "Img1",
            "Img2",/*
            "Img3",
            "Img4",
            "Img5",
            "Img6",
            "Img7",
            "Img8",
            "Img9",
            "Img10",
            "Img11",
            "Img12",
            "Img13",*/
    };

    private final Integer image_ids[] = {
            R.drawable.ic_action_name,
            R.drawable.ic_action_name2,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);

        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        super.onCreateDrawer(loggedIn);

        final TextView selText = (TextView)findViewById(R.id.SelectText);
        selText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //make images selectable
                ImageButton temp = (ImageButton)findViewById(R.id.btnUpload);
                temp.setVisibility(View.VISIBLE);
                temp = (ImageButton)findViewById(R.id.btnTrash);
                temp.setVisibility(View.VISIBLE);

                //for(CreateList c : adapter.galleryList) {
                //[hashmap]? of ___, chkbox pairs stored in myadapter, chkbox listener adds to hashmap
                //}
            }
        });

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.imagegallery);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),2);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<CreateList> createLists = prepareData();
        adapter = new MyAdapter(getApplicationContext(), createLists);
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<CreateList> prepareData(){

        ArrayList<CreateList> theimage = new ArrayList<>();
        for(int i = 0; i< image_titles.length; i++){
            CreateList createList = new CreateList();
            createList.setImage_title(image_titles[i]);
            createList.setImage_ID(image_ids[i]);
            theimage.add(createList);
        }
/*
        String path = Environment.getRootDirectory().toString();//change to path being used
        File f = new File(path);
        File file[] = f.listFiles();
        for (int i=0; i < file.length; i++) {
            CreateList createList = new CreateList();
            createList.setImage_Location(file[i].getName());
            theimage.add(createList);
        }
*/
        return theimage;
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCam:
                addData();
            case R.id.btnTrash:
                delSelected();
            case R.id.btnUpload:
                uploadSelected();
        }
    }

    public void addData() {
        Intent intent = new Intent(this, AddData.class);
        intent.putExtra("LOGGEDIN", loggedIn);
        startActivity(intent);

    }

    public void delSelected() {
        //? confirm delete
        //for(__ in adapter.hashmap)
        //delete/upload

        //remove chkboxes, buttons, etc
    }

    public void uploadSelected() {
        //? confirm upload

        //for(__ in adapter.hashmap)
        //delete/upload

        //remove chkboxes, buttons, etc
    }

}
