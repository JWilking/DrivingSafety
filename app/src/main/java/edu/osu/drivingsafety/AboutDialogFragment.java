package edu.osu.drivingsafety;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by adamcchampion on 2014/09/22.
 */
public class AboutDialogFragment extends DialogFragment {
    private HelperClass mHelperInstance;
    private String mDialogText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mHelperInstance == null) {
            mHelperInstance = HelperClass.getInstance();
        }

        mDialogText = mHelperInstance.getFromPreferences(R.string.google_play_text_key, "");

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.about_app)
                .setMessage(mDialogText)
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.remove(fm.findFragmentByTag(getString(R.string.about_fragment)));
                        ft.commit();
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
