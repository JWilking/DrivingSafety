package edu.osu.drivingsafety;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class Settings extends PreferenceActivity {

    private static final String PREFS_NAME = "Initial_Ref";
    private boolean mInitial;

    private final String TAG = ((Object) this).getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Configuration");
        addPreferencesFromResource(R.xml.default_reference);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mInitial = settings.getBoolean("initial", true);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
        updatePrefs();
    }

    @Override
    public void onSaveInstanceState(Bundle instanceState) {
        Log.d(TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(instanceState);
        updatePrefs();
    }

    private static final int MENU_CONT = Menu.FIRST;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Standard menu
        menu.add(0, MENU_CONT, 0, "Continue")
                .setShortcut('0', 'c');
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CONT:
                if (mInitial) {
                    updatePrefs();
                    Intent intent = new Intent(this, DrivingMapActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    updatePrefs();
                    setResult(RESULT_OK);
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updatePrefs() {
        Log.d(TAG, "updatePrefs()");
        Preference defaultPref = findPreference("default_vibrate");
        SharedPreferences settings = defaultPref.getSharedPreferences();
        String sensitivity = settings.getString("default_Sensitivity", "Medium");
        String alarmInfo = settings.getString("default_AlarmInfo", "Fall down");
        String sound = settings.getString("default_Sound", "one");
        boolean vibrate = settings.getBoolean("default_vibrate", false);

        settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("initial", false);
        editor.putString("sensitivity", sensitivity);
        editor.putString("alarmInfo", alarmInfo);
        editor.putString("sound", sound);
        editor.putBoolean("vibrate", vibrate);
        // Don't forget to commit your edits!!!
        editor.commit();
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.commit_button:
//                if (mInitial) {
//                    updatePrefs();
//                    finish();
//                } else {
//                    updatePrefs();
//                    setResult(RESULT_OK);
//                    finish();
//                }
//                break;
//        }
//    }
}
