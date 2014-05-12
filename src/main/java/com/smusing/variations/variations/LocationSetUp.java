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
import android.view.MenuItem;

import com.factual.driver.Factual;

public class LocationSetUp extends Activity {

    Factual factual = new Factual("Your Key Here",
            "Your Secret Here");

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
    public final LocationListener locationListener = new LocationListener() {
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




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
