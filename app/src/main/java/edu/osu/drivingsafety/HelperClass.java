package edu.osu.drivingsafety;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlarmManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by adamcchampion on 2015/02/11.
 * Helper class to manage the app's Context.
 */
public class HelperClass {

    // Instance fields.
    private SharedPreferences mPreference;
    private View mSensorView;
    private String mLegalese;
    private Intent mServiceIntent;
    private boolean mIsAlerted, mToAlert, mNotificationClosed;

    // Static fields.
    private static Context mContext;
    private static HelperClass instance;

    private static final String TAG = HelperClass.class.getSimpleName();

    public HelperClass() {
    }

    public static HelperClass getInstance() {
        return getInstance(mContext);
    }

    public static HelperClass getInstance(Context ctx) {
        mContext = ctx;
        if (instance == null) {
            instance = new HelperClass();

            Log.i("HelperClass", "Initializing HelperClass Instance");

            if (ctx != null) {
                instance.mPreference = PreferenceManager.getDefaultSharedPreferences(ctx);
            }
        }
        return instance;
    }

//    public void saveToPreferences(int keyID, Boolean value) {
//        SharedPreferences.Editor editor = mPreference.edit();
//        editor.putString(mContext.getString(keyID), Boolean.toString(value));
//
//        if (!editor.commit()) {
//            Log.d(TAG, "Error committing string with ID " + keyID + " to shared prefs");
//        }
//    }

//    public boolean getFromPreferences(int keyID, boolean defaultValue) {
//        return mPreference.getBoolean(mContext.getString(keyID), defaultValue);
//    }

    public synchronized void saveToPreferences(int keyID, String value) {
        SharedPreferences.Editor editor = mPreference.edit();
        synchronized (editor) {
            editor.putString(mContext.getString(keyID), value);
        }

        if (!editor.commit()) {
            Log.d(TAG, "Error committing string with ID " + keyID + " to shared prefs");
        }
    }

    public synchronized String getFromPreferences(int keyID, String defaultValue) {
        if (mPreference == null) {
            mPreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        }


        return mPreference.getString(mContext.getString(keyID), defaultValue);
    }

    public synchronized SharedPreferences getSharedPrefs() {
        return mPreference;
    }

//    public NotificationManager getNotificationManager() {
//        return mNotificationManager;
//    }

    public synchronized void setSensorView(View v) {
        mSensorView = v;
    }

    public synchronized View getSensorView() {
        return mSensorView;
    }

    public synchronized void setLegalese(String legal) {
        mLegalese = legal;
    }

    public synchronized String getLegalese() {
        return mLegalese;
    }

    public Context getContext() {
        return mContext;
    }

    public synchronized void setIsAlerted(boolean b) {
        mIsAlerted = b;
    }

    public synchronized boolean getIsAlerted() {
        return mIsAlerted;
    }

    public synchronized void setToAlert(boolean b) {mToAlert = b;}

    public synchronized boolean getToAlert() { return mToAlert; }

}
