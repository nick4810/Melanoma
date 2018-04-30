package com.capstone.nick.melanoma;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity that displays the 'Settings' screen. Shows preferences available to user, along with
 * other options.
 */
public class SettingsActivity extends NavigatingActivity {
    private String userEmail;

    private StorageReference mStorageRef;
    private FirebaseDatabase mDatabase;

    /**
     * Displays all settings/options to user.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userEmail = prefs.getString("USEREMAIL", "");
        super.onCreateDrawer();

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance();
    }

    /**
     * When button pressed on this screen, currently used for 'Delete Account' option
     * @param v is used to identify the button that was pressed
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delAccount:
                deleteAccount();
        }
    }


    /**
     * The option to delete the users account. This will delete all local files on their device, log
     * them out, and return them to the main menu.
     * Note: currently this does not delete all images in Firebase
     */
    private void deleteAccount() {
        //confirm before deleting account
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);

        final Intent intent = new Intent(this, MainScreen.class);
        intent.putExtra("LOGMEOUT", true);

        final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/";

        builder.setTitle("Confirm Selection")
                .setMessage("Are you absolutely sure you want to delete your account and all associated files/images?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //go through with deletion
                        final File deleteDir = new File(path);
                        /* Notice:
                           Deleting an account does not currently delete the photos locally or from
                           Firebase in order to save the patient data
                         */
                        //delete local data
                        //deleteRecursive(deleteDir);
                        //delete firebase storage data
                        //deleteFromFirebase();

                        //delete database entries
                        DatabaseReference ref = mDatabase.getReference("Users/"+userEmail.replaceAll("\\.", ","));
                        ref.removeValue();

                        StorageReference fileRef = mStorageRef.child(userEmail + "/profile.txt");
                        try {//delete profile txt file
                            System.out.println("Attempt delete of: "+fileRef.toString());
                            fileRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //upon deletion go to main screen
                                    startActivity(intent);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            //upon deletion go to main screen
                            startActivity(intent);
                        }
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


    /**
     * TODO
     * wait for firebase processes to finish - progress dialog?
     * review viewData delete method
     */
    private void deleteFromFirebase() {
        DatabaseReference ref = mDatabase.getReference("Users/"+userEmail.replaceAll("\\.", ",")+"/");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> allIDs = dataSnapshot.getChildren();//get all image ids
                for (DataSnapshot imageID: allIDs) {//foreach image id, get each child
                    Iterable<DataSnapshot> imageData = imageID.getChildren();
                    for(DataSnapshot imageFile : imageData) {//foreach child, get download url, delete from firebase storage
                        try {
                            mStorageRef.child(imageFile.getValue().toString()).delete();
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * Deletes all files in directory
     * @param fileOrDirectory is object to delete
     */
    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
        return true;

    }

}
