package com.eddyluo.bucommunityapp;

import android.app.ListActivity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.app.SearchManager;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * CHANGED MY MIND ON THIS. HAVING A SECOND ACTIVITY IN THIS KIND OF APP IS A BAD IDEA.
 */
public class SearchableActivity extends ListActivity {
    // Get the intent, verify the action and get the query
    String query;
    ArrayList<Building> storedContent = new ArrayList<Building>();
    ArrayList<String> contentArray = new ArrayList<String>();
    ListView displaySearchResult;
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.building_search_results);
        displaySearchResult = (ListView) findViewById(R.id.lvResults);
        Intent intent = getIntent();
        handleIntent(intent);

    }

    private void handleIntent(Intent intent) {
        storedContent = intent.getParcelableArrayListExtra("buildingNames");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            // put code from solution here
            for(Building each : storedContent){
                if (each.getName().contains(query)){
                    contentArray.add(each.getName());
                }
            }
            // adapter = new ArrayAdapter<String>(this,);
        }
    }
}
