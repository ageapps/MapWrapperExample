package es.age.apps.mapwrapperexample;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by adricacho on 3/10/16.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback {


    private View view;
    private SupportMapFragment mapFragment;
    private OnMapLoadedListener onMapLoadedListener;
    private GoogleMap mMap;
    private String TAG = "MapFragment";

    // Container Activity must implement this interface
    public interface OnMapLoadedListener {
        public void onMapLoaded(GoogleMap map);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);
        FragmentManager fm = getChildFragmentManager();
        mapFragment = ((SupportMapFragment) fm.findFragmentById(R.id.map));
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (onMapLoadedListener != null) {
            onMapLoadedListener.onMapLoaded(googleMap);

        }
        Log.d(TAG, "onMapReady: ");
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onMapLoadedListener = (OnMapLoadedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
}
