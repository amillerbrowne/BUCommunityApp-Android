package com.eddyluo.bucommunityapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class Building implements Parcelable {
	// Building name, full name, and type sent in parcel
	private String name;              // Name of the building
	private String fullName;          // Full name of the building
    private String type;              // Type of building
    public int originalColor;        // Original color of the building (NOT SENT IN PARCEL)
	public ArrayList<LatLng> coords; // Coordinates of the building (NOT SENT IN PARCEL)
	private PolygonOptions poBuilding; // Polygon Data (NOT SENT IN PARCEL)
	private Polygon buildingOnMap;    // Building on the map (NOT SENT IN PARCEL)
	
	public Building(ArrayList<LatLng> initialCoords, String initialName, String wholeName, String buildingType) { // accepts arraylist
		// Adds coordinates to an Array List
		coords = initialCoords;
		name = initialName; // building code
		fullName = wholeName; // full name of the building
		type = buildingType;
		switch (buildingType) {
			case "residence":
				originalColor = Color.rgb(1, 70, 32); // dark green
				break;
			case "services":
				originalColor = Color.rgb(204, 153, 255); // pink
				break;
			case "athletic":
				originalColor = Color.rgb(204, 102, 0);
				break;
			default: // educational buildings
				originalColor = Color.rgb(204, 0, 0);
		}
		poBuilding = makePolygon(coords);
	}
	
	public void setName(String newName) {
		name = newName;
	}

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public String getType() {
        return type;
    }

	public LatLng getCenterCoordinate() {
		LatLng centerCoordinate;
		
		List<LatLng> vertices = poBuilding.getPoints();
		double minCoordX = vertices.get(0).latitude;
		double maxCoordX = vertices.get(0).latitude;
		double minCoordY = vertices.get(0).longitude;
		double maxCoordY = vertices.get(0).longitude;
		for(int i=0; i < (vertices.size()); i++) {
			if (vertices.get(i).latitude < minCoordX) {
				minCoordX = vertices.get(i).latitude;
			} else if (vertices.get(i).latitude > maxCoordX) {
				maxCoordX = vertices.get(i).latitude;
			} 
			if (vertices.get(i).longitude < minCoordY) {
				minCoordY = vertices.get(i).longitude;
			} else if (vertices.get(i).longitude > maxCoordY) {
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel pc, int flags) {
        pc.writeString(name);
        pc.writeString(fullName);
        pc.writeString(type);
	}
    // Object regeneration
    public static final Parcelable.Creator<Building> CREATOR = new Parcelable.Creator<Building>() {
        public Building createFromParcel(Parcel pc) {
            return new Building(pc);
        }
        public Building[] newArray(int size) {
            return new Building[size];
        }
    };

	public Building(Parcel pc) { // Used for searching only
        name = pc.readString();
        fullName = pc.readString();
        type = pc.readString();
	}
}
