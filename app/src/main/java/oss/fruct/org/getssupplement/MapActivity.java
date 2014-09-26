package oss.fruct.org.getssupplement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

import oss.fruct.org.getssupplement.Api.CategoriesGet;
import oss.fruct.org.getssupplement.Database.GetsDbHelper;
import oss.fruct.org.getssupplement.Model.CategoriesResponse;
import oss.fruct.org.getssupplement.Model.DatabaseType;


public class MapActivity extends Activity implements LocationListener {

    static Context context;

    private MapView mMapView;

    public static Location getLocation() {
        return sLocation;
    }

    private static void setLocation(Location sLocation) {
        MapActivity.sLocation = sLocation;
    }

    private static Location sLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        context = getApplicationContext();

        setUpLocation();

        if (!isAutuhorized()) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        } else {
            CategoriesGet categoriesGet = new CategoriesGet(Settings.getToken(getApplicationContext())) {
                @Override
                public void onPostExecute(CategoriesResponse response) {
                    GetsDbHelper dbHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.DATA_FROM_API);
                    dbHelper.addCategories(response.categories);
                }
            };
            //categoriesGet.execute();
        }

        mMapView = (MapView) findViewById(R.id.acitivity_map_mapview);
        setUpMapView();
    }

    private void setUpMapView() {
        mMapView.setClickable(true);
        mMapView.getController().setZoom(17);
        mMapView.setUseDataConnection(true);
        mMapView.setUserLocationEnabled(true);

        if (sLocation != null)
            mMapView.getController().setCenter(new LatLng(getLocation().getLatitude(), getLocation().getLongitude()));
        else
            mMapView.getController().setZoom(3);

    }

    /**
     * Check if token exists (NOT token validity)
     * @return token existance state
     */
    public static boolean isAutuhorized() {
        Log.d(Const.TAG + " token", Settings.getToken(context) + "  ");
        return Settings.getToken(context) != null;
    }

    private void setUpLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        String provider = locationManager.getBestProvider(criteria, true);

        if (provider == null) {
            return;
        }

        //In order to make sure the device is getting the location, request updates.
        locationManager.requestLocationUpdates(provider, 1, 0, this);
        setLocation(locationManager.getLastKnownLocation(provider));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_add) {
            Intent intent = new Intent(this, AddNewPointActivity.class);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
