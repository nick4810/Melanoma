package com.capstone.nick.melanoma;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import static com.google.android.gms.common.SignInButton.COLOR_DARK;
import static com.google.android.gms.common.SignInButton.SIZE_WIDE;

/**
 * An activity that allows new users to sign-up for the service. It offers them a form to fill out
 * to collect personal data in order to help with future diagnoses. The user can then complete their
 * sign-up by connecting their Google account.
 */
public class NewUser extends NavigatingActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    //private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;
    private GoogleSignInAccount account;
    private boolean signInResult =false;

    private String userEmail;
    private StorageReference mStorageRef;

    /**
     * When the activity is launched, form is displayed, client is built for Google sign-in, and
     * styling of buttons changed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        /*TODO
        ** new users log in with google, load data submitted into database
         */
        super.onCreateDrawer();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton)findViewById(R.id.sign_in_button_newUser);
        signInButton.setStyle(SIZE_WIDE, COLOR_DARK);
        signInButton.setOnClickListener(this);
    }

    /**
     * When button pressed on this screen, currently used to start the Google sign-in process
     * @param v is used to identify the button that was pressed
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button_newUser:
                signIn();
                break;
        }

    }

    /**
     * Start the sign-in process, capture the data entered on completion
     */
    public void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

        if(signInResult) {
            uploadChanges();
        }
    }

    /**
     * Collect the data that the user entered into the form, along with the preferences that were
     * selected. This data is saved in their profile.txt file in the root of their directory. After
     * file is saved, go to the default 'View Images' screen.
     */
    public void uploadChanges() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor1 = settings.edit();
        CheckBox audio_chk = (CheckBox)findViewById(R.id.newUser_audio);
        if(audio_chk.isChecked()) {//user chose to use audio memos, save preference
            editor1.putBoolean("pref_audio", true);
            editor1.apply();
        }
        //Set this preference to off. Since this is a Shared Preference other users can change it,
        //but initially we'll set it to disabled.
        editor1.putBoolean("pref_wifiOnly", false);
        editor1.apply();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        FileHandler saver = new FileHandler();
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/";
        String data = "";

        //get data from Google account
        data+="First Name: ";
        data+= account.getGivenName();
        data+="\nLast Name: ";
        data+= account.getFamilyName();
        data+="\nEmail: ";
        data+= account.getEmail();

        data+="\nSpecialty: ";
        EditText temp = (EditText)findViewById(R.id.newUser_spec);
        data+= temp.getText().toString();
        data+="\nMedical ID: ";
        temp = (EditText)findViewById(R.id.newUser_medID);
        data+= temp.getText().toString();
        data+="\nCountry: ";
        temp = (EditText)findViewById(R.id.newUser_country);
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

        //System.out.println(data);
        //save file
        saver.saveToFile(path, "profile.txt", data);
        final Uri file = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/profile.txt"));
        StorageReference fileRef = mStorageRef.child(userEmail+"/profile.txt");
        //upload to firebase
        fileRef.putFile(file);

        Intent intent = new Intent(this, AllPatients.class);
        startActivity(intent);
    }


    /**
     * Google method to start sign-in
     */
    @Override
    public void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    /**
     * Google method to resume sign-in process
     */
    @Override
    protected void onResume() {
        super.onResume();
        hideProgressDialog();
    }

    /**
     * Google method called upon receiving sign-in result
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /**
     * Google method to handle sign-in result. If signed in the nav drawer updates to reflect this,
     * and a Firebase entry is created/accessed.
     * @param result The sign-in result returned by the GoogleSignIn process
     */
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully
            account = result.getSignInAccount(); //account of logged in user
            userEmail =account.getEmail();

            //set a preference for storing user's name, email
            SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
            prefEditor.putString("USEREMAIL", userEmail);
            prefEditor.putString("USERNAME", account.getGivenName()+" "+account.getFamilyName());
            prefEditor.apply();

            super.onCreateDrawer();
            // Google Sign In was successful, authenticate with Firebase
            firebaseAuthWithGoogle(account);

            signInResult =true;

        } else {
            // Signed out, show unauthenticated UI.
            super.onCreateDrawer();
            //updateUI(false);

            signInResult =false;
        }
    }


    /**
     * Handles the Firebase sign-in
     * @param acct Google account that was used to sign in
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }

                    }
                });
    }


    /**
     * Connection failed when trying to sign in
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    /**
     * Stopping the sign-in process
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Showing progress of sign-in
     */
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    /**
     * Hiding progress of sign-in
     */
    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

}
