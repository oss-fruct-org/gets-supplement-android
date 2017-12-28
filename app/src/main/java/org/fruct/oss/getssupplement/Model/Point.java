package org.fruct.oss.getssupplement.Model;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Andrey on 19.07.2015.
 */
public class Point {
    private static String TAG = "Point";

    public int id;
    public int categoryId;
    private String name;
    private Map<String, String> localNames = null;
    public String description;
    public String url;
    public String access;
    public String time;
    public double latitude;
    public double longitude;
    public float rating;
    public String uuid;
    public long markerId = -1;
    public int streetId;

    public Point() {
    }

    public Point(int id, String name, String url, String access, String time, double latitude, double longitude, float rating, String uuid) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.access = access;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.uuid = uuid;
    }

    public String getName() {
        if (localNames == null) {
            localNames = new HashMap<>();
            try {
                JSONObject json = new JSONObject(name);
                Iterator<String> temp = json.keys();
                while (temp.hasNext()) {
                    String key = temp.next();
                    localNames.put(key, json.get(key).toString());
                }
            } catch (Exception e) {
                //Log.d(TAG, "Name: " + name + " not parsed: " + e.getMessage());
                localNames.put("name", name);
            }
        }

        if (localNames.containsKey("name_" + Locale.getDefault().getLanguage()))
            return localNames.get("name_" + Locale.getDefault().getLanguage());
        else
            return localNames.get("name");
    }

    public void setName(String name) {
        this.name = name;
        localNames = null;
    }
}