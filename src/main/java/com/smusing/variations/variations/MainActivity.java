package com.smusing.variations.variations;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.factual.driver.Circle;
import com.factual.driver.Query;
import com.factual.driver.ReadResponse;
import com.google.api.client.util.Lists;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends LocationSetUp {
    public final static String EXTRA_MESSAGE = "com.smusing.variations.variations.OBJ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set up Factual Query
        FactualRetrievalTask task = new FactualRetrievalTask();

        //set up location
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(newCriteria(), true);
        Location l = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider, 2000, 10,
                locationListener);

        //ask for all places within the nearest 5000 meters, with a built in Check for GPS turned on.
        if (l != null) {
            Query q = new Query()
                    .within(new Circle(l.getLatitude(), l.getLongitude(), 5000))
                    .sortAsc("$distance")
                    .only("name", "address", "cuisine")
                    .limit(25);
            task.execute(q);
        } else {
            String s = "Please Turn on your Location Services" +
                    "\n and hit refresh after a few seconds";

            ArrayList<String> array = new ArrayList<String>();
            array.add(s);
            final ListView lView = (ListView) findViewById(R.id.display_messages);
            lView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array));

        }
    }
    //Location changed checker
    public void onLocationChanged(Location l) {
        FactualRetrievalTask task = new FactualRetrievalTask();
        if (l != null) {
            Query q = new Query()
                    .within(new Circle(l.getLatitude(), l.getLongitude(), 5000))
                    .sortAsc("$distance")
                    .only("name", "address", "cuisine")
                    .limit(25);
            task.execute(q);
        }
    }

    //Performs the Query
    private class FactualRetrievalTask extends AsyncTask<Query, Integer, List<ReadResponse>> {
        //performs the query itself
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

        //tells the app what to show
        @Override
        protected void onPostExecute(List<ReadResponse> responses) {
            final ArrayList<String> list = new ArrayList<String>();
            final ArrayList<String> lname = new ArrayList<String>();

            for (ReadResponse response : responses) {
                for (Map<String, Object> restaurant : response.getData()) {
                    //the setup
                    String name = (String) restaurant.get("name");
                    String address = (String) restaurant.get("address");
                    JSONArray cusine = (JSONArray) restaurant.get("cuisine");
                    ArrayList<String> cuisine = new ArrayList<String>();
                    if (cusine != null) {
                        int len = cusine.length();
                        for (int i = 0; i < len; i++) {
                            try {
                                cuisine.add(cusine.get(i).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    String cuisine_string=cuisine.toString().replace("[","").replace("]","");

                    //logic and display
                    lname.add(name);
                    if (address != null) {
                        if (cusine != null) {
                            list.add("Current Address: " + address + "\n"+"Cusine: " + cuisine_string);
                        } else {
                            list.add("Closest Address: " + address + "\nCuisine: Not Listed");
                        }
                    } else {
                        list.add("Closest Address: Not Listed" + "\nCuisine: Not Listed");
                    }
                }
            }
            //setting it up to display in a list view
            final String[] array = new String[list.size()];
            final String[] nArray = new String[lname.size()];

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
                    LayoutInflater inflater = (LayoutInflater) context.
                            getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View rView = inflater.inflate(R.layout.row, parent, false);
                    TextView textView = (TextView) rView.findViewById(R.id.secondLine);
                    TextView tView = (TextView) rView.findViewById(R.id.thirdLine);
                    textView.setText(nArray[position]);
                    tView.setText(array[position]);
                    return rView;
                }
            }

            //setting it up to show results
            final ListView lView = (ListView) findViewById(R.id.display_messages);
            lView.setAdapter(new MySimpleArrayAdapter(MainActivity.this, array));

            //setting up the onClick to display more information
            lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Object obj = nArray[position];

                    Intent intent = new Intent(MainActivity.this, PlaceInfo.class);
                    intent.putExtra(EXTRA_MESSAGE, obj.toString());
                    startActivity(intent);
                }
            });
        }
    }

    //custom MainActivity Menu in order to have a search bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Location setup
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(newCriteria(), true);
        Location l = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider, 2000, 10,
                locationListener);
        FactualRetrievalTask task = new FactualRetrievalTask();

        switch (item.getItemId()) {
            case R.id.refresh:
                //clone of OnCreate. Find a new way of doing this!!!
                if (l != null) {
                    Query q = new Query()
                            .within(new Circle(l.getLatitude(), l.getLongitude(), 5000))
                            .sortAsc("$distance")
                            .only("name", "address", "cuisine")
                            .limit(25);
                    task.execute(q);
                } else {
                    String s = "Please Turn on your Location Services" +
                            "\n and hit refresh after a few seconds";

                    ArrayList<String> array = new ArrayList<String>();
                    array.add(s);
                    final ListView lView = (ListView) findViewById(R.id.display_messages);
                    lView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array));

                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
