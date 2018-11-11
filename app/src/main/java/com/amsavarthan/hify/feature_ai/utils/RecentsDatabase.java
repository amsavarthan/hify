package com.amsavarthan.hify.feature_ai.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecentsDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME="Recents.db";
    public static final String DATABASE_TABLE_NAME="queries";
    public static final String COLUMN_ID="id";
    public static final String COLUMN_QUERY="search_query";

    private HashMap hp;

    public RecentsDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table queries "+"(id integer primary key, search_query text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS queries");
        onCreate(db);
    }

    public boolean insertQuery(String query){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("search_query",query);
        db.insert("queries",null,contentValues);
        return true;
    }

    public Cursor getData(int id){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor res=db.rawQuery("select * from queries where id="+id+"",null);
        return res;
    }

    public Integer deleteQuery(Integer id){
        SQLiteDatabase db=this.getWritableDatabase();
        return db.delete("queries","id = ? ",new String[]{Integer.toString(id)});
    }

    public List<String> getAllQueries(){
        List<String> list=new ArrayList<String>();

        SQLiteDatabase db=this.getReadableDatabase();
        Cursor res=db.rawQuery("select * from queries",null);
        res.moveToFirst();

        while(!res.isAfterLast()){
            list.add(res.getString(res.getColumnIndex(COLUMN_QUERY)));
            res.moveToNext();
        }

        return list;
    }

    public boolean clear() {

        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("delete from "+ DATABASE_TABLE_NAME);
        return true;

    }
}
