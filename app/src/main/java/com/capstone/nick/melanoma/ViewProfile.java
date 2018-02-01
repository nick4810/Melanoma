package com.capstone.nick.melanoma;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ViewProfile extends NavigatingActivity  {
    private boolean loggedIn;
    private String  userEmail;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        findViewById(R.id.saveDets).setBackgroundColor(Color.GREEN);

        //set these details to not-editable
        EditText editText = (EditText)findViewById(R.id.editFname);
        editText.setEnabled(false);
        editText = (EditText)findViewById(R.id.editLname);
        editText.setEnabled(false);
        editText = (EditText)findViewById(R.id.editEmail);
        editText.setEnabled(false);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        //StorageReference fileRef = mStorageRef.child(userEmail+"/Raw Images/"+filename);


        //FirebaseDatabase database = FirebaseDatabase.getInstance();
        //DatabaseReference myRef = database.getReference("message");


        //get details from firebase
        // Read from the database
        /*
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
*/
        userEmail = getIntent().getExtras().getString("EMAIL");
        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        super.onCreateDrawer(loggedIn, userEmail);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveDets:
                uploadChanges();
        }
    }

    public void uploadChanges() {
        EditText temp = (EditText)findViewById(R.id.editFname);
        String fName = temp.getText().toString();
        temp = (EditText)findViewById(R.id.editLname);
        String lName = temp.getText().toString();
        temp = (EditText)findViewById(R.id.editEmail);
        String email = temp.getText().toString();

        RadioButton gend = (RadioButton)findViewById(R.id.toggleMale);
        boolean male = gend.isSelected();
        gend = (RadioButton)findViewById(R.id.toggleFemale);
        boolean female = gend.isSelected();

        //EditText temp = (EditText)findViewById(R.id.DOB);
        //String dob = temp.getText().toString();
        //TODO
        //ethnicity change
        //file writing/upload

        // Write a message to the database
        //myRef.setValue("Hello, World!");
    }
}
