package edu.osu.drivingsafety;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by adamcchampion on 2015/02/05.
 */
public class GetGooglePlayTextTask extends AsyncTask<Context, Void, String> {

    Context mCtx;
    HelperClass mHelperInstance;
    private final String TAG = ((Object) this).getClass().getSimpleName();

    @Override
    protected String doInBackground(Context... params) {
        String str = "";

        if (params.length > 0) {
            mCtx = (Context) params[0];

            str = mCtx.getString(R.string.code_credit) + "\n\nLegal Notices\n\n";
            Log.d(TAG, "Fetching open source software license info...");
            String googlePlayStr = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(mCtx);
            Log.d(TAG, "Fetched open source software license info.");

            str = str + googlePlayStr;
        }
        return str;
    }

    @Override
    protected void onPostExecute(String str) {
        Log.d(TAG, "onPostExecute()");
        //Log.d(TAG, "str = " + str);
        mHelperInstance = HelperClass.getInstance();
        Log.d(TAG, "Writing license string to shared prefs");
        mHelperInstance.saveToPreferences(R.string.google_play_text_key, str);
        Log.d(TAG, "Wrote license string to shared prefs");
        //mHelperInstance.setLegalese(str);
        //Log.d(TAG, "Stored legalese in helper instance");
    }
}
