package org.fruct.oss.getssupplement.Api;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.widget.ProgressBar;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.IconHolder;
import org.fruct.oss.getssupplement.MainActivity;
import org.fruct.oss.getssupplement.Model.Point;
import org.fruct.oss.getssupplement.Model.PointsResponse;
import org.fruct.oss.getssupplement.R;
import org.fruct.oss.getssupplement.Settings;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yaroslav21 on 12.10.16.
 */
public class WaitForLoad extends AsyncTask<Void, Void, Void> {

    //private MapView mMapView;

    private MapboxMap mapBoxMap;
    private Resources getRes;
    private GetsDbHelper dbHelper;
    private Activity mapActivity;

    public void setParams(MapboxMap m, Resources r, GetsDbHelper db, Activity a) {
        mapBoxMap = m;
        getRes = r;
        dbHelper = db;
        mapActivity = a;
    }

    public MarkerOptions addMarker(Point point) {
        //Marker marker = new Marker(mMapView, point.name, "", new LatLng(point.latitude, point.longitude));
/*
        Drawable drawableImage = IconHolder.getInstance().getDrawableByCategoryId(getRes, point.categoryId);

        // TODO: separating based on (un)publishing


        if (point.access == null || point.access.indexOf("w") != -1) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation((float) 0.3);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            drawableImage.setColorFilter(filter);
        }


        marker.setIcon(new Icon(drawableImage));

        marker.setRelatedObject(point);
*/
        Drawable drawableImage = IconHolder.getInstance().getDrawableByCategoryId(getRes, point.categoryId);
        IconFactory iconFactory = IconFactory.getInstance(mapActivity); //this);
        // TODO: separating based on (un)publishing
        if (point.access == null || point.access.contains("w")) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation((float) 0.3);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            drawableImage.setColorFilter(filter);
        }

        Icon icon = null;

        //if (1 == 0)//!drawableImage.equals(null))
            icon = iconFactory.fromDrawable(drawableImage);

        //Marker marker = mapBoxMap.addMarker(new MarkerOptions().position(new LatLng(point.latitude, point.longitude)).icon(icon));
        MarkerOptions marker = new MarkerOptions().position(new LatLng(point.latitude, point.longitude)).icon(icon);
        //point.markerId = marker.getId();
        return marker;
    }


    @Override
    public Void doInBackground(Void... params) {

        int coll = 0;

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            coll++;
            System.out.print("ПРОВЕРКА GET POINTS\n");
            if (MainActivity.getPointsArray() != null) {
                break;
            }
            if (coll > 100) {
                break;
            }
        }
        System.out.print("Успешно скачаны точки!\n");

        coll = 0;
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            coll++;
            System.out.print("ПРОВЕРКА GET CATEGORY\n");
            if (MainActivity.getCategoryArray() != null) {
                break;
            }
            if (coll > 100) {
                break;
            }
        }
        System.out.print("Успешно скачаны категории!\n");

        /*switch (params[0]) {
            case 0:
                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (MainActivity.getPointsArray() != null) {
                        break;
                    }
                }
                break;

            case 1:
                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (MainActivity.getCategoryArray() != null) {
                        break;
                    }
                }
                break;

            default:
                break;
        }*/
        return null;
    }

    @Override
    public void onPostExecute(Void param) {
        //progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        //progressBar.setVisibility(ProgressBar.VISIBLE);

        ArrayList<MarkerOptions> markers = new ArrayList<>();
        ArrayList<Point> points = dbHelper.getPoints(-1);
        if (points != null) {
            for (Point point : points) {
                if (Settings.getIsChecked(mapActivity.getApplicationContext(), point.categoryId))
                    markers.add(this.addMarker(point));
            }
        }


        mapBoxMap.addMarkers(markers);

        //progressBar.setVisibility(ProgressBar.INVISIBLE);

    }
}
