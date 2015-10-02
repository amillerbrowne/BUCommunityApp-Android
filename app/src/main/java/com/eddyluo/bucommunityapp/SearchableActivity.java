package com.eddyluo.bucommunityapp;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.app.SearchManager;
import java.util.ArrayList;

/**
 * Meant to handle searching and finding of buildings.
 * First, you search for a building name or abbreviation.
 * You probably should be able to sort by building type.
 * Then, you choose the building and it's highlighted.
 */
public class SearchableActivity extends ListActivity {
    // Get the intent, verify the action and get the query
    ArrayList<String> storedContent = new ArrayList<String>();
    ArrayList<String> contentArray = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.building_search_results);
        Intent intent = getIntent();
        handleIntent(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            // put code from solution here

        }
    }
}
