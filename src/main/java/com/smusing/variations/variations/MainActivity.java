package com.smusing.variations.variations;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

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
    //String to help intents send along info
    public final static String EXTRA_MESSAGE = "com.smusing.variations.variations.OBJ";
    ListView lView;
    FactualRetrievalTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set up Factual Query
        task = new FactualRetrievalTask();

        //ask for all places within the nearest 5000 meters, with a built in Check for Location Servicesturned on.
        if (getLocation() != null) {
            Query q = new Query()
                    .within(new Circle(getLocation().getLatitude(), getLocation().getLongitude(), 5000))
                    .sortAsc("$distance")
                    .only("name", "address", "cuisine")
                    .limit(25);
            task.execute(q);
        } else {
            //triggers the failsafe
            failsafe.add(s);
            lView = (ListView) findViewById(R.id.display_messages);
            lView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, failsafe));
        }
    }

    //Performs the Query in the background
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

        /*
            tells the app what to show
            rather large so i want to take the time to explain what happens
        */
        @Override
        protected void onPostExecute(List<ReadResponse> responses) {

            final ArrayList<String> list = new ArrayList<String>(); //array for all info
            final ArrayList<String> lname = new ArrayList<String>();    //array for name only

            //i do it this way so that the name can be used to trigger an intent
            for (ReadResponse response : responses) {
                for (Map<String, Object> restaurant : response.getData()) {
                    String name = (String) restaurant.get("name");
                    String address = (String) restaurant.get("address");

                    //array for cuisine
                    //have to leave it in here or else the list just adds up
                    //and you have a pizza place that serves tacos,sushi, and cupcakes
                    final ArrayList<String> cuisine = new ArrayList<String>();

                    //the cuisine is in an array
                    JSONArray cusine = (JSONArray) restaurant.get("cuisine");
                    //if cuisine is there get the length, add it to string based off location
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
                    //we break the cuisine format into a more readable format
                    //otherwise you get [pizza, pasta, wings]
                    String cuisine_string=cuisine.toString().replace("[","").replace("]","");

                    //logic and display
                    //we only add name so we can later use this array to search for more info
                    lname.add(name);
                    //set up a series of if/else because not all data will come back
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
            //put our ArrayList results into a StringArray
            list.toArray(array);
            lname.toArray(nArray);

            //setting it up to show results
            final ListView lView = (ListView) findViewById(R.id.display_messages);
            lView.setAdapter(new MySimpleArrayAdapter(MainActivity.this, array, nArray));

            //setting up the onClick to display more information
            //this triggers the next activity to do a search based off name, see PlaceInfo for more
            lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    //if location is not null, get the object from the array based off position
                    //start an activity via intent and sending
                    if (getLocation() !=null) {
                        Object obj = nArray[position];
                        Intent intent = new Intent(MainActivity.this, PlaceInfo.class);
                        intent.putExtra(EXTRA_MESSAGE, obj.toString());
                        startActivity(intent);
                    } else {
                        failsafe.add(s);
                        final ListView lView = (ListView) findViewById(R.id.display_messages);
                        lView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, failsafe));

                    }
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
        switch (item.getItemId()) {
            case R.id.refresh:
                //refreshes the list if location is found
                if (getLocation() != null) {
                    Query q = new Query()
                            .within(new Circle(getLocation().getLatitude(), getLocation().getLongitude(), 5000))
                            .sortAsc("$distance")
                            .only("name", "address", "cuisine")
                            .limit(25);
                    task.execute(q);
                } else {
                    failsafe.add(s);
                    lView = (ListView) findViewById(R.id.display_messages);
                    lView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, failsafe));
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
