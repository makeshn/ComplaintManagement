package com.example.android.flagquiz;

import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;

public class SettingsActivityFragment extends PreferenceFragment{
    @Override

   public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences);
    }
}