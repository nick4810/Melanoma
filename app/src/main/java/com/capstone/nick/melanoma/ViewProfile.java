package com.capstone.nick.melanoma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

//TODO
//check connection - then get profile from Firebase, otherwise locally
//Give ability to change profile picture/header image

/**
 * Activity to view the user's profile data. Displays information such as name, dob, ethnicity, etc.
 * User can modify some of these fields, but others are pulled from their Google account. If the
 * questionnaire was completed when they signed up, that data is displayed here.
 */
public class ViewProfile extends NavigatingActivity  {
    private String  userEmail;
    private StorageReference mStorageRef;

    private static int RESULT_LOAD_IMG = 1;
    private String imgDecodableString;

    private String sel_image ="";
    private String PROFILEIMG = "PROFILEIMAGE";
    private String HEADERIMG = "HEADERIMAGE";

    /**
     * When activity created, all data pulled from profile.txt is displayed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userEmail = prefs.getString("USEREMAIL", "");
        super.onCreateDrawer();

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

        //change the profile picture/header if user has them set
        String profPath =android.os.Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).toString() + "/" + userEmail + "/profile.jpg";
        String headPath =android.os.Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).toString() + "/" + userEmail + "/header.jpg";
        File chk_prof = new File(profPath);
        File chk_head = new File(headPath);

        if(chk_prof.exists()) {
            //Drawable pImg = new BitmapDrawable(getResources(), profPath);
            ImageView prof = (ImageView)findViewById(R.id.img_profile);
            prof.setImageBitmap(decodeSampledBitmapFromResource(profPath, 125, 125));
        }
        if(chk_head.exists()) {
            Drawable hImg = new BitmapDrawable(getResources(), decodeSampledBitmapFromResource(headPath, 500, 135));
            findViewById(R.id.img_profileHeader).setBackground(hImg);
        }

    }

    /**
     * Loads data from profile.txt into the appropriate fields.
     */
    private void loadProfile() {
        StorageReference fileRef = mStorageRef.child(userEmail+"/profile.txt");

        File rootPath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/");
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
            EditText setText = (EditText) findViewById(R.id.editFname);
            setText.setText(lines[0].substring(12));
            setText = (EditText) findViewById(R.id.editLname);
            setText.setText(lines[1].substring(11));
            setText = (EditText) findViewById(R.id.editEmail);
            setText.setText(lines[2].substring(7));

            setText = (EditText) findViewById(R.id.editSpec);
            setText.setText(lines[3].substring(11));
            setText = (EditText) findViewById(R.id.edit_medID);
            setText.setText(lines[4].substring(12));
            setText = (EditText) findViewById(R.id.editCountry);
            setText.setText(lines[5].substring(9));

            //set the radio button selections
            String gendStr = lines[6].substring(5);
            RadioGroup setSel = (RadioGroup) findViewById(R.id.gendGroup);
            if (gendStr.equals("Male"))
                setSel.check(R.id.toggleMale);
            else if (gendStr.equals("Female"))
                setSel.check(R.id.toggleFemale);

            setText = (EditText) findViewById(R.id.editDOB);
            setText.setText(lines[7].substring(15));

            String ethStr = lines[8].substring(11);
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
                break;
            case R.id.img_profile:
                sel_image =PROFILEIMG;
                loadImagefromGallery(v);
                break;
            case R.id.img_profileHeader:
                sel_image =HEADERIMG;
                loadImagefromGallery(v);
                break;
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

        data+="\nSpecialty: ";
        temp = (EditText)findViewById(R.id.editSpec);
        data+= temp.getText().toString();
        data+="\nMedical ID: ";
        temp = (EditText)findViewById(R.id.edit_medID);
        data+= temp.getText().toString();
        data+="\nCountry: ";
        temp = (EditText)findViewById(R.id.editCountry);
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
        final Uri file = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/profile.txt"));
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

    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);

                //save to local directory
                saveFile(imgDecodableString, sel_image);

                cursor.close();

                ImageView imgView = new ImageView(this);
                if (sel_image.equals(PROFILEIMG)) {
                    imgView = (ImageView) findViewById(R.id.img_profile);
                } else if (sel_image.equals(HEADERIMG)) {
                    imgView = (ImageView) findViewById(R.id.img_profileHeader);
                }
                // Set the Image in ImageView after decoding the String
                imgView.setImageBitmap(BitmapFactory
                        .decodeFile(imgDecodableString));

            } else {
                Toast.makeText(this, "You haven't picked an image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }

    void saveFile(String oldPath, String imageChoice) {
        String destinationFilename = "";
        if(imageChoice.equals(PROFILEIMG)) {
            destinationFilename = android.os.Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + "/" + userEmail + "/profile.jpg";
        } else {
            destinationFilename = android.os.Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + "/" + userEmail + "/header.jpg";
        }

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(oldPath));
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Used to create thumbnails for images
     * @param path Image file directory
     * @param reqWidth Requested width of thumbnail
     * @param reqHeight Requested height of thumbnail
     * @return Bitmap for thumbnail
     */
    private Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * Finds the largest number to divide by while still keeping requested width/height
     * @param options Options for creating Bitmap
     * @param reqWidth Requested width of thumbnail
     * @param reqHeight Requested height of thumbnail
     * @return Largest value for creating smallest thumbnail
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
