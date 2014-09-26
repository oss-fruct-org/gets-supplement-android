package oss.fruct.org.getssupplement.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import oss.fruct.org.getssupplement.Const;
import oss.fruct.org.getssupplement.Model.Category;
import oss.fruct.org.getssupplement.Model.DatabaseType;
import oss.fruct.org.getssupplement.Model.Point;

/**
 * Created by alexander on 05.09.14.
 */
public class GetsDbHelper extends SQLiteOpenHelper {

    // Prefix is used to define Db type: internal Db for storing GeTS data or
    // internal Db for storing temporary data that should be uploaded to remote server
    private DatabaseType databaseType;

    public GetsDbHelper(Context context, DatabaseType _databaseType) {
        super(context, getDatabasePrefix(_databaseType) + Const.DB_INTERNAL_NAME, null, 1);
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
                        "longitude real" +
                        ");"
        );

        db.execSQL("create table " + Const.DB_INTERNAL_CATEGORIES + "(" +
                        "_id integer primary key," + // Unique id, connected with API
                        "name text," +
                        "description text," +
                        "url text" +
                        ");"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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
    public void addCategory(int id, String name, String description, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("_id", id);
        cv.put("name", name);
        cv.put("description", description);
        cv.put("url", url);

        db.insert(Const.DB_INTERNAL_CATEGORIES, null, cv);

        db.close();
    }

    public void addCategories(ArrayList<Category> categories) {
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            this.addCategory(category.id, category.name, category.description, category.url);
        }

    }

    public ArrayList<Category> getCategories() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(true, Const.DB_INTERNAL_CATEGORIES, null, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndex("_id");
            int indexName = cursor.getColumnIndex("name");
            int indexDescription = cursor.getColumnIndex("description");
            int indexUrl = cursor.getColumnIndex("url");

            ArrayList<Category> list = new ArrayList<Category>();
            do {
                Category category = new Category();
                category.id = cursor.getInt(indexId);
                category.name = cursor.getString(indexName);
                category.description = cursor.getString(indexDescription);
                category.url = cursor.getString(indexUrl);
            } while (cursor.moveToNext());

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
    public void addPoint(String name, String url, String access, String time, String latitude, String longitude) {
        addPoint(name, url, access, time, Float.parseFloat(latitude), Float.parseFloat(longitude));
    }

    public void addPoint(String name, String url, String access, String time, float latitude, float longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("name", name);
        cv.put("url", url);
        cv.put("access", access);
        cv.put("time", time); // TODO: normalize time
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);

        db.insert(Const.DB_INTERNAL_POINTS, null, cv);

        db.close();
    }

    public void addPoints(ArrayList<Point> points) {
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            this.addPoint(point.name, point.url, point.access, point.time, point.latitude, point.longitude);
        }

    }

    public ArrayList<Point> getPoints() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(true, Const.DB_INTERNAL_POINTS, null, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndex("_id");
            int indexName = cursor.getColumnIndex("name");
            int indexAccess = cursor.getColumnIndex("access");
            int indexTime = cursor.getColumnIndex("time");
            int indexLatitude = cursor.getColumnIndex("latitude");
            int indexLongitude = cursor.getColumnIndex("longitude");
            int indexUrl = cursor.getColumnIndex("url");

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
            } while (cursor.moveToNext());

            db.close();
            return list;
        }

        db.close();
        return null;
    }

}
