package com.capstone.nick.melanoma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;

/**
 * Activity to display details of the image that was just taken. At this point the user has the
 * option to add additional textual notes, or if they have changed the setting, they can record an
 * audio message.
 */
public class ImageDetails extends NavigatingActivity {
    private boolean loggedIn;
    private String userEmail;
    private String location;

    private String filename;
    private String path;
    private String date;
    private String time;

    private boolean useAudioMemo;
    private ImageButton recordingButton;
    private ImageButton stopButton;
    private ImageButton playButton;
    private Button matchButton;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {android.Manifest.permission.RECORD_AUDIO};

    private MediaRecorder mediaRecorder;
    String voiceStoragePath;

    MediaPlayer mediaPlayer;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    /**
     * On activity creation, a thumbnail of the image is shown, along with the date/time that the
     * image was taken. It also displays the body location that was previously selected, and gives
     * the user the option to change this via a dropdown box. Either a text box or audio buttons
     * will be displayed depending on the setting.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        useAudioMemo = sharedPreferences.getBoolean("pref_audio", false);
        if(useAudioMemo) {//wants to use audio memos, not textual
            //hide text box
            findViewById(R.id.img_notes).setVisibility(View.GONE);

            recordingButton = (ImageButton)findViewById(R.id.recording_button);
            stopButton = (ImageButton)findViewById(R.id.stop_button);
            playButton = (ImageButton)findViewById(R.id.play_button);
            //show recording buttons
            recordingButton.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.VISIBLE);
            //disable these buttons for now
            stopButton.setEnabled(false);
            stopButton.setBackgroundResource(R.drawable.button_stop_disabled);
            playButton.setEnabled(false);
            playButton.setBackgroundResource(R.drawable.button_play_disabled);

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.RECORD_AUDIO)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.RECORD_AUDIO},
                            REQUEST_RECORD_AUDIO_PERMISSION);
                }
            }

            recordingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mediaRecorder == null){
                        initializeMediaRecord();
                    }
                    startAudioRecording();
                }
            });

            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopAudioRecording();
                }
            });

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playLastStoredAudioMusic();
                    mediaPlayerPlaying();
                }
            });
        }

        userEmail = getIntent().getExtras().getString("EMAIL");
        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        location = getIntent().getExtras().getString("LOCATION");

        filename = getIntent().getExtras().getString("FILE");
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/";
        voiceStoragePath = path + "Raw Images/";
        path+="JPEG Images/";

        //audio file
        voiceStoragePath = voiceStoragePath + filename + ".3gpp";
        System.out.println("Audio path : " + voiceStoragePath);

        String tmpFile ="JPEG";//text file name
        tmpFile+=filename.substring(3);
        tmpFile+=".jpg";
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path+tmpFile), 150, 200);
        ImageView img = (ImageView)findViewById(R.id.image_taken);
        img.setImageBitmap(thumbnail);//set thumbnail into image view

        filename+=".txt";//string for txt file
        date = getIntent().getExtras().getString("DATE");
        time = getIntent().getExtras().getString("TIME");
        super.onCreateDrawer(loggedIn, userEmail);

        //set dropdown selection
        Spinner mySpinner = (Spinner)findViewById(R.id.img_location);
        mySpinner.setSelection(((ArrayAdapter)mySpinner.getAdapter()).getPosition(location));

        //set date/time into edittexts
        EditText setDet = (EditText)findViewById(R.id.img_date);
        setDet.setText(date, TextView.BufferType.EDITABLE);
        setDet = (EditText)findViewById(R.id.img_time);
        setDet.setText(time, TextView.BufferType.EDITABLE);

    }

    /**
     * When a button is pressed on this screen. Currently used for when the user is done, as this
     * will take them back to the 'display images' screen.
     * @param v
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_saveImage:
                saveMemoFile();
        }

        Intent intent = new Intent(this, ViewData.class);
        intent.putExtra("LOGGEDIN", loggedIn);
        intent.putExtra("EMAIL", userEmail);

        startActivity(intent);
    }

    /**
     * Depending on their preference, this will save the text file of the additional notes.
     */
    private void saveMemoFile() {
        if(!useAudioMemo) {
            saveTextFile();
        }
    }

    /**
     * Saves all of the data pertaining to the image in a text file. Filename is the same as the
     * image filename.
     */
    private void saveTextFile() {
        FileHandler saver = new FileHandler();
        String data = "";
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/Raw Images/";

        //gather data entered
        data+="Date: ";
        EditText dataSource = (EditText)findViewById(R.id.img_date);
        data+= dataSource.getText().toString();
        data+="\nTime: ";
        dataSource = (EditText)findViewById(R.id.img_time);
        data+= dataSource.getText().toString();
        data+="\nLocation: ";
        Spinner locSource = (Spinner)findViewById(R.id.img_location);
        data+= locSource.getSelectedItem().toString();
        data+="\nNotes:\n";
        dataSource = (EditText)findViewById(R.id.img_notes);
        data+= dataSource.getText().toString();

        //save file
        if(!saver.saveToFile(path, filename, data)) {
            System.out.println("Error saving file");
        }
    }

    @Override
    /**
     * Used to request permissions for recording audio memos.
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                initializeMediaRecord();
                break;
        }
        if (!permissionToRecordAccepted ) {
            finish();
        }

    }

    /**
     * Uses the MediaRecorder, starts the recording and disables buttons not currently needed.
     */
    private void startAudioRecording(){
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recordingButton.setEnabled(false);
        recordingButton.setBackgroundResource(R.drawable.button_rec_disabled);
        stopButton.setEnabled(true);
        stopButton.setBackgroundResource(R.drawable.button_stop_enabled);
    }

    /**
     * Stops the recording and disables buttons not currently needed.
     */
    private void stopAudioRecording(){
        if(mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        stopButton.setEnabled(false);
        stopButton.setBackgroundResource(R.drawable.button_stop_disabled);
        playButton.setEnabled(true);
        playButton.setBackgroundResource(R.drawable.button_play_enabled);
    }

    /**
     * Plays the audio file that the user just recorded and disables buttons not currently needed.
     */
    private void playLastStoredAudioMusic(){
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(voiceStoragePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
        recordingButton.setEnabled(true);
        recordingButton.setBackgroundResource(R.drawable.button_rec_enabled);
        playButton.setEnabled(false);
        playButton.setBackgroundResource(R.drawable.button_play_disabled);
    }

    private void stopAudioPlay(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void mediaPlayerPlaying(){
        if(!mediaPlayer.isPlaying()){
            stopAudioPlay();
        }
    }

    /**
     * Sets up the MediaRecorder with details specified.
     */
    private void initializeMediaRecord(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(voiceStoragePath);
    }

}
