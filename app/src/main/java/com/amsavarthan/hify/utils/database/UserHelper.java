package com.amsavarthan.hify.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

/**
 * Created by amsavarthan on 2/3/18.
 */

public class UserHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Hify.db";
    public static final String CONTACTS_TABLE_NAME = "contacts";
    public static final String CONTACTS_COLUMN_ID = "id";
    public static final String CONTACTS_COLUMN_USERNAME = "username";
    public static final String CONTACTS_COLUMN_NAME = "name";
    public static final String CONTACTS_COLUMN_EMAIL = "email";
    public static final String CONTACTS_COLUMN_IMAGE = "image";
    public static final String CONTACTS_COLUMN_PASS = "password";
    public static final String CONTACTS_COLUMN_LOCATION = "location";
    public static final String CONTACTS_COLUMN_BIO = "bio";

    private HashMap hp;

    public UserHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table contacts " + "(id integer primary key,username text, name text,email text,password text,image text,location text,bio text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);
    }

    public void insertContact(String username, String name, String email, String image, String password, String location, String bio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);
        contentValues.put("name", name);
        contentValues.put("email", email);
        contentValues.put("password", password);
        contentValues.put("image", image);
        contentValues.put("location", location);
        contentValues.put("bio", bio);
        db.insert("contacts", null, contentValues);
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from contacts where id="+id+"", null );
    }

    public boolean updateContact(Integer id,String username, String name, String email, String image,String location,String bio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("email", email);
        contentValues.put("username", username);
        contentValues.put("location", location);
        contentValues.put("image", image);
        contentValues.put("bio", bio);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public boolean updateContactNoImage(Integer id,String username, String name, String email, String location,String bio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("email", email);
        contentValues.put("username", username);
        contentValues.put("location", location);
        contentValues.put("bio", bio);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }


    public boolean updateContactNameandImage (Integer id, String name, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("image", image);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public void updateContactNameandEmail(Integer id, String name, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("email", email);
        db.update("contacts", contentValues, "id = ? ", new String[]{Integer.toString(id)});
    }

    public void updateContactName(Integer id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
    }


    public void updateContactLocation(Integer id, String location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("location", location);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
    }

    public void updateContactBio(Integer id, String bio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("bio", bio);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
    }

    public void updateContactUserName(Integer id, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
    }

    public void updateContactImage(Integer id, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("image", image);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
    }

    public void updateContactPassword(Integer id, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("password", password);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
    }

    public void updateContactEmail(Integer id, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        db.update("contacts", contentValues, "id = ? ", new String[]{Integer.toString(id)});
    }


    public Integer deleteContact (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

}