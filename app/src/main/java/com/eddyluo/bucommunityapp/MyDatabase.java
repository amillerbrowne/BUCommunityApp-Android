package com.eddyluo.bucommunityapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Edward on 11/24/2015. Based on SQLiteAssetHelper
 */
public class MyDatabase extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "buildingdata.sqlite3";
    private static final int DATABASE_VERSION = 2;

    public MyDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade(2);
    }

    public SQLiteDatabase readBuilding() {
        return getReadableDatabase();
    }

    public Cursor getBuildingNames() {
        return getReadableDatabase().rawQuery("SELECT * FROM Buildings", null);
    }

    public Cursor getBuildingVertices(int id) {
        return getReadableDatabase().rawQuery("SELECT Vertex, x, y FROM Points WHERE _id = "+ id, null);
    }
}