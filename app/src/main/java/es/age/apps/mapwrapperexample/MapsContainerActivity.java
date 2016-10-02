package es.age.apps.mapwrapperexample;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.google.android.gms.maps.GoogleMap;

import es.age.apps.mapwrapperexample.utils.LocationActivity;

/**
 * Created by adricacho on 3/10/16.
 */

public class MapsContainerActivity extends LocationActivity implements MapFragment.OnMapLoadedListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container_maps);

        MapFragment mapFragment = new MapFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.add(R.id.fragment_container, mapFragment, "mapFragment");
        transaction.commit();
    }

    @Override
    public void onMapLoaded(GoogleMap map) {
        super.onLocationMapReady(map);
    }
}
