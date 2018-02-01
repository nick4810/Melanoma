package com.capstone.nick.melanoma;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.SignInButton;

import static com.google.android.gms.common.SignInButton.COLOR_DARK;
import static com.google.android.gms.common.SignInButton.SIZE_WIDE;

public class NewUser extends NavigatingActivity {

    private boolean loggedIn;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        userEmail = getIntent().getExtras().getString("EMAIL");
        boolean logMeOut = getIntent().getExtras().getBoolean("LOGMEOUT");

        /*TODO
        ** create a questionnaire for new users, load data submitted into database
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

    }


}