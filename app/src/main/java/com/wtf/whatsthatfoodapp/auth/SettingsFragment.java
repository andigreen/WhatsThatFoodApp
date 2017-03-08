package com.wtf.whatsthatfoodapp.auth;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.wtf.whatsthatfoodapp.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_preferences);
    }

}
