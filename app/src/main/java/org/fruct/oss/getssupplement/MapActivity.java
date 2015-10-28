package org.fruct.oss.getssupplement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
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
import org.fruct.oss.getssupplement.Api.PointsDelete;
import org.fruct.oss.getssupplement.Api.PublishChannel;
import org.fruct.oss.getssupplement.Api.UserInfoGet;
import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.BasicResponse;
import org.fruct.oss.getssupplement.Model.CategoriesResponse;
import org.fruct.oss.getssupplement.Model.DatabaseType;
import org.fruct.oss.getssupplement.Api.PointsGet;
import org.fruct.oss.getssupplement.Model.PointsResponse;
import org.fruct.oss.getssupplement.Model.Point;
import org.fruct.oss.getssupplement.Model.UserInfoResponse;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.logging.LogRecord;

public class MapActivity extends Activity implements LocationListener {

    static Context context;

    private Menu menu;

    public MapView mMapView;

    public static Location getLocation() {
        return sLocation;
    }

    private static void setLocation(Location sLocation) {
        MapActivity.sLocation = sLocation;
    }

    private static Location sLocation;

    private boolean followingState;

    private boolean succesLoading = false;

    ProgressBar progressBar;

    private LocationProvider currentProvider = null;

    private LocationManager locationManager;

    private  LocationProvider gpsProvider;

    private LocationProvider networkProvider;

    private Timer mapOffset;

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
        followingState = false;

        setUpLocation();
        context = getApplicationContext();
        mMapView = (MapView) findViewById(R.id.activity_map_mapview);

        setUpMapView();

        if (!isAuthorized()) {
            if (isInternetConnectionAvailable()) {
                Intent i = new Intent(this, LoginActivity.class);
                startActivityForResult(i, Const.INTENT_RESULT_TOKEN);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.network_error_authorization), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Log.d(Const.TAG, "Authorized, downloading categories");
            checkUserStatus();
            loadPoints();
       /*     Timer collisionTimer = new Timer();
            final Handler handler = new Handler();
            collisionTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            selectAvailableProvider();
                            if(succesLoading) {
                                if (currentProvider != null && !followingState)
                                    startFollow();
                                if (currentProvider == null && followingState)
                                    stopFollow();
                            }
                        }
                    });
                }
            }, 100, 6L * 10);*/
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

                ibBottomPanelDelete.setVisibility(View.INVISIBLE);
                ibBottomPanelEdit.setVisibility(View.INVISIBLE);
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



        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
        networkProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);


        if (gpsProvider != null) {
            Location gpsLocation = locationManager.getLastKnownLocation(gpsProvider.getName());
            // If gps isn't connected yet, try to obtain network location
            if (gpsLocation == null)
                setLocation(locationManager.getLastKnownLocation(networkProvider.getName()));
            return;
        }

        if (networkProvider != null) {
            setLocation(locationManager.getLastKnownLocation(networkProvider.getName()));
            return;
        }

        Toast.makeText(this, "Can't determine location", Toast.LENGTH_SHORT).show();
    }

    private void deleteMarker(Marker marker) {
        if (marker != null) {
            mMapView.removeMarker(marker);
            marker.closeToolTip();
            hideBottomPanel();
        }
    }

    private void deletePoint(Point point) {
        PointsDelete pointsDelete = new PointsDelete(Settings.getToken(getApplicationContext()), point) {
            @Override
            protected void onPostExecute(BasicResponse response) {
                super.onPostExecute(response);
                if (response.code == 0)
                    deleteMarker(getCurrentSelectedMarker());
                else
                    Toast.makeText(getApplicationContext(), getString(R.string.error_deleting_point), Toast.LENGTH_SHORT).show();
            }
        };
        pointsDelete.execute();
    }

    private void loadPoints() {

        if (getLocation() == null) {
            Log.e(Const.TAG, "Locations is null");
            return;
        }
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        final PointsGet pointsGet = new PointsGet(Settings.getToken(getApplicationContext()),
                getLocation().getLatitude(), getLocation().getLongitude(), Const.API_POINTS_RADIUS) {

            @Override
            public void onPostExecute(final PointsResponse response) {

                // TODO: do it in new thread
                for (Point point : response.points) {
                    addMarker(point);
                }
                Toast.makeText(getApplicationContext(), getString(R.string.successful_download), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                succesLoading =true;
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

    private void checkUserStatus() {
        // Get user info
        String usrToken = Settings.getToken(getApplicationContext());
        UserInfoGet userInfoGet = new UserInfoGet(usrToken) {
            @Override
            protected void onPostExecute(UserInfoResponse userInfoResponse) {
                super.onPostExecute(userInfoResponse);

                // Save user status
                if (userInfoResponse == null) {
                    Log.d(Const.TAG, "userInfoResponse == null");
                    Settings.saveBoolean(getApplicationContext(), Const.PREFS_IS_TRUSTED_USER, false);
                } else {
                    Log.d(Const.TAG, "Is trusted user: " + userInfoResponse.isTrustedUser);
                    Settings.saveBoolean(getApplicationContext(), Const.PREFS_IS_TRUSTED_USER, true);

                    MenuItem menuItem = menu.findItem(R.id.action_publish);
                    menuItem.setVisible(true);
                    menuItem = menu.findItem(R.id.action_unpublish);
                    menuItem.setVisible(true);
                }
            }
        };

        if (usrToken == null || usrToken.equals(""))
            Settings.saveBoolean(getApplicationContext(), Const.PREFS_IS_TRUSTED_USER, false);
        else userInfoGet.execute();
    }

    RelativeLayout rlBottomPanel = null;
    TextView tvBottomPanelName = null;
    TextView tvBottomPanelDescription = null;
    ImageView ivBottomPanelArrowRight = null;
    ImageView ivBottomPanelIcon = null;
    ImageButton ibBottomPanelDelete = null;
    ImageButton ibBottomPanelEdit = null;
    View viGradient = null;


    private void initBottomPanel() {
        if (rlBottomPanel == null)
            rlBottomPanel = (RelativeLayout) findViewById(R.id.activity_map_bottom_panel);
        else
            return;

        if (tvBottomPanelName == null)
            tvBottomPanelName = (TextView) findViewById(R.id.acitivity_map_point_name);

        if (tvBottomPanelDescription == null)
            tvBottomPanelDescription = (TextView) findViewById(R.id.acitivity_map_point_description);

        if (ibBottomPanelDelete == null)
            ibBottomPanelDelete = (ImageButton) findViewById(R.id.activity_map_point_delete);

        if (ibBottomPanelEdit == null)
            ibBottomPanelEdit = (ImageButton) findViewById(R.id.activity_map_point_edit);

        if (ivBottomPanelIcon == null)
            ivBottomPanelIcon = (ImageView) findViewById(R.id.activity_map_bottom_panel_icon);

        if (viGradient == null)
            viGradient = findViewById(R.id.activity_map_bottom_panel_gradient);

        if (ivBottomPanelArrowRight == null)
            ivBottomPanelArrowRight = (ImageView) findViewById(R.id.acitivity_map_bottom_panel_arrow_right);
    }

    private void setBottomPanelData(final Point point) {

        initBottomPanel();

        tvBottomPanelName.setText(point.name);

        String descriptionText = "";

        if (point.description != null && !point.description.equals("") && !point.description.equals("{}"))
            descriptionText += point.description + "\n";

        if (point.rating != 0)
            descriptionText += getString(R.string.rating) + point.rating;

        if (!descriptionText.equals("")) {
            tvBottomPanelDescription.setText(descriptionText.trim());
            tvBottomPanelDescription.setVisibility(View.VISIBLE);
        } else {
            tvBottomPanelDescription.setVisibility(View.INVISIBLE);
        }

        if (point.access == null || point.access.indexOf("w") != -1) {
            ibBottomPanelDelete.setVisibility(View.VISIBLE);
            ibBottomPanelEdit.setVisibility(View.VISIBLE);
        }
        if (IconHolder.getInstance().getDrawableByCategoryId(getResources(), point.categoryId) != null)
            ivBottomPanelIcon.setImageDrawable(IconHolder.getInstance().getDrawableByCategoryId(getResources(), point.categoryId));

        // TODO: browsing arrows
        // ivBottomPanelArrowRight.setVisibility(View.VISIBLE);

        Log.d(Const.TAG + " marker clicked ", point.name + " " + point.description);

        ibBottomPanelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePoint(point);
            }
        });

        ibBottomPanelEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MapActivity.this, AddNewPointActivity.class);

                intent.putExtra("latitude", point.latitude);
                intent.putExtra("longitude", point.longitude);
                intent.putExtra("name", point.name);
                intent.putExtra("categoryId", point.categoryId);
                intent.putExtra("description", point.description);
                intent.putExtra("rating", point.rating);
                intent.putExtra("uuid", point.uuid);

                intent.putExtra("token", Settings.getToken(getApplicationContext()));
                intent.putExtra("zoomLevel", mMapView.getZoomLevel());
                intent.putExtra("isInEdit", true);

                startActivityForResult(intent, Const.INTENT_RESULT_NEW_POINT);

            }
        });

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
                ibBottomPanelDelete.setVisibility(View.INVISIBLE);
                ibBottomPanelEdit.setVisibility(View.INVISIBLE);
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

    private boolean isBottomPanelShowed() {
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

    private void addMarker(Point point) {
        Marker marker = new Marker(mMapView, point.name, "", new LatLng(point.latitude, point.longitude));

        Drawable drawableImage = IconHolder.getInstance().getDrawableByCategoryId(getResources(), point.categoryId);

        // TODO: separating based on (un)publishing

        /*
        if (point.access == null || point.access.indexOf("w") != -1) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation((float) 0.3);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            drawableImage.setColorFilter(filter);
        }
        */

        marker.setIcon(new Icon(drawableImage));

        marker.setRelatedObject(point);

        mMapView.addMarker(marker);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_map, menu);


        return true;
    }

    private void stopFollow() {
        followingState = false;
        menu.findItem(R.id.follow_location).getIcon().setColorFilter(null);
        setLocation(locationManager.getLastKnownLocation(currentProvider.getName()));
        locationManager.removeUpdates(this);
        mMapView.setMapOrientation(0);
        mapOffset.cancel();
    }

    private void startFollow() {
        followingState = true;
        if (menu.findItem(R.id.follow_location) != null)
            menu.findItem(R.id.follow_location).getIcon().setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);
        setLocation(locationManager.getLastKnownLocation(currentProvider.getName()));
        mMapView.getController().setZoomAnimated(19, new LatLng(getLocation().getLatitude(), getLocation().getLongitude()), true, false);
        locationManager.requestLocationUpdates(currentProvider.getName(), 1000, 50, this);
        mapOffset = new Timer();
        final Handler uiHandler = new Handler();
        mapOffset.schedule(new TimerTask() {
            @Override
            public void run() {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setLocation(locationManager.getLastKnownLocation(currentProvider.getName()));
                        mMapView.getController().setZoomAnimated(19, new LatLng(getLocation().getLatitude(), getLocation().getLongitude()), true, false);
                    }
                });
            }
        }, 0L, 6L * 10);

    }

    private void selectAvailableProvider() {
        if (networkProvider != null)
            currentProvider = networkProvider;
        if (gpsProvider != null) {
            if (locationManager.getLastKnownLocation(gpsProvider.getName()) != null)
                currentProvider = gpsProvider;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.follow_location) {
            selectAvailableProvider();
            if(currentProvider != null) {
                if (followingState)
                    stopFollow();
                else
                    startFollow();
            }
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            Intent intent = new Intent(this, AddNewPointActivity.class);
            intent.putExtra("zoomLevel", mMapView.getZoomLevel());
            startActivityForResult(intent, Const.INTENT_RESULT_NEW_POINT);
        }

        if (id == R.id.activity_map_refresh) {
            mMapView.clear();
            loadPoints();
        }

        if (id == R.id.action_publish) {
            Intent intent = new Intent(this, CategoryActivity.class);
            startActivityForResult(intent, Const.INTENT_RESULT_PUBLISH);
        }

        if (id == R.id.action_unpublish) {
            Intent intent = new Intent(this, CategoryActivity.class);
            startActivityForResult(intent, Const.INTENT_RESULT_UNPUBLISH);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Const.INTENT_RESULT_CODE_OK) {
            return;
        }

        if (requestCode == Const.INTENT_RESULT_PUBLISH) {

            int category = data.getIntExtra("category", 0);

            PublishChannel publishChannel = new PublishChannel(Settings.getToken(getApplicationContext()), category, Const.API_PUBLISH) {
                @Override
                protected void onPostExecute(BasicResponse response) {
                    super.onPostExecute(response);

                    if (response.code == 0)
                        Toast.makeText(getApplicationContext(), getString(R.string.successful_publish), Toast.LENGTH_SHORT).show();
                    else if (response.code == 2)
                        Toast.makeText(getApplicationContext(), getString(R.string.repeated_publish), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), getString(R.string.unsuccessful_publish), Toast.LENGTH_SHORT).show();
                }
            };

            publishChannel.execute();
        }

        if (requestCode == Const.INTENT_RESULT_UNPUBLISH) {

            int category = data.getIntExtra("category", 0);

            PublishChannel publishChannel = new PublishChannel(Settings.getToken(getApplicationContext()), category, Const.API_UNPUBLISH) {
                @Override
                protected void onPostExecute(BasicResponse response) {
                    super.onPostExecute(response);

                    if (response.code == 0)
                        Toast.makeText(getApplicationContext(), getString(R.string.successful_unpublish), Toast.LENGTH_SHORT).show();
                    else if (response.code == 2)
                        Toast.makeText(getApplicationContext(), getString(R.string.repeated_unpublish), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), getString(R.string.unsuccessful_unpublish), Toast.LENGTH_SHORT).show();
                }
            };

            publishChannel.execute();
        }

        if (requestCode == Const.INTENT_RESULT_NEW_POINT) {

            String pointName = data.getStringExtra("name");
            String pointUrl = data.getStringExtra("url");
            float rating = data.getFloatExtra("rating", 0f);
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            int pointCategory = data.getIntExtra("category", 0);

            final String deleteUuid = data.getStringExtra("deleteUuid");
            final int deleteCategoryId = data.getIntExtra("deleteCategoryId", 0);

            // Delete old point if there was point editing
            if (deleteUuid != null) {

                Point point = new Point();
                point.uuid = deleteUuid;
                point.categoryId = deleteCategoryId;

                PointsDelete pointsDelete = new PointsDelete(Settings.getToken(getApplicationContext()), point) {
                    @Override
                    protected void onPostExecute(BasicResponse response) {
                        super.onPostExecute(response);
                        if (response.code == 0)
                            deleteMarker(getCurrentSelectedMarker());
                        else {
                            Toast.makeText(getApplicationContext(), R.string.unsuccessful_edit,Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                pointsDelete.execute();
            }

            PointsAdd pointsAdd = new PointsAdd(Settings.getToken(getApplicationContext()),
                    pointCategory,
                    pointName,
                    rating,
                    latitude,
                    longitude,
                    System.currentTimeMillis()
            );

            Point point = new Point();

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

                point.name = pointName;
                point.url = pointUrl;
                point.latitude = latitude;
                point.longitude = longitude;
                point.rating = rating;
                point.categoryId = pointCategory;
                addMarker(point);

                Toast.makeText(getApplicationContext(), getString(R.string.saved_to_local_db), Toast.LENGTH_SHORT).show();
            } else {
                // Send to API
                pointsAdd.execute();
            }

            try {
                PointsResponse response = pointsAdd.get();

                if (response.code == 0) { // FIXME: codes
                    if (deleteUuid != null)
                        Toast.makeText(getApplicationContext(), getString(R.string.successful_edit), Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(getApplicationContext(), getString(R.string.successuflly_sent), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(Const.TAG, "Points adding error, response code: " + response.code);
                    return;
                }
                try {
                    point = response.points.get(0);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

                addMarker(point);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            mMapView.getController().animateTo(
                    new LatLng(point.latitude, point.longitude, 16)
            );
        }

        if (requestCode == Const.INTENT_RESULT_TOKEN) {
            // Save token
            Settings.saveString(getApplicationContext(), Const.PREFS_AUTH_TOKEN, data.getStringExtra("token"));
            checkUserStatus();
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

