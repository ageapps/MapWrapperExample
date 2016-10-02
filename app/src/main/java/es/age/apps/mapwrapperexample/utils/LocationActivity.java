package es.age.apps.mapwrapperexample.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;


/**
 * Created by adricacho on 2/10/16.
 */

public abstract class LocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private String TAG = "LocationActivity";

    private GoogleApiClient mGoogleApiClient;
    private boolean mPermissionDenied = false;

    private OnGoogleAPIConnectedListener onGoogleAPIConnectedListener;

    private GoogleMap googleMap;

    private Location currentLocation;

    private double defaultLatitude = 0;
    private double defaultLongitude = 0;

    private boolean gpsNeeded = false;

    // OnConnected Interface
    public interface OnGoogleAPIConnectedListener {
        public void onConnected();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (LocationUtils.checkPlayServices(this)) {
            LocationUtils.checkNetworkConnection(this);
            buildGoogleApiClient();
        }

    }


    public void setOnGoogleAPIConnectedListener(OnGoogleAPIConnectedListener listener) {
        onGoogleAPIConnectedListener = listener;
    }

    public void onLocationMapReady(GoogleMap mapInActivity) {
        googleMap = mapInActivity;
        enableMyLocation();
    }


    public void setDefaultLocation(double latitude, double longitude) {
        defaultLatitude = latitude;
        defaultLongitude = longitude;
    }

    public void setGpsNeeded(boolean gpsNeeded) {
        this.gpsNeeded = gpsNeeded;
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    public void enableMyLocation() {
        Log.d(TAG, "enableMyLocation: googleMap");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (googleMap != null) {
            // Access to the location has been granted to the app.

            // Check if location is enabled
            if (LocationUtils.isGPSConnected(LocationActivity.this)) {
                googleMap.setMyLocationEnabled(true);
                displayLocation();
            } else {
                // Show request GPS enabled
                LocationUtils.requestLocation(mGoogleApiClient, this, !gpsNeeded);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG, "buildGoogleApiClient: ");
        // Create an instance of GoogleAPIClient.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        if (onGoogleAPIConnectedListener != null) {
            onGoogleAPIConnectedListener.onConnected();
        }
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: ");
    }


    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    /**
     * Method to display the location on UI
     */
    private void displayLocation() {

        if (googleMap != null) {
            Location lastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);


            if (lastLocation != null) {
                currentLocation = lastLocation;
            } else {
                currentLocation = new Location("DefaultLocation");
                currentLocation.setLatitude(defaultLatitude);
                currentLocation.setLongitude(defaultLongitude);
            }

            CameraPosition position = CameraPosition.builder()
                    .target(new LatLng(currentLocation.getLatitude(),
                            currentLocation.getLongitude()))
                    .zoom(13f)
                    .bearing(0.0f)
                    .tilt(0.0f)
                    .build();

            googleMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case LocationUtils.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        enableMyLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
            case LocationUtils.REQUEST_GPS_SETTINGS:
                if (LocationUtils.isGPSConnected(this)) {
                    enableMyLocation();
                }
        }
    }


}
