package com.capstone.nick.melanoma;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

//TODO
//check connection - then get profile from Firebase, otherwise locally

/**
 * Activity to view the user's profile data. Displays information such as name, dob, ethnicity, etc.
 * User can modify some of these fields, but others are pulled from their Google account. If the
 * questionnaire was completed when they signed up, that data is displayed here.
 */
public class ViewProfile extends NavigatingActivity  {
    private boolean loggedIn;
    private String  userEmail;
    private StorageReference mStorageRef;

    private boolean gotFile_firebase;

    @Override
    /**
     * When activity created, all data pulled from profile.txt is displayed.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        userEmail = getIntent().getExtras().getString("EMAIL");
        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        super.onCreateDrawer(loggedIn, userEmail);

        //populate fields with data found
        mStorageRef = FirebaseStorage.getInstance().getReference();
        loadProfile();

        findViewById(R.id.saveDets).setBackgroundColor(Color.GREEN);

        //set these details to not-editable
        EditText editText = (EditText)findViewById(R.id.editFname);
        editText.setEnabled(false);
        editText = (EditText)findViewById(R.id.editLname);
        editText.setEnabled(false);
        editText = (EditText)findViewById(R.id.editEmail);
        editText.setEnabled(false);


    }

    /**
     * Loads data from profile.txt into the appropriate fields.
     */
    private void loadProfile() {
        StorageReference fileRef = mStorageRef.child(userEmail+"/profile.txt");

        File rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/");
        final File localFile = new File(rootPath,"profile.txt");

        fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                //downloaded file
                gotFile_firebase =true;
                //System.out.println("ViewProfile got profile from firebase");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //failed to download file
                gotFile_firebase =false;
                //System.out.println("ViewProfile failed to get profile from firebase");
            }
        });
        //get contents of file
        FileHandler reader = new FileHandler();
        String content = reader.readFile(rootPath.toString(), "profile.txt");
        String[] lines = content.split("\\n");

        try {
            //set the edittext fields
            EditText setText = (EditText) findViewById(R.id.editFname);
            setText.setText(lines[0].substring(12));
            setText = (EditText) findViewById(R.id.editLname);
            setText.setText(lines[1].substring(11));
            setText = (EditText) findViewById(R.id.editEmail);
            setText.setText(lines[2].substring(7));
            setText = (EditText) findViewById(R.id.editDOB);
            setText.setText(lines[4].substring(15));

            //set the radio button selections
            String gendStr = lines[3].substring(5);
            RadioGroup setSel = (RadioGroup) findViewById(R.id.gendGroup);
            if (gendStr.equals("Male"))
                setSel.check(R.id.toggleMale);
            else if (gendStr.equals("Female"))
                setSel.check(R.id.toggleFemale);

            String ethStr = lines[5].substring(11);
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
     * When button pressed on this screen, currently used to save updates and reupload profile.txt
     * @param v is used to identify the button that was pressed
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveDets:
                uploadChanges();
        }
    }

    /**
     * When the user makes changes, this will save them to their profile.txt, and will upload the
     * file into Firebase.
     */
    private void uploadChanges() {
        FileHandler saver = new FileHandler();
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/";
        String data = "";

        //get data from edittexts
        data+="First Name: ";
        EditText temp = (EditText)findViewById(R.id.editFname);
        data+= temp.getText().toString();
        data+="\nLast Name: ";
        temp = (EditText)findViewById(R.id.editLname);
        data+= temp.getText().toString();
        data+="\nEmail: ";
        temp = (EditText)findViewById(R.id.editEmail);
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
        //save file
        saver.saveToFile(path, "profile.txt", data);
        final Uri file = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/profile.txt"));
        StorageReference fileRef = mStorageRef.child(userEmail+"/profile.txt");
        //upload file to firebase
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
