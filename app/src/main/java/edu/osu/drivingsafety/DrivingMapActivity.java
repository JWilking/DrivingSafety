package edu.osu.drivingsafety;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Date;

public class DrivingMapActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private final String TAG = ((Object) this).getClass().getSimpleName();
    private SharedPreferences mSettings;
    private long vibrating_time[] = new long[]{Constants.VIBRATION_SHORT, Constants.VIBRATION_MED,
            Constants.VIBRATION_SHORT, Constants.VIBRATION_LONG};// preference related.

    private HelperClass mHelperInstance;

    private TextView mSpeedView;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    private AudioManager volumeControl;

    /* TEST: SPEED LIMIT */


    private Uri soundUri;  // preference related.
    private long vibration[];  // preference related.
    private String alarmInfo;  // preference related.

    private final static double G = SensorManager.GRAVITY_EARTH;
    private final static double MPS_TO_MPH = 2.236936292054;
    private final static double DEFAULT_LAT = 40.0;
    private final static double DEFAULT_LNG = -83.0;
    private final static float DEFAULT_ZOOM = 4;
    private final static int RG_REQUEST = 0;
    private final static int DRIVE_SPEED_MPH = 20;
    private final static int DRIVE_SPEED_DEC = 2;

    private SensorService mService;
    private SensorReaderView myReaderView;
    private DrivingMapActivity mDrivingMapActivity = this;
    private boolean isSensorScreen;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates;

    protected String mLastUpdateTime;
    private NotificationManager mNotifyMgr;
    private boolean gotGooglePlayText = false;

    private DialogFragment sensorReaderDialogFragment, aboutDialogFragment;

    private int numTimesShowSensors = 0;

    private Intent mBindIntent;
    private static boolean mIsServiceBound = false;

    private ServiceConnection mConnection;

    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG, "buildGoogleApiClient()");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "+++ onCreate() +++");

        if (mHelperInstance == null) {
            mHelperInstance = HelperClass.getInstance(getApplicationContext());
        }

//        Intent bindIntent = new Intent(this, SensorService.class);
//        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

        mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mLocationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);


        volumeControl  = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(location.hasAltitude() && location.hasSpeed()) {
                    location.getLatitude();

                    /* For low-speed Testing */
                    /*
                    double speed_mps = location.getSpeed();

                    mSpeedView.setText("Speed: " + String.valueOf(speed_mps) + " m/s");

                    if(speed_mps > 0)
                    {
                        //Mute phone
                        volumeControl.setStreamMute(AudioManager.STREAM_RING, true);
                        volumeControl.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                        volumeControl.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    }
                    else
                    {
                        //Unmute phone
                        volumeControl.setStreamMute(AudioManager.STREAM_RING, false);
                        volumeControl.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                        volumeControl.setStreamMute(AudioManager.STREAM_MUSIC, false);
                    }
                    */


                    /* Set mph */
                    //double speed_mph = location.getSpeed() * MPS_TO_MPH;
                    /* Set decimeters */
                    double speed_dec = location.getSpeed() * 10;

                    mSpeedView.setText("Speed: " + String.format("%.2f", speed_dec) +  " d/s");

                    //if(speed_mph > DRIVE_SPEED_MPH)
                    if(speed_dec > DRIVE_SPEED_DEC)
                    {
                        //Mute phone
                        volumeControl.setStreamMute(AudioManager.STREAM_RING, true);
                        volumeControl.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                        volumeControl.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    }
                    else
                    {
                        //Unmute phone
                        volumeControl.setStreamMute(AudioManager.STREAM_RING, false);
                        volumeControl.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                        volumeControl.setStreamMute(AudioManager.STREAM_MUSIC, false);
                    }



                } else {
                    System.out.println("Speed Unavailable");
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        //mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        //myReaderView = new SensorReaderView(this);

        mRequestingLocationUpdates = false;
        mIsServiceBound = false;
        mLastUpdateTime = "";

        // Setup Google Play services client for location updates.
        buildGoogleApiClient();

        setContentView(R.layout.activity_driving_map);
        setUpMapIfNeeded();
        mSpeedView = (TextView) findViewById(R.id.speed);

        updateValuesFromBundle(savedInstanceState);
    }

    private void setUpMenu(Menu menu) {
        MenuItem locOnOff = menu.findItem(R.id.menu_location_track);
        locOnOff.setIcon(R.drawable.ic_location_off_grey600_48dp);
        MenuItem showSensors = menu.findItem(R.id.menu_show_dialog);
        showSensors.setIcon(R.drawable.ic_gauge_gray);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "+ onResume() +");

        super.onResume();

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        setUpEula();
        setUpMapIfNeeded();
        setUpGooglePlayText();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "- onPause() -");
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    private synchronized void cleanup() {
        myReaderView = null;
        if (mConnection != null) {
            if (mBindIntent != null) {
                if (mIsServiceBound) { unbindService(mConnection); }
                stopService(new Intent(this, SensorService.class));
            }
        }
        System.gc();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "-+ onRestart() +-");
        super.onRestart();
    }

    private synchronized void setUpView() {
        cleanup();
        myReaderView = new SensorReaderView(getApplicationContext());
        synchronized (myReaderView) {
            myReaderView.bigACCDetected = false;
            myReaderView.blockedstate = false;
        }
        setThreshold();
        mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName classname, IBinder serviceBinder) {
                //Called when the connection is made.
                mService = ((SensorService.MyBinder) serviceBinder).getService();
                mService.copyObject(myReaderView, mDrivingMapActivity);
            }

            public boolean isConnected() {
                return mService == null;
            }

            public void onServiceConnected(ComponentName className) {
                mIsServiceBound = true;
            }

            public void onServiceDisconnected(ComponentName className) {
                //Received when the service unexpectedly disconnects.
                mService = null;
            }
        };

        mBindIntent = new Intent(this, SensorService.class);
        bindService(mBindIntent, mConnection, Context.BIND_AUTO_CREATE);
        startService(mBindIntent);
    }

    private void cleanUpDialogFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(fm.findFragmentByTag(getString(R.string.sensor_reader_fragment)));
        ft.commit();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "++ onStart() ++");
        if (mHelperInstance == null) {
            mHelperInstance = HelperClass.getInstance();
        }

        synchronized (this) {
            if (sensorReaderDialogFragment != null) {
                cleanUpDialogFragment();
                sensorReaderDialogFragment.dismiss();
            }

            if (myReaderView != null) {
                myReaderView = null;
            }

            setUpView();
            getPrefs();
        }
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "-- onStop() --");
        super.onStop();

        mGoogleApiClient.disconnect();
        cleanup();

        if (mNotifyMgr != null) {
            mNotifyMgr.cancel(R.string.notifier);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "--- onDestroy() ---");
        if (mNotifyMgr != null) {
            mNotifyMgr.cancel(R.string.notifier);
        }

        cleanup();

        if (mConnection != null)
        {
            mConnection = null;
            System.gc();
        }

        if (sensorReaderDialogFragment != null) {
            cleanUpDialogFragment();
        }

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mHelperInstance == null) {
            mHelperInstance = HelperClass.getInstance();
        }
        savedInstanceState.putBoolean(mHelperInstance.getFromPreferences(R.string.requesting_location_updates_key, ""),
                mRequestingLocationUpdates);
        savedInstanceState.putParcelable(mHelperInstance.getFromPreferences(R.string.location_key, ""), mLastLocation);
        savedInstanceState.putString(mHelperInstance.getFromPreferences(R.string.last_updated_time_string_key, ""), mLastUpdateTime);

        FragmentManager fm = getSupportFragmentManager();

        if (aboutDialogFragment != null) {
            savedInstanceState.putString(mHelperInstance.getFromPreferences(R.string.about_fragment, ""),
                    getString(R.string.about_fragment));
            //aboutDialogFragment.dismiss();
        }

        if (sensorReaderDialogFragment != null) {
            savedInstanceState.putString(mHelperInstance.getFromPreferences(R.string.sensor_reader_fragment, ""),
                    getString(R.string.sensor_reader_fragment));
            //sensorReaderDialogFragment.dismiss();
        }

        // Stop the service
        if (mBindIntent != null) {
            stopService(mBindIntent);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (mHelperInstance == null) {
            mHelperInstance = HelperClass.getInstance(getApplicationContext());
        }

        if (savedInstanceState != null && mHelperInstance != null) {
            String prefStr = mHelperInstance.getFromPreferences(R.string.about_fragment, "");
            if (prefStr != null && prefStr.length() > 0) {
                aboutDialogFragment = new AboutDialogFragment();
                aboutDialogFragment.show(getSupportFragmentManager(), getString(R.string.about_fragment));
            }

            String prefStr2 = mHelperInstance.getFromPreferences(R.string.sensor_reader_fragment, "");
            if (prefStr2 != null && prefStr2.length() > 0) {
                sensorReaderDialogFragment = new SensorReaderDialogFragment();
                sensorReaderDialogFragment.show(getSupportFragmentManager(), getString(R.string.sensor_reader_fragment));
            }
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(DEFAULT_LAT, DEFAULT_LNG), DEFAULT_ZOOM));
        //mMap.setTrafficEnabled(true);
    }

    private void setUpGooglePlayText() {
        if (!gotGooglePlayText) {
            GetGooglePlayTextTask getGooglePlayTextTask = new GetGooglePlayTextTask();
            getGooglePlayTextTask.execute(getApplicationContext());
            gotGooglePlayText = true;
        }
    }

    private void setUpEula()
    {
        String isEulaAccepted = mHelperInstance.getFromPreferences(R.string.eula_accepted_key, "false");
        if (isEulaAccepted.contains("false"))
        {
            DialogFragment eulaDialogFragment = new EulaDialogFragment();
            eulaDialogFragment.show(getSupportFragmentManager(), "eula");
        }
    }

    /**
     * Code from https://github.com/googlesamples/android-play-location/blob/master/LocationUpdates/
     * app/src/main/java/com/google/android/gms/location/sample/locationupdates/MainActivity.java
     *
     * Requests start of location updates. Does nothing if updates have already been requested.
     */
    public void doStartLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        }
    }

    /**
     * Code from https://github.com/googlesamples/android-play-location/blob/master/LocationUpdates/
     * app/src/main/java/com/google/android/gms/location/sample/locationupdates/MainActivity.java
     *
     * Requests removal of location updates. Does nothing if updates were not previously requested.
     */
    public void doStopLocationUpdates() {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            stopLocationUpdates();
        }
    }

//    public void showProgressBar() {
//        if (mProgressBar == null) {
//            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
//        }
//        mProgressBar.setVisibility(View.VISIBLE);
//        mProgressBar.bringToFront();
//    }
//
//    public void hideProgressBar() {
//        if (mProgressBar == null) {
//            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
//        }
//        mProgressBar.setVisibility(View.INVISIBLE);
//    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is
        // present.
        getMenuInflater().inflate(R.menu.activity_driving_map, menu);
        setUpMenu(menu);
        return true;
    }

    /**
     * Handle user menu selection.
     * @param item Selected MenuItem
     * as@return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();

        switch (item.getItemId()) {
            case R.id.menu_settings:
                Log.d(TAG, "'Settings' selected");
                Intent mIntent = new Intent(this, Settings.class);
                startActivityForResult(mIntent, RG_REQUEST);
                break;
            case R.id.menu_show_dialog:
                //showProgressBar();
                if (myReaderView != null) {
                    numTimesShowSensors += 1;

                    if (numTimesShowSensors > 1) {
                        setUpView();
                    }

                    mHelperInstance.setSensorView(myReaderView);
                    myReaderView.blockedstate = false;
                    mHelperInstance.setIsAlerted(false);

                    if (sensorReaderDialogFragment == null) {
                        sensorReaderDialogFragment = new SensorReaderDialogFragment();
                    }
                    sensorReaderDialogFragment.show(fm, getString(R.string.sensor_reader_fragment));
                }

                break;
            case R.id.menu_about:
                Log.d(TAG, "'About' selected");

                if (aboutDialogFragment == null) {
                    aboutDialogFragment = new AboutDialogFragment();
                }
                aboutDialogFragment.show(fm, getString(R.string.about_fragment));
                break;
            case R.id.menu_exit:
                Log.d(TAG, "'Quit' selected");
                new AlertDialog.Builder(this)
                    .setTitle("Quit")
                    .setMessage("Are you sure?")// index is "+index)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    finish();
                                }
                            })
                    .setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //Do Nothing.
                                }
                            })
                    .show();
                break;
            case R.id.menu_location_track:
                if (!mRequestingLocationUpdates) {
                    doStartLocationUpdates();
                    item.setIcon(R.drawable.ic_location_on_grey600_48dp);
                    Toast.makeText(mHelperInstance.getContext(), getString(R.string.toast_location_on), Toast.LENGTH_SHORT).show();
                }
                else {
                    doStopLocationUpdates();
                    item.setIcon(R.drawable.ic_location_off_grey600_48dp);
                    Toast.makeText(mHelperInstance.getContext(), getString(R.string.toast_location_off), Toast.LENGTH_SHORT).show();
                }

        }
        return true;
    }

    private void getPrefs() {
        String soundType = mHelperInstance.getFromPreferences(R.string.sound_key, getString(R.string.sound_default));

        if (soundType.equals("one")) {
            soundUri = Uri.parse("file:///system/media/audio/alarms/Alarm_Beep_01.ogg");
            //soundUri = Uri.parse("android.resource" + File.pathSeparator + getApplicationContext().getPackageName() +
            //  File.separator + "R.raw.alarm_beep_01");
        } else if (soundType.equals("two")) {
            soundUri = Uri.parse("file:///system/media/audio/alarms/Alarm_Buzzer.ogg");
            //soundUri = Uri.parse("android.resource" + File.pathSeparator + getApplicationContext().getPackageName() +
            //        File.separator + "R.raw.alarm_buzzer");
        } else if (soundType.equals("silent")) {
            soundUri = null;
        }
        Log.d(TAG, "soundUri: " + soundUri);

        String vibrationOrNot = mHelperInstance.getFromPreferences(R.string.vibrate_key, "true");

        if (!vibrationOrNot.contains("false")) {
            vibration = vibrating_time;
        } else {
            vibration = null;
        }

        alarmInfo = mHelperInstance.getFromPreferences(R.string.alarm_key, getString(R.string.alarm_default));

        setThreshold();
    }

    private synchronized void setThreshold() {
        Log.d(TAG, "setThreshold()");
        String sensitivity = mHelperInstance.getFromPreferences(R.string.sensitivity_key, getString(R.string.sensitivity_medium));
        android.util.Log.i(TAG, "Sensitivity value: " + sensitivity);
        if (sensitivity.equals("High")) {
            myReaderView.threshold = 0.95 * G;
            myReaderView.setSimpleDetect(false);
        } else if (sensitivity.equals("Medium")) {
            myReaderView.threshold = 0.7 * G;
            myReaderView.setSimpleDetect(false);
        } else {                                //if (sensitivity == "Low"){
            myReaderView.threshold = 0.5 * G;
            myReaderView.setSimpleDetect(true);
        }

    }

    @Override
    //will be called when the *Detector* activity receives *setting* activity result.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RG_REQUEST) {
            if (resultCode == RESULT_OK) {
                getPrefs();
            }
        }
    }

    /**
     * Display a Alarm
     */
    public void showAlarms(Long Time) {
        android.util.Log.i(TAG, "Received call, start showAlarms");
        showNotification(Time);
        android.util.Log.i(TAG, "showNotification() finished");
        showAlert();
        android.util.Log.i(TAG, "showAlert() finished");
        myReaderView.blockedstate = true;
    }

    private void showAlert() {
        /**  generate the dialog! */
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alarm_text))
                .setMessage(alarmInfo)
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //Do Nothing.
                            }
                        }).show();
        //pop-up dialog
//	    Toast.makeText(this, "for test",Toast.LENGTH_LONG).show();
    }

    private void showNotification(Long Time) {
        Log.d(TAG, "showNotification() called");
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotificationActivity.class), 0);
        //test.class is the class will be invoked in next intent(just the "contentIntent")
        //It could be make phone call
        // construct the Notification object.
        Notification notif = new Notification(R.drawable.ic_warning_amber_48dp,
                "Alarm\n" + alarmInfo, Time);
        // Set the info for the views that show in the notification panel.
        // must set this for content view, or will throw a exception
        notif.setLatestEventInfo(this, getString(R.string.alarm_text),
                alarmInfo, contentIntent);
        //an array of longs of times to turn the vibrator off and on.
        // after a 100ms delay, vibrate for 250ms, pause for 100 ms and
        // then vibrate for 500ms.
        // for being able to vibrate, the permission should be add in manifest.xml
        notif.vibrate = vibration;
        // play alarm sound
        notif.sound = soundUri;
        if (mNotifyMgr != null) {
            mNotifyMgr.notify(R.string.notifier, notif);
        }
    }

    // Location handling code is from Google Inc.:
    // https://developer.android.com/training/location/receive-location-updates.html; the code is at
    // https://github.com/googlesamples/android-play-location/blob/master/LocationUpdates/app/
    // src/main/java/com/google/android/gms/location/sample/locationupdates/MainActivity.java

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google Play services connection established");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google Play services connection suspended; error code: " + i);
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Google Play services connection failed; connectionResult.getErrorCode() = " +
                connectionResult.getErrorCode());
    }

    protected void createLocationRequest() {
        Log.d(TAG, "createLocationRequest()");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.TEN_SEC_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.FIVE_SEC_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        Log.d(TAG, "stopLocationUpdates()");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mSpeedView.setText(getResources().getString(R.string.speed_label));
        mLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        System.out.println(location.getSpeed());
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Log.d(TAG, "onLocationChanged(): location: (" + location.getLatitude() + ", " +
                location.getLongitude() + "); time: " + mLastUpdateTime);
    }

    protected void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates()");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                0, mLocationListener);

    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Init helper instance if it's null.
            if (mHelperInstance == null) {
                mHelperInstance = HelperClass.getInstance();
            }

            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(mHelperInstance.getFromPreferences(R.string.requesting_location_updates_key, ""))) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        getString(R.string.requesting_location_updates_key));
            }

            // Update the value of mLastLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(mHelperInstance.getFromPreferences(R.string.location_key, ""))) {
                // Since location key was found in the Bundle, we can be sure that mLastLocation
                // is not null.
                mLastLocation = savedInstanceState.getParcelable(getString(R.string.location_key));
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(mHelperInstance.getFromPreferences(R.string.last_updated_time_string_key, ""))) {
                mLastUpdateTime = savedInstanceState.getString(getString(R.string.last_updated_time_string_key));
            }
        }
    }
}
