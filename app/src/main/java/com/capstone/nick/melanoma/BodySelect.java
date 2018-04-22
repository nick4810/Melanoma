package com.capstone.nick.melanoma;
/*
Note: not owner of human body image. Taken from the following source:
Link: https://commons.wikimedia.org/wiki/File:Human_body_schemes.png
Author: S.raeder, Creator:"MenschSDermatome" by Uwe Thormann - xfig. Licensed under
CC BY-SA 3.0 via Wikimedia Commons - https://commons.wikimedia.org/wiki/File:MenschSDermatome.png
Date: 6 October 2015
License: Creative Commons Attribution-Share Alike 4.0 International
    link: https://creativecommons.org/licenses/by-sa/4.0/legalcode

Modifications:
	Added color, selection boxes, split image in half.
	Also minor touch ups to fingers/toes/face/etc.

 */
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An activity to display a human body image where the user can select
 * the location that they are photoing.
 */
public class BodySelect extends NavigatingActivity implements View.OnTouchListener {

    private String userEmail;

    private TextView nextText;
    private TextView locationText;
    private Boolean front =true;


    /**
     * When activity is created, body image for selection is shown, along with a toggle switch for
     * front/back, and 'Next' is displayed in top-right of screen to progress.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_select);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userEmail = prefs.getString("USEREMAIL", "");
        super.onCreateDrawer();


        locationText = (TextView)findViewById(R.id.txt_Location);

        ImageView iv = (ImageView) findViewById (R.id.image);
        if (iv != null) {
            iv.setOnTouchListener (this);
        }


        final Switch toggleView = (Switch)findViewById(R.id.body_view);
        toggleView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                ImageView imageView = (ImageView) findViewById(R.id.image);
                int nextImage;

                if(isChecked){
                    front=false;
                    nextImage = R.drawable.human_areas_back;
                    toggleView.setText(R.string.back);
                } else {
                    front=true;
                    nextImage = R.drawable.human_areas_front;
                    toggleView.setText(R.string.front);
                }
                imageView.setImageResource (nextImage);
                imageView.setTag (nextImage);
            }
        });


        final Intent intent = new Intent(this, AddData.class);

        nextText = (TextView)findViewById(R.id.txt_Next);
        nextText.setTextColor(Color.BLUE);
        nextText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("LOCATION", locationText.getText().toString());
                startActivity(intent);
            }
        });
    }


    /**
     * Respond to the user touching the screen.
     * Change selected location based on where the user touched.
     */
    public boolean onTouch (View v, MotionEvent ev)
    {
        boolean handledHere;

        nextText.setVisibility(View.VISIBLE);

        final int action = ev.getAction();

        final int evX = (int) ev.getX();
        final int evY = (int) ev.getY();

        // If we cannot find the imageView, return.
        ImageView imageView = (ImageView) v.findViewById (R.id.image);
        if (imageView == null) return false;

        // Now that we know the current resource being displayed we can handle the DOWN and UP events.

        switch (action) {
            case MotionEvent.ACTION_DOWN :
                handledHere = true;
                break;

            case MotionEvent.ACTION_UP :
                // On the UP, we do the click action.
                // The hidden image (human_areas_select) has multiple hotspots on it.
                // Use image_areas to determine which region the user touched.
                v.performClick();
                int touchColor = getHotspotColor (R.id.image_areas, evX, evY);

                // Compare the touchColor to the expected values. Switch to a different image, depending on what color was touched.
                // Note that we use a Color Tool object to test whether the observed color is close enough to the real color to
                // count as a match. We do this because colors on the screen do not match the map exactly because of scaling and
                // varying pixel density.
                ColorTool ct = new ColorTool ();
                int tolerance = 25;

                Resources res = getResources(); 
                String[] locations = res.getStringArray(R.array.locations_array);

                if (ct.closeMatch (Color.RED, touchColor, tolerance)) {
                    //right hand (front)
                    locationText.setText(locations[0]);
                    if (!front) locationText.setText(locations[1]);
                }
                else if (ct.closeMatch (Color.YELLOW, touchColor, tolerance)) {
                    //right arm (front)
                    locationText.setText(locations[2]);
                    if (!front) locationText.setText(locations[3]);
                }
                else if (ct.closeMatch (Color.BLUE, touchColor, tolerance)) {
                    //left arm (front)
                    locationText.setText(locations[3]);
                    if (!front) locationText.setText(locations[2]);
                }
                else if (ct.closeMatch (Color.CYAN, touchColor, tolerance)) {
                    //left foot (front)
                    locationText.setText(locations[10]);
                    if (!front) locationText.setText(locations[9]);
                }
                else if (ct.closeMatch (Color.MAGENTA, touchColor, tolerance)) {
                    //chest
                    locationText.setText(locations[5]);
                    if (!front) locationText.setText(locations[11]);
                }
                else if (ct.closeMatch (Color.GRAY, touchColor, tolerance)) {
                    //right foot (front)
                    locationText.setText(locations[9]);
                    if (!front) locationText.setText(locations[10]);
                }
                else if (ct.closeMatch (Color.LTGRAY, touchColor, tolerance)) {
                    //head
                    locationText.setText(locations[4]);
                }
                else if (ct.closeMatch (Color.DKGRAY, touchColor, tolerance)) {
                    //torso
                    locationText.setText(locations[6]);
                    if (!front) locationText.setText(locations[12]);
                }
                else if (ct.closeMatch (Color.GREEN, touchColor, tolerance)) {
                    //right leg (front)
                    locationText.setText(locations[7]);
                    if (!front) locationText.setText(locations[8]);
                }
                else if (ct.closeMatch (Color.parseColor("#660a80" /*purple*/), touchColor, tolerance)) {
                    //left hand (front)
                    locationText.setText(locations[1]);
                    if (!front) locationText.setText(locations[0]);
                }
                else if (ct.closeMatch (Color.parseColor("#ff680a" /*orange*/), touchColor, tolerance)) {
                    //left leg (front)
                    locationText.setText(locations[8]);
                    if (!front) locationText.setText(locations[7]);
                }

                handledHere = true;
                break;

            default:
                handledHere = false;
        } // end switch

        return handledHere;
    }

    /**
     * Resume the activity.
     */
    @Override protected void onResume() {
        super.onResume();

    }


    /**
     * Get the color from the hotspot image at point x-y.
     *
     */
    public int getHotspotColor (int hotspotId, int x, int y) {
        ImageView img = (ImageView) findViewById (hotspotId);
        if (img == null) {
            Log.d ("ImageAreasActivity", "Hot spot image not found");
            return 0;
        } else {
            img.setDrawingCacheEnabled(true);
            Bitmap hotspots = Bitmap.createBitmap(img.getDrawingCache());
            if (hotspots == null) {
                Log.d ("ImageAreasActivity", "Hot spot bitmap was not created");
                return 0;
            } else {
                img.setDrawingCacheEnabled(false);
                return hotspots.getPixel(x, y);
            }
        }
    }

}

/**
 * A class with methods to help with colors.
 */
class ColorTool {

    /**
     * Return true if the two colors are a pretty good match.
     * To be a good match, all three color values (RGB) must be within the tolerance value given.
     *
     * @param color1 int
     * @param color2 int
     * @param tolerance int - the max difference that is allowed for any of the RGB components
     * @return boolean
     */
    boolean closeMatch(int color1, int color2, int tolerance) {
        if (Math.abs (Color.red (color1) - Color.red (color2)) > tolerance ) return false;
        if (Math.abs (Color.green (color1) - Color.green (color2)) > tolerance ) return false;
        if (Math.abs (Color.blue (color1) - Color.blue (color2)) > tolerance ) return false;

        return true;
    }

}

