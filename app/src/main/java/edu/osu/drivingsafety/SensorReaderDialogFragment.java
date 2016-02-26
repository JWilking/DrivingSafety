package edu.osu.drivingsafety;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by adamcchampion on 2015/03/19.
 */
public class SensorReaderDialogFragment extends DialogFragment {

    private String TAG = ((Object) this).getClass().getSimpleName();
    private HelperClass mHelperInstance;

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout to use as dialog or embedded fragment
//        return inflater.inflate(R.layout.sensor_dialog, container, false);
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mHelperInstance = HelperClass.getInstance();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(mHelperInstance.getSensorView())
                .setTitle(R.string.sensor_view_dialog_title)
            .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.remove(fm.findFragmentByTag(getString(R.string.sensor_reader_fragment)));
                    ft.commit();
                    dialog.dismiss();
                }
            });

        return builder.create();
    }
}
