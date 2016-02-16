package com.eddyluo.bucommunityapp;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Edward on 12/31/2015.
 */
public class BUShuttle {
    private int id;
    private LatLng location;

    public BUShuttle(int id, LatLng location) {
        this.id = id;
        this.location = location;
    }

    public int getId() {
        return id;
    }

}
