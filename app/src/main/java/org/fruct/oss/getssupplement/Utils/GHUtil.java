package org.fruct.oss.getssupplement.Utils;

import android.content.Context;
import android.widget.Toast;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.BikeFlagEncoder;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.FootFlagEncoder;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.PointList;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.fruct.oss.getssupplement.Settings;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Andrey on 27.03.2016.
 * GraphHopper helper
 */
public class GHUtil {

    private GraphHopper gh;
    private QueryResult qr;
    public String closestEdge;
    public int closestStreetId;

    public GHUtil(String path) throws IllegalStateException {

        GraphHopper gh = new GraphHopper().forMobile();
        gh.setEncodingManager(new EncodingManager(new ArrayList<FlagEncoder>(4) {{
            add(new FootFlagEncoder());
            add(new FootPriorityFlagEncoder());
        }}, 8));
        gh.setCHEnable(false);
        if (!gh.load(path)) {
           gh = null;
        }
        this.gh = gh;
    }

    /**
     * Get closest point on the graph which need magnet to
     * Use before getClosestStreet()!
     */
    public LatLng getClosestPoint(LatLng point) {

        if (point == null)
            return null;

        LatLng magnetPoint;
        LocationIndex index = gh.getLocationIndex();
        this.qr = index.findClosest(point.getLatitude(), point.getLongitude(), EdgeFilter.ALL_EDGES);

        if (!qr.isValid()) {
            System.out.println("Точки не найдены!!!");
            return null;
        }

        PointList pointList = qr.getClosestEdge().fetchWayGeometry(3);
        System.out.println(pointList);
        ArrayList<LatLng> wayPoints = new ArrayList<LatLng>();
        double minDist = Double.MAX_VALUE;
        LatLng closestPoint = new LatLng(point);

        for (int i = 1; i < pointList.size(); i++)
        {
            LatLng basePoint = new LatLng(pointList.getLat(i - 1), pointList.getLon(i - 1));
            LatLng adjPoint = new LatLng(pointList.getLat(i), pointList.getLon(i));
            LatLng currClosestPoint = GeoUtil.pointProject(point, basePoint, adjPoint);

            if (point.distanceTo(currClosestPoint) < minDist)
                closestPoint = currClosestPoint;
        }
        this.closestEdge = qr.getClosestEdge().getName();
        this.closestStreetId = qr.getClosestEdge().getEdge();
        return closestPoint;
    }

    /**
     * Don't use before getClosestStreet()
     */
    public String getClosestStreet() {
        return closestEdge;
    }

    public int getClosestStreetId() {
        return closestStreetId;
    }

    public GraphHopper getGH() { return gh; }

    public static void downloadGHMap(String url, String name, Context context) throws IOException {

        String fileName = Settings.getStorageDir(context) + "/" + name;
        URL link = new URL(url);

        InputStream in = new BufferedInputStream(link.openStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;
        while (-1!=(n=in.read(buf)))
        {
            out.write(buf, 0, n);
        }
        out.close();
        in.close();
        byte[] response = out.toByteArray();

        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(response);
        fos.close();
    }
}
