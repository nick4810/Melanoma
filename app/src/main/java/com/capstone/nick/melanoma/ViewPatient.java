package com.capstone.nick.melanoma;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

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

public class ViewPatient extends NavigatingActivity {
    private String userEmail;
    private String patient_det;

    private StorageReference mStorageRef;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_patient);

        patient_det = getIntent().getExtras().getString("PATIENTDETAILS");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userEmail = prefs.getString("USEREMAIL", "");
        super.onCreateDrawer();

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance();
        loadPatientData();

        findViewById(R.id.editFname).setEnabled(false);
        findViewById(R.id.editLname).setEnabled(false);
        findViewById(R.id.editID).setEnabled(false);
        findViewById(R.id.saveDets).setBackgroundColor(Color.GREEN);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveDets:
                uploadChanges();
                break;
        }
    }

    private void loadPatientData() {
        String[] fileDet =patient_det.split("_");

        String patientID ="";
        String patientName ="";
        try {
            //directory name is [patientID]_[patientName]
            patientID =fileDet[0];
            patientName =fileDet[1];
        } catch (Exception e) {
            //e.printStackTrace();
        }

        StorageReference fileRef = mStorageRef.child("Patients/"+patientID+"/profile.txt");

        File rootPath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/"+patientID+"_"+patientName+"/");
        final File localFile = new File(rootPath,"profile.txt");

        fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                //downloaded file
                //System.out.println("ViewProfile got profile from firebase");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //failed to download file
                //System.out.println("ViewProfile failed to get profile from firebase");
            }
        });
        //get contents of file
        FileHandler reader = new FileHandler();
        String content = reader.readFile(rootPath.toString(), "profile.txt");
        String[] lines = content.split("\\n");

        try {
            //set the edittext fields
            EditText setText = (EditText) findViewById(R.id.editID);
            setText.setText(lines[0].substring(4));
            setText = (EditText) findViewById(R.id.editFname);
            setText.setText(lines[1].substring(12));
            setText = (EditText) findViewById(R.id.editLname);
            setText.setText(lines[2].substring(11));

            setText = (EditText) findViewById(R.id.newUser_height);
            setText.setText(lines[3].substring(8));
            setText = (EditText) findViewById(R.id.newUser_weight);
            setText.setText(lines[4].substring(8));

            //set the radio button selections
            String gendStr = lines[5].substring(5);
            RadioGroup setSel = (RadioGroup) findViewById(R.id.gendGroup);
            if (gendStr.equals("Male"))
                setSel.check(R.id.toggleMale);
            else if (gendStr.equals("Female"))
                setSel.check(R.id.toggleFemale);

            setText = (EditText) findViewById(R.id.editDOB);
            setText.setText(lines[6].substring(15));

            String ethStr = lines[7].substring(11);
            setSel = (RadioGroup) findViewById(R.id.ethGroup);
            if (ethStr.equals("Caucasian/White"))
                setSel.check(R.id.toggle_white);
            else if (ethStr.equals("African American/Black"))
                setSel.check(R.id.toggle_black);
            else if (ethStr.equals("Asian"))
                setSel.check(R.id.toggle_asian);
            else if (ethStr.equals("Hispanic"))
                setSel.check(R.id.toggle_hisp);
            else if (ethStr.equals("Prefer Not To Answer"))
                setSel.check(R.id.toggle_prefNo);
            else if (ethStr.equals("Other"))
                setSel.check(R.id.toggle_other);
        } catch(Exception e) { e.printStackTrace(); }

    }

    /**
     * When the user makes changes, this will save them to their profile.txt, and will upload the
     * file into Firebase.
     */
    private void uploadChanges() {
        String[] fileDet =patient_det.split("_");

        String patientID ="";
        String patientName ="";
        try {
            //directory name is [patientID]_[patientName]
            patientID =fileDet[0];
            patientName =fileDet[1];
        } catch (Exception e) {
            //e.printStackTrace();
        }
        FileHandler saver = new FileHandler();
        String data = "";

        //add patient ID
        data+="ID: ";
        data+= patientID;

        //get data from edittexts
        data+="\nFirst Name: ";
        EditText temp = (EditText)findViewById(R.id.editFname);
        data+= temp.getText().toString();
        data+="\nLast Name: ";
        temp = (EditText)findViewById(R.id.editLname);
        data+= temp.getText().toString();

        data+="\nHeight: ";
        temp = (EditText)findViewById(R.id.newUser_height);
        data+= temp.getText().toString();
        data+="\nWeight: ";
        temp = (EditText)findViewById(R.id.newUser_weight);
        data+= temp.getText().toString();

        //get data from radio buttons
        data+="\nSex: ";
        RadioGroup radioButtonGroup = (RadioGroup)findViewById(R.id.gendGroup);
        int radioButtonID = radioButtonGroup.getCheckedRadioButtonId();
        if(radioButtonID >0) {
            View radioButton = radioButtonGroup.findViewById(radioButtonID);
            int idx = radioButtonGroup.indexOfChild(radioButton);

            RadioButton r = (RadioButton) radioButtonGroup.getChildAt(idx);
            data+= r.getText().toString();
        }

        data+="\nDate of Birth: ";
        temp = (EditText)findViewById(R.id.editDOB);
        data+= temp.getText().toString();

        data+="\nEthnicity: ";
        radioButtonGroup = (RadioGroup)findViewById(R.id.ethGroup);
        radioButtonID = radioButtonGroup.getCheckedRadioButtonId();
        if(radioButtonID >0) {
            View radioButton = radioButtonGroup.findViewById(radioButtonID);
            int idx = radioButtonGroup.indexOfChild(radioButton);

            RadioButton r = (RadioButton) radioButtonGroup.getChildAt(idx);
            data+= r.getText().toString();
        }
        //System.out.println(data);
        String path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/"+patientID+"_"+patientName+"/";
        //save file
        saver.saveToFile(path, "profile.txt", data);

        final Uri file = Uri.fromFile(new File(path+"profile.txt"));
        StorageReference fileRef = mStorageRef.child("Patients/"+patientID+"/profile.txt");
        //upload file to firebase
        //final DatabaseReference proRef = mDatabase.getReference("Patients/"+patientID+"/");
        fileRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //file uploaded
                //System.out.println("ViewProfile uploaded profile to firebase");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //failed to upload file
                //System.out.println("ViewProfile failed upload of profile to firebase");
            }
        });

        //let user know file has been saved
        findViewById(R.id.savedTxt).setVisibility(View.VISIBLE);

    }
}
