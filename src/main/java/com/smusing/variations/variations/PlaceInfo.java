package com.smusing.variations.variations;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.factual.driver.Circle;
import com.factual.driver.Query;
import com.factual.driver.ReadResponse;
import com.google.api.client.util.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlaceInfo extends LocationSetUp {
    public final static String EXTRA_MESSAGE = "com.smusing.variations.variations.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_info);

        //set up Factual
        FactualRetrievalTask task=new FactualRetrievalTask();

        //set up location
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String provider=locationManager.getBestProvider(newCriteria(), true);
        Location l = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider, 2000, 10,
                locationListener);

        //ask for all places within the nearest x meters.
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        Query q = new Query()
                .field("name").isEqual(message)
                .within(new Circle(l.getLatitude(), l.getLongitude(), 5000))
                .sortAsc("$distance")
                .only("name", "address", "tel", "website", "latitude", "longitude")
                .limit(25);
        task.execute(q);
    }

    public void viewMap(View view){
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        Intent maptent = new Intent(this, PlaceMaps.class);
        maptent.putExtra(EXTRA_MESSAGE, message);
        startActivity(maptent);
    }

    protected class FactualRetrievalTask extends AsyncTask<Query, Integer, List<ReadResponse>> {
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

        @Override
        protected void onPostExecute(List<ReadResponse> responses) {
            final ArrayList<String> list=new ArrayList<String>();
            final ArrayList<String> lname=new ArrayList<String>();
            for (ReadResponse response : responses) {
                for (Map<String, Object> restaurant : response.getData()) {
                    final String name = (String) restaurant.get("name");
                    final String address = (String) restaurant.get("address");
                    final String phone = (String) restaurant.get("tel");
                    final String website=(String) restaurant.get("website");
                    lname.add(name);
                    if (phone != null && !phone.isEmpty()) {
                        if (address != null && !address.isEmpty()) {
                            if (website !=null && !website.isEmpty()){
                                list.add("Phone Number: " + phone + "\nAddress: " + address+"\nWebsite: "+website);
                            }
                            else {
                                list.add("Phone Number: " + phone + "\nAddress: " + address + "\nWebsite: None Listed");
                            }
                        }
                        else {
                            list.add("Phone Number: " + phone + "\nAddress: None listed"+ "\nWebsite: None Listed");
                        }
                    }
                    else {
                        list.add("Phone Number: None listed." + "\nAddress: None listed"+ "\nWebsite: None Listed");
                    }

                    final String[] array=new String[list.size()];
                    final String[] nArray=new String[lname.size()];


                    list.toArray(array);
                    lname.toArray(nArray);

                    class MySimpleArrayAdapter extends ArrayAdapter<String> {
                        private final Context context;

                        public MySimpleArrayAdapter(Context context, String[] values) {
                            super(context, R.layout.row, values);
                            this.context = context;
                        }

                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            LayoutInflater inflater=(LayoutInflater)context.
                                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View rView=inflater.inflate(R.layout.row, parent, false);
                            TextView textView=(TextView) rView.findViewById(R.id.secondLine);
                            TextView tView=(TextView) rView.findViewById(R.id.thirdLine);
                            textView.setText(nArray[position]);
                            tView.setText(array[position]);
                            return rView;
                        }
                    }

                    //setting it up to show results
                    final ListView lView = (ListView) findViewById(R.id.display_name);
                    lView.setAdapter(new MySimpleArrayAdapter(PlaceInfo.this, array));
                }
            }
        }
    }
}


