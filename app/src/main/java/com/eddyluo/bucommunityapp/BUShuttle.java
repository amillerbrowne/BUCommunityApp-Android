package com.eddyluo.bucommunityapp;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Edward on 12/31/2015.
 */
public class BUShuttle {
    private int id;
    private LatLng location;

    public BUShuttle(int id) {
        this.id = id;

    }

    public int getId() {
        return id;
    }

}
