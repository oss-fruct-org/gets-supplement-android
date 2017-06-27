package org.fruct.oss.getssupplement.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.fruct.oss.getssupplement.Utils.Const;
import org.fruct.oss.getssupplement.Model.Category;
import org.fruct.oss.getssupplement.Model.Point;

import java.util.ArrayList;

/**
 * Created by Andrey on 19.07.2015.
 */
public class GetsDbHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 6;

    private static GetsDbHelper sInstance;

    public enum SCOPE {
        TEMPORARY,
        CACHE
    }

    public static GetsDbHelper getInstance(Context context) {
        if (sInstance == null)
            sInstance = new GetsDbHelper(context);
        return sInstance;
    }


    private GetsDbHelper(Context context) {
        super(context, Const.DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + Const.DB_POINTS + "(" +
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

        db.execSQL("create table " + Const.DB_CATEGORIES + "(" +
                "_id integer primary key," + // Unique id, connected with API
                "name text," +
                "description text," +
                "iconurl text" +
                ");"
        );

        db.execSQL("create table " + Const.DB_CACHED_POINTS + "(" +
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

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + Const.DB_POINTS);
        db.execSQL("drop table if exists " + Const.DB_CATEGORIES);
        db.execSQL("drop table if exists " + Const.DB_CACHED_POINTS);
        onCreate(db);
    }

    public void clearDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from " + Const.DB_POINTS);
        //db.execSQL("delete from " + Const.DB_CATEGORIES);
    }

    public void cachePoint(Point point) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("_id", point.id);
        cv.put("categoryId", point.categoryId);
        cv.put("name", point.name);
        cv.put("url", point.url);
        cv.put("access", point.access);
        cv.put("time", point.time);
        cv.put("description", point.description);
        cv.put("latitude", point.latitude);
        cv.put("longitude", point.longitude);
        cv.put("rating", point.rating);
        cv.put("uuid", point.uuid);
        cv.put("markerId", point.markerId);

        db.insertWithOnConflict(Const.DB_CACHED_POINTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public ArrayList<Point> getCachedPoints(int category) {
        return getPoints(category, SCOPE.CACHE);
    }

    public int deletePointByMarkerId(long id) {
        SQLiteDatabase db = getReadableDatabase();
        int n = db.delete(Const.DB_POINTS, "markerId=" + id, null);
        if (n > 0)
            return n;
        else
            return db.delete(Const.DB_CACHED_POINTS, "markerId=" + id, null);
    }

    public int deleteCachedPoint(int id) {
        SQLiteDatabase db = getReadableDatabase();
        return db.delete(Const.DB_CACHED_POINTS, "_id=" + id, null);
    }

    public int deletePoint(int id) {
        SQLiteDatabase db = getReadableDatabase();
        return db.delete(Const.DB_POINTS, "_id=" + id, null);
    }


    public void deleteAllPoints() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Const.DB_POINTS, null, null);
    }
    /**
     * Storing API's categories
     */
    public String getCategoryName(int categoryId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(true, Const.DB_CATEGORIES, null, "_id=" + categoryId, null, null, null, null, null);
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
        db.insertWithOnConflict(Const.DB_CATEGORIES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void addCategories(ArrayList<Category> categories) {
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            this.addCategory(category.id, category.getAllNames(), category.getAllDescriptions(), category.urlIcon);
        }
    }

    public ArrayList<Category> getCategories() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(true, Const.DB_CATEGORIES, null, null, null, null, null, null, null);


        if (cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndex("_id");
            int indexName = cursor.getColumnIndex("name");
            int indexDescription = cursor.getColumnIndex("description");
            int indexUrl = cursor.getColumnIndex("iconurl");

            ArrayList<Category> list = new ArrayList<Category>();
            do {
                Category category = new Category();
                category.id = cursor.getInt(indexId);
                category.setName(cursor.getString(indexName));
                category.setDescription(cursor.getString(indexDescription));
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
     * Storing API's POINT
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

    public void addPoint(int categoryId, String name, String url, String access, long time, String description,
                         String latitude, String longitude, float rating, String uuid, long markerId) {
        addPoint(categoryId, name, url, access, time + "", description, Float.parseFloat(latitude), Float.parseFloat(longitude), rating, uuid, markerId);
    }

    public void addPoint(int categoryId, String name, String url, String access, String time, String description,
                         double latitude, double longitude, float rating, String uuid, long markerId) {
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

        db.insertWithOnConflict(Const.DB_POINTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void addPoints(ArrayList<Point> points) {
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);

            // TODO: convert time
            this.addPoint(point.categoryId, point.name, point.url, point.access, "0",
                    point.description, point.latitude, point.longitude, point.rating,
                    point.uuid, point.markerId);
        }
    }


    public ArrayList<Point> getAllPoints(int categoryId) {
        ArrayList<Point> points1 = getPoints(categoryId, SCOPE.TEMPORARY);
        ArrayList<Point> points2 = getPoints(categoryId, SCOPE.CACHE);
        if (points1 != null && points2 != null) {
            points1.addAll(points2);
            return points1;
        } else {
            if (points1 == null && points2 == null)
                return null;
            else
                if (points1 == null)
                    return points2;
                else
                    return points1;
        }
    }

    public ArrayList<Point> getPoints(int categoryId, SCOPE scope) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor;
        String dbName;

        if (scope == SCOPE.TEMPORARY)
            dbName = Const.DB_POINTS;
        else
            dbName = Const.DB_CACHED_POINTS;

        if (categoryId == Const.ALL_CATEGORIES)
            cursor = db.query(true, dbName, null, null, null, null, null, null, null);
        else
            cursor = db.query(true, dbName, null, "categoryId = " + categoryId, null, null, null, null, null);

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

    public Point getPointByMarkerId(long id) {
        Point point = getPointByMarkerIdInScope(id, SCOPE.TEMPORARY);
        if (point != null)
            return point;
        else
            return getPointByMarkerIdInScope(id, SCOPE.CACHE);
    }

    private Point getPointByMarkerIdInScope(long id, SCOPE scope) {
        SQLiteDatabase db = getReadableDatabase();
        String dbName;
        if (scope == SCOPE.TEMPORARY)
            dbName = Const.DB_POINTS;
        else
            dbName = Const.DB_CACHED_POINTS;

        Cursor cursor = db.query(true, dbName, null, "markerId = " + id, null, null, null, null, null);

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
