package com.capstone.nick.melanoma;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
/* TODO
* Keep track of the user's moles, separate them out by location, data, etc. Allow them to be
* labeled for easy identification.
* In ImageDetails allow new image to be tagged with label.
* Separate images in ViewData by mole.
 */
public class ViewMoles extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_moles);
    }
}
