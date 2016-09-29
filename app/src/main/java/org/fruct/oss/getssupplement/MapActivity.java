package org.fruct.oss.getssupplement;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.os.Handler;
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

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.fruct.oss.getssupplement.Api.CategoriesGet;
import org.fruct.oss.getssupplement.Api.PointsAdd;
import org.fruct.oss.getssupplement.Api.PointsDelete;
import org.fruct.oss.getssupplement.Api.PublishChannel;
import org.fruct.oss.getssupplement.Api.UserInfoGet;
import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.BasicResponse;
import org.fruct.oss.getssupplement.Model.CategoriesResponse;
import org.fruct.oss.getssupplement.Model.Category;
import org.fruct.oss.getssupplement.Model.DatabaseType;
import org.fruct.oss.getssupplement.Api.PointsGet;
import org.fruct.oss.getssupplement.Model.PointsResponse;
import org.fruct.oss.getssupplement.Model.Point;
import org.fruct.oss.getssupplement.Model.UserInfoResponse;
import org.fruct.oss.getssupplement.Utils.DirUtil;
import org.fruct.oss.getssupplement.Utils.DownloadGraphTask;
import org.fruct.oss.getssupplement.Utils.DownloadXmlTask;
import org.fruct.oss.getssupplement.Utils.XmlUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MapActivity extends Activity implements LocationListener {

    static Context context;

    private Menu menu;

    private MapView mMapView;

    private double mCurrentZoom;

    public static Location getLocation() {
        return sLocation;
    }

    private static void setLocation(Location sLocation) {
        MapActivity.sLocation = sLocation;
    }

    private static Location sLocation;

    private static LatLng loadCenter;

    private static LatLng getLoadCenter() {
        return loadCenter;
    }

    private static void setLoadCenter(LatLng location) {
        MapActivity.loadCenter = location;
    }

    private boolean followingState;

    private boolean succesLoading = false;

    private boolean isLocationOn = false;

    private ProgressBar progressBar;

    private LocationProvider currentProvider = null;

    private LocationManager locationManager;

    private  LocationProvider gpsProvider;

    private LocationProvider networkProvider;

    private Timer mapOffset;

    private GetsDbHelper dbHelper;
    private GetsDbHelper dbHelperSend;

    private ArrayList<Category> categoryArrayList;

    private Marker getCurrentSelectedMarker() {
        return currentSelectedMarker;
    }

    private void setCurrentSelectedMarker(Marker currentSelectedMarker) {
        this.currentSelectedMarker = currentSelectedMarker;
    }

    Marker currentSelectedMarker = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, getString(R.string.mapbox_token));
        setContentView(R.layout.activity_map);
        followingState = false;

        dbHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.DATA_FROM_API);

        checkGraphUpdate();


        setUpLocation();

        try {
            setLoadCenter(new LatLng(sLocation.getLatitude(), sLocation.getLongitude()));
        }
        catch(NullPointerException e) {
            e.printStackTrace();
        }
        finally {
            setLoadCenter(new LatLng(61.784626, 34.345600));
        }

        context = getApplicationContext();
        mMapView = (MapView) findViewById(R.id.activity_map_mapview);
        mMapView.onCreate(savedInstanceState);

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
            checkUserStatus();
            loadPoints();
            Timer collisionTimer = new Timer();
            final Handler handler = new Handler();
            collisionTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (succesLoading) {
                                selectAvailableProvider();
                                if (isLocationOn && currentProvider == null) {
                                    isLocationOn = false;
                                    stopFollow();
                                }
                                if (!isLocationOn && currentProvider != null) {
                                    if (startFollow())
                                        isLocationOn = true;
                                }
                            }
                        }
                    });
                }
            }, 100, 6L * 10);
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
                                                        }
                                                        else Toast.makeText(getApplicationContext(), R.string.toast_download_error, Toast.LENGTH_SHORT).show();
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


    public static boolean isAuthorized() {
        return Settings.getToken(context) != null;
    }


    private void setUpMapView() {
        mMapView.setClickable(true);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                if (sLocation != null) {
                    LatLng coords = new LatLng(getLocation().getLatitude(), getLocation().getLongitude());
                    CameraPosition position = new CameraPosition.Builder()
                            .target(coords)
                            .zoom(17)
                            .build();
                    mapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), 3000);
                }

//                mapboxMap.setMinZoom(14);
                mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
//                        getPointById(marker.getId());

                        // old
                        setCurrentSelectedMarker(marker);
                        ibBottomPanelDelete.setVisibility(View.INVISIBLE);
                        ibBottomPanelEdit.setVisibility(View.INVISIBLE);
//                        Point point = (Point) marker.getRelatedObject();

//                        setBottomPanelData(point);
                        return false;
                    }
                });
                mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {
                        clearBottomPanelData();
                    }
                });
                mapboxMap.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition position) {
                        mCurrentZoom = position.zoom;
                    }
                });
            }
        });

//        mMapView.getController().setZoom(17);
//        mMapView.setUseDataConnection(true);
//        mMapView.setUserLocationEnabled(true);
//        mMapView.setDiskCacheEnabled(true);
//        mMapView.setMinZoomLevel(14);

        findViewById(R.id.activity_map_my_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        if (sLocation != null) {
                            LatLng coords = new LatLng(getLocation().getLatitude(), getLocation().getLongitude());
                            CameraPosition position = new CameraPosition.Builder()
                                    .target(coords)
                                    .zoom(17)
                                    .build();
                            mapboxMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(position), 2000);
                        }
                    }
                });
            }
/*                mMapView.getController().setZoomAnimated(19,
                        new LatLng(
                                mMapView.getUserLocation().getLatitude(),
                                mMapView.getUserLocation().getLongitude()),
                        true,
                        false
                );
            }*/
        });

        findViewById(R.id.acitivity_map_app_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AppInfoActivity.class);
                startActivityForResult(intent, Const.INTENT_RESULT_APP_INFO);
            }
        });

        hideBottomPanel();
    }

    private void setUpLocation() {

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
            networkProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);
            if (gpsProvider != null) {
                try {
                    Location gpsLocation = locationManager.getLastKnownLocation(gpsProvider.getName());
                    // If gps isn't connected yet, try to obtain network location
                    if (gpsLocation == null)
                        setLocation(locationManager.getLastKnownLocation(networkProvider.getName()));
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                if (sLocation != null)
                    return;
            }

            if (networkProvider != null) {
                try {
                    setLocation(locationManager.getLastKnownLocation(networkProvider.getName()));
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                if (sLocation != null)
                    return;
            }

        // Set Petrozavodsk city if undefined
        sLocation = new Location("Undefined");
        sLocation.setLatitude(61.784626);
        sLocation.setLongitude(34.345600);

        Toast.makeText(this, "Can't determine location", Toast.LENGTH_SHORT).show();
    }

    private void deleteMarker(final Marker marker) {
        if (marker != null) {
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    mapboxMap.removeMarker(marker);
                }
            });
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
            return;
        }

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        final PointsGet pointsGet = new PointsGet(Settings.getToken(getApplicationContext()),
                getLocation().getLatitude(), getLocation().getLongitude(),
                Const.API_POINTS_RADIUS) {
            @Override
            protected void onProgressUpdate(final Point... point) {
                super.onProgressUpdate(point);
                if (Settings.getIsChecked(getApplicationContext(), point[0].categoryId))
                    mMapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(MapboxMap mapboxMap) {
//                            addMarker(point[0], mapboxMap);
                        }
                    });
            }

            @Override
            public void onPostExecute(final PointsResponse response) {

                if (response == null) {
                    return;
                }

                Toast.makeText(getApplicationContext(), getString(R.string.successful_download), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                succesLoading = true;

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dbHelper.clearDatabase();
                        dbHelper.addPoints(response.points);
                    }
                });
                t.start();

//                GetsDbHelper dbHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.DATA_FROM_API);
//                ArrayList<Point> points = dbHelper.getPoints();
                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        for (Point point : response.points) {
                            if (Settings.getIsChecked(getApplicationContext(), point.categoryId))
                                addMarker(point, mapboxMap);
                        }
                    }
                });
            }

        };


        CategoriesGet categoriesGet = new CategoriesGet(Settings.getToken(getApplicationContext())) {
            @Override
            public void onPostExecute(CategoriesResponse response) {
                if (response == null)
                    return;

                categoryArrayList = response.categories;
                dbHelper.addCategories(response.categories);

                if (menu != null) {
                    MenuItem miActions = menu.findItem(R.id.action_category_actions);
                    if (miActions != null)
                        miActions.setEnabled(true);
                }

                pointsGet.execute();
            }
        };

        categoriesGet.execute();

    }
    private void loadFilteredPoints() {
        //dbHelper.getFilteredPoints();
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
                intent.putExtra("zoomLevel", mCurrentZoom);
                intent.putExtra("isInEdit", true);

                startActivityForResult(intent, Const.INTENT_RESULT_NEW_POINT);

            }
        });

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

    private Marker addMarker(final Point point, MapboxMap mapboxMap) {

        /**
         * https://www.mapbox.com/help/android-markers/
         * Не добавлять title/snippet
         * set point id = marker id
         */

//        Marker marker = new Marker(mMapView, point.name, "", new LatLng(point.latitude, point.longitude));

        Drawable drawableImage = IconHolder.getInstance().getDrawableByCategoryId(getResources(), point.categoryId);
        IconFactory iconFactory = IconFactory.getInstance(this);
        // TODO: separating based on (un)publishing
        if (point.access == null || point.access.contains("w")) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation((float) 0.3);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            drawableImage.setColorFilter(filter);
        }
        final Icon icon = iconFactory.fromDrawable(drawableImage);
        Marker marker;

        mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(point.latitude, point.longitude))
                .icon(icon));

//        marker.setRelatedObject(point);
//        mMapView.addMarker(marker);
        return null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_map, menu);
/*
        selectAvailableProvider();
        if(currentProvider != null && getLocation() != null) {
            isLocationOn = true;
            startFollow();
        }
*/

        return true;
    }

    private void stopFollow() {
        followingState = false;
        menu.findItem(R.id.follow_location).getIcon().setColorFilter(null);
        try {
            setLocation(locationManager.getLastKnownLocation(networkProvider.getName()));
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
//        mMapView.setMapOrientation(0);
        mapOffset.cancel();
    }

    private boolean startFollow() {
        try {
            setLocation(locationManager.getLastKnownLocation(currentProvider.getName()));
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if(getLocation() != null) {
            followingState = true;
            if (menu.findItem(R.id.follow_location) != null)
                menu.findItem(R.id.follow_location).getIcon().setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    LatLng coords = new LatLng(getLocation().getLatitude(), getLocation().getLongitude());
                    CameraPosition position = new CameraPosition.Builder()
                            .target(coords)
                            .zoom(19)
                            .build();
                    mapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), 2000);

                }
            });
//            mMapView.getController().setZoomAnimated(19, new LatLng(getLocation().getLatitude(), getLocation().getLongitude()), true, false);
            try {
                locationManager.requestLocationUpdates(currentProvider.getName(), 1000, 50, this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            mapOffset = new Timer();
            final Handler uiHandler = new Handler();
            mapOffset.schedule(new TimerTask() {
                @Override
                public void run() {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            selectAvailableProvider();
                            if (currentProvider != null) {
                                try {
                                    setLocation(locationManager.getLastKnownLocation(currentProvider.getName()));
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                }
                                if(getLocation() == null) {
                                    stopFollow();
                                    return;
                                }
//                                mMapView.getController().setZoomAnimated(19, new LatLng(getLocation().getLatitude(), getLocation().getLongitude()), true, false);
                                mMapView.getMapAsync(new OnMapReadyCallback() {
                                    @Override
                                    public void onMapReady(MapboxMap mapboxMap) {
                                        LatLng coords = new LatLng(getLocation().getLatitude(), getLocation().getLongitude());
                                        CameraPosition position = new CameraPosition.Builder()
                                                .target(coords)
                                                .zoom(19)
                                                .build();
                                        mapboxMap.animateCamera(CameraUpdateFactory
                                                .newCameraPosition(position), 2000);

                                    }
                                });
                            }
                        }
                    });
                }
            }, 0L, 6L * 10);
            return true;
        }
        else
            return false;
    }

    private void selectAvailableProvider() {
        currentProvider = null;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
        networkProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);
        if (networkProvider != null && isInternetConnectionAvailable())
            currentProvider = networkProvider;
        if (gpsProvider != null) {
            try {
                if (locationManager.getLastKnownLocation(gpsProvider.getName()) != null)
                    currentProvider = gpsProvider;
            } catch (SecurityException e) {
                e.printStackTrace();
            }
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
            intent.putExtra("zoomLevel", mCurrentZoom);
            startActivityForResult(intent, Const.INTENT_RESULT_NEW_POINT);
        }

        if (id == R.id.activity_map_refresh) {
//            mMapView.clear();
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    //TODO: check is it working?
                    mapboxMap.removeAnnotations();
                }
            });
            setUpLocation();
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
                    streetId,
                    System.currentTimeMillis(),
                    dbHelperSend
            );

            Point point = new Point();

            // Save to local database when no Internet connection
            if (!isInternetConnectionAvailable()) {
                GetsDbHelper dbSaveHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.USER_GENERATED);
                dbSaveHelper.addPoint(pointCategory,
                        pointName,
                        pointUrl,
                        "?",
                        System.currentTimeMillis() + "",
                        "",
                        latitude,
                        longitude,
                        rating,
                        "?",
                        -1);

                point.name = pointName;
                point.url = pointUrl;
                point.latitude = latitude;
                point.longitude = longitude;
                point.rating = rating;
                point.categoryId = pointCategory;
                final Point finalPoint1 = point;
                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        addMarker(finalPoint1, mapboxMap);
                    }
                });

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
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                final Point finalPoint2 = point;
                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        addMarker(finalPoint2, mapboxMap);
                    }
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

//            mMapView.getController().animateTo(new LatLng(point.latitude, point.longitude, 16));
            final Point finalPoint = point;
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    LatLng coords = new LatLng(finalPoint.latitude, finalPoint.longitude);
                    CameraPosition position = new CameraPosition.Builder()
                            .target(coords)
                            .zoom(16)
                            .build();
                    mapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), 2000);

                }
            });
        }

        if (requestCode == Const.INTENT_RESULT_CATEGORY_ACTIONS) {
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    //TODO: check is it working?
                    mapboxMap.removeAnnotations();
                    ArrayList<Point> points;
                    for (int i = 0; i < categoryArrayList.size(); i++) {
                        if (Settings.getIsChecked(getApplicationContext(), categoryArrayList.get(i).id)) {
                            points = dbHelper.getPoints(categoryArrayList.get(i).id);
                            if (points != null) {
                                for (Point point : points)
                                    addMarker(point, mapboxMap);
                            }
                        }
                    }
                }
            });
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
    @Override
    public void onResume() {
        super.onResume();
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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}

