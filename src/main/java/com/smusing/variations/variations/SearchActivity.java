package com.smusing.variations.variations;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchActivity extends LocationSetUp {
    public final static String EXTRA_MESSAGE = "com.smusing.variations.variations.OBJ";

    ListView lView;

    //Handle the intent received from the search
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        handleIntent(getIntent());
    }

    //on new intent handle the intent
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    //the handleIntent behaves the same as OnCreate in MainActivity, but instead of a search it uses a query to sort
    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            FactualRetrievalTask task = new FactualRetrievalTask();

            //set up location
            locationManager.requestLocationUpdates(provider, 2000, 10,
                    locationListener);

            //ask for all places within the nearest x meters.
            if (l != null) {
                Query q = new Query()
                        .within(new Circle(l.getLatitude(), l.getLongitude(), 5000))
                        .sortAsc("$distance")
                        .search(query)
                        .only("name", "address", "cuisine")
                        .limit(25);
                task.execute(q);
            } else {
                failsafe.add(s);
                lView = (ListView) findViewById(R.id.search_results);
                lView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, failsafe));
            }
        }
    }

    //the execute of the query and putting it into listview
    private class FactualRetrievalTask extends AsyncTask<Query, Integer, List<ReadResponse>> {
        //performs the query and returns results
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
        //binds the results to a viewable format
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

                    //displaying and formatting the cuisine
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
                    String skr=cuisine.toString().replace("[","").replace("]","");

                    //logic and display
                    lname.add(name);
                    if (address != null) {
                        if (cusine != null) {
                            list.add("Closest Address: " + address + "\nCuisine: " + skr);
                        } else {
                            list.add("Closest Address: " + address + "\nCuisine: Not Listed");
                        }
                    } else {
                        list.add("Closest Address: Not Listed" + "\nCuisine: Not Listed");
                    }
                }
            }
            //set up to bind the results to listview
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
            lView = (ListView) findViewById(R.id.search_results);
            lView.setAdapter(new MySimpleArrayAdapter(SearchActivity.this, array));

            //setting it up for more info
            lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    locationManager.requestLocationUpdates(provider, 2000, 10,
                            locationListener);

                    if (l !=null){
                        Object obj = nArray[position];

                        Intent intent = new Intent(SearchActivity.this, PlaceInfo.class);
                        intent.putExtra(EXTRA_MESSAGE, obj.toString());
                        startActivity(intent);
                    }else{
                        failsafe.add(s);
                        lView = (ListView) findViewById(R.id.search_results);
                        lView.setAdapter(new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, failsafe));
                    }
                }
            });
        }
    }
}
