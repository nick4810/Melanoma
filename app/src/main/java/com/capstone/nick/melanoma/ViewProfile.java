package com.capstone.nick.melanoma;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;

//TODO
//file writing/upload/download
//check connection - then get profile from Firebase, otherwise locally
public class ViewProfile extends NavigatingActivity  {
    private boolean loggedIn;
    private String  userEmail;
    private StorageReference mStorageRef;

    @Override
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

    private void loadProfile() {
        StorageReference fileRef = mStorageRef.child(userEmail+"/profile.txt");

        File rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/");
        final File localFile = new File(rootPath,"profile.txt");

        fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                //downloaded file
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //failed to download file
            }
        });
        //get contents of file
        FileHandler reader = new FileHandler();
        String content = reader.readFile(rootPath.toString(), "profile.txt");
        String[] lines = content.split("\\n");

        //set the edittext fields
        EditText setText = (EditText)findViewById(R.id.editFname);
        setText.setText(lines[0].substring(12));
        setText = (EditText)findViewById(R.id.editLname);
        setText.setText(lines[1].substring(11));
        setText = (EditText)findViewById(R.id.editEmail);
        setText.setText(lines[2].substring(7));
        setText = (EditText)findViewById(R.id.editDOB);
        setText.setText(lines[4].substring(15));

        //set the radio button selections
        String gendStr =lines[3].substring(5);
        RadioGroup setSel = (RadioGroup)findViewById(R.id.gendGroup);
        if(gendStr.equals("Male"))
            setSel.check(R.id.toggleMale);
        else if(gendStr.equals("Female"))
            setSel.check(R.id.toggleFemale);

        String ethStr =lines[5].substring(11);
        setSel = (RadioGroup)findViewById(R.id.ethGroup);
        if(ethStr.equals("Caucasian/White"))
            setSel.check(R.id.toggle_white);
        else if(ethStr.equals("African American/Black"))
            setSel.check(R.id.toggle_black);
        else if(ethStr.equals("Asian"))
            setSel.check(R.id.toggle_asian);
        else if(ethStr.equals("Hispanic"))
            setSel.check(R.id.toggle_hisp);
        else if(ethStr.equals("Prefer Not To Answer"))
            setSel.check(R.id.toggle_prefNo);
        else if(ethStr.equals("Other"))
            setSel.check(R.id.toggle_other);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveDets:
                uploadChanges();
        }
    }

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
        fileRef.putFile(file);

        //let user know file has been saved
        TextView saved = (TextView)findViewById(R.id.savedTxt);
        saved.setVisibility(View.VISIBLE);

    }
}
