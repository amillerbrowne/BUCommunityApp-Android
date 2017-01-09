package com.eddyluo.bucommunityapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.location.LocationManager;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final LatLng GSU = new LatLng(42.351028, -71.109000); // George Sherman Union
    private static final LatLng MED = new LatLng(42.336238, -71.072367); // Medical Campus
    private final static int INTERVAL = 1000 * 5; // 5 seconds
    CameraPosition initialPosition;
    GoogleMap BUmap; // class variable used for the map
    LocationManager locationManager;
    AutoCompleteTextView tvSearchQuery;
    String tExplanation;
    ArrayList<Building> BUBuildings = new ArrayList<>();
    SparseArray<Marker> allBuses = new SparseArray<>();
    Timer timer;
    final Handler h = new Handler();
    TimerTask readShuttles;
    int explanationDuration = Toast.LENGTH_LONG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initialPosition = new CameraPosition.Builder()
                .target(GSU)
                .bearing(9.5f)
                .zoom(16.0f)
                .build();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (BUmap == null) {
            MapFragment mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.MapFragment);
            mapFrag.getMapAsync(this);
        }

        // Pop up explanation of the app

        Context appStarted = getApplicationContext();
        tExplanation = getResources().getString(R.string.tap_get_name);
        Toast introToast = Toast.makeText(appStarted, tExplanation, explanationDuration);
        introToast.show();
    }

    @Override
    protected void onStart() { // Called when activity starts or restarts
        super.onStart();
        timer = new Timer(); // Reinitialize timer
        readShuttles = new ReadShuttles(); // Reinitialize timer task
        timer.schedule(readShuttles, 0, INTERVAL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel(); // cancel timer
        readShuttles.cancel(); // Cancel TimerTask
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.main, menu);
        // Initiates the search manager's options
        ArrayList<String> allNames = new ArrayList<>();
        for (Building b : BUBuildings) {
            allNames.add(b.getFullName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, R.id.drop_item, allNames);
        tvSearchQuery = (AutoCompleteTextView) menu.findItem(R.id.action_search).getActionView();
        tvSearchQuery.setHint(getResources().getString(R.string.find_building));
        tvSearchQuery.setAdapter(adapter);
        tvSearchQuery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                buildingSearch(adapterView.getItemAtPosition(i).toString());
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
            if (BUmap != null) {
                BUmap.moveCamera(CameraUpdateFactory.newCameraPosition(initialPosition));
            }
            return true;
        }
        if (id == R.id.switch_to_MED) {
            if (BUmap != null){
                BUmap.moveCamera(CameraUpdateFactory.newLatLngZoom(MED, 16.0f));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void selectBuilding(Building bLoc) {
        // Plan: make this display which classes are currently in session
        /*
        if (bLoc.getColor() == Color.BLUE) {

        }
        */
        if (BUmap != null) {
            BUmap.animateCamera(CameraUpdateFactory.newLatLng(bLoc.getCenterCoordinate())); // move camera to building
        }
        bLoc.setColor(Color.BLUE);
        Context polygonpressed = getApplicationContext();
        String polygonwriting = bLoc.getFullName() + " (" + bLoc.getName() + ")" + getResources().getString(R.string.building_chosen);
        Toast tDispName = Toast.makeText(polygonpressed, polygonwriting, Toast.LENGTH_SHORT);
        tDispName.show();
    }

    public boolean buildingSearch(String query) {
        boolean buildingFound = false;
        int iter = 0;

        while (BUBuildings.size() > iter) {
            Building toCheck = BUBuildings.get(iter);
            if (query.equalsIgnoreCase(toCheck.getFullName())) {
                buildingFound = true;
                selectBuilding(toCheck);
            } else {
                toCheck.setColor(toCheck.originalColor);
            }
            iter++;
        }
        if (!buildingFound) {
            Context bNotFound = getApplicationContext();
            String notFoundMessage = getResources().getString(R.string.not_found);
            Toast tDispName = Toast.makeText(bNotFound, notFoundMessage, Toast.LENGTH_SHORT);
            tDispName.show();
        }
        return super.onSearchRequested();
    }

    private boolean isNetworkAvailable() { // Used to check for connection to shuttle data.
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        BUmap = googleMap;
        setUpMap();
    }

    public void setUpMap() {
        BUmap.setBuildingsEnabled(false); // disable 3D buildings
        BUmap.moveCamera(CameraUpdateFactory.newCameraPosition(initialPosition));
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

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ArrayList<LatLng> vertices = new ArrayList<>(); // initializes a list of vertices
        String buildingCode;
        String officialName;
        String buildingType;

        // Reading list of buildings

        MyDatabase buildingData = new MyDatabase(this);
        Cursor readNames = buildingData.getBuildingNames();
        Cursor vertexData;

        while (readNames.moveToNext()) {
            vertexData = buildingData.getBuildingVertices(readNames.getInt(0));
            buildingCode = readNames.getString(1);
            officialName = readNames.getString(2);
            buildingType = readNames.getString(3);
            while (vertexData.moveToNext()) {
                double latitude = vertexData.getDouble(1);
                double longitude = vertexData.getDouble(2);
                vertices.add(new LatLng(latitude, longitude));
            }
            BUBuildings.add(new Building(vertices, buildingCode, officialName, buildingType)); // add the building
            vertices.clear();
        }
        buildingData.close(); // close file


        for (Building buildingToAdd : BUBuildings) {
            buildingToAdd.addToMap(BUmap);
        }
    }

    private class ReadShuttles extends TimerTask {
        @Override
        public void run() {

            Runnable runnable = new Runnable() {
                MyAsyncTask findShuttles = new MyAsyncTask();
                public void run() {
                    if (isNetworkAvailable()) findShuttles.execute();
                }
            };
            h.post(runnable);
        }
    }

    private class MyAsyncTask extends AsyncTask<String, String, Void> {
        InputStream inputStream = null;
        String result = "";

        @Override
        protected Void doInBackground(String... params) {

            String url_select = "http://www.bu.edu/bumobile/rpc/bus/livebus.json.php";

            try {
                URL url = new URL(url_select);
                HttpURLConnection connectTo = (HttpURLConnection) url.openConnection();
                connectTo.setRequestProperty("User-Agent", "");
                connectTo.setRequestMethod("POST");
                connectTo.setDoInput(true);
                connectTo.connect();

                // Read content and log
                inputStream = connectTo.getInputStream();
            } catch (IOException e4) {
                Log.e("IOException", e4.toString());
                e4.printStackTrace();
            }
            // Convert response to string using String Builder
            try {
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                StringBuilder sBuilder = new StringBuilder();

                String line;
                while ((line = bReader.readLine()) != null) {
                    sBuilder.append(line);
                    sBuilder.append("\n");
                }

                inputStream.close();
                result = sBuilder.toString();

            } catch (Exception e) {
                Log.e("St.Build,BuffRead", "Error converting result " + e.toString());
            }
            return null;
        } // protected Void doInBackground(String... params)

        @Override
        protected void onPostExecute(Void v) {
            //parse JSON data
            try {
                JSONObject busData = new JSONObject(result);
                if (busData.getString("title").equals("BU Bus Positions")) {
                    JSONObject resultSet = busData.getJSONObject("ResultSet");
                    JSONArray results = resultSet.getJSONArray("Result");
                    for (int i = 0; i < results.length(); i++) {
                        try { // loop over all of them so that one malformed JSON doesn't break it
                            JSONObject bus = results.getJSONObject(i);
                            int call_name = bus.getInt("call_name");
                            double lat = bus.getDouble("lat");
                            double lng = bus.getDouble("lng");
                            float heading = (float) bus.getInt("heading");
                            LatLng busLocation = new LatLng(lat,lng);
                            if (allBuses.get(call_name) != null) {
                                animateMarker(allBuses.get(call_name), busLocation, heading, false);
                            } else {
                                String bus_type;
                                switch (call_name/100) {
                                    case 20:
                                        bus_type = "Large BUS";
                                        break;
                                    case 21:
                                        bus_type = "Small BUS";
                                        break;
                                    default:
                                        bus_type = "BUS";
                                }
                                if (BUmap != null) {
                                    Marker busMark = BUmap.addMarker(new MarkerOptions()
                                            .position(busLocation)
                                            .title(bus_type)
                                            .rotation(heading)
                                            .flat(true));
                                    allBuses.put(call_name, busMark);
                                }
                            }
                        } catch (JSONException e) {
                            Log.e("JSONException", "Error: " + e.toString());
                        } // catch (JSONException e)
                    }
                }
            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } // catch (JSONException e)

        } // protected void onPostExecute(Void v)
    }
    public void animateMarker(final Marker marker, final LatLng toPosition,  final float heading,
                              final boolean hideMarker) {
        if (BUmap != null) {
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            Projection proj = BUmap.getProjection();
            Point startPoint = proj.toScreenLocation(marker.getPosition());
            final float startAngle = heading;
            final LatLng startLatLng = proj.fromScreenLocation(startPoint);
            final long duration = 500;

            final Interpolator interpolator = new LinearInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed
                            / duration);
                    double lng = t * toPosition.longitude + (1 - t)
                            * startLatLng.longitude;
                    double lat = t * toPosition.latitude + (1 - t)
                            * startLatLng.latitude;
                    float angle = t * heading + (1-t)*startAngle;
                    marker.setPosition(new LatLng(lat, lng));
                    marker.setRotation(angle);
                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    } else {
                        if (hideMarker) {
                            marker.setVisible(false);
                        } else {
                            marker.setVisible(true);
                        }
                    }
                }
            });

        }
    }

}