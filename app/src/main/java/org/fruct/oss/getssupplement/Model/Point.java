package org.fruct.oss.getssupplement.Model;

/**
 * Created by Andrey on 19.07.2015.
 */
public class Point {
    public int id;
    public int categoryId;
    public String name;
    public String description;
    public String url;
    public String access;
    public String time;
    public double latitude;
    public double longitude;
    public float rating;
    public String uuid;

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
}