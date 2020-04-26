package com.example.aplicatiecontrolmasinaarduino;

import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private static final String TAG = "PreferencesActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "IN onCreate method!" );
        super.onCreate(savedInstanceState);
        ActivityHelper.initialize(this);
        Log.d(TAG, "OUT onCreate method!" );
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "IN onSharedPreferenceChanged method!" );
        Preference preferences = findPreference(key);

        if (preferences instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preferences;
            preferences.setSummary(listPref.getEntry());
            ActivityHelper.initialize(this);
        }

        if (preferences instanceof EditTextPreference) {
            EditTextPreference editPref = (EditTextPreference) preferences;
            preferences.setSummary(editPref.getText());
        }
        Log.d(TAG, "OUT onSharedPreferenceChanged method!" );
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "IN onPause method!" );
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
        Log.d(TAG, "OUT onPause method!" );
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "IN onResume method!" );
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        Map<String, ?> keys = PreferenceManager.getDefaultSharedPreferences(this).getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            Log.d(TAG, entry.getKey() + ": " + entry.getValue().toString());
            Preference pref = findPreference(entry.getKey());
            if (pref != null) {
                pref.setSummary(entry.getValue().toString());
            }
        }
        Log.d(TAG, "OUT onResume method!" );
        super.onResume();
    }
}