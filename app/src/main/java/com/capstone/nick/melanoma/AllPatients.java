package com.capstone.nick.melanoma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;


public class AllPatients extends NavigatingActivity {
    private String userEmail;
    private String patient_det;

    private PatientAdapter adapter;

    private StorageReference mStorageRef;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_patients);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userEmail = prefs.getString("USEREMAIL", "");
        patient_det = prefs.getString("PATIENTDETAILS", "");
        super.onCreateDrawer();

        //set up image gallery
        final RecyclerView recyclerView = (RecyclerView)findViewById(R.id.patientgallery);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayout.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        ArrayList<String> patientList = prepareData();
        adapter = new PatientAdapter(getApplicationContext(), patientList, userEmail);
        recyclerView.setAdapter(adapter);

        /**
         * TODO
         * add ability to remove patients
         * search not working properly

        SearchView searchBox = (SearchView)findViewById(R.id.patient_search);
        searchBox.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //adapter.resetView();
                adapter.findAndRemove(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //adapter.resetView();
                adapter.findAndRemove(newText);
                return true;
            }
        });
         */

        //for firebase storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance();

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addPatient:
                Intent intent = new Intent(this, NewPatient.class);
                startActivity(intent);
                break;
        }
    }


    private ArrayList<String> prepareData(){
        ArrayList<String> allPatients = new ArrayList<>();

        String path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/";//specify path
        //System.out.println(path);

        File f = new File(path);
        File file[] = f.listFiles();
        try {
            for (File thisPat : file) {
                String dirName =thisPat.getName();
                if(!dirName.equals("profile.txt") && !dirName.equals("profile.jpg") && !dirName.equals("header.jpg")) {
                    allPatients.add(dirName);
                }
            }
        } catch (NullPointerException e) {
            System.out.println("Null Patient list");
        }

        return allPatients;
    }
}
