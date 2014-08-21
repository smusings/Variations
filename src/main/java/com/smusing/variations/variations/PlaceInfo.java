package com.smusing.variations.variations;

import android.content.Context;
import android.content.Intent;
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


        //ask for all places within the nearest x meters.
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        //query, simple because MainActivity already checks for location as it is
        Query q = new Query()
                .field("name").isEqual(message)
                .within(new Circle(getLocation().getLatitude(), getLocation().getLongitude(), 5000))
                .sortAsc("$distance")
                .only("name", "address","address_extended", "locality", "region", "postcode", "tel", "website")
                .limit(25);
        task.execute(q);
    }

    //view map intent to send the intent from MainActivity to PlaceMaps (Look for a better way!)
    public void viewMap(View view){
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        Intent maptent = new Intent(this, PlaceMaps.class);
        maptent.putExtra(EXTRA_MESSAGE, message);
        startActivity(maptent);
    }

    protected class FactualRetrievalTask extends AsyncTask<Query, Integer, List<ReadResponse>> {
        //returns results of query
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

        //binds results to an array to a list view
        @Override
        protected void onPostExecute(List<ReadResponse> responses) {
            final ArrayList<String> list=new ArrayList<String>();
            final ArrayList<String> lname=new ArrayList<String>();
            for (ReadResponse response : responses) {
                for (Map<String, Object> restaurant : response.getData()) {
                    //String set up
                    final String name = (String) restaurant.get("name");
                    final String address=(String) restaurant.get("address");
                    final String addresse = (String) restaurant.get("address_extended");
                    final String phone = (String) restaurant.get("tel");
                    final String website=(String) restaurant.get("website");
                    final String locality=(String) restaurant.get("locality");
                    final String zip=(String) restaurant.get("postcode");
                    final String state=(String) restaurant.get("region");
                    lname.add(name);
                    //logic to display results
                    if (phone != null && !phone.isEmpty()) {
                        if (address != null && !address.isEmpty()) {
                            if (addresse!=null && !address.isEmpty()) {
                                if (website != null && !website.isEmpty()) {
                                    list.add("Phone Number: " + phone + "\nAddress: " + address +", "+ addresse
                                            + ", "+locality+ ", "+ state+ " " + zip+ "\nWebsite: " + website);
                                } else {
                                    list.add("Phone Number: " + phone + "\nAddress: " + address +" "+ addresse
                                            + ", "+locality+ ", "+ state+ " " + zip+"\nWebsite: None Listed");
                                }
                            } else {
                                list.add("Phone Number: " + phone + "\nAddress: " + address
                                        + ", "+locality+ ", "+ state+ " " + zip+ "\nWebsite: None Listed");
                            }
                        } else {
                            list.add("Phone Number: " + phone + "\nAddress: None listed"+ "\nWebsite: None Listed");
                        }
                    } else {
                        list.add("Phone Number: None listed." + "\nAddress: None listed"+ "\nWebsite: None Listed");
                    }
                    //set up to bind results to array to listview
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


