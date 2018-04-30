package com.capstone.nick.melanoma;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.ViewHolder> {
    private ArrayList<String> patientList;
    public ArrayList<ViewHolder> patientViews = new ArrayList<>();//list of all images

    public ArrayList<String> modifyList;
    public ArrayList<ViewHolder> modifyViews;

    private Context context;
    private String userEmail;

    public PatientAdapter(final Context context, final ArrayList<String> patientList, String email) {
        this.userEmail = email;
        this.patientList = patientList;
        this.context = context;

        this.modifyList = patientList;
        this.modifyViews = patientViews;

    }

    @Override
    public PatientAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.patient_layout, viewGroup, false);
        ViewHolder temp = new ViewHolder(view);
        patientViews.add(temp);//add to list of all views

        return temp;
    }

    @Override
    public void onBindViewHolder(final PatientAdapter.ViewHolder viewHolder, int i) {
        String fileStr =patientList.get(i);
        String[] fileDet =fileStr.split("_");

        String pID ="";
        String pName ="";
        try {
            //directory name is [patientID]_[patientName]
            pID =fileDet[0];
            pName =fileDet[1];
        } catch (Exception e) {
            //e.printStackTrace();
        }
        viewHolder.name.setText(pName);
        viewHolder.ID.setText(pID);

        final Intent intent = new Intent(context, ViewPatient.class);
        intent.putExtra("PATIENTDETAILS", fileStr);

        viewHolder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(intent);
            }
        });

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String patient_det = settings.getString("PATIENTDETAILS", "");
        if(patient_det.equals(fileStr)) {
            viewHolder.btn.setChecked(true);
        }
        viewHolder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.equals(viewHolder.btn)) {
                    //Toast.makeText(context, "checkedNum:" +String.valueOf(patientViews.indexOf(viewHolder)), Toast.LENGTH_SHORT).show();
                    String fileStr = patientList.get(patientViews.indexOf(viewHolder));
                    SharedPreferences.Editor editor1 = settings.edit();
                    editor1.putString("PATIENTDETAILS", fileStr);
                    editor1.apply();

                    for (ViewHolder cellView : patientViews) {
                        if (!cellView.equals(viewHolder)) {
                            cellView.btn.setChecked(false);
                        }
                    }
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public void findAndRemove(ArrayList<String> fullList, String query) {
        modifyList = fullList;
        modifyViews = patientViews;
        patientList.clear();
        //patientViews.clear();
        if(query.isEmpty()) {
            patientList = modifyList;
            patientViews = modifyViews;
        } else {
            for(String pat : modifyList) {
                if(pat.toLowerCase().contains(query.toLowerCase())) {
                    patientList.add(pat);
                    patientViews.add(modifyViews.get(modifyList.indexOf(pat)));
                }

            }
        }
        notifyDataSetChanged();
    }

    
    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView name;
        public TextView ID;
        private ImageView img;
        public RadioButton btn;

        public ViewHolder(View view) {
            super(view);

            name = (TextView)view.findViewById(R.id.patient_name);
            ID = (TextView)view.findViewById(R.id.patient_ID);
            img = (ImageView) view.findViewById(R.id.img_profile);
            btn = (RadioButton)view.findViewById(R.id.patient_select);
        }
    }
}
