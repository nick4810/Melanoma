package com.capstone.nick.melanoma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import static com.google.android.gms.common.SignInButton.COLOR_DARK;
import static com.google.android.gms.common.SignInButton.SIZE_WIDE;

public class NewUser extends NavigatingActivity {

    private boolean loggedIn;
    private String userEmail;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        userEmail = getIntent().getExtras().getString("EMAIL");
        boolean logMeOut = getIntent().getExtras().getBoolean("LOGMEOUT");

        /*TODO
        ** new users log in with google, load data submitted into database
        ** when creating user from main screen, create default profile.txt
         */
        super.onCreateDrawer(loggedIn, userEmail);

        SignInButton signInButton = (SignInButton)findViewById(R.id.sign_in_button_newUser);
        signInButton.setStyle(SIZE_WIDE, COLOR_DARK);
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button_newUser:
                signIn();
                break;
        }
    }

    public void signIn() {
        //if signedIn uploadChanges
    }

    public void uploadChanges() {
        CheckBox audio_chk = (CheckBox)findViewById(R.id.newUser_audio);
        if(audio_chk.isChecked()) {//user chose to use audio memos, save preference
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor1 = settings.edit();
            editor1.putBoolean("pref_audio", true);
            editor1.apply();
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();

        FileHandler saver = new FileHandler();
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/";
        String data = "";

        //get data from edittexts
        data+="First Name: ";
        //EditText temp = (EditText)findViewById(R.id.editFname);
        //data+= temp.getText().toString();
        data+="\nLast Name: ";
        //temp = (EditText)findViewById(R.id.editLname);
        //data+= temp.getText().toString();
        data+="\nEmail: ";
        //temp = (EditText)findViewById(R.id.editEmail);
        //data+= temp.getText().toString();

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
        EditText temp = (EditText)findViewById(R.id.newUser_dob);
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
        //System.out.println(data);
        //save file
        saver.saveToFile(path, "profile.txt", data);
        final Uri file = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/profile.txt"));
        StorageReference fileRef = mStorageRef.child(userEmail+"/profile.txt");
        //upload to firebase
        fileRef.putFile(file);


    }


}
