package com.eddyluo.bubathroomlocator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class Building {
	public ArrayList<LatLng> coords; // Coordinates of the building
	public String name;              // Name of the building
	private PolygonOptions poBuilding; // Polygon Data
	private Polygon buildingOnMap;    // Building on the map
	
	public Building(LatLng[] initialCoords, String initialName) { // accepts array
		// Adds coordinates to an Array List
		coords = new ArrayList<LatLng>(Arrays.asList(initialCoords));
		name = initialName;
		poBuilding = makePolygon(coords);
	}
	
	public Building(ArrayList<LatLng> initialCoords, String initialName) { // accepts arraylist
		// Adds coordinates to an Array List
		coords = initialCoords;
		name = initialName;
		poBuilding = makePolygon(coords);
	}
	
	public void setName(String newName) {
		name = newName;
	}
    
	public LatLng getCenterCoordinate() {
		LatLng centerCoordinate;
		double minCoordX;
		double minCoordY;
		double maxCoordX;
		double maxCoordY;
		
		List<LatLng> vertices = poBuilding.getPoints();
		minCoordX = vertices.get(0).latitude;
		maxCoordX = vertices.get(0).latitude;
		minCoordY = vertices.get(0).longitude;
		maxCoordY = vertices.get(0).longitude;
		for(int i=0; i < (vertices.size()); i++) {
			if (vertices.get(i).latitude < minCoordX) {
				minCoordX = vertices.get(i).latitude;
			} else if (vertices.get(i).latitude > maxCoordX) {
				maxCoordX = vertices.get(i).latitude;
			} 
			if (vertices.get(i).longitude < minCoordY) {
				minCoordY = vertices.get(i).longitude;
			} else if (vertices.get(i).longitude < maxCoordY) {
				maxCoordY = vertices.get(i).longitude;
			}
		}
		double coordX = (minCoordX+maxCoordX)/2;
		double coordY = (minCoordY+maxCoordY)/2;
		centerCoordinate = new LatLng(coordX, coordY);
		return centerCoordinate;
	}
	
	public Polygon addToMap(GoogleMap map) {
		buildingOnMap = map.addPolygon(poBuilding);
		return buildingOnMap;
	}
	
    private PolygonOptions makePolygon(ArrayList<LatLng> arg) {
	    LatLng[] data = arg.toArray(new LatLng[arg.size()]);
	    PolygonOptions polygonOptions = new PolygonOptions();
	    for(int i=0; i < (data.length); i++) {
		        polygonOptions.add(data[i]).strokeWidth(2);
		        polygonOptions.fillColor(Color.RED);
		    }
		return polygonOptions;
    }
    
    public boolean isPointInPolygon(LatLng tap, Polygon buildingOnMap) {
		List<LatLng> vertices = buildingOnMap.getPoints();
	    int intersectCount = 0;
	    for(int j=0; j<vertices.size()-1; j++) {
	        if( rayCastIntersect(tap, vertices.get(j), vertices.get(j+1)) ) {
	            intersectCount++;
	        }
	    }
	    return (intersectCount%2 == 1); // odd = inside, even = outside;
	}

	private boolean rayCastIntersect(LatLng tap, LatLng vertA, LatLng vertB) {

	    double aY = vertA.latitude;
	    double bY = vertB.latitude;
	    double aX = vertA.longitude;
	    double bX = vertB.longitude;
	    double pY = tap.latitude;
	    double pX = tap.longitude;

	    if ( (aY>pY && bY>pY) || (aY<pY && bY<pY) || (aX<pX && bX<pX) ) {
	        return false; // a and b can't both be above or below pt.y, and a or b must be east of pt.x
	    }

	    double m = (aY-bY) / (aX-bX);               // Rise over run
	    double bee = (-aX) * m + aY;                // y = mx + b
	    double x = (pY - bee) / m;                  // algebra is neat!

	    return x > pX;
	}
}
