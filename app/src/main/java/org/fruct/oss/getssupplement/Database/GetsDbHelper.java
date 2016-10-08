package org.fruct.oss.getssupplement.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        super(context, getDatabasePrefix(_databaseType) + Const.DB_INTERNAL_NAME, null, 5);
        this.databaseType = _databaseType;
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
                        "categoryId," +
                        "name text," +
                        "description text," +
                        "url text," +
                        "access text," +
                        "time text," +
                        "latitude real," +
                        "longitude real," +
                        "rating real," +
                        "uuid text," +
                        "markerId integer" +
                        ");"
        );

        db.execSQL("create table " + Const.DB_INTERNAL_CATEGORIES + "(" +
                        "_id integer primary key," + // Unique id, connected with API
                        "name text," +
                        "description text," +
                        "iconurl text" +
                        ");"
        );

        db.execSQL("create table " + Const.DB_TEMP_POINTS + "(" +
                "_id integer primary key autoincrement," + // Internal id, not connected with API
                "categoryId," +
                "title text," +
                "token text," +
                "latitude real," +
                "longitude real," +
                "rating real," +
                "streetId text," +
                "time text," +
                "status text," +
                "code text," +
                "message text" +
                ");"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + Const.DB_INTERNAL_POINTS);
        db.execSQL("drop table if exists " + Const.DB_INTERNAL_CATEGORIES);
        db.execSQL("drop table if exists " + Const.DB_TEMP_POINTS);
        onCreate(db);
    }


    public SQLiteDatabase getWrData() {
        SQLiteDatabase dbtemp = getWritableDatabase();
        return dbtemp;
    }

    public SQLiteDatabase getRdData() {
        SQLiteDatabase dbtemp = getReadableDatabase();
        return dbtemp;
    }


    public void clearDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from " + Const.DB_INTERNAL_POINTS);
        //db.execSQL("delete from " + Const.DB_INTERNAL_CATEGORIES);
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
            return list;
        }
        cursor.close();
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
        addPoint(point.categoryId,
                point.name,
                point.url,
                point.access,
                point.time,
                point.description,
                point.latitude,
                point.longitude,
                point.rating,
                point.uuid,
                point.markerId
        );
    }

    public void addPoint(int categoryId, String name, String url, String access, long time, String description, String latitude, String longitude, float rating, String uuid, long markerId) {
        addPoint(categoryId, name, url, access, time + "", description, Float.parseFloat(latitude), Float.parseFloat(longitude), rating, uuid, markerId);
    }

    public void addPoint(int categoryId, String name, String url, String access, String time, String description, double latitude, double longitude, float rating, String uuid, long markerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("categoryId", categoryId);
        cv.put("name", name);
        cv.put("url", url);
        cv.put("access", access);
        cv.put("time", time); // TODO: normalize time
        cv.put("description", description);
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        cv.put("rating", rating);
        cv.put("uuid", uuid);
        cv.put("markerId", markerId);

        db.insertWithOnConflict(Const.DB_INTERNAL_POINTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void addPoints(ArrayList<Point> points) {
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);

            // TODO: convert time
            this.addPoint(point.categoryId, point.name, point.url, point.access, "0", point.description, point.latitude, point.longitude, point.rating, point.uuid, point.markerId);
        }
    }

    public ArrayList<Point> getPoints(int categoryId) {

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor;

        /*
        double dLat = loadCenter.getLatitude();
        double dLng = loadCenter.getLongitude();
        // TODO: convert geo coordinates
        if (categoryId == Const.ALL_CATEGORIES)
            cursor = db.query(true, Const.DB_INTERNAL_POINTS, null,
                    "latitude BETWEEN " + (dLat - 0.01) + " AND " + (dLat + 0.01) + " AND " +
                    "longitude BETWEEN " + (dLng - 0.03) + " AND " + (dLng + 0.03),
                    null, null, null, null, null);
       */
        if (categoryId == Const.ALL_CATEGORIES)
            cursor = db.query(true, Const.DB_INTERNAL_POINTS, null, null, null, null, null, null, null);
        else
            cursor = db.query(true, Const.DB_INTERNAL_POINTS, null, "categoryId = " + categoryId, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndex("_id");
            int indexCategoryId = cursor.getColumnIndex("categoryId");
            int indexName = cursor.getColumnIndex("name");
            int indexAccess = cursor.getColumnIndex("access");
            int indexTime = cursor.getColumnIndex("time");
            int indexDescription = cursor.getColumnIndex("description");
            int indexLatitude = cursor.getColumnIndex("latitude");
            int indexLongitude = cursor.getColumnIndex("longitude");
            int indexUrl = cursor.getColumnIndex("url");
            int indexRating = cursor.getColumnIndex("rating");
            int indexUuid = cursor.getColumnIndex("uuid");
            int indexMarkerId = cursor.getColumnIndex("markerId");

            ArrayList<Point> list = new ArrayList<Point>();
            do {
                Point point = new Point();
                point.id = cursor.getInt(indexId);
                point.categoryId = cursor.getInt(indexCategoryId);
                point.name = cursor.getString(indexName);
                point.url = cursor.getString(indexUrl);
                point.access = cursor.getString(indexAccess);
                point.time = cursor.getString(indexTime);
                point.description = cursor.getString(indexDescription);
                point.latitude = cursor.getFloat(indexLatitude);
                point.longitude = cursor.getFloat(indexLongitude);
                point.rating = cursor.getFloat(indexRating);
                point.uuid = cursor.getString(indexUuid);
                point.markerId = cursor.getLong(indexMarkerId);

                list.add(point);
            } while (cursor.moveToNext());

            cursor.close();
            return list;
        }

        cursor.close();
        return null;
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    public Point getPointByMarkerId(long id) {

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(true, Const.DB_INTERNAL_POINTS, null, "markerId = " + id, null, null, null, null, null);

        int indexId = cursor.getColumnIndex("_id");
        int indexCategoryId = cursor.getColumnIndex("categoryId");
        int indexName = cursor.getColumnIndex("name");
        int indexAccess = cursor.getColumnIndex("access");
        int indexTime = cursor.getColumnIndex("time");
        int indexDescription = cursor.getColumnIndex("description");
        int indexLatitude = cursor.getColumnIndex("latitude");
        int indexLongitude = cursor.getColumnIndex("longitude");
        int indexUrl = cursor.getColumnIndex("url");
        int indexRating = cursor.getColumnIndex("rating");
        int indexUuid = cursor.getColumnIndex("uuid");
        int indexMarkerId = cursor.getColumnIndex("markerId");

        if (cursor.moveToFirst()) {
            Point point = new Point();
            point.id = cursor.getInt(indexId);
            point.categoryId = cursor.getInt(indexCategoryId);
            point.name = cursor.getString(indexName);
            point.url = cursor.getString(indexUrl);
            point.access = cursor.getString(indexAccess);
            point.time = cursor.getString(indexTime);
            point.description = cursor.getString(indexDescription);
            point.latitude = cursor.getFloat(indexLatitude);
            point.longitude = cursor.getFloat(indexLongitude);
            point.rating = cursor.getFloat(indexRating);
            point.uuid = cursor.getString(indexUuid);
            point.markerId = cursor.getLong(indexMarkerId);

            cursor.close();
            return point;
        } else {
            cursor.close();
            return null;
        }
    }
}
