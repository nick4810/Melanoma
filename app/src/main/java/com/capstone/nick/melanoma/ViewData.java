package com.capstone.nick.melanoma;
//Credit to https://www.androidauthority.com/how-to-build-an-image-gallery-app-718976/
//for implementing a gallery image view
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
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

    private StorageReference mStorageRef;

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
                for(MyAdapter.ViewHolder view : adapter.imageViews) {
                    view.chkBox.setVisibility(View.VISIBLE);
                }
            }
        });

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.imagegallery);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),2);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<CreateList> createLists = prepareData();
        adapter = new MyAdapter(getApplicationContext(), createLists);
        recyclerView.setAdapter(adapter);

        //for firebase storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
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
        if(adapter.selViews.size()!=0) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);

            builder.setTitle("Confirm Selection")
                    .setMessage("Are you sure you want to delete the selected?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            for (MyAdapter.ViewHolder view : adapter.selViews) {
                                /*
                                //delete locally
                                File dir = getFilesDir();
                                File file = new File(dir, "my_filename");
                                boolean deleted = file.delete();
                                //delete from firebase
                                // Create a reference to the file to delete
                                StorageReference desertRef = mStorageRef.child("images/desert.jpg");

                                // Delete the file
                                desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // File deleted successfully
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Uh-oh, an error occurred!
                                    }
                                });
                                */
                            }
                            //make images not selectable
                            ImageButton temp = (ImageButton) findViewById(R.id.btnUpload);
                            temp.setVisibility(View.INVISIBLE);
                            temp = (ImageButton) findViewById(R.id.btnTrash);
                            temp.setVisibility(View.INVISIBLE);

                            for (MyAdapter.ViewHolder view : adapter.imageViews) {
                                view.chkBox.setChecked(false);
                                view.chkBox.setVisibility(View.INVISIBLE);
                            }
                            adapter.selViews.clear();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            //for(__ in adapter.hashmap)
            //delete/upload

        }
    }

    public void uploadSelected() {
        if(adapter.selViews.size()!=0) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);

            builder.setTitle("Confirm Selection")
                    .setMessage("Are you sure you want to upload the selected?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with upload
                            for (MyAdapter.ViewHolder view : adapter.selViews) {
                                /*
                                Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
                                StorageReference riversRef = mStorageRef.child("images/rivers.jpg");

                                riversRef.putFile(file)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                // Get a URL to the uploaded content
                                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                // Handle unsuccessful uploads
                                                // ...
                                            }
                                        });
                                        */
                            }
                            //make images not selectable
                            ImageButton temp = (ImageButton) findViewById(R.id.btnUpload);
                            temp.setVisibility(View.INVISIBLE);
                            temp = (ImageButton) findViewById(R.id.btnTrash);
                            temp.setVisibility(View.INVISIBLE);

                            for (MyAdapter.ViewHolder view : adapter.imageViews) {
                                view.chkBox.setChecked(false);
                                view.chkBox.setVisibility(View.INVISIBLE);
                            }
                            adapter.selViews.clear();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            //for(__ in adapter.hashmap)
            //delete/upload

        }
    }

}
