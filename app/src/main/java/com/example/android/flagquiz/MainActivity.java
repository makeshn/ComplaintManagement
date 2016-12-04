package com.example.android.flagquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

import static android.R.attr.id;

public class MainActivity extends AppCompatActivity {
    public static final String region = "pref_regionstoinclude";
    public static final String number = "pref_numberofchoices";
    private boolean phoneDevice = true;
    private boolean prefchanged = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferencesChangeListener);
        if (phoneDevice)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (prefchanged) {
            MainActivityFragment quizfrag = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizfragment);
            quizfrag.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this));
            quizfrag.updateRegions(PreferenceManager.getDefaultSharedPreferences(this));
            quizfrag.quizreset();
            prefchanged = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        } else
            return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent preferenceintent = new Intent(this, SettingsActivity.class);
        startActivity(preferenceintent);

        return super.onOptionsItemSelected(item);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            prefchanged = true;
            MainActivityFragment quizfra = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizfragment);
            if (key.equals(number)) {
                quizfra.updateGuessRows(sharedPreferences);
                quizfra.quizreset();
            } else if (key.equals(region)) {
                Set<String> regions = sharedPreferences.getStringSet(region, null);
                if (regions != null && regions.size() > 0) {
                    quizfra.updateRegions(sharedPreferences);
                    quizfra.quizreset();

                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    regions.add(getString(R.string.default_region));
                    editor.putStringSet(region,regions);
                    editor.apply();
                    Toast.makeText(MainActivity.this,R.string.default_region_message,Toast.LENGTH_SHORT).show();

                }

            }
            Toast.makeText(MainActivity.this,"Restarting Quiz",Toast.LENGTH_SHORT).show();
        }
    };

}