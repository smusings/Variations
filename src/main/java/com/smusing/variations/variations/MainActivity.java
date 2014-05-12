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
import android.widget.AdapterView;
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


public class MainActivity extends LocationSetUp{
    public final static String EXTRA_MESSAGE = "com.smusing.variations.variations.OBJ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set up Factual
        FactualRetrievalTask task=new FactualRetrievalTask();

        //set up location
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String provider=locationManager.getBestProvider(newCriteria(), true);
        Location l = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider, 2000, 10,
                locationListener);

        //ask for all places within the nearest x meters.
        Query q=new Query()
                .within(new Circle(l.getLatitude(), l.getLongitude(), 5000))
                .sortAsc("$distance")
                .only("name","price")
                .limit(25);
        task.execute(q);
    }

    private class FactualRetrievalTask extends AsyncTask<Query, Integer, List<ReadResponse>> {
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
            final ArrayList<String> list = new ArrayList<String>();
            final ArrayList<String> lname=new ArrayList<String>();
            for (ReadResponse response : responses) {
                for (Map<String, Object> restaurant : response.getData()) {
                    String name = (String) restaurant.get("name");
                    Number price = (Number) restaurant.get("price");
                    lname.add(name);
                    if (price != null) {
                        list.add("Price: " + price);
                    } else {
                        list.add("Price: Not Listed");
                    }
                }
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
            final ListView lView = (ListView) findViewById(R.id.display_messages);
            lView.setAdapter(new MySimpleArrayAdapter(MainActivity.this, array));

            //setting it up for more info
            lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Object obj=nArray[position];

                    Intent intent= new Intent(MainActivity.this, PlaceInfo.class);
                    intent.putExtra(EXTRA_MESSAGE, obj.toString());
                    startActivity(intent);
                }
            });
        }
    }
}
