package com.capstone.nick.melanoma;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import java.io.File;

public class SettingsActivity extends NavigatingActivity {
    private boolean loggedIn;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        userEmail = getIntent().getExtras().getString("EMAIL");
        super.onCreateDrawer(loggedIn, userEmail);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delAccount:
                deleteAccount();
        }
    }

    private void deleteAccount() {
        //confirm before deleting account
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);

        final Intent intent = new Intent(this, MainScreen.class);
        intent.putExtra("LOGGEDIN", loggedIn);
        intent.putExtra("LOGMEOUT", true);
        intent.putExtra("EMAIL", userEmail);

        final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/";

        builder.setTitle("Confirm Selection")
                .setMessage("Are you absolutely sure you want to delete your account and all associated files/images?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //go through with deletion
                        //delete firebase data
                        //delete local data
                        File deleteDir = new File(path);
                        deleteRecursive(deleteDir);
                        //upon deletion go to main screen
                        startActivity(intent);
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

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();

    }

}
