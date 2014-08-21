package com.smusing.variations.variations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.factual.driver.Factual;

import java.util.ArrayList;

public class LocationSetUp extends Activity {

    /*
        enables location for activities
        sets up the LocationManager and the services
        then we get the best provider (GPS, provider, etc)
        then we define location so we can use it in methods that dont call for it specifically


    */

    public Location getLocation() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(newCriteria(), true);
        Location l = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider, 2000, 10,
                locationListener);


        return l;
    }
    //failsafe if location services are not on
    String s="Please Turn on your Location Services, and hit refresh after a few seconds";
    ArrayList<String> failsafe = new ArrayList<String>();


    //Enables Factual
    Factual factual = new Factual("","");

    /*
        Uses the following criteria to decide what the best Location Provider is
    */
    public Criteria newCriteria(){
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);
        c.setPowerRequirement(Criteria.POWER_LOW);
        c.setAltitudeRequired(false);
        c.setBearingRequired(false);
        c.setSpeedRequired(false);
        c.setCostAllowed(true);
        return c;
    }

    public void updatedWithNewLocation(Location location) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location l = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
    }

    //Location Listener
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updatedWithNewLocation(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };



    //Set up for Menu everywhere but Main Activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.about:
                Intent aboutent=new Intent(this, MenuAbout.class);
                startActivity(aboutent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
