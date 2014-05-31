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
        if (l !=null){
            Query q = new Query()
                    .within(new Circle(l.getLatitude(), l.getLongitude(), 5000))
                    .sortAsc("$distance")
                    .only("name", "address", "cuisine")
                    .limit(25);
            task.execute(q);
        } else {
            String s="Please Turn on your Location Services"+
                    "\n and hit refresh after a few seconds";

            ArrayList<String> array=new ArrayList<String>();
            array.add(s);
            final ListView lView = (ListView) findViewById(R.id.display_messages);
            lView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array));

        }
    }

    public void onLocationChanged(Location l){
        FactualRetrievalTask task=new FactualRetrievalTask();
        if (l !=null){
            Query q = new Query()
                    .within(new Circle(l.getLatitude(), l.getLongitude(), 5000))
                    .sortAsc("$distance")
                    .only("name", "address", "cuisine")
                    .limit(25);
            task.execute(q);
        }
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
                    String address = (String) restaurant.get("address");
                    JSONArray cusine=(JSONArray) restaurant.get("cuisine");
                    lname.add(name);
                    if (address != null) {
                        if (cusine != null) {
                            list.add("Closest Address: " + address+"\nCuisine: "+cusine.toString());
                        }
                        else {
                            list.add("Closest Address: " + address + "\nCuisine: Not Listed");
                        }
                    } else {
                        list.add("Closest Address: Not Listed"+"\nCuisine: Not Listed");
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

        //the set up needed
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String provider=locationManager.getBestProvider(newCriteria(), true);
        Location l = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider, 2000, 10,
                locationListener);
        FactualRetrievalTask task=new FactualRetrievalTask();

        switch (item.getItemId()){
            case R.id.refresh:

                if (l !=null){
                Query q = new Query()
                        .within(new Circle(l.getLatitude(), l.getLongitude(), 5000))
                        .sortAsc("$distance")
                        .only("name", "address", "cuisine")
                        .limit(25);
                task.execute(q);
            } else {
                    String s="Please Turn on your Location Services"+
                            "\n and hit refresh after a few seconds";

                    ArrayList<String> array=new ArrayList<String>();
                    array.add(s);
                    final ListView lView = (ListView) findViewById(R.id.display_messages);
                    lView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array));

                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
