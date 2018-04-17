package com.capstone.nick.melanoma;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import static com.google.android.gms.common.SignInButton.COLOR_DARK;
import static com.google.android.gms.common.SignInButton.SIZE_WIDE;

/**
 * Main screen of application. Can be used to display logos, designs, sign-in screen, etc.
 * Basic retrieval of the Google user's ID, email address, and profile. If a user was previously
 * signed in, and that sign-in is still cached in system, user will be automatically logged-in.
 * Credit to developers.google.com for this implementation of a Google Login system.
 */
public class MainScreen extends NavigatingActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    //private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;


    private boolean loggedIn = false;
    private boolean logMeOut = false;
    private boolean signMeIn = true;
    private String userEmail;

    /**
     * On activity creation, shows welcome screen/message, sign-in options, etc. If redirected here
     * from another activity by the 'Sign Out' option, user will be signed out of application and
     * Google account.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        if(getIntent().getExtras() !=null) {
            loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
            logMeOut = getIntent().getExtras().getBoolean("LOGMEOUT");
            signMeIn = getIntent().getExtras().getBoolean("AUTOSIGN");
            userEmail = getIntent().getExtras().getString("EMAIL");
        }

        SignInButton signInButton = (SignInButton)findViewById(R.id.sign_in_button);
        signInButton.setStyle(SIZE_WIDE, COLOR_DARK);


        super.onCreateDrawer(loggedIn, userEmail);
        mAuth = FirebaseAuth.getInstance();

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);

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

        if (logMeOut){
            final Intent intent = new Intent(this, MainScreen.class);
            intent.putExtra("AUTOSIGN", false);
            /* TODO:
            ** check parameters needed for this intent
            */
            mGoogleApiClient.connect();
            mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {

                    FirebaseAuth.getInstance().signOut();
                    if(mGoogleApiClient.isConnected()) {
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (status.isSuccess()) {
                                    Log.d(TAG, "User Logged out");
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Log.d(TAG, "Google API Client Connection Suspended");
                }
            });
        }
    }


    @Override
    /**
     * Google method to start a cached "silent" sign-in
     */
    public void onStart() {
        super.onStart();

        if(signMeIn) {
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
    }

    @Override
    /**
     * Google method to resume sign-in process
     */
    protected void onResume() {
        super.onResume();
        hideProgressDialog();
    }

    @Override
    /**
     * Google method called upon receiving sign-in result
     */
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
            // Signed in successfully, show authenticated UI.
            loggedIn=true;
            super.onCreateDrawer(loggedIn, userEmail);
            //mStatusTextView.setText(R.string.signed_in_fmt);
            updateUI(true);
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = result.getSignInAccount(); //account of logged in user
            userEmail = account.getEmail();
            firebaseAuthWithGoogle(account);

        } else {
            // Signed out, show unauthenticated UI.
            loggedIn=false;
            super.onCreateDrawer(loggedIn, userEmail);
            updateUI(false);
        }
    }

    /**
     * Gather data from the user's Google account to create an initial profile.txt.
     * This data is saved in their profile.txt file in the root of their directory.
     */
    private void buildProfile(GoogleSignInAccount account, String path) {
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

        FileHandler saver = new FileHandler();
        String data = "";

        //get data from Google account
        data+="First Name: ";
        data+= account.getGivenName();
        data+="\nLast Name: ";
        data+= account.getFamilyName();
        data+="\nEmail: ";
        data+= account.getEmail();

        //get data from radio buttons
        data+="\nSex: ";

        data+="\nDate of Birth: ";

        data+="\nEthnicity: ";
        //System.out.println(data);
        //save file
        saver.saveToFile(path, "profile.txt", data);
        final Uri file = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/profile.txt"));
        StorageReference fileRef = mStorageRef.child(userEmail+"/profile.txt");
        //upload to firebase
        fileRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //file uploaded
                //System.out.println("MainScreen uploaded profile to firebase");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //failed to upload file
                //System.out.println("MainScreen failed upload of profile to firebase");
            }
        });
    }


    /**
     * Start the sign-in process
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    /**
     * Handles the Firebase sign-in
     * @param acct Google account that was used to sign in
     */
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        final Intent intent = new Intent(this, ViewData.class);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            String profilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/";
                            File chk_profile = new File(profilePath + "profile.txt");
                            if(!chk_profile.exists()) {
                                //System.out.println("MainScreen building profile");
                                buildProfile(acct, profilePath);
                            }

                            intent.putExtra("LOGGEDIN", loggedIn);
                            intent.putExtra("EMAIL", userEmail);
                            startActivity(intent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }

                    }
                });
    }


    @Override
    /**
     * Connection failed when trying to sign in
     */
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    /**
     * Stopping the sign-in process
     */
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

    /**
     * Update the activity based on signed-in status
     * @param signedIn status of sign-in
     */
    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        } else {
            //mStatusTextView.setText(R.string.signed_out);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }
    }

    @Override
    /**
     * When button pressed on this screen, currently used to start the Google sign-in process, or
     * swap to the new-user sign up page.
     * @param v is used to identify the button that was pressed
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.btn_newUser:
                Intent intent = new Intent(this, NewUser.class);
                intent.putExtra("LOGGEDIN", loggedIn);
                startActivity(intent);
        }
    }
}
