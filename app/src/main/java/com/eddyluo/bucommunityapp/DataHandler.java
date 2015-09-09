package com.eddyluo.bucommunityapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataHandler {
	public static final String NAME = "name";
	public static final String EMAIL = "email";
	public static final String TABLE_NAME = "myTable";
	public static final String DATABASE_NAME = "myDatabase";
	public static final int DATABASE_VERSION = 1;
	public static final String TABLE_CREATE = "create table myTable (name text not null, email text not null)";
	
	DatabaseHelper dbHelper;
	Context ctx;
	SQLiteDatabase db;
	
	public DataHandler(Context ctx) {
		this.ctx = ctx;
		dbHelper = new DatabaseHelper(ctx);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		public DatabaseHelper(Context ctx) {
			super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {

			try {
				db.execSQL(TABLE_CREATE);
				} catch(SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			db.execSQL("DROP TABLE IF EXISTS myTable");
			onCreate(db);
		}
		
	}
	public DataHandler open() {
		db = dbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public long insertData(String name, String email) {
		ContentValues content = new ContentValues();
		content.put(NAME, name);
		content.put(EMAIL, email);
		return db.insertOrThrow(TABLE_NAME, null, content);
	}
	
public Cursor returnData() {
	
	return db.query(TABLE_NAME, new String[] {NAME, EMAIL}, null, null, null, null, null);
	
}
	
}
