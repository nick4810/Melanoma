package com.capstone.nick.melanoma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class NewPatient extends NavigatingActivity  {
    private String  userEmail;

    private StorageReference mStorageRef;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_patient);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userEmail = prefs.getString("USEREMAIL", "");
        super.onCreateDrawer();

        //populate fields with data found
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance();

        findViewById(R.id.newPatient).setBackgroundColor(Color.GREEN);

    }


    /**
     * When button pressed on this screen,
     * @param v is used to identify the button that was pressed
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newPatient:
                addNewPatient();
                break;
        }
    }

    /**
     * TODO
     * Add ability to add/change patient's profile picture
     */
    private void addNewPatient() {
        String patientID = String.valueOf(getID());

        final DatabaseReference ref = mDatabase.getReference(userEmail.replaceAll("\\.", ",")+"/Patients/"+patientID);
        ref.setValue(true);

        String patientName ="";//for combining first and last name

        FileHandler saver = new FileHandler();
        String data = "";

        //add patient ID
        data+="ID: ";
        data+= patientID;

        //get data from edittexts
        data+="\nFirst Name: ";
        EditText temp = (EditText)findViewById(R.id.newUser_Fname);
        data+= temp.getText().toString();
        patientName+= temp.getText().toString();
        data+="\nLast Name: ";
        temp = (EditText)findViewById(R.id.newUser_Lname);
        data+= temp.getText().toString();
        patientName+= " ";
        patientName+= temp.getText().toString();

        data+="\nHeight: ";
        temp = (EditText)findViewById(R.id.edit_height);
        data+= temp.getText().toString();
        data+="\nWeight: ";
        temp = (EditText)findViewById(R.id.edit_weight);
        data+= temp.getText().toString();

        //get data from radio buttons
        data+="\nSex: ";
        RadioGroup radioButtonGroup = (RadioGroup)findViewById(R.id.newUser_gend);
        int radioButtonID = radioButtonGroup.getCheckedRadioButtonId();
        if(radioButtonID >0) {
            View radioButton = radioButtonGroup.findViewById(radioButtonID);
            int idx = radioButtonGroup.indexOfChild(radioButton);

            RadioButton r = (RadioButton) radioButtonGroup.getChildAt(idx);
            data+= r.getText().toString();
        }

        data+="\nDate of Birth: ";
        temp = (EditText)findViewById(R.id.newUser_dob);
        data+= temp.getText().toString();

        data+="\nEthnicity: ";
        radioButtonGroup = (RadioGroup)findViewById(R.id.newUser_eth);
        radioButtonID = radioButtonGroup.getCheckedRadioButtonId();
        if(radioButtonID >0) {
            View radioButton = radioButtonGroup.findViewById(radioButtonID);
            int idx = radioButtonGroup.indexOfChild(radioButton);

            RadioButton r = (RadioButton) radioButtonGroup.getChildAt(idx);
            data+= r.getText().toString();
        }
        data+="\nNotes: ";
        temp = (EditText)findViewById(R.id.newUser_notes);
        data+= temp.getText().toString();

        System.out.println(data);
        String path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/"+patientID+"_"+patientName+"/";
        //save file
        saver.saveToFile(path, "profile.txt", data);

        final Uri file = Uri.fromFile(new File(path+"profile.txt"));
        StorageReference fileRef = mStorageRef.child("Patients/"+patientID+"/profile.txt");
        //upload file to firebase
        fileRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //file uploaded
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //failed to upload file
                //System.out.println("ViewProfile failed upload of profile to firebase");
            }
        });

        Intent intent = new Intent(this, AllPatients.class);
        startActivity(intent);

    }


    public static long getID() {
        long LIMIT = 10000000000L;
        long last = 0;
        // 10 digits.
        long id = System.currentTimeMillis() % LIMIT;
        if ( id <= last ) {
            id = (last + 1) % LIMIT;
        }
        return id;
    }
}
