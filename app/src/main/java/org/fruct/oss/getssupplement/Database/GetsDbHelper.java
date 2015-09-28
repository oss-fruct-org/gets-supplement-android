package org.fruct.oss.getssupplement.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.fruct.oss.getssupplement.Const;
import org.fruct.oss.getssupplement.Model.Category;
import org.fruct.oss.getssupplement.Model.DatabaseType;
import org.fruct.oss.getssupplement.Model.Point;

import java.util.ArrayList;

/**
 * Created by Andrey on 19.07.2015.
 */
public class GetsDbHelper extends SQLiteOpenHelper{

    // Prefix is used to define Db type: internal Db for storing GeTS data or
    // internal Db for storing temporary data that should be uploaded to remote server
    private DatabaseType databaseType;

    public GetsDbHelper(Context context, DatabaseType _databaseType) {
        super(context, getDatabasePrefix(_databaseType) + Const.DB_INTERNAL_NAME, null, 3);
        this.databaseType = databaseType;
    }

    private static String getDatabasePrefix(DatabaseType databaseType) {
        if (databaseType == DatabaseType.DATA_FROM_API)
            return "api_";

        return "user_";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + Const.DB_INTERNAL_POINTS + "(" +
                        "_id integer primary key autoincrement," + // Internal id, not connected with API
                        "name text," +
                        "url text," +
                        // FIXME: no description?
                        "access text," + // TODO: how to use?
                        "time text," +
                        "latitude real," +
                        "longitude real," +
                        "rating real," +
                        "uuid text" +
                        ");"
        );

        db.execSQL("create table " + Const.DB_INTERNAL_CATEGORIES + "(" +
                        "_id integer primary key," + // Unique id, connected with API
                        "name text," +
                        "description text," +
                        "iconurl text" +
                        ");"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }



    public void clearDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from " + Const.DB_INTERNAL_POINTS);
        db.execSQL("delete from " + Const.DB_INTERNAL_CATEGORIES);
    }

    /**
     *
     *
     *
     *
     * Storing API's categories
     *
     *
     *
     */
    public String getCategoryName(int categoryId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(true, Const.DB_INTERNAL_CATEGORIES, null, "_id="+categoryId, null, null, null, null, null);
        String categoryName = null;

        if (cursor.moveToNext()) {
            int indexName = cursor.getColumnIndex("name");

            categoryName = cursor.getString(indexName);
        }

        cursor.close();
        db.close();

        return categoryName;
    }

    public void addCategory(int id, String name, String description, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("_id", id);
        cv.put("name", name);
        cv.put("description", description);
        cv.put("iconurl", url);
        db.insertWithOnConflict(Const.DB_INTERNAL_CATEGORIES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void addCategories(ArrayList<Category> categories) {
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            this.addCategory(category.id, category.name, category.description, category.urlIcon);
        }
    }

    public ArrayList<Category> getCategories() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(true, Const.DB_INTERNAL_CATEGORIES, null, null, null, null, null, null, null);
        Log.d(Const.TAG, "Db categories cursor count:" + cursor.getCount());


        if (cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndex("_id");
            int indexName = cursor.getColumnIndex("name");
            int indexDescription = cursor.getColumnIndex("description");
            int indexUrl = cursor.getColumnIndex("iconurl");

            ArrayList<Category> list = new ArrayList<Category>();
            do {
                Category category = new Category();
                category.id = cursor.getInt(indexId);
                category.name = cursor.getString(indexName);
                category.description = cursor.getString(indexDescription);
                category.urlIcon = cursor.getString(indexUrl);

                list.add(category);

            } while (cursor.moveToNext());

            cursor.close();
            db.close();
            return list;
        }

        db.close();
        return null;
    }







    /**
     *
     *
     *
     *
     * Storing API's POINT
     *
     *
     *
     */
    public void addPoint(Point point) {
        addPoint(point.name,
                point.url,
                point.access,
                point.time,
                point.latitude,
                point.longitude,
                point.rating
        );
    }

    public void addPoint(String name, String url, String access, long time, String latitude, String longitude, float rating) {
        addPoint(name, url, access, time + "", Float.parseFloat(latitude), Float.parseFloat(longitude), rating);
    }

    public void addPoint(String name, String url, String access, String time, double latitude, double longitude, float rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("name", name);
        cv.put("url", url);
        cv.put("access", access);
        cv.put("time", time); // TODO: normalize time
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        cv.put("rating", rating);

        db.insert(Const.DB_INTERNAL_POINTS, null, cv);
        Log.d(Const.TAG + " testing", "addPoint pre db.close");

        db.close();
    }

    public void addPoints(ArrayList<Point> points) {
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);

            // TODO: convert time
            this.addPoint(point.name, point.url, point.access, "0", point.latitude, point.longitude, point.rating);
        }
    }

    public ArrayList<Point> getPoints() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(true, Const.DB_INTERNAL_POINTS, null, null, null, null, null, null, null);
        Log.d(Const.TAG + " testing", "getPoints cursor " + cursor.getCount());
        if (cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndex("_id");
            int indexName = cursor.getColumnIndex("name");
            int indexAccess = cursor.getColumnIndex("access");
            int indexTime = cursor.getColumnIndex("time");
            int indexLatitude = cursor.getColumnIndex("latitude");
            int indexLongitude = cursor.getColumnIndex("longitude");
            int indexUrl = cursor.getColumnIndex("url");
            int indexRating = cursor.getColumnIndex("rating");

            ArrayList<Point> list = new ArrayList<Point>();
            do {
                Point point = new Point();
                point.id = cursor.getInt(indexId);
                point.name = cursor.getString(indexName);
                point.url = cursor.getString(indexUrl);
                point.access = cursor.getString(indexAccess);
                point.time = cursor.getString(indexTime);
                point.latitude = cursor.getFloat(indexLatitude);
                point.longitude = cursor.getFloat(indexLongitude);
                point.rating = cursor.getFloat(indexRating);

                list.add(point);
            } while (cursor.moveToNext());

            Log.d(Const.TAG + " testing", "getPoints dbclose " + list.size());

            db.close();
            return list;
        }

        db.close();
        return null;
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }
}
