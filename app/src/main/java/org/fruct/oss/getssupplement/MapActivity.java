package org.fruct.oss.getssupplement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;

import org.fruct.oss.getssupplement.Api.CategoriesGet;
import org.fruct.oss.getssupplement.Api.PointsAdd;
import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.CategoriesResponse;
import org.fruct.oss.getssupplement.Model.DatabaseType;
import org.fruct.oss.getssupplement.Api.PointsGet;
import org.fruct.oss.getssupplement.Model.PointsResponse;
import org.fruct.oss.getssupplement.Model.Point;

public class MapActivity extends Activity implements LocationListener{

    static Context context;
    public MapView mMapView;
    public static Location getLocation() {
        return sLocation;
    }

    private static void setLocation(Location sLocation) {
        MapActivity.sLocation = sLocation;
    }

    private static Location sLocation;


    public Marker getCurrentSelectedMarker() {
        return currentSelectedMarker;
    }

    public void setCurrentSelectedMarker(Marker currentSelectedMarker) {
        this.currentSelectedMarker = currentSelectedMarker;
    }


    Marker currentSelectedMarker = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        setUpLocation();
        context = getApplicationContext();
        mMapView = (MapView) findViewById(R.id.activity_map_mapview);

        setUpMapView();

        if (!isAuthorized()) {
            if(isInternetConnectionAvailable()) {
                Intent i = new Intent(this, LoginActivity.class);
                startActivityForResult(i, Const.INTENT_RESULT_TOKEN);
            }
            else
            {
                Toast.makeText(getApplicationContext(), getString(R.string.network_error_authorization), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Log.d(Const.TAG, "Authorized, downloading categories");
            loadPoints();
        }
    }

    public static boolean isAuthorized() {
        return Settings.getToken(context) != null;
    }
    private void setUpMapView() {
        mMapView.setClickable(true);
        mMapView.getController().setZoom(17);
        mMapView.setUseDataConnection(true);
        mMapView.setUserLocationEnabled(true);
        mMapView.setDiskCacheEnabled(true);

        findViewById(R.id.activity_map_my_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getLocation() == null)
                    return;

                mMapView.getController().setZoomAnimated(19,
                        new LatLng(
                                getLocation().getLatitude(),
                                getLocation().getLongitude()),
                        true,
                        false
                );
            }
        });

        findViewById(R.id.activity_map_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMapView.clear();
                loadPoints();
            }
        });

        if (sLocation != null)
            mMapView.getController().setCenter(new LatLng(getLocation().getLatitude(), getLocation().getLongitude()));
        else
            mMapView.getController().setZoom(3);

        hideBottomPanel();

        mMapView.setMapViewListener(new MapViewListener() {
            @Override
            public void onShowMarker(MapView mapView, Marker marker) {

            }

            @Override
            public void onHideMarker(MapView mapView, Marker marker) {

            }

            @Override
            public void onTapMarker(MapView mapView, Marker marker) {

                setCurrentSelectedMarker(marker);

                marker.getToolTip(mapView).getView().setVisibility(View.GONE);
                Point point = (Point) marker.getRelatedObject();
                Log.d(Const.TAG, "Marker clicked: " + point.name);

                setBottomPanelData(point); // TODO: description

            }

            @Override
            public void onLongPressMarker(MapView mapView, Marker marker) {

            }

            @Override
            public void onTapMap(MapView mapView, ILatLng iLatLng) {
                clearBottomPanelData();
            }

            @Override
            public void onLongPressMap(MapView mapView, ILatLng iLatLng) {

            }
        });

    }


    private void setUpLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationProvider gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
        LocationProvider networkProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);

        if (gpsProvider != null) {
            locationManager.requestLocationUpdates(gpsProvider.getName(), 1000, 50, this);
            Location gpsLocation = locationManager.getLastKnownLocation(gpsProvider.getName());
            // If gps isn't connected yet, try to obtain network location
            if (gpsLocation == null)
                setLocation(locationManager.getLastKnownLocation(networkProvider.getName()));
            return;
        }

        if (networkProvider != null) {
            locationManager.requestLocationUpdates(networkProvider.getName(), 1000, 100, this);
            setLocation(locationManager.getLastKnownLocation(networkProvider.getName()));
            return;
        }

        Toast.makeText(this, "Can't determine location", Toast.LENGTH_SHORT).show();
    }
    private void loadPoints() {

        if (getLocation() == null) {
            Log.e(Const.TAG, "Locations is null");
            return;
        }
        final PointsGet pointsGet = new PointsGet(Settings.getToken(getApplicationContext()),
                getLocation().getLatitude(), getLocation().getLongitude(), Const.API_POINTS_RADIUS) {

            @Override
            public void onPostExecute(final PointsResponse response) {

                for (Point point : response.points) {
                    Marker marker = new Marker(mMapView, point.name, "", new LatLng(point.latitude, point.longitude));
                    marker.setIcon(new Icon(IconHolder.getInstance().getDrawableByCategoryId(getResources(), point.categoryId)));
                    marker.setRelatedObject(point);
                    mMapView.addMarker(marker);
                }
                Toast.makeText(getApplicationContext(),  getString(R.string.succesful_download), Toast.LENGTH_SHORT).show();
            }

        };


        CategoriesGet categoriesGet = new CategoriesGet(Settings.getToken(getApplicationContext())) {
            @Override
            public void onPostExecute(CategoriesResponse response) {
                if (response == null)
                    return;

                GetsDbHelper dbHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.DATA_FROM_API);
                dbHelper.addCategories(response.categories);
                pointsGet.execute();
            }
        };

        categoriesGet.execute();

    }

    RelativeLayout rlBottomPanel = null;
    TextView tvBottomPanelName = null;
    TextView tvBottomPanelDescription = null;
    ImageView ivBottomPanelIcon = null;
    View viGradient = null;


    private void initBottomPanel(){
        if (rlBottomPanel == null)
            rlBottomPanel = (RelativeLayout) findViewById(R.id.activity_map_bottom_panel);
        else
            return;

        if (tvBottomPanelName == null)
            tvBottomPanelName = (TextView) findViewById(R.id.acitivity_map_point_name);

        if (tvBottomPanelDescription == null)
            tvBottomPanelDescription = (TextView) findViewById(R.id.acitivity_map_point_description);

        if (ivBottomPanelIcon == null)
            ivBottomPanelIcon = (ImageView) findViewById(R.id.activity_map_bottom_panel_icon);

        if (viGradient == null)
            viGradient = findViewById(R.id.activity_map_bottom_panel_gradient);
    }

    private void setBottomPanelData(final Point point) {

        initBottomPanel();

        tvBottomPanelName.setText(point.name);

        String descriptionText = "";

        if (point.description != null && !point.description.equals(""))
            descriptionText += point.description + "\n";

        if (point.rating != 0)
            descriptionText += getString(R.string.rating) + point.rating;

        if (!descriptionText.equals("")) {
            tvBottomPanelDescription.setText(descriptionText.trim());
            tvBottomPanelDescription.setVisibility(View.VISIBLE);
        } else {
            tvBottomPanelDescription.setVisibility(View.INVISIBLE);
        }

        if (IconHolder.getInstance().getDrawableByCategoryId(getResources(), point.categoryId) != null)
            ivBottomPanelIcon.setImageDrawable(IconHolder.getInstance().getDrawableByCategoryId(getResources(), point.categoryId));


        Log.d(Const.TAG + " marker clicked ", point.name + " " + point.description);

        Log.d(Const.TAG, point.uuid + "  = uuid");
        if (!isBottomPanelShowed())
            showBottomPanel();

    }

    private void hideBottomPanel() {
        initBottomPanel();

        Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rlBottomPanel.setVisibility(View.INVISIBLE);
                viGradient.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        if (rlBottomPanel == null) {
            rlBottomPanel = (RelativeLayout) findViewById(R.id.activity_map_bottom_panel);
            viGradient = findViewById(R.id.activity_map_bottom_panel_gradient);
        }

        rlBottomPanel.setAnimation(fadeOut);
        viGradient.setAnimation(fadeOut);

    }

    private void showBottomPanel() {

        initBottomPanel();

        Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rlBottomPanel.setVisibility(View.VISIBLE);
                viGradient.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        rlBottomPanel.setAnimation(fadeOut);
        viGradient.setAnimation(fadeOut);
        rlBottomPanel.setVisibility(View.VISIBLE);

    }

    private boolean isBottomPanelShowed(){
        return rlBottomPanel.getVisibility() == View.VISIBLE;
    }

    private void clearBottomPanelData() {
        //setBottomPanelData("", "", null);
        initBottomPanel();
        hideBottomPanel();
    }

    public static boolean isInternetConnectionAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
            return true;

        return false;
    }

    private void addMarker(Point point){
        Marker marker = new Marker(mMapView, point.name, "", new LatLng(point.latitude, point.longitude));
        marker.setIcon(new Icon(IconHolder.getInstance().getDrawableByCategoryId(getResources(), point.categoryId)));
        marker.setRelatedObject(point);
        mMapView.addMarker(marker);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            Intent intent = new Intent(this, AddNewPointActivity.class);
            intent.putExtra("zoomLevel", mMapView.getZoomLevel());
            startActivityForResult(intent, Const.INTENT_RESULT_NEW_POINT);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Const.INTENT_RESULT_CODE_OK) {
            return;
        }


        if (requestCode == Const.INTENT_RESULT_NEW_POINT) {
            String pointName = data.getStringExtra("name");
            String pointUrl = data.getStringExtra("url");
            float rating = data.getFloatExtra("rating", 0f);
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            int pointCategory = data.getIntExtra("category", 0);


            // Save to local database when no Internet connection
            if (!isInternetConnectionAvailable()) {
                GetsDbHelper dbHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.USER_GENERATED);
                dbHelper.addPoint(pointName,
                        pointUrl,
                        "?",
                        System.currentTimeMillis() + "",
                        latitude,
                        longitude,
                        rating);

                Toast.makeText(getApplicationContext(), getString(R.string.saved_to_local_db), Toast.LENGTH_SHORT).show();
            } else {
                // Send to API
                PointsAdd pointsAdd = new PointsAdd(Settings.getToken(getApplicationContext()),
                        pointCategory,
                        pointName,
                        rating,
                        latitude,
                        longitude,
                        System.currentTimeMillis()
                ) {
                    @Override
                    public void onPostExecute(PointsResponse response) {
                        if (response.code == 0) { // FIXME: codes
                            Toast.makeText(getApplicationContext(), getString(R.string.successuflly_sent), Toast.LENGTH_SHORT).show();
                        } else {
                            // TODO
                        }

                    }
                };

                pointsAdd.execute();
            }

            // Finally, add marker to the map (it is not in array of APIs markers yet, it will be after next sync
            Point point = new Point();
            point.name = pointName;
            point.url = pointUrl;
            point.latitude = latitude;
            point.longitude = longitude;
            point.rating = rating;
            point.categoryId = pointCategory;

            mMapView.getController().animateTo(
                    new LatLng(
                            latitude,
                            longitude,
                            16)
            );

            addMarker(point);
        }

        if (requestCode == Const.INTENT_RESULT_TOKEN) {
            // Save token
            Settings.saveString(getApplicationContext(), Const.PREFS_AUTH_TOKEN, data.getStringExtra("token"));
            loadPoints();
        }
    }
    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onLocationChanged(Location location) {
        sLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
