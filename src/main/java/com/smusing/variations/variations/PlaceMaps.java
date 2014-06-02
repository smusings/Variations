package com.smusing.variations.variations;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;

import com.factual.driver.Circle;
import com.factual.driver.Query;
import com.factual.driver.ReadResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.util.Lists;

import java.util.List;
import java.util.Map;

public class PlaceMaps extends LocationSetUp {

    //needed things
    private GoogleMap gmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //turn off the Variations actionbar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_place_info);

        //the setupfor map
        gmap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        //set up Factual
        FactualRetrievalTask task=new FactualRetrievalTask();

        //set up location
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String provider=locationManager.getBestProvider(newCriteria(), true);
        Location l = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider, 2000, 10,
                locationListener);

        //ask for all places within the nearest 5000 meters.
        Intent intent = getIntent();
        String message = intent.getStringExtra(PlaceInfo.EXTRA_MESSAGE);
        Query q=new Query()
                .field("name").isEqual(message)
                .within(new Circle(l.getLatitude(), l.getLongitude(), 5000))
                .sortAsc("$distance")
                .only("name", "address", "tel","address_extended", "latitude", "longitude")
                .limit(15);
        task.execute(q);

        //sets up your current location
        final LatLng CURRENT=new LatLng(l.getLatitude(), l.getLongitude());
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(CURRENT, (float) 14));

        //enable location button
        gmap.setMyLocationEnabled(true);

        //disables zoom controls
        gmap.getUiSettings().setZoomControlsEnabled(false);

        //set what the button does
        gmap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            public boolean onMyLocationButtonClick() {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location l = locationManager.getLastKnownLocation(locationManager.PASSIVE_PROVIDER);
                gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(CURRENT, (float) 14));
                return true;
            }
        });

        //limits zoom out amount
        gmap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            public void onCameraChange(CameraPosition position) {
                float maxZoom = 10;
                if (position.zoom < maxZoom)
                    gmap.animateCamera(CameraUpdateFactory.zoomTo(maxZoom));
            }
        });
    }

    protected class FactualRetrievalTask extends AsyncTask<Query, Integer, List<ReadResponse>> {
        //performs query and returns results
        @Override
        protected List<ReadResponse> doInBackground(Query... params) {
            List<ReadResponse> results = Lists.newArrayList();
            for (Query q : params) {
                results.add(factual.fetch("restaurants", q));
            }
            return results;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        //binds the results into an array adapter to be displayed in a list view
        @Override
        protected void onPostExecute(List<ReadResponse> responses) {
            for (ReadResponse response : responses) {
                for (Map<String, Object> restaurant : response.getData()) {
                    //string setup
                    final String name = (String) restaurant.get("name");
                    final String address = (String) restaurant.get("address");
                    final String addresse = (String) restaurant.get("address_extended");
                    Number distance = (Number) restaurant.get("$distance");
                    Number lat=(Number) restaurant.get("latitude");
                    Number lng=(Number) restaurant.get("longitude");

                    //location setup
                    LatLng latlngplace=new LatLng(lat.floatValue(), lng.floatValue());

                    //marker
                    gmap.addMarker(new MarkerOptions()
                            .position(latlngplace)
                            .title(name+ " , \n"+ address+", "+addresse)
                            .snippet(distance.toString() + " meters away"));
                }
            }
        }
    }
}

