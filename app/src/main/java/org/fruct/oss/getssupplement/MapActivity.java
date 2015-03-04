package org.fruct.oss.getssupplement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;
import com.squareup.okhttp.internal.http.Response;

import org.fruct.oss.getssupplement.Api.CategoriesGet;
import org.fruct.oss.getssupplement.Api.PointsAdd;
import org.fruct.oss.getssupplement.Api.PointsDelete;
import org.fruct.oss.getssupplement.Api.PointsGet;
import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.BasicResponse;
import org.fruct.oss.getssupplement.Model.CategoriesResponse;
import org.fruct.oss.getssupplement.Model.DatabaseType;
import org.fruct.oss.getssupplement.Model.Point;
import org.fruct.oss.getssupplement.Model.PointsResponse;

import java.util.ArrayList;



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

        context = getApplicationContext();

        setUpLocation();

        Log.d(Const.TAG + "token", "_" + Settings.getToken(getApplicationContext()) + "_");

        if (!isAutuhorized()) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, Const.INTENT_RESULT_TOKEN);
            // TODO: make intent for result (load points after authorization)
        } else {
            Log.d(Const.TAG, "Authorized, downloading categories");
            loadPoints();
        }

        mMapView = (MapView) findViewById(R.id.acitivity_map_mapview);
        setUpMapView();
    }

    private void loadPoints() {

        if (getLocation() == null) {
            Log.e(Const.TAG, "Locations is null");
            Toast.makeText(this, "Can;t determine location", Toast.LENGTH_SHORT).show();
            return;
        }

        final PointsGet pointsGet = new PointsGet(Settings.getToken(getApplicationContext()),
                getLocation().getLatitude(), getLocation().getLongitude(), Const.API_POINTS_RADIUS) {

            @Override
            public void onPostExecute(final PointsResponse response) {
                // TODO: check for response code
                Log.d(Const.TAG, "Categories has been downloaded");

                new Thread (new Runnable() {
                    @Override
                    public void run() {
                        GetsDbHelper dbHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.DATA_FROM_API);
                        dbHelper.addPoints(response.points);
                    }
                }).run();

                Log.d(Const.TAG, "Points array size: " + response.points.size());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //ArrayList<Marker> markers = new ArrayList<Marker>();

                        // Add marker through 'low level' style
                        for (Point point : response.points) {
                            Marker marker = new Marker(mMapView, point.name, "", new LatLng(point.latitude, point.longitude));
                            marker.setIcon(new Icon(getApplicationContext(), Icon.Size.LARGE, "marker-stroked", "000000")); // TODO: marker appearance
                            marker.setRelatedObject(point);
                            //markers.add(marker);
                            mMapView.addMarker(marker);
                        }

                    }
                }).run();

            }

        };


        CategoriesGet categoriesGet = new CategoriesGet(Settings.getToken(getApplicationContext())) {
            @Override
            public void onPostExecute(CategoriesResponse response) {
                GetsDbHelper dbHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.DATA_FROM_API);
                dbHelper.addCategories(response.categories);
                Log.d(Const.TAG, "Categories has been downloaded");

                pointsGet.execute();
            }
        };

        categoriesGet.execute();
    }

    private void setUpMapView() {
        mMapView.setClickable(true);
        mMapView.getController().setZoom(17);
        mMapView.setUseDataConnection(true);
        mMapView.setUserLocationEnabled(true);

        findViewById(R.id.acitivity_map_my_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getLocation() == null)
                    return;

                mMapView.getController().animateTo(
                        new LatLng(
                                getLocation().getLatitude(),
                                getLocation().getLongitude())
                );
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
            public void onHidemarker(MapView mapView, Marker marker) {

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


    RelativeLayout rlBottomPanel = null;
    TextView tvBottomPanelName = null;
    TextView tvBottomPanelDescription = null;
    ImageButton ibBottomPanelUrl = null;
    ImageButton ibBottomPanelMore = null;
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

        if (ibBottomPanelUrl == null)
            ibBottomPanelUrl = (ImageButton) findViewById(R.id.activity_map_point_url);

        if (ibBottomPanelMore == null)
            ibBottomPanelMore = (ImageButton) findViewById(R.id.activity_map_point_more);

        if (viGradient == null)
            viGradient = findViewById(R.id.activity_map_bottom_panel_gradient);
    }

    private void clearBottomPanelData() {
        //setBottomPanelData("", "", null);
        initBottomPanel();
        hideBottomPanel();
    }

    private void setBottomPanelData(final Point point) {

        initBottomPanel();

        tvBottomPanelName.setText(point.name);

        if (point.rating != 0) {
            tvBottomPanelDescription.setText(getString(R.string.rating) + point.rating);
            tvBottomPanelDescription.setVisibility(View.VISIBLE);
        } else {
            tvBottomPanelDescription.setVisibility(View.GONE);
        }

        Log.d(Const.TAG + " marker clicked ", point.url + " ");
        if (point.url != null && !point.url.replace(" ", "").equals("")) {
            ibBottomPanelUrl.setVisibility(View.VISIBLE);
            ibBottomPanelUrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i = new Intent(Intent.ACTION_VIEW);

                    if (!point.url.startsWith("http://") && !point.url.startsWith("https://"))
                        i.setData(Uri.parse("http://" + point.url));

                    else
                        i.setData(Uri.parse(point.url));

                    Log.d(Const.TAG, "Marker url: _" + point.url + "_");

                    startActivity(i);
                }
            });
        } else {
            ibBottomPanelUrl.setVisibility(View.INVISIBLE);
        }

        final PopupMenu popupMenu = new PopupMenu(getApplicationContext(), ibBottomPanelMore);
        popupMenu.getMenuInflater().inflate(R.menu.map_point_more, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                PointsDelete pointsDelete = new PointsDelete(Settings.getToken(getApplicationContext()),
                        0, // FIXME: point has no categoryId
                        point.uuid
                        ) {
                    @Override
                    public void onPostExecute(BasicResponse response) {
                        //if (response.code == 0) // TODO
                        deleteMarker(getCurrentSelectedMarker());
                        Toast.makeText(getApplicationContext(), "TODO: delete marker from API", Toast.LENGTH_SHORT).show();
                    }
                };

                pointsDelete.execute();

                return false;
            }
        });

        ibBottomPanelMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });

        Log.d(Const.TAG, point.uuid + "  = uuid");

        if (point.uuid == null) {
            ibBottomPanelMore.setVisibility(View.INVISIBLE);
        } else
            ibBottomPanelMore.setVisibility(View.VISIBLE);


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


    private void addMarker(Point point){//(LatLng position, String title) {
        //Log.d(Const.TAG, "addMarker");
        Marker marker = new Marker(mMapView, point.name, "", new LatLng(point.latitude, point.longitude));
        marker.setIcon(new Icon(getApplicationContext(), Icon.Size.LARGE, "marker-stroked", "000000")); // TODO: marker appearance
        marker.setRelatedObject(point);
        addMarkerLowLevel(marker);
    }

    private void addMarkerLowLevel(Marker marker) {
        mMapView.addMarker(marker);
    }

    private void deleteMarker(Marker marker) {
        if (marker != null) {
            mMapView.removeMarker(marker);
            marker.closeToolTip();
            hideBottomPanel();
        }

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
            startActivityForResult(intent, Const.INTENT_RESULT_NEW_POINT);
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Const.INTENT_RESULT_CODE_OK) {
            // TODO
            return;
        }

        // Do actions when new point created
        if (requestCode == Const.INTENT_RESULT_NEW_POINT) {
            String pointName = data.getStringExtra("name");
            //String pointDescription = data.getStringExtra("description");
            String pointUrl = data.getStringExtra("url");
            float rating = data.getFloatExtra("rating", 0f);

            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            //double altitude = data.getDoubleExtra("altitude", 0);

            int pointCategory = data.getIntExtra("category", 0);

            Log.d(Const.TAG, "Intent result point: " + pointName + rating);

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
                        //pointDescription,
                        //pointUrl,
                        latitude,
                        longitude,
                        //altitude,
                        System.currentTimeMillis()
                ) {
                    @Override
                    public void onPostExecute(PointsResponse response) {
                        if (response.code == 0) { // FIXME: codes
                            Toast.makeText(getApplicationContext(), "Successfully sent to server", Toast.LENGTH_SHORT).show();
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


    public static boolean isInternetConnectionAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
            return true;

        return false;
    }


    @Override
    public void onLocationChanged(Location location) {
        sLocation = location;
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
