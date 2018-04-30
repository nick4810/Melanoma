package com.capstone.nick.melanoma;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * This class is used to populate the recyclerview for the image gallery. It stores lists of images,
 * selected images, sets thumbnails, etc. It is also used to register the onClickListeners, and
 * create the image titles that are more user-friendly.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<CreateList> galleryList;

    public ArrayList<ViewHolder> imageViews = new ArrayList<>();//list of all images
    public ArrayList<ViewHolder> selViews = new ArrayList<>();//list of selected images

    private Context context;

    private String userEmail;
    private String patient_det;

    public MyAdapter(Context context, ArrayList<CreateList> galleryList, String email, String det) {
        this.userEmail = email;
        this.galleryList = galleryList;
        this.context = context;
        this.patient_det = det;
    }

    /**
     * When new image added, add to recyclerview for viewing and to total list of images.
     */
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_layout, viewGroup, false);
        ViewHolder temp = new ViewHolder(view);
        imageViews.add(temp);//add to list of all images

        return temp;
    }

    /**
     * Set the image title, thumbnail, onClickListeners
     */
    @Override
    public void onBindViewHolder(final MyAdapter.ViewHolder viewHolder, int i) {
        String fileStr =galleryList.get(i).getImage_title();
        viewHolder.setFilename(fileStr);

        //create a more user-friendly title showing date/time
        //"JPEG_" + location + "_" + yyyy_MM_dd_HH_mm_ss_SSS + ".jpg");
        int len = fileStr.length();
        String newTitle =fileStr.substring(5, len-27);
        newTitle+= fileStr.substring(len-23, len-16);//month, day
        newTitle+=fileStr.substring(len-27, len-22);//year
        newTitle+=fileStr.substring(len-16, len-14);//hour
        newTitle+=":";
        newTitle+=fileStr.substring(len-13, len-11);//min
        viewHolder.title.setText(newTitle);//set imageview title

        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/"+userEmail+"/"+patient_det+"/JPEG Images/";
        path+=fileStr;
        //set thumbnail into imageview
        viewHolder.img.setImageBitmap(decodeSampledBitmapFromResource(path, 150, 150));

        viewHolder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context,"Image",Toast.LENGTH_SHORT).show();
                //show bigger view of image
            }
        });

        viewHolder.chkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((CheckBox)v).isChecked();
                if(isChecked) {//add to list of selected images
                    selViews.add(viewHolder);
                    //Toast.makeText(context,String.valueOf(selViews.size()),Toast.LENGTH_SHORT).show();
                } else { //already checked, remove selection
                    selViews.remove(viewHolder);
                    //Toast.makeText(context,String.valueOf(selViews.size()),Toast.LENGTH_SHORT).show();
                }
            }
        });
        viewHolder.chkBox.setVisibility(View.INVISIBLE);
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

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    /**
     * This class binds the thumbnail, title, checkbox, and filename to the image itself.
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        private ImageView img;
        public CheckBox chkBox;
        public String filename;
        public ViewHolder(View view) {
            super(view);

            title = (TextView)view.findViewById(R.id.title);
            img = (ImageView) view.findViewById(R.id.img);
            chkBox = (CheckBox)view.findViewById(R.id.chkImage);
        }

        public void setFilename(String name) {
            filename =name;
        }
    }
}
