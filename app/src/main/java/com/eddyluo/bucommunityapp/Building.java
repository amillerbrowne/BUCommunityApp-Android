package com.eddyluo.bucommunityapp;

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
	public String fullName;          // Full name of the building
    public String type;              // Type of building
    public int originalColor;        // Original color of the building
	private PolygonOptions poBuilding; // Polygon Data
	private Polygon buildingOnMap;    // Building on the map
	
	public Building(LatLng[] initialCoords, String initialName) { // accepts array
		// Adds coordinates to an Array List
		coords = new ArrayList<LatLng>(Arrays.asList(initialCoords));
		name = initialName;
		poBuilding = makePolygon(coords);
        originalColor = Color.rgb(204,0,0);
	}
	
	public Building(ArrayList<LatLng> initialCoords, String initialName) { // accepts arraylist
		// Adds coordinates to an Array List
		coords = initialCoords;
		name = initialName;
		poBuilding = makePolygon(coords);
        originalColor = Color.rgb(204,0,0);
	}
	
	public Building(ArrayList<LatLng> initialCoords, String initialName, String wholeName, String buildingType) { // accepts arraylist
		// Adds coordinates to an Array List
		coords = initialCoords;
		name = initialName; // building code
		fullName = wholeName; // full name of the building
		type = buildingType;
        switch (buildingType) {
            case "residence":
                originalColor = Color.rgb(1,70,32); // dark green
                break;
            case "services":
                originalColor = Color.rgb(204,153,255);
                break;
            case "athletic":
                originalColor = Color.rgb(204,102,0);
                break;
            default: // educational buildings
                originalColor = Color.rgb(204,0,0);
        }
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
	public Polygon getPolygon() {
		return buildingOnMap;
	}
	
	public void setColor(int color) {
		buildingOnMap.setFillColor(color);
	}
	
	public int getColor() {
		return buildingOnMap.getFillColor();
	}
	
    private PolygonOptions makePolygon(ArrayList<LatLng> arg) {
	    LatLng[] data = arg.toArray(new LatLng[arg.size()]);
	    PolygonOptions polygonOptions = new PolygonOptions();
	    for(int i=0; i < (data.length); i++) {
		        polygonOptions.add(data[i]).strokeWidth(2);
		        polygonOptions.fillColor(originalColor);
		    }
		return polygonOptions;
    }
    
    public boolean isPointInPolygon(LatLng tap) {
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
