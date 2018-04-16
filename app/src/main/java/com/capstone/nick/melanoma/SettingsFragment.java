package com.capstone.nick.melanoma;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Fragment to load the preferences file
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}