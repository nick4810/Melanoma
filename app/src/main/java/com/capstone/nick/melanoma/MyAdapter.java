package com.capstone.nick.melanoma;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<CreateList> galleryList;
    //public hashmap?
    public ArrayList<ViewHolder> imageViews = new ArrayList<>();
    public ArrayList<ViewHolder> selViews = new ArrayList<>();

    private Context context;

    private String userEmail;

    public MyAdapter(Context context, ArrayList<CreateList> galleryList, String email) {
        this.userEmail = email;
        this.galleryList = galleryList;
        this.context = context;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_layout, viewGroup, false);
        ViewHolder temp = new ViewHolder(view);
        imageViews.add(temp);
        return temp;
    }

    @Override
    public void onBindViewHolder(final MyAdapter.ViewHolder viewHolder, int i) {
        String fileStr =galleryList.get(i).getImage_title();
        String newTitle = fileStr.substring(10,16);
        newTitle+=fileStr.substring(5,10);
        newTitle+=fileStr.substring(16,18);
        newTitle+=":";
        newTitle+=fileStr.substring(19,21);
        viewHolder.title.setText(newTitle);

        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/JPEG Images/";
        path+=fileStr;
        viewHolder.img.setImageBitmap(decodeSampledBitmapFromResource(path, 150, 150));

        viewHolder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Image",Toast.LENGTH_SHORT).show();
                //show bigger view of image
            }
        });

        viewHolder.chkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Checked",Toast.LENGTH_SHORT).show();
                selViews.add(viewHolder);
                //if(!isChecked()) add to list of selected images
            }
        });
        viewHolder.chkBox.setVisibility(View.INVISIBLE);
    }

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

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        private ImageView img;
        public CheckBox chkBox;
        public ViewHolder(View view) {
            super(view);

            title = (TextView)view.findViewById(R.id.title);
            img = (ImageView) view.findViewById(R.id.img);
            chkBox = (CheckBox)view.findViewById(R.id.chkImage);
        }
    }
}
