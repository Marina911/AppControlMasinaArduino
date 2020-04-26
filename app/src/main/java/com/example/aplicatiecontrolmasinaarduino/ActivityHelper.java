package com.example.aplicatiecontrolmasinaarduino;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.util.Log;

public class ActivityHelper {
    private static final String TAG = "ActivityHelper";
    public static void initialize(Activity activity) {
        Log.d(TAG, "IN initialize method!" );
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        String orientation = preferences.getString("prefOrientation", "Null");
        if ("Landscape".equals(orientation)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if ("Portrait".equals(orientation)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
        Log.d(TAG, "OUT initialize method!" );
    }

}