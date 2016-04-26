package org.fruct.oss.getssupplement.Utils;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.fruct.oss.getssupplement.Model.PointXY;

/**
 * Created by Andrey on 29.03.2016.
 */
public class GeoUtil {
    private static final int EARTH_RADIUS = 6371;

    public LatLng pointProjection () {
        return new LatLng(0,0);
    }


    public static PointXY mercatorProject(LatLng point) {
        // lng as lambda, lat as phi
        double radlat = point.getLatitude() * Math.PI / 180;
        double radlng = point.getLongitude() * Math.PI / 180;
        return new PointXY(radlng, Math.log(Math.tan(Math.PI/4 + radlat/2)));
    }

    public static LatLng projectInverse(double x, double y) {
        double radlat = 2 * Math.atan(Math.exp(y)) - Math.PI/2;
        double deglat = radlat * 180 / Math.PI;
        double deglng = x * 180 / Math.PI;
        return new LatLng(deglat, deglng);
    }

    // Point projection on the line
    public static LatLng pointProject(LatLng projected, LatLng point1, LatLng point2) {
        PointXY mercProjected = mercatorProject(projected);
        PointXY mercPoint1 = mercatorProject(point1);
        PointXY mercPoint2 = mercatorProject(point2);

        double x1 = mercPoint1.x;
        double y1 = mercPoint1.y;
        double x2 = mercPoint2.x;
        double y2 = mercPoint2.y;
        double x3 = mercProjected.x;
        double y3 = mercProjected.y;
        double x4, y4;

        x4 = ((x2 - x1) * (y2 - y1) * (y3 - y1) + x1 * Math.pow(y2 - y1, 2) + x3 * Math.pow(x2 - x1, 2)) / (Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
        y4 = (y2 - y1) * (x4 - x1) / (x2 - x1) + y1;
        return projectInverse(x4, y4);
    }
}
