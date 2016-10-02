package es.age.apps.mapwrapperexample.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import es.age.apps.mapwrapperexample.R;

/**
 * Created by adricacho on 2/10/16.
 */

public class LocationUtils {

    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    public static final int REQUEST_CHECK_SETTINGS = 0x1;

    public static final int REQUEST_GPS_SETTINGS = 2;


    public static String TAG = "LocationUtils";

    /**
     * Method to verify google play services on the device
     */
    public static boolean checkPlayServices(final Activity ctx) {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(ctx);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, ctx,
                        PLAY_SERVICES_RESOLUTION_REQUEST,
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                ctx.finish();
                            }
                        }).show();
            } else {
                Toast.makeText(ctx,
                        R.string.error_device_not_supported, Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }


        return true;
    }



    public static void checkNetworkConnection(Activity ctx) {
        if (!isConnected(ctx)) {
            showErrorConectionDialog(ctx);
        }
    }

    /**
     * Show dialog with no network connection message
     */
    public static void showErrorConectionDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.dialog_network_error_title);  // Internet not found
        Resources res = activity.getResources();
        builder.setMessage(String.format(res.getString(R.string.dialog_network_error_body), res.getString(R.string.app_name)));
        builder.setPositiveButton(activity.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
        builder.create().show();
    }


    /**
     * Check internet connection
     */
    public static boolean isConnected(Context ctx) {
        ConnectivityManager conMgr = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = conMgr.getActiveNetworkInfo();
        if (i == null)
            return false;
        if (!i.isConnected())
            return false;
        if (!i.isAvailable())
            return false;
        return true;
    }


    /**
     * Check gps connection
     */
    public static boolean isGPSConnected(Context ctx) {
        LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        boolean i = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return i;
    }


    /**
     * Reques user location using FusedLocationAPI, so the user doesn´t have to go to settings
     */
    public static boolean requestLocation(GoogleApiClient client, final AppCompatActivity activity, final boolean cancelable) {
        if (activity != null) {
            Log.d(TAG, "requestLocation: ");
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);

            if (client != null) {

                final PendingResult<LocationSettingsResult> result =
                        LocationServices.SettingsApi.checkLocationSettings(client,
                                builder.build());

                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                        final Status status = locationSettingsResult.getStatus();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                Log.i(TAG, "All location settings are satisfied.");
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                                        "upgrade location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the result
                                    // in onActivityResult().
                                    status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);

                                } catch (IntentSender.SendIntentException e) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                    if (!cancelable) {
                                        activity.finish();
                                    }
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                                        "not created.");
                                checkLocationEnabledClassic(activity, cancelable);
                                break;
                        }
                    }
                });
            } else {
                Toast.makeText(activity, R.string.error_location_check, Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        }
        return false;
    }


    /**
     * Request location using the "classic" way, it means, setting an intent to Location settings and letting the user activate them.
     * This is used when the user disabled location with FusedLocationAPI
     */
    public static void checkLocationEnabledClassic(final Activity activity, final boolean cancelable) {
        if (activity != null) {
            Log.d(TAG, "checkLocationEnabledClassic: ");

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.dialog_location_access_title);  // GPS not found
            builder.setMessage(R.string.dialog_location_access_body); // Want to enable?
            builder.setPositiveButton(activity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    activity.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_GPS_SETTINGS);
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!cancelable) {
                        activity.finish();
                    }
                    dialog.dismiss();
                }
            });

            builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (!cancelable) {
                            activity.finish();
                        }
                        return true;
                    }
                    return false;
                }
            });

            AlertDialog dialog = builder.create();
            if (!cancelable) {
                dialog.setCanceledOnTouchOutside(false);
            }
            dialog.show();

        }
    }

}
