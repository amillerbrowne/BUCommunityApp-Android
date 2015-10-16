package com.eddyluo.bucommunityapp;

/*
 * I'd like for the code to be more organized.
 *
*/

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import android.app.SearchManager;
import android.location.LocationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
	
	private static final LatLng GSU = new LatLng(42.351028, -71.109000);
	private static final LatLng MED = new LatLng(42.336238, -71.072367);
	final int startCoordIterator = 3; // change based on the index of the first coordinate of the building
	GoogleMap BUmap; // class variable used for the map
    LocationManager locationManager;
	SearchView searchView;
	CharSequence tExplanation = "Tap a building to find its name!";
    ArrayList<Building> BUBuildings = new ArrayList<>();
	int explanationDuration = Toast.LENGTH_LONG;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BUmap = ((MapFragment)getFragmentManager().findFragmentById(R.id.MapFragment)).getMap();
        if (BUmap != null) {
            BUmap.moveCamera(CameraUpdateFactory.newLatLngZoom(GSU, 16.0f));
            BUmap.setMyLocationEnabled(true); // location shown on map. plan to show which building you're in
        }
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		ArrayList<LatLng> vertices = new ArrayList<>(); // initializes a list of vertices
		InputStream inputStream = getResources().openRawResource(R.raw.buildinglist);
		CSVFile csvFile = new CSVFile(inputStream);
		List<String[]> buildingList = csvFile.read();
		String buildingCode;
		String officialName;
		String buildingType;
		for (String[] bOptions : buildingList) { // this class places buildings into the arraylist
			buildingCode = bOptions[0]; // initializes building code
			officialName = bOptions[1]; // initializes full name
			buildingType = bOptions[2]; // initializes color of building
			for (int ii = startCoordIterator; ii < bOptions.length; ii++) {
				if ((bOptions[ii]).length() != 0) {
					String[] latAndLong = bOptions[ii].split(",");
					double latitude = Double.parseDouble(latAndLong[0]);
					double longitude = Double.parseDouble(latAndLong[1]);
					vertices.add(new LatLng(latitude, longitude)); // adds the vertex to the thing
				}
			}
			BUBuildings.add(new Building(vertices, buildingCode, officialName, buildingType)); // finally adds the building
			vertices.clear();
		}
		for (Building buildingToAdd: BUBuildings) {
			buildingToAdd.addToMap(BUmap);
		}
		
		Context appStarted = getApplicationContext();
		Toast introToast = Toast.makeText(appStarted, tExplanation, explanationDuration);
		introToast.show();

		
		BUmap.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng tap) {
                for (Building bLoc : BUBuildings) {
                    if (bLoc.isPointInPolygon(tap)) {
                        selectBuilding(bLoc);
                    } else {
                        bLoc.setColor(bLoc.originalColor);
                    }
                }
            }
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present
		getMenuInflater().inflate(R.menu.main, menu);
		// Initiates the search manager's options
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getResources().getString(R.string.find_building));
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onSearchRequested();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		int id = item.getItemId();
		if (id == R.id.switch_to_CRC) {
			BUmap.moveCamera(CameraUpdateFactory.newLatLngZoom(GSU, 16.0f));
            return true;
        }
        if (id == R.id.switch_to_MED) {
			BUmap.moveCamera(CameraUpdateFactory.newLatLngZoom(MED, 16.0f));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    public void selectBuilding(Building bLoc) {
        // Plan: make this show something interesting about the building
        if (bLoc.getColor() == Color.BLUE) {

        }
        BUmap.moveCamera(CameraUpdateFactory.newLatLng(bLoc.getCenterCoordinate()));
        bLoc.setColor(Color.BLUE);
        Context polygonpressed = getApplicationContext();
        String polygonwriting = bLoc.getFullName() + " (" + bLoc.getName() + ") selected.";
        Toast tDispName = Toast.makeText(polygonpressed, polygonwriting, Toast.LENGTH_SHORT);
        tDispName.show();
    }

	@Override
	public boolean onSearchRequested() {
        String query = searchView.getQuery().toString();
        boolean buildingFound = false;
        int iter = 0;

        while (!buildingFound && BUBuildings.size() > iter) {
            Building toCheck = BUBuildings.get(iter);
            if (query.equalsIgnoreCase(toCheck.getName())) {
                buildingFound = true;
                selectBuilding(toCheck);
            }
            iter++;
        }
        if (!buildingFound) {
            Context bNotFound = getApplicationContext();
            String notFoundMessage = "Building not found.";
            Toast tDispName = Toast.makeText (bNotFound, notFoundMessage, Toast.LENGTH_SHORT);
            tDispName.show();
        }
		return super.onSearchRequested();
	}

}