package org.fruct.oss.getssupplement.ui.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.telemetry.location.AndroidLocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;

import org.fruct.oss.getssupplement.Api.CategoriesGet;
import org.fruct.oss.getssupplement.Api.PointsDelete;
import org.fruct.oss.getssupplement.Api.PointsGet;
import org.fruct.oss.getssupplement.Api.PublishChannel;
import org.fruct.oss.getssupplement.Api.UserInfoGet;
import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.BasicResponse;
import org.fruct.oss.getssupplement.Model.CategoriesResponse;
import org.fruct.oss.getssupplement.Model.Category;
import org.fruct.oss.getssupplement.Model.Point;
import org.fruct.oss.getssupplement.Model.PointsResponse;
import org.fruct.oss.getssupplement.Model.UserInfoResponse;
import org.fruct.oss.getssupplement.R;
import org.fruct.oss.getssupplement.Utils.Const;
import org.fruct.oss.getssupplement.Utils.DirUtil;
import org.fruct.oss.getssupplement.Utils.DownloadGraphTask;
import org.fruct.oss.getssupplement.Utils.DownloadXmlTask;
import org.fruct.oss.getssupplement.Utils.IconHolder;
import org.fruct.oss.getssupplement.Utils.Settings;
import org.fruct.oss.getssupplement.Utils.XmlUtil;

import java.io.File;
import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {
    private static String TAG = "MapActivity";

    private Menu menu;
    private ProgressBar progressBar;

    private MapView mMapView;
    private MapboxMap mMapboxMap;

    private static final int PERMISSIONS_LOCATION = 0;

    private boolean mIsFollowingEnabled = false;
    private double mCurrentZoom;

    private LocationEngine locationEngine;
    private GetsDbHelper dbHelper, dbHelperSend;

    private ArrayList<Category> categoryArrayList;
    private Marker currentSelectedMarker = null;

    private Toolbar toolbar;
    private NavigationView nvMain;

    /**
     * Bottom panel views
     */
    private RelativeLayout rlBottomPanel;
    private TextView tvBottomPanelName, tvBottomPanelDescription;
    private ImageView ivBottomPanelIcon;
    private ImageButton ibBottomPanelDelete, ibBottomPanelEdit;
    private View viGradient;

    private ActionBarDrawerToggle mDrawerToggle;

    private LocationEngineListener mLocationListener = new LocationEngineListener() {
        @Override
        public void onConnected() {

        }

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                // Move the map camera to where the user location is
                mMapboxMap.setCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(location))
                        .zoom(16)
                        .build());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        dbHelper = GetsDbHelper.getInstance(getApplicationContext());

        setToolbar();
        setNavigation();

        checkGraphUpdate();
        initBottomPanel();

        locationEngine = AndroidLocationEngine.getLocationEngine(MapActivity.this);
        mMapView = (MapView) findViewById(R.id.activity_map_mapview);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                MapActivity.this.mMapboxMap = mapboxMap;
                setUpMapView();

                if (isInternetConnectionAvailable()) {
                    loadPoints();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.network_error_authorization), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.menu_title_map));
    }

    private void setNavigation() {
        final DrawerLayout dl = (DrawerLayout) findViewById(R.id.dlMain);
        mDrawerToggle = new ActionBarDrawerToggle(this,
                dl,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close);

        dl.addDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

        nvMain = (NavigationView) findViewById(R.id.nvMain);
        nvMain.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                item.setChecked(true);

                Intent i = new Intent();
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                switch (item.getItemId()) {
                    case R.id.nav_map:
                        break;
                    case R.id.nav_profile:
                        i.setClass(MapActivity.this, LoginActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_queue:
                        i.setClass(MapActivity.this, QueueActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_policy:
                        i.setClass(MapActivity.this, PolicyActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_info:
                        i.setClass(MapActivity.this, AppInfoActivity.class);
                        startActivity(i);
                        break;
                    default:
                        dl.closeDrawers();
                }

                dl.closeDrawers();

                return true;
            }
        });
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setUpMapView() {
        if (mMapboxMap != null) {
            mMapboxMap.getUiSettings().setRotateGesturesEnabled(false);
            mMapboxMap.getUiSettings().setTiltGesturesEnabled(false);
            mMapboxMap.setMyLocationEnabled(true);
            mMapboxMap.setMinZoomPreference(12);
            Location location = mMapboxMap.getMyLocation();

            CameraPosition position;
            mCurrentZoom = 17;
            if (location != null) {
                position = new CameraPosition.Builder()
                        .target(new LatLng(location))
                        .zoom(mCurrentZoom)
                        .build();
            } else {
                position = new CameraPosition.Builder()
                        .target(getDefaultLocation())
                        .zoom(mCurrentZoom)
                        .build();
            }
            mMapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 3000);

            mMapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    Log.d(TAG, "Marker " + marker.getId() + " was pressed");
                    setCurrentSelectedMarker(marker);
                    ibBottomPanelDelete.setVisibility(View.INVISIBLE);
                    ibBottomPanelEdit.setVisibility(View.INVISIBLE);

                    Point point = dbHelper.getPointByMarkerId(marker.getId());
                    if (point != null)
                        setBottomPanelData(point);

                    return false;
                }
            });

            mMapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng point) {
                    hideBottomPanel();
                }
            });

            mMapboxMap.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition position) {
                    mCurrentZoom = position.zoom;
                }
            });
        }

        ImageButton ibMyLocation = (ImageButton) findViewById(R.id.activity_map_my_location);
        ibMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMapboxMap != null) {
                    Location location = mMapboxMap.getMyLocation();
                    if (location != null) {
                        CameraPosition position = new CameraPosition.Builder()
                                .target(new LatLng(location))
                                .zoom(16)
                                .build();
                        mMapboxMap.animateCamera(CameraUpdateFactory
                                .newCameraPosition(position), 2000);
                    } else {
                        requestGps();
                        CameraPosition position = new CameraPosition.Builder()
                                .target(getDefaultLocation())
                                .zoom(16)
                                .build();
                        mMapboxMap.animateCamera(CameraUpdateFactory
                                .newCameraPosition(position), 2000);
                    }
                }
            }
        });
    }

    private LatLng getDefaultLocation() {
        return new LatLng(61.784626, 34.345600);
    }

    private void deleteMarker(Marker marker) {
        if (marker != null && mMapboxMap != null) {
            mMapboxMap.removeMarker(marker);
            hideBottomPanel();
        }
    }

    private void deletePoint(final Point point) {
        PointsDelete pointsDelete = new PointsDelete(Settings.getToken(this), point) {
            @Override
            protected void onPostExecute(BasicResponse response) {
                super.onPostExecute(response);
                if (response.code == 0) {
                    deleteMarker(getCurrentSelectedMarker());
                }
                else
                    Toast.makeText(getApplicationContext(), getString(R.string.error_deleting_point), Toast.LENGTH_SHORT).show();
            }
        };
        pointsDelete.execute();
    }

    private void loadPoints() {
        if (mMapboxMap == null)
            return;

        dbHelper.deleteAllPoints();

        Location location = mMapboxMap.getMyLocation();
        LatLng center;
        if (location != null)
            center = new LatLng(location.getLatitude(), location.getLongitude());
        else
            center = getDefaultLocation();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        final PointsGet pointsGet = new PointsGet(Settings.getToken(getApplicationContext()),
                center.getLatitude(), center.getLongitude(),
                Const.API_POINTS_RADIUS) {
            @Override
            public void onPostExecute(final PointsResponse response) {
                progressBar.setVisibility(ProgressBar.GONE);

                if (response == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.unsuccessful_download), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.points == null || response.code != 0) {
                    if (response.code == 1) {
                        Toast.makeText(MapActivity.this, getString(R.string.error_auth_reqiured), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(getApplicationContext(), getString(R.string.unsuccessful_download), Toast.LENGTH_SHORT).show();
                    return;
                }

                menu.getItem(1).setVisible(true);

                Toast.makeText(getApplicationContext(), getString(R.string.successful_download), Toast.LENGTH_SHORT).show();

                final ArrayList<Point> points = new ArrayList<>();
                for (Point point : response.points) {
                    if (Settings.getIsChecked(getApplicationContext(), point.categoryId)) {
                        point.markerId = addMarker(point).getId();
                        points.add(point);
                    } else {
                        points.add(point);
                    }
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dbHelper.addPoints(points);
                    }
                }).start();
            }
        };

        CategoriesGet categoriesGet = new CategoriesGet(Settings.getToken(getApplicationContext()), getBaseContext()) {
            @Override
            public void onPostExecute(CategoriesResponse response) {
                if (response == null)
                    return;

                Log.d(TAG, "Download categories size=" + (response.categories == null ? response.categories : response.categories.size()));
                categoryArrayList = response.categories;
                dbHelper.addCategories(response.categories);
                if (menu != null) {
                    MenuItem miActions = menu.findItem(R.id.action_category_actions);
                    if (miActions != null)
                        miActions.setEnabled(true);
                }

                pointsGet.execute();

                ArrayList<Point> points = dbHelper.getCachedPoints(Const.ALL_CATEGORIES);
                if (points != null)
                    for (Point point : points) {
                        dbHelper.deleteCachedPoint(point.id);
                        if (Settings.getIsChecked(getApplicationContext(), point.categoryId)) {
                            point.markerId = addMarker(point).getId();
                            dbHelper.cachePoint(point);
                        } else {
                            dbHelper.cachePoint(point);
                        }
                    }
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
                    Settings.saveBoolean(getApplicationContext(), Const.PREFS_IS_TRUSTED_USER, false);
                } else {
                    Settings.saveBoolean(getApplicationContext(), Const.PREFS_IS_TRUSTED_USER, true);
                    if (menu != null) {
                        MenuItem menuItem = menu.findItem(R.id.action_category_actions);
                        menuItem.setVisible(true);
                    }
                }
            }
        };
        if (usrToken == null || usrToken.equals(""))
            Settings.saveBoolean(getApplicationContext(), Const.PREFS_IS_TRUSTED_USER, false);
        else
            userInfoGet.execute();
    }

    private void initBottomPanel() {
        rlBottomPanel = (RelativeLayout) findViewById(R.id.activity_map_bottom_panel);
        tvBottomPanelName = (TextView) findViewById(R.id.acitivity_map_point_name);
        tvBottomPanelDescription = (TextView) findViewById(R.id.acitivity_map_point_description);
        ibBottomPanelDelete = (ImageButton) findViewById(R.id.activity_map_point_delete);
        ibBottomPanelEdit = (ImageButton) findViewById(R.id.activity_map_point_edit);
        ivBottomPanelIcon = (ImageView) findViewById(R.id.activity_map_bottom_panel_icon);
        viGradient = findViewById(R.id.activity_map_bottom_panel_gradient);
    }

    private void setBottomPanelData(final Point point) {
        tvBottomPanelName.setText(point.getName());
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

        ibBottomPanelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (point.id < 0)
                    dbHelper.deletePointByMarkerId(point.markerId);
                else
                    deletePoint(point);
            }
        });

        ibBottomPanelEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, AddNewPointActivity.class);
                intent.putExtra("latitude", point.latitude);
                intent.putExtra("longitude", point.longitude);
                intent.putExtra("name", point.getName());
                intent.putExtra("categoryId", point.categoryId);
                intent.putExtra("description", point.description);
                intent.putExtra("rating", point.rating);
                intent.putExtra("uuid", point.uuid);
                intent.putExtra("token", Settings.getToken(getApplicationContext()));
                intent.putExtra("zoomLevel", mCurrentZoom);
                intent.putExtra("isInEdit", true);
                startActivityForResult(intent, Const.INTENT_RESULT_NEW_POINT);
            }
        });

        if (!isBottomPanelShowed())
            showBottomPanel();
    }


    private void hideBottomPanel() {
        if (!isBottomPanelShowed())
            return;

        Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rlBottomPanel.setVisibility(View.GONE);
                viGradient.setVisibility(View.GONE);
                ibBottomPanelDelete.setVisibility(View.GONE);
                ibBottomPanelEdit.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        rlBottomPanel.startAnimation(fadeOut);
        viGradient.startAnimation(fadeOut);
    }

    private void showBottomPanel() {
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
        rlBottomPanel.startAnimation(fadeOut);
        viGradient.startAnimation(fadeOut);
        rlBottomPanel.setVisibility(View.VISIBLE);
    }

    private boolean isBottomPanelShowed() {
        return rlBottomPanel.getVisibility() == View.VISIBLE;
    }

    public boolean isInternetConnectionAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        return false;
    }

    /**
     * @return markerId
     */
    private synchronized Marker addMarker(Point point) {
        /**
         * https://www.mapbox.com/help/android-markers/
         * Не добавлять title/snippet
         * set point id = marker id
         */

        //Drawable drawableImage = IconHolder.getInstance().getDrawableByCategoryId(getResources(), point.categoryId);
        Bitmap mutableBitmap = IconHolder.getInstance().getBitmapByCategoryId(point.categoryId);
        IconFactory iconFactory = IconFactory.getInstance(this);
        // TODO: separating based on (un)publishing
        if (point.access == null || point.access.contains("w")) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation((float) 0.3);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            //drawableImage.setColorFilter(filter);
            Canvas canvasResult = new Canvas(mutableBitmap);
            Paint paint = new Paint();
            paint.setColorFilter(filter);
            canvasResult.drawBitmap(mutableBitmap, 0, 0, paint);
        }
        Icon icon = iconFactory.fromBitmap(mutableBitmap);
        return mMapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(point.latitude, point.longitude))
                .icon(icon));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.follow_location) {
            Location location = mMapboxMap.getMyLocation();
            if (location == null) {
                requestGps();
                return true;
            }

            mIsFollowingEnabled = !mIsFollowingEnabled;
            toggleLocationFollowing(mIsFollowingEnabled);

            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(location))
                    .zoom(16)
                    .build();

            mMapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 2000);
        }

        if (id == R.id.action_add) {
            Intent intent = new Intent(this, AddNewPointActivity.class);
            intent.putExtra("zoomLevel", mCurrentZoom);
            if (mMapboxMap.getMyLocation() == null) {
                intent.putExtra("latitude", getDefaultLocation().getLatitude());
                intent.putExtra("longitude", getDefaultLocation().getLongitude());
            } else {
                intent.putExtra("latitude", mMapboxMap.getMyLocation().getLatitude());
                intent.putExtra("longitude", mMapboxMap.getMyLocation().getLongitude());
            }
            startActivityForResult(intent, Const.INTENT_RESULT_NEW_POINT);
        }

        if (id == R.id.activity_map_refresh) {
            if (mMapboxMap != null)
                mMapboxMap.clear();
            loadPoints();
        }

        if (id == R.id.action_category_actions) {
            Intent intent = new Intent(this, CategoryActionsActivity.class);
            startActivityForResult(intent, Const.INTENT_RESULT_CATEGORY_ACTIONS);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Const.INTENT_RESULT_CODE_OK) {
            return;
        }

        // TODO: delete  requstCode
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
            int streetId = data.getIntExtra("streetId", -1);

            String deleteUuid = data.getStringExtra("deleteUuid");
            int deleteCategoryId = data.getIntExtra("deleteCategoryId", 0);

            // Delete old point if there was point editing
            if (deleteUuid != null) {

                final Point point = new Point();
                point.uuid = deleteUuid;
                point.categoryId = deleteCategoryId;

                PointsDelete pointsDelete = new PointsDelete(Settings.getToken(getApplicationContext()), point) {
                    @Override
                    protected void onPostExecute(BasicResponse response) {
                        super.onPostExecute(response);
                        if (response.code == 0) {
                            deleteMarker(getCurrentSelectedMarker());
                            dbHelper.deletePointByMarkerId(getCurrentSelectedMarker().getId());
                        }
                        else {
                            Toast.makeText(getApplicationContext(), R.string.unsuccessful_edit, Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                pointsDelete.execute();
            }

            Point point = new Point();
            point.id = Settings.generateId(this);
            point.setName(pointName);
            point.url = pointUrl;
            point.latitude = latitude;
            point.longitude = longitude;
            point.rating = rating;
            point.categoryId = pointCategory;

            if (mMapboxMap != null) {
                point.markerId = addMarker(point).getId();
                dbHelper.cachePoint(point);

                Toast.makeText(getApplicationContext(), getString(R.string.saved_to_local_db), Toast.LENGTH_SHORT).show();
            }
/*
            PointsAdd pointsAdd = new PointsAdd(Settings.getToken(getApplicationContext()),
                    pointCategory,
                    pointName,
                    rating,
                    latitude,
                    longitude,
                    streetId,
                    System.currentTimeMillis());

            Point point = new Point();

            // Save to local database when no Internet connection
            if (!isInternetConnectionAvailable()) {
                point.name = pointName;
                point.url = pointUrl;
                point.latitude = latitude;
                point.longitude = longitude;
                point.rating = rating;
                point.categoryId = pointCategory;

                if (mMapboxMap != null) {
                    addMarker(point);
                    dbHelper.cachePoint(point);
                }

                Toast.makeText(getApplicationContext(), getString(R.string.saved_to_local_db), Toast.LENGTH_SHORT).show();
            } else {
                // Send to API
                pointsAdd.execute();
            }

            try {
                PointsResponse response = pointsAdd.get();

                if (response != null && response.code == 0) { // FIXME: codes
                    if (deleteUuid != null)
                        Toast.makeText(getApplicationContext(), getString(R.string.successful_edit), Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(getApplicationContext(), getString(R.string.successuflly_sent), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    return;
                }
                try {
                    point = response.points.get(0);
                    if (mMapboxMap != null)
                        addMarker(point);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }*/

            if (mMapboxMap != null) {
                LatLng coords = new LatLng(point.latitude, point.longitude);
                CameraPosition position = new CameraPosition.Builder()
                        .target(coords)
                        .zoom(16)
                        .build();
                mMapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 2000);

            }
        }

        if (requestCode == Const.INTENT_RESULT_CATEGORY_ACTIONS) {
            if (mMapboxMap != null) {
                mMapboxMap.clear();
                ArrayList<Point> points;
                for (int i = 0; i < categoryArrayList.size(); i++) {
                    if (Settings.getIsChecked(getApplicationContext(), categoryArrayList.get(i).id)) {
                        points = dbHelper.getAllPoints(categoryArrayList.get(i).id);
                        if (points != null) {
                            for (Point point : points) {
                                Marker marker = addMarker(point);
                                if (point.markerId != -1) {
                                    marker.setId(point.markerId);
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    private void checkGraphUpdate() {
        String dataPath = Settings.getStorageDir(getApplicationContext());

        if (dataPath == null) {
            DirUtil.StorageDirDesc[] contentPaths = DirUtil.getPrivateStorageDirs(this);
            dataPath = contentPaths[0].path;
            Settings.saveString(getApplicationContext(), Const.PREF_STORAGE_PATH, dataPath);
        }

        File unpackedRootDir = new File(Settings.getStorageDir(getApplicationContext()), "/unpacked");
        if (!unpackedRootDir.exists())
            unpackedRootDir.mkdir();

        DownloadXmlTask downloadXmlTask = new DownloadXmlTask(Const.URL_ROOT_XML) {
            @Override
            protected void onPostExecute(String response) {
                if (response != null) {
                    XmlUtil xml = new XmlUtil(response);
                    final String newHash = xml.getHashByRegionId(Const.ID_REGION_KARELIA);
                    final String oldHash = Settings.getMapHash(getApplicationContext());
                    final String fileName = xml.getFileNameByRegionId(Const.ID_REGION_KARELIA);
                    final String url = xml.getUrlByRegionId(Const.ID_REGION_KARELIA);

                    if (newHash == null)
                        return;

                    if (oldHash == null || !oldHash.equals(newHash) || !new File(Settings.getStorageDir(getApplicationContext()),
                            fileName).exists()) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                        builder
                                .setMessage(R.string.text_download_message)
                                .setCancelable(false)
                                .setPositiveButton(R.string.text_download_positive,
                                        new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialog, int id) {
                                                Toast.makeText(getApplicationContext(), R.string.toast_download_start, Toast.LENGTH_SHORT).show();
                                                DownloadGraphTask downloadGraphTask = new DownloadGraphTask(url, fileName, getApplicationContext()) {
                                                    @Override
                                                    protected void onPostExecute(Boolean success) {
                                                        if (success) {
                                                            Toast.makeText(getApplicationContext(), R.string.toast_download_success, Toast.LENGTH_SHORT).show();
                                                            Settings.saveMapHash(getApplicationContext(), newHash);
                                                        } else
                                                            Toast.makeText(getApplicationContext(), R.string.toast_download_error, Toast.LENGTH_SHORT).show();
                                                    }
                                                };
                                                downloadGraphTask.execute();

                                                dialog.cancel();
                                            }

                                        })
                                .setNegativeButton(R.string.text_download_negative,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    // If ghz-file exists but not unpacked
                    else if (new File(Settings.getStorageDir(getApplicationContext()), fileName).exists())
                        DirUtil.unzipInStorage(getApplicationContext(), fileName);

                }
            }
        };
        downloadXmlTask.execute();
    }

    private Marker getCurrentSelectedMarker() {
        return currentSelectedMarker;
    }

    private void setCurrentSelectedMarker(Marker currentSelectedMarker) {
        this.currentSelectedMarker = currentSelectedMarker;
    }

    public boolean isAuthorized() {
        return Settings.getToken(getApplicationContext()) != null;
    }

    private void setMapPannable(boolean b) {
        mMapboxMap.getUiSettings().setZoomGesturesEnabled(b);
        mMapboxMap.getUiSettings().setScrollGesturesEnabled(b);
    }

    @UiThread
    public void requestGps() {
        // Check if user has granted location permission
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
        }
    }

    private void toggleLocationFollowing(boolean enabled) {
        setMapPannable(!enabled);
        if (enabled) {
            menu.findItem(R.id.follow_location).getIcon().
                    setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);
            locationEngine.addLocationEngineListener(mLocationListener);
        } else {
            menu.findItem(R.id.follow_location).getIcon().
                    setColorFilter(null);
            locationEngine.removeLocationEngineListener(mLocationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_LOCATION: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Location lastLocation = locationEngine.getLastLocation();
                    if (lastLocation != null) {
                        CameraPosition position = new CameraPosition.Builder()
                                .target(new LatLng(lastLocation))
                                .zoom(16)
                                .build();
                        mMapboxMap.animateCamera(CameraUpdateFactory
                                .newCameraPosition(position), 2000);
                    }
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        nvMain.getMenu().getItem(0).setChecked(true);
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        dbHelper.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}