package com.eddyluo.bucommunityapp;

/*
 * I'd like for the code to be more organized.
 * 
 */

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.eddyluo.bucommunityapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	
	private static final LatLng GSU = new LatLng(42.351028, -71.109000);
	private static final LatLng MED = new LatLng(42.336238, -71.072367);
	final int startCoordIterator = 2; // change based on the index of the first coordinate of the building
	boolean displayBuildingNames = true;
	CharSequence tExplanation;
	int explanationDuration = Toast.LENGTH_LONG; // When toasts appear, they stay on screen for the maximum default length.
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final GoogleMap BUmap = ((MapFragment)getFragmentManager().findFragmentById(R.id.MapFragment)).getMap();
        if (BUmap != null) {
            BUmap.moveCamera(CameraUpdateFactory.newLatLngZoom(GSU, 16.0f));
        }
		initializeVars(BUmap);
		
		final ArrayList<Building> BUBuildings = new ArrayList<Building>();
		ArrayList<LatLng> vertices = new ArrayList<LatLng>(); // initializes a list of vertices
		ArrayList<LatLng> alPho = new ArrayList<LatLng>();
		InputStream inputStream = getResources().openRawResource(R.raw.buildinglist);
		CSVFile csvFile = new CSVFile(inputStream);
		List<String[]> buildingList = csvFile.read();
		String buildingCode;
		String officialName;
		
		for (String[] bOptions : buildingList) { // this class places buildings into the arraylist
			buildingCode = bOptions[0]; // initializes building code
			officialName = bOptions[1]; // initializes full name
			// originalColor = bOptions[2]; // initializes color of building
			for (int ii = startCoordIterator; ii < bOptions.length; ii++) {
				if ((bOptions[ii].toString()).length() != 0) {
					String[] latAndLong = bOptions[ii].split(","); // error happens here?
					double latitude = Double.parseDouble(latAndLong[0]);
					double longitude = Double.parseDouble(latAndLong[1]);
					vertices.add(new LatLng(latitude, longitude)); // adds the vertex to the thing
				}
			}
			BUBuildings.add(new Building(vertices, buildingCode, officialName)); // finally adds the building
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
				// TODO Auto-generated method stub
				for (Building bLoc: BUBuildings) {
					if (bLoc.isPointInPolygon(tap)) {
						// The following is mostly placeholder until I make the new activity for indoor maps.
                        if (bLoc.getColor() == Color.BLUE) {
                        	
                        }
						BUmap.moveCamera(CameraUpdateFactory.newLatLng(bLoc.getCenterCoordinate())); 
						bLoc.setColor(Color.BLUE);
						Context polygonpressed = getApplicationContext();
						String polygonwriting = bLoc.fullName +" pressed."; 
						Toast tDispName = Toast.makeText(polygonpressed, polygonwriting, Toast.LENGTH_SHORT);
						tDispName.show();
					} else {
						bLoc.setColor(Color.RED);
					}
				}			
			}
		});
		
	}

	public void initializeVars(GoogleMap map) {
		// Initializes all buttons to their XML locations.
		/* By default, all BU buildings are visible and marked in
		 * red. Options label all buildings by their 3-4 digit code.
		 */
		// String findBuildingName = getResources().getString(R.string.find_building);
		// tExplanation = "To select a building, choose '".concat(findBuildingName).concat("' from the Options menu.");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item, GoogleMap BUmap) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.iFindBuilding) {
			// Used to test
			Context bPressed = getApplicationContext();
			Toast argh = Toast.makeText(bPressed, tExplanation, explanationDuration);
			argh.show();
			// Remove after test
			return true;
		}
		if (id == R.id.displayNamesSwitch) {
			if (displayBuildingNames == true) {
				displayBuildingNames = false;
			} else {
				displayBuildingNames = true;
			}
			return true;
		}
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
	
}