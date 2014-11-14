package com.eddyluo.bubathroomlocator;

import java.util.ArrayList;
import java.util.List;
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
	
	Button toCRCampus, toMedCampus;
	private static final LatLng GSU = new LatLng(42.351028, -71.109000);
	private static final LatLng MED = new LatLng(42.336238, -71.072367);
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
		ArrayList<LatLng> vertices; // initializes a list of vertices
		ArrayList<LatLng> alPho = new ArrayList<LatLng>();
		alPho.add(new LatLng(42.349218, -71.105253));
		alPho.add(new LatLng(42.348902, -71.105321));
		alPho.add(new LatLng(42.349153, -71.106644));
		alPho.add(new LatLng(42.349381, -71.106598));
		final Building bPho = new Building(alPho, "PHO");
		final Polygon pgPho = bPho.addToMap(BUmap);
		/*
		PolygonOptions poPho = new PolygonOptions()
			.add(new LatLng(42.349218, -71.105253),
				 new LatLng(42.348902, -71.105321),
				 new LatLng(42.349153, -71.106644),
				 new LatLng(42.349381, -71.106598))
			.fillColor(Color.RED)
			.strokeWidth(2);
		*/
		BUmap.addMarker(new MarkerOptions()
			.position(new LatLng(42.350743, -71.108457))
			.title("GSU George Sherman Union"));
		Context appStarted = getApplicationContext();
		Toast introToast = Toast.makeText(appStarted, tExplanation, explanationDuration);
		introToast.show();
		toCRCampus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				BUmap.moveCamera(CameraUpdateFactory.newLatLngZoom(GSU, 16.0f));
			}
			
		});
		toMedCampus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				BUmap.moveCamera(CameraUpdateFactory.newLatLngZoom(MED, 16.0f));
			}
			
		});
		
		BUmap.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng tap) {
				// TODO Auto-generated method stub
				if (bPho.isPointInPolygon(tap, pgPho)) {
					// The following is mostly placeholder until I make the new activity for indoor maps.
					BUmap.moveCamera(CameraUpdateFactory.newLatLngZoom(bPho.getCenterCoordinate(), 16.0f)); 
					Context polygonpressed = getApplicationContext();
					String polygonwriting = "Photonics Center pressed."; 
					Toast wtfan = Toast.makeText(polygonpressed, polygonwriting, explanationDuration);
					wtfan.show();
				}
				
			}
			
		});
		
	}

	public void initializeVars(GoogleMap map) {
		// Initializes all buttons to their XML locations.
		/* By default, all BU buildings are visible and marked in
		 * red. Options label all buildings by their 3-4 digit code.
		 */
		toCRCampus = (Button) findViewById(R.id.bCRCampus); 
		toMedCampus = (Button) findViewById(R.id.bMedCampus);
		String findBuildingName = getResources().getString(R.string.find_building);
		tExplanation = "To select a building, choose '".concat(findBuildingName).concat("' from the Options menu.");
		
		
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