package com.capstone.nick.melanoma;
//Credit to https://www.androidauthority.com/how-to-build-an-image-gallery-app-718976/
//for implementing a gallery image view
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

import static android.net.ConnectivityManager.TYPE_WIFI;

/**
 * This activity displays the images in a gallery view. All images are displayed with a label that
 * describes the image. The user has the option to select multiple
 * images and delete/upload them. They can also choose to add a new image.
 */
public class ViewData extends NavigatingActivity {
    private boolean loggedIn;
    private String userEmail;

    private MyAdapter adapter;

    private StorageReference mStorageRef;

    /*TODO
    **known issue: checkboxes not appearing off-screen
    * refresh after deletion
     */
    @Override
    /**
     * When activity created, all images displayed, also a button to take a new image, and 'Select'
     * text to select multiple images to upload/delete.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);

        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        userEmail = getIntent().getExtras().getString("EMAIL");
        super.onCreateDrawer(loggedIn, userEmail);

        //add an option to select images
        final TextView selText = (TextView)findViewById(R.id.SelectText);
        selText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selText.getText().toString().equals("Select")) {
                    //change text
                    selText.setText("Cancel");
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
                } else if(selText.getText().toString().equals("Cancel")) {
                    //change text
                    selText.setText("Select");
                    //go to normal view
                    findViewById(R.id.btnUpload).setVisibility(View.GONE);
                    findViewById(R.id.btnTrash).setVisibility(View.GONE);

                    for(MyAdapter.ViewHolder view : adapter.imageViews) {
                        view.chkBox.setChecked(false);
                        view.chkBox.setVisibility(View.GONE);
                    }
                    adapter.selViews.clear();

                }
            }
        });
        //set up image gallery
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.imagegallery);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),2);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<CreateList> createLists = prepareData();
        adapter = new MyAdapter(getApplicationContext(), createLists, userEmail);
        recyclerView.setAdapter(adapter);

        //for firebase storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    /**
     * Populate the gallery with the images found in the directory.
     * @return
     */
    private ArrayList<CreateList> prepareData(){

        ArrayList<CreateList> theimage = new ArrayList<>();

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/JPEG Images/";//specify path
        //System.out.println(path);

        File f = new File(path);
        File file[] = f.listFiles();
        try {
            for (int i = 0; i < file.length; i++) {
                CreateList createList = new CreateList();
                createList.setImage_title(file[i].getName());
                theimage.add(createList);
            }
        } catch (NullPointerException e) {
            System.out.println("Null list");
        }

        return theimage;
    }


    /**
     * When button pressed on this screen, currently used to initiate adding new image, also
     * deleting and uploading them.
     * @param v is used to identify the button that was pressed
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCam:
                addData();
                break;
            case R.id.btnTrash:
                delSelected();
                break;
            case R.id.btnUpload:
                uploadSelected();
                break;
        }
    }

    /**
     * If the user chooses to add a new image, start 'New Image' activity.
     */
    public void addData() {
        Intent intent = new Intent(this, BodySelect.class);
        intent.putExtra("LOGGEDIN", loggedIn);
        intent.putExtra("EMAIL", userEmail);
        startActivity(intent);

    }

    /**
     * This will delete all images that were selected. A confirmation will appear before deleting
     * them. Deletes both image files, and either audio/txt file associated with image.
     */
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
                                //delete raw image file
                                String viewFile =view.filename.substring(4, view.filename.length()-3);
                                String filename = "RAW" + viewFile+ "dng";
                                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/Raw Images/"+filename);
                                boolean deleted = file.delete();
                                if(deleted) System.out.println("deleted: "+filename);
                                //delete from firebase
                                StorageReference fileRef = mStorageRef.child(userEmail+"/Raw Images/"+filename);
                                fileRef.delete();

                                //delete txt file
                                filename = "RAW" + viewFile+ "txt";
                                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/Raw Images/"+filename);
                                deleted = file.delete();
                                if(deleted) System.out.println("deleted: "+filename);
                                //delete from firebase
                                fileRef = mStorageRef.child(userEmail+"/Raw Images/"+filename);
                                fileRef.delete();

                                //delete audio file
                                filename = "RAW" + viewFile+ "3gpp";
                                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/Raw Images/"+filename);
                                deleted = file.delete();
                                if(deleted) System.out.println("deleted: "+filename);
                                //delete from firebase
                                fileRef = mStorageRef.child(userEmail+"/JPEG Images/"+filename);
                                fileRef.delete();

                                //delete jpg image file
                                filename = "JPEG" + viewFile+ "jpg";
                                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/JPEG Images/"+filename);
                                deleted = file.delete();
                                if(deleted) System.out.println("deleted: "+filename);
                                //delete from firebase
                                fileRef = mStorageRef.child(userEmail+"/JPEG Images/"+filename);
                                fileRef.delete();

                            }
                            //make images not selectable
                            findViewById(R.id.btnUpload).setVisibility(View.GONE);
                            findViewById(R.id.btnTrash).setVisibility(View.GONE);

                            for (MyAdapter.ViewHolder view : adapter.imageViews) {
                                view.chkBox.setChecked(false);
                                view.chkBox.setVisibility(View.GONE);
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

        }
    }

    /**
     * This will upload all images that were selected. A confirmation box will appear before the
     * upload. Uploads both image files, and either txt/audio file associated with image.
     */
    public void uploadSelected() {
        if(adapter.selViews.size()!=0) {
            final AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            final boolean useWifiOnly = sharedPreferences.getBoolean("pref_wifiOnly", false);

            final Intent settingsIntent = new Intent(this, SettingsActivity.class);
            settingsIntent.putExtra("LOGGEDIN", loggedIn);
            settingsIntent.putExtra("EMAIL", userEmail);

            builder.setTitle("Confirm Selection")
                    .setMessage("Are you sure you want to upload the selected?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(useWifiOnly) {//wants to use wifi only, not cellular data
                                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo networkInfo = null;
                                if (connMgr != null) {
                                    networkInfo = connMgr.getActiveNetworkInfo();
                                }
                                if(networkInfo !=null && networkInfo.getType() !=TYPE_WIFI) {
                                    //notify user that pref is set to wifi-only, and no wifi was detected
                                    builder.setTitle("No Connection")
                                            .setMessage("Wi-Fi connection needed in order to proceed. This can be changed in the Settings.")
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //do nothing
                                                }
                                            })
                                            .setNegativeButton("Settings", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // go to settings page
                                                    startActivity(settingsIntent);
                                                }
                                            })
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();

                                } else {
                                    // has wifi, continue with upload
                                    for (MyAdapter.ViewHolder view : adapter.selViews) {
                                        uploadToFirebase(view);
                                    }
                                }
                            } else {
                                // pref not set, continue with upload
                                for (MyAdapter.ViewHolder view : adapter.selViews) {
                                    uploadToFirebase(view);
                                }
                            }
                            //make images non-selectable
                            findViewById(R.id.btnUpload).setVisibility(View.GONE);
                            findViewById(R.id.btnTrash).setVisibility(View.GONE);

                            for (MyAdapter.ViewHolder view : adapter.imageViews) {
                                view.chkBox.setChecked(false);
                                view.chkBox.setVisibility(View.GONE);
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

        }
    }

    /**
     * Used to upload the images to Firebase.
     * @param view This is the image object
     */
    private void uploadToFirebase(MyAdapter.ViewHolder view) {
        //filenames
        String viewFile =view.filename.substring(4, view.filename.length()-3);
        String filename = "RAW" + viewFile+"dng";
        String filename_jpg = view.filename;
        String filename_txt = "RAW" + viewFile+"txt";
        String filename_aud = "RAW" + viewFile+"3gpp";

        //Uri for uploading
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/Raw Images/";
        String path_jpg = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/JPEG Images/";
        final Uri file = Uri.fromFile(new File(path+filename));
        final Uri file_jpg = Uri.fromFile(new File(path_jpg+filename_jpg));
        final Uri file_txt = Uri.fromFile(new File(path+filename_txt));
        final Uri file_aud = Uri.fromFile(new File(path+filename_aud));

        File chk_raw = new File(path+filename);
        File chk_txt = new File(path + filename_txt);
        File chk_aud = new File(path + filename_aud);

        //upload image files
        StorageReference fileRef;
        fileRef = mStorageRef.child(userEmail+"/JPEG Images/"+filename_jpg);
        fileRef.putFile(file_jpg);

        //raw file may not exist on some devices
        if(chk_raw.exists()) {
            fileRef = mStorageRef.child(userEmail + "/Raw Images/" + filename);
            fileRef.putFile(file);

        }
        //upload audio/text file if exists
        if(chk_txt.exists()) {
            fileRef = mStorageRef.child(userEmail+"/Raw Images/"+filename_txt);
            fileRef.putFile(file_txt);
            //System.out.println("txt exists");

        } else if(chk_aud.exists()) {
            fileRef = mStorageRef.child(userEmail+"/Raw Images/"+filename_aud);
            fileRef.putFile(file_aud);
            //System.out.println("aud exists");

        }


    }

}
