package com.capstone.nick.melanoma;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BodySelect extends NavigatingActivity implements View.OnTouchListener {

    private boolean loggedIn;
    private String userEmail;
    private TextView nextText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_select);

        userEmail = getIntent().getExtras().getString("EMAIL");
        loggedIn = getIntent().getExtras().getBoolean("LOGGEDIN");
        super.onCreateDrawer(loggedIn, userEmail);



        final Intent intent = new Intent(this, AddData.class);
        intent.putExtra("LOGGEDIN", loggedIn);
        intent.putExtra("EMAIL", userEmail);

        nextText = (TextView)findViewById(R.id.txt_Next);
        nextText.setTextColor(Color.BLUE);
        nextText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });

        ImageView iv = (ImageView) findViewById (R.id.image);
        if (iv != null) {
            iv.setOnTouchListener (this);
        }

        toast ("Touch the screen to discover where the regions are.");
    }


    /**
     * Respond to the user touching the screen.
     * Change images to make things appear and disappear from the screen.
     *
     */

    public boolean onTouch (View v, MotionEvent ev)
    {
        boolean handledHere = false;

        nextText.setVisibility(View.VISIBLE);

        final int action = ev.getAction();

        final int evX = (int) ev.getX();
        final int evY = (int) ev.getY();
        int nextImage = -1;			// resource id of the next image to display

        // If we cannot find the imageView, return.
        ImageView imageView = (ImageView) v.findViewById (R.id.image);
        if (imageView == null) return false;

        // When the action is Down, see if we should show the "pressed" image for the default image.
        // We do this when the default image is showing. That condition is detectable by looking at the
        // tag of the view. If it is null or contains the resource number of the default image, display the pressed image.
        Integer tagNum = (Integer) imageView.getTag ();
        int currentResource = (tagNum == null) ? R.drawable.p2_ship_default : tagNum.intValue ();

        // Now that we know the current resource being displayed we can handle the DOWN and UP events.

        switch (action) {
            case MotionEvent.ACTION_DOWN :
                if (currentResource == R.drawable.p2_ship_default) {
                    nextImage = R.drawable.p2_ship_pressed;
                    handledHere = true;
       /*
       } else if (currentResource != R.drawable.p2_ship_default) {
         nextImage = R.drawable.p2_ship_default;
         handledHere = true;
       */
                } else handledHere = true;
                break;

            case MotionEvent.ACTION_UP :
                // On the UP, we do the click action.
                // The hidden image (image_areas) has three different hotspots on it.
                // The colors are red, blue, and yellow.
                // Use image_areas to determine which region the user touched.
                v.performClick();
                int touchColor = getHotspotColor (R.id.image_areas, evX, evY);

                // Compare the touchColor to the expected values. Switch to a different image, depending on what color was touched.
                // Note that we use a Color Tool object to test whether the observed color is close enough to the real color to
                // count as a match. We do this because colors on the screen do not match the map exactly because of scaling and
                // varying pixel density.
                ColorTool ct = new ColorTool ();
                int tolerance = 25;
                //nextImage = R.drawable.p2_ship_default;
                if (ct.closeMatch (Color.RED, touchColor, tolerance))
                    //nextImage = R.drawable.;
                else if (ct.closeMatch (Color.BLUE, touchColor, tolerance))
                    //nextImage = R.drawable.p2_ship_powered;
                else if (ct.closeMatch (Color.YELLOW, touchColor, tolerance))
                    //nextImage = R.drawable.p2_ship_no_star;
                else if (ct.closeMatch (Color.WHITE, touchColor, tolerance))
                    //nextImage = R.drawable.p2_ship_default;

                // If the next image is the same as the last image, go back to the default.
                //toast ("Current image: " + currentResource + " next: " + nextImage);
                if (currentResource == nextImage) {
                    //nextImage = R.drawable.p2_ship_default;
                }
                handledHere = true;
                break;

            default:
                handledHere = false;
        } // end switch

        if (handledHere) {

            if (nextImage > 0) {
                imageView.setImageResource (nextImage);
                imageView.setTag (nextImage);
            }
        }
        return handledHere;
    }

    /**
     * Resume the activity.
     */

    @Override protected void onResume() {
        super.onResume();

    }


/**
 */
// More methods

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

    /**
     * Show a string on the screen via Toast.
     *
     * @param msg String
     * @return void
     */

    public void toast (String msg)
    {
        Toast.makeText (getApplicationContext(), msg, Toast.LENGTH_LONG).show ();
    }

}

/**
 * A class with methods to help with colors.
 * (Only one method so far.)
 *
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

