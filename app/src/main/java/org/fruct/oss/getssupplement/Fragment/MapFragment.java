package org.fruct.oss.getssupplement.Fragment;/**
 * Created by Yaroslav21 on 27.07.16.
 */

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.fruct.oss.getssupplement.AddNewPointActivity;
import org.fruct.oss.getssupplement.Api.CategoriesGet;
import org.fruct.oss.getssupplement.Api.PointsAdd;
import org.fruct.oss.getssupplement.Api.PointsDelete;
import org.fruct.oss.getssupplement.Api.PublishChannel;
import org.fruct.oss.getssupplement.Api.UserInfoGet;
import org.fruct.oss.getssupplement.Api.WaitForLoad;
import org.fruct.oss.getssupplement.AppInfoActivity;
import org.fruct.oss.getssupplement.CategoryActionsActivity;
import org.fruct.oss.getssupplement.Const;
import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.IconHolder;
import org.fruct.oss.getssupplement.LoginActivity;
import org.fruct.oss.getssupplement.MainActivity;
import org.fruct.oss.getssupplement.MapActivity;
import org.fruct.oss.getssupplement.Model.BasicResponse;
import org.fruct.oss.getssupplement.Model.CategoriesResponse;
import org.fruct.oss.getssupplement.Model.Category;
import org.fruct.oss.getssupplement.Model.DatabaseType;
import org.fruct.oss.getssupplement.Api.PointsGet;
import org.fruct.oss.getssupplement.Model.PointsResponse;
import org.fruct.oss.getssupplement.Model.Point;
import org.fruct.oss.getssupplement.Model.UserInfoResponse;
import org.fruct.oss.getssupplement.R;
import org.fruct.oss.getssupplement.Settings;
import org.fruct.oss.getssupplement.Utils.DirUtil;
import org.fruct.oss.getssupplement.Utils.DownloadGraphTask;
import org.fruct.oss.getssupplement.Utils.DownloadXmlTask;
import org.fruct.oss.getssupplement.Utils.XmlUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


//import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;

public class MapFragment extends Fragment {//implements LocationListener {//, OnMapReadyCallback, GoogleMap.OnMapLoadedCallback{


    private Menu menu;
    private ProgressBar progressBar;

    private MapView mMapView;
    private MapboxMap mMapboxMap;

    private static final int PERMISSIONS_LOCATION = 0;

    private boolean mIsFollowingEnabled = false;
    private double mCurrentZoom;

    private LocationServices locationServices;
    private GetsDbHelper dbHelper, dbHelperSend, dbHelperW;

    private ArrayList<Category> categoryArrayList;
    //private Marker currentSelectedMarker = null;

    /**
     * Bottom panel views
     */
    RelativeLayout rlBottomPanel = null;
    TextView tvBottomPanelName = null;
    TextView tvBottomPanelDescription = null;
    ImageView ivBottomPanelArrowRight = null;
    ImageView ivBottomPanelIcon = null;
    ImageButton ibBottomPanelDelete = null;
    ImageButton ibBottomPanelEdit = null;
    View viGradient = null;

    private com.mapbox.mapboxsdk.location.LocationListener mLocationListener = new com.mapbox.mapboxsdk.location.LocationListener() {
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

    /*public MapFragment() {
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_map, container, false);
        //MapActivity Starter = new MapActivity();
        setHasOptionsMenu(true);
        setRetainInstance(true);

        return rootView;
    }

    /*
    private MapboxMap mMapboxMap;


    ProgressBar progressBar;

    static Context context;

    private Menu menu;

    public MapView mMapView;

    public static Location getLocation() {
        return sLocation;
    }

    private static void setLocation(Location sLocation) {
        MapFragment.sLocation = sLocation;
    }

    private static Location sLocation;

    private static LatLng loadCenter;

    private static LatLng getLoadCenter() {
        return loadCenter;
    }

    private static void setLoadCenter(LatLng location) {
        MapFragment.loadCenter = location;
    }

    private boolean followingState;

    //private boolean succesLoading = false;

    private boolean isLocationOn = false;

    private LocationProvider currentProvider = null;

    private LocationManager locationManager;

    private  LocationProvider gpsProvider;

    private LocationProvider networkProvider;

    private Timer mapOffset;

    private long timeout;

    public GetsDbHelper dbHelper;
    public GetsDbHelper dbHelperW;
    private GetsDbHelper dbHelperSend;

    public ArrayList<Category> categoryArrayList;
*/
    public Marker getCurrentSelectedMarker() {
        return currentSelectedMarker;
    }

    public void setCurrentSelectedMarker(Marker currentSelectedMarker) {
        this.currentSelectedMarker = currentSelectedMarker;
    }

    Marker currentSelectedMarker = null;

    Bundle _savedInstanceState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _savedInstanceState = savedInstanceState;
    }

    @Override
    public void onStart() {

        //getLocation();

        //long timeout2 = System.currentTimeMillis();
        super.onStart();
        //followingState = false;

        dbHelper = new GetsDbHelper(getActivity().getApplicationContext(), DatabaseType.DATA_FROM_API);
        dbHelperW = new GetsDbHelper(getActivity().getApplicationContext(), DatabaseType.DATA_FROM_API);
        dbHelperSend = new GetsDbHelper(getActivity().getApplicationContext(), DatabaseType.DATA_FROM_API);

        checkGraphUpdate();
        //setUpLocation();


        checkGraphUpdate();
        initBottomPanel();

        locationServices = LocationServices.getLocationServices(getActivity());//MainActivity.this);
        mMapView = (MapView) getActivity().getCurrentFocus().findViewById(R.id.activity_map_mapview);
        mMapView.onCreate(_savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                MapFragment.this.mMapboxMap = mapboxMap;
                setUpMapView();

                if (!isAuthorized()) {
                    if (isInternetConnectionAvailable()) {
                        Intent i = new Intent(getActivity(), LoginActivity.class);
                        startActivityForResult(i, Const.INTENT_RESULT_TOKEN);
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.network_error_authorization), Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    checkUserStatus();
                    loadPoints();
                }
            }
        });
/*
        try {
            setLoadCenter(new LatLng(sLocation.getLatitude(), sLocation.getLongitude()));
        }
        catch(NullPointerException e) {
            e.printStackTrace();
        }
        finally {
            setLoadCenter(new LatLng(61.784626, 34.345600));
        }

        context = getActivity().getApplicationContext();
        mMapView = (MapView) getActivity().findViewById(R.id.activity_map_mapview);

        final Handler h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 1) {
                    setUpMapView();
                }
            };
        };

        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        long timeoutT = System.currentTimeMillis();
                        TimeUnit.MILLISECONDS.sleep(50);
                        if (mMapView != null) {
                            h.sendEmptyMessage(1);
                            //System.out.print("!!!!!!!!!!!!!! mMapView NOT EMPTY !!!!!!!!!!!!");
                            System.out.print(System.currentTimeMillis() - timeoutT + " - ВРЕМЯ РАБОТЫ Thread\n");
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        if (!isAuthorized()) {
            if (isInternetConnectionAvailable()) {
                Intent i = new Intent(getActivity(), LoginActivity.class); //m.createLoginActivity();
                startActivityForResult(i, Const.INTENT_RESULT_TOKEN);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.network_error_authorization), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            checkUserStatus();
            loadPoints();
            /*
            System.out.print(System.currentTimeMillis() - timeout + " - ВРЕМЯ РАБОТЫ loadPoints\n");

            timeout = System.currentTimeMillis();
            Timer collisionTimer = new Timer();
            System.out.print(System.currentTimeMillis() - timeout + " - ВРЕМЯ РАБОТЫ new Timer\n");

            timeout = System.currentTimeMillis();
            final Handler handler = new Handler();
            System.out.print(System.currentTimeMillis() - timeout + " - ВРЕМЯ РАБОТЫ new Handler\n");

            timeout = System.currentTimeMillis();
            collisionTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //System.out.print(System.currentTimeMillis() - timeout + " - ВРЕМЯ РАБОТЫ TimerTask\n");

                    timeout = System.currentTimeMillis();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            //System.out.print(System.currentTimeMillis() - timeout + " - ВРЕМЯ РАБОТЫ handler.post\n");
                            if (MainActivity.isSuccessLoading()) {
                                timeout = System.currentTimeMillis();
                                selectAvailableProvider();
                                System.out.print(System.currentTimeMillis() - timeout + " - ВРЕМЯ РАБОТЫ selectAvailableProvider\n");
                                if (isLocationOn && currentProvider == null) {
                                    isLocationOn = false;
                                    timeout = System.currentTimeMillis();
                                    stopFollow();
                                    System.out.print(System.currentTimeMillis() - timeout + " - ВРЕМЯ РАБОТЫ stopFollow\n");
                                }
                                if (!isLocationOn && currentProvider != null) {
                                    timeout = System.currentTimeMillis();
                                    if (startFollow())
                                        isLocationOn = true;
                                    System.out.print(System.currentTimeMillis() - timeout + " - ВРЕМЯ РАБОТЫ startFollow\n");
                                }
                            }
                        }
                    });
                }
            }, 100, 6L * 10);
        }
        System.out.print(System.currentTimeMillis() - timeout2 + " - ВРЕМЯ РАБОТЫ OnStart\n");*/
    //}
    }

    private void setUpMapView() {
        if (mMapboxMap != null) {
            mMapboxMap.getUiSettings().setRotateGesturesEnabled(false);
            mMapboxMap.getUiSettings().setLogoEnabled(false);
            mMapboxMap.getUiSettings().setTiltGesturesEnabled(false);
            mMapboxMap.setMyLocationEnabled(true);
            mMapboxMap.setMinZoom(14);
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
                        .target(new LatLng(61.784626, 34.345600))
                        .zoom(mCurrentZoom)
                        .build();
            }
            mMapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 3000);

            mMapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
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

        ImageButton ibMyLocation = (ImageButton) getActivity().findViewById(R.id.activity_map_my_location);
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
                    }
                }
            }
        });

        /*ImageButton ibMapInfo = (ImageButton) getActivity().findViewById(R.id.acitivity_map_app_info);
        ibMapInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), AppInfoActivity.class);
                startActivityForResult(intent, Const.INTENT_RESULT_APP_INFO);
            }
        });*/
    }

    private void deleteMarker(Marker marker) {
        if (marker != null && mMapboxMap != null) {
            mMapboxMap.removeMarker(marker);
            hideBottomPanel();
        }
    }

    private void deletePoint(Point point) {
        PointsDelete pointsDelete = new PointsDelete(Settings.getToken(getActivity().getApplicationContext()), point) {
            @Override
            protected void onPostExecute(BasicResponse response) {
                super.onPostExecute(response);
                if (response.code == 0)
                    deleteMarker(getCurrentSelectedMarker());
                else
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_deleting_point), Toast.LENGTH_SHORT).show();
            }
        };
        pointsDelete.execute();
    }

    private void loadPoints() {
        Location location = mMapboxMap.getMyLocation();
        if (location == null) {
            return;
        }

        /// ЗДЕСЬ БЫЛ КОД ИЗ MainActivity под методом downloadPoints

        final WaitForLoad w = new WaitForLoad();
        w.setParams(mMapboxMap, getResources(), dbHelperW, getActivity());

        //ArrayList<Point> points;
        //mMapView.clear();


        w.execute();

        /*
        WaitForLoad ww = new WaitForLoad();
        ww.execute(1);
        try {
            ww.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        categoryArrayList = MainActivity.getCategoryArray();
        for (int i = 0; i < categoryArrayList.size(); i++) {
            if (Settings.getIsChecked(getActivity().getApplicationContext(), categoryArrayList.get(i).id)) {

                WaitForLoad ww2 = new WaitForLoad();
                ww2.execute(0);
                try {
                    ww2.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                points = dbHelper.getPoints(categoryArrayList.get(i).id);
                if (points != null) {
                    for (Point point : points)
                        if (Settings.getIsChecked(getActivity().getApplicationContext(), point.categoryId))
                            addMarker(point);
                }
            }
        }*/
    }

    private void downloadPoints() {
        Location location = mMapboxMap.getMyLocation();
        if (location == null) {
            return;
        }

        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        final PointsGet pointsGet = new PointsGet(Settings.getToken(getActivity().getApplicationContext()),
                location.getLatitude(), location.getLongitude(),
                Const.API_POINTS_RADIUS) {
            @Override
            public void onPostExecute(final PointsResponse response) {
                if (response == null) {
                    return;
                }
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.successful_download), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                if (mMapboxMap != null) {
                    for (Point point : response.points) {
                        if (Settings.getIsChecked(getActivity().getApplicationContext(), point.categoryId))
                            addMarker(point);
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dbHelper.clearDatabase();
                            dbHelper.addPoints(response.points);
                        }
                    }).start();
                }
            }
        };

        CategoriesGet categoriesGet = new CategoriesGet(Settings.getToken(getActivity().getApplicationContext())) {
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
        String usrToken = Settings.getToken(getActivity().getApplicationContext());
        UserInfoGet userInfoGet = new UserInfoGet(usrToken) {
            @Override
            protected void onPostExecute(UserInfoResponse userInfoResponse) {
                super.onPostExecute(userInfoResponse);

                // Save user status
                if (userInfoResponse == null) {
                    Settings.saveBoolean(getActivity().getApplicationContext(), Const.PREFS_IS_TRUSTED_USER, false);
                } else {
                    Settings.saveBoolean(getActivity().getApplicationContext(), Const.PREFS_IS_TRUSTED_USER, true);
                    if (menu != null) {
                        MenuItem menuItem = menu.findItem(R.id.action_category_actions);
                        menuItem.setVisible(true);
                    }
                }
            }
        };

        if (usrToken == null || usrToken.equals(""))
            Settings.saveBoolean(getActivity().getApplicationContext(), Const.PREFS_IS_TRUSTED_USER, false);
        else userInfoGet.execute();
    }


    private void initBottomPanel() {
        if (rlBottomPanel == null)
            rlBottomPanel = (RelativeLayout) getActivity().findViewById(R.id.activity_map_bottom_panel);
        else
            return;

        if (tvBottomPanelName == null)
            tvBottomPanelName = (TextView) getActivity().findViewById(R.id.acitivity_map_point_name);

        if (tvBottomPanelDescription == null)
            tvBottomPanelDescription = (TextView) getActivity().findViewById(R.id.acitivity_map_point_description);

        if (ibBottomPanelDelete == null)
            ibBottomPanelDelete = (ImageButton) getActivity().findViewById(R.id.activity_map_point_delete);

        if (ibBottomPanelEdit == null)
            ibBottomPanelEdit = (ImageButton) getActivity().findViewById(R.id.activity_map_point_edit);

        if (ivBottomPanelIcon == null)
            ivBottomPanelIcon = (ImageView) getActivity().findViewById(R.id.activity_map_bottom_panel_icon);

        if (viGradient == null)
            viGradient = getActivity().findViewById(R.id.activity_map_bottom_panel_gradient);

        if (ivBottomPanelArrowRight == null)
            ivBottomPanelArrowRight = (ImageView) getActivity().findViewById(R.id.acitivity_map_bottom_panel_arrow_right);
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

                Intent intent = new Intent(getActivity(), AddNewPointActivity.class); //m.createAddNewPointActivity();

                intent.putExtra("latitude", point.latitude);
                intent.putExtra("longitude", point.longitude);
                intent.putExtra("name", point.name);
                intent.putExtra("categoryId", point.categoryId);
                intent.putExtra("description", point.description);
                intent.putExtra("rating", point.rating);
                intent.putExtra("uuid", point.uuid);

                intent.putExtra("token", Settings.getToken(getActivity().getApplicationContext()));
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

        Animation fadeOut = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fade_out);
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
            rlBottomPanel = (RelativeLayout) getActivity().findViewById(R.id.activity_map_bottom_panel);
            viGradient = getActivity().findViewById(R.id.activity_map_bottom_panel_gradient);
        }

        rlBottomPanel.setAnimation(fadeOut);
        viGradient.setAnimation(fadeOut);

    }

    private void showBottomPanel() {

        initBottomPanel();

        Animation fadeOut = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fade_in);
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

    public boolean isInternetConnectionAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        return false;
    }

    private void addMarker(Point point) {
        /**
         * https://www.mapbox.com/help/android-markers/
         * Не добавлять title/snippet
         * set point id = marker id
         */

        Drawable drawableImage = IconHolder.getInstance().getDrawableByCategoryId(getResources(), point.categoryId);
        IconFactory iconFactory = IconFactory.getInstance(getActivity().getApplicationContext()); //this);
        // TODO: separating based on (un)publishing
        if (point.access == null || point.access.contains("w")) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation((float) 0.3);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            drawableImage.setColorFilter(filter);
        }

        Icon icon = null;
        if (1 == 0)
            icon = iconFactory.fromDrawable(drawableImage);
        Marker marker = mMapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(point.latitude, point.longitude))
                .icon(icon));
        point.markerId = marker.getId();



/*
        Marker marker = new Marker(mMapView, point.name, "", new LatLng(point.latitude, point.longitude));

        Drawable drawableImage = IconHolder.getInstance().getDrawableByCategoryId(getResources(), point.categoryId);

        // TODO: separating based on (un)publishing


        if (point.access == null || point.access.indexOf("w") != -1) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation((float) 0.3);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            drawableImage.setColorFilter(filter);
        }


        marker.setIcon(new Icon(drawableImage));

        marker.setRelatedObject(point);

        mMapView.addMarker(marker);
        //return marker;*/
    }

    private void addMarkers(ArrayList<Point> points) {
        ArrayList<MarkerOptions> markers = new ArrayList<>();
        for (Point point : points) {

            /**
             * https://www.mapbox.com/help/android-markers/
             * Не добавлять title/snippet
             * set point id = marker id
             */

            Drawable drawableImage = IconHolder.getInstance().getDrawableByCategoryId(getResources(), point.categoryId);
            IconFactory iconFactory = IconFactory.getInstance(getActivity().getApplicationContext()); //this);
            // TODO: separating based on (un)publishing
            if (point.access == null || point.access.contains("w")) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation((float) 0.3);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                drawableImage.setColorFilter(filter);
            }

            Icon icon = null;
            if (1 == 0)
                    icon = iconFactory.fromDrawable(drawableImage);
            markers.add(new MarkerOptions()
                    .position(new LatLng(point.latitude, point.longitude))
                    .icon(icon));


                //point.markerId = markers.get(markers.size() - 1);
        }
        List<Marker> m = mMapboxMap.addMarkers(markers);
        int mIndex = 0;
        for (Point point : points) {
            point.markerId = m.get(mIndex).getId();
            mIndex++;
        }

        /*
        {
            Marker marker = new Marker(mMapView, point.name, "", new LatLng(point.latitude, point.longitude));

            Drawable drawableImage = IconHolder.getInstance().getDrawableByCategoryId(getResources(), point.categoryId);

            // TODO: separating based on (un)publishing


            if (point.access == null || point.access.indexOf("w") != -1) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation((float) 0.3);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                drawableImage.setColorFilter(filter);
            }


            marker.setIcon(new Icon(drawableImage));

            marker.setRelatedObject(point);

            markers.add(marker);
        }
        mMapView.addMarkers(markers);
        //return marker;*/
    }

    /*
    private void stopFollow() {
        try {
            followingState = false;
            menu.findItem(R.id.follow_location).getIcon().setColorFilter(null);
            setLocation(locationManager.getLastKnownLocation(networkProvider.getName()));
            locationManager.removeUpdates(this);
            mMapView.setMapOrientation(0);
            mapOffset.cancel();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private boolean startFollow() {
        try {
            timeout = System.currentTimeMillis();
            setLocation(locationManager.getLastKnownLocation(currentProvider.getName()));
            System.out.print(System.currentTimeMillis() - timeout + " - ВРЕМЯ РАБОТЫ 1 !!!!!!!!\n");
            long timeout2 = System.currentTimeMillis();
            if (getLocation() != null) {
                followingState = true;
                timeout = System.currentTimeMillis();
                if (menu.findItem(R.id.follow_location) != null)
                    menu.findItem(R.id.follow_location).getIcon().setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);
                System.out.print(System.currentTimeMillis() - timeout + " - ВРЕМЯ РАБОТЫ 2 !!!!!!!!\n");
                timeout = System.currentTimeMillis();
                mMapView.getController().setZoomAnimated(19, new LatLng(getLocation().getLatitude(), getLocation().getLongitude()), true, false);
                System.out.print(System.currentTimeMillis() - timeout + " - ВРЕМЯ РАБОТЫ 3 !!!!!!!!\n");
                timeout = System.currentTimeMillis();
                locationManager.requestLocationUpdates(currentProvider.getName(), 1000, 50, this);
                System.out.print(System.currentTimeMillis() - timeout + " - ВРЕМЯ РАБОТЫ 4 !!!!!!!!\n");
                timeout = System.currentTimeMillis();
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
                                    if (getLocation() == null) {
                                        stopFollow();
                                        return;
                                    }
                                    mMapView.getController().setZoomAnimated(19, new LatLng(getLocation().getLatitude(), getLocation().getLongitude()), true, false);
                                }
                            }
                        });
                    }
                }, 0L, 6L * 10);
                System.out.print(System.currentTimeMillis() - timeout + " - ВРЕМЯ РАБОТЫ 5 !!!!!!!!\n");
                System.out.print(System.currentTimeMillis() - timeout2 + " - ВРЕМЯ РАБОТЫ IF !!!!!!!!\n");
                return true;
            } else
                return false;
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return false;
    }*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_map, menu);
        this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.follow_location) {
            /*selectAvailableProvider();
            if(currentProvider != null) {
                if (followingState)
                    stopFollow();
                else
                    startFollow();*/
            toggleGps(!mIsFollowingEnabled);
            Location location = mMapboxMap.getMyLocation();
            if (location != null) {
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(location))
                        .zoom(16)
                        .build();

                mMapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 2000);

            }
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            Intent intent = new Intent(getActivity(), AddNewPointActivity.class); //m.createAddNewPointActivity();
            //intent.putExtra("zoomLevel", mMapView.getZoomLevel());
            intent.putExtra("zoomLevel", mCurrentZoom);
            intent.putExtra("latitude", mMapboxMap.getMyLocation().getLatitude());
            intent.putExtra("longitude", mMapboxMap.getMyLocation().getLongitude());
            startActivityForResult(intent, Const.INTENT_RESULT_NEW_POINT);
        }

        if (id == R.id.activity_map_refresh) {
            //mMapView.clear();
            //setUpLocation();
            if (mMapboxMap != null)
                mMapboxMap.removeAnnotations();
            downloadPoints();
        }

        if (id == R.id.action_category_actions) {
            Intent intent = new Intent(getActivity(), CategoryActionsActivity.class); //m.createCategoryActivity();
            startActivityForResult(intent, Const.INTENT_RESULT_CATEGORY_ACTIONS);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //timeout = System.currentTimeMillis();

        if (resultCode != Const.INTENT_RESULT_CODE_OK) {
            return;
        }

        // TODO: delete  requstCode
        if (requestCode == Const.INTENT_RESULT_PUBLISH) {

            int category = data.getIntExtra("category", 0);

            PublishChannel publishChannel = new PublishChannel(Settings.getToken(getActivity().getApplicationContext()), category, Const.API_PUBLISH) {
                @Override
                protected void onPostExecute(BasicResponse response) {
                    super.onPostExecute(response);

                    if (response.code == 0)
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.successful_publish), Toast.LENGTH_SHORT).show();
                    else if (response.code == 2)
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.repeated_publish), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.unsuccessful_publish), Toast.LENGTH_SHORT).show();
                }
            };

            publishChannel.execute();
        }

        if (requestCode == Const.INTENT_RESULT_UNPUBLISH) {

            int category = data.getIntExtra("category", 0);

            PublishChannel publishChannel = new PublishChannel(Settings.getToken(getActivity().getApplicationContext()), category, Const.API_UNPUBLISH) {
                @Override
                protected void onPostExecute(BasicResponse response) {
                    super.onPostExecute(response);

                    if (response.code == 0)
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.successful_unpublish), Toast.LENGTH_SHORT).show();
                    else if (response.code == 2)
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.repeated_unpublish), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.unsuccessful_unpublish), Toast.LENGTH_SHORT).show();
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

                Point point = new Point();
                point.uuid = deleteUuid;
                point.categoryId = deleteCategoryId;

                PointsDelete pointsDelete = new PointsDelete(Settings.getToken(getActivity().getApplicationContext()), point) {
                    @Override
                    protected void onPostExecute(BasicResponse response) {
                        super.onPostExecute(response);
                        if (response.code == 0)
                            deleteMarker(getCurrentSelectedMarker());
                        else {
                            Toast.makeText(getActivity().getApplicationContext(), R.string.unsuccessful_edit,Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                pointsDelete.execute();
            }

            //System.out.print("!!!!! POINTS ADD RESULT 1 !!!!!");

            PointsAdd pointsAdd = new PointsAdd(Settings.getToken(getActivity().getApplicationContext()),
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
            /*if (!isInternetConnectionAvailable()) {
                /*GetsDbHelper dbSaveHelper = new GetsDbHelper(getActivity().getApplicationContext(), DatabaseType.USER_GENERATED);
                dbSaveHelper.addPoint(pointCategory,
                        pointName,
                        pointUrl,
                        "?",
                        System.currentTimeMillis() + "",
                        "",
                        latitude,
                        longitude,
                        rating,
                        "?");*/
                /*
                point.name = pointName;
                point.url = pointUrl;
                point.latitude = latitude;
                point.longitude = longitude;
                point.rating = rating;
                point.categoryId = pointCategory;

                if (mMapboxMap != null) {
                    addMarker(point);
                    GetsDbHelper dbSaveHelper = new GetsDbHelper(getActivity().getApplicationContext(), DatabaseType.USER_GENERATED);
                    dbSaveHelper.addPoint(point);
                }
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.saved_to_local_db), Toast.LENGTH_SHORT).show();
            } else {*/
                // Send to API
                pointsAdd.execute();
            //}

            try {
                PointsResponse response = pointsAdd.get();

                ArrayList<Point> responsePoints = new ArrayList<>();
                if (response != null) {
                    responsePoints = response.points;
                }

                if (response != null && response.code == 0) { // FIXME: codes
                    if (deleteUuid != null)
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.successful_edit), Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.successuflly_sent), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    return;
                }
                try {
                    //point = response.points.get(0);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                if (mMapboxMap != null)
                    addMarkers(responsePoints);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

           /* mMapView.getController().animateTo(
                    new LatLng(point.latitude, point.longitude, 16)
            );*/
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
                mMapboxMap.removeAnnotations();
                ArrayList<Point> points;
                //mMapView.clear();
                if (MainActivity.getCategoryArray() != null) {
                    categoryArrayList = MainActivity.getCategoryArray();
                    for (int i = 0; i < categoryArrayList.size(); i++) {
                        if (Settings.getIsChecked(getActivity().getApplicationContext(), categoryArrayList.get(i).id)) {
                            points = dbHelper.getPoints(categoryArrayList.get(i).id);
                            if (points != null) {
                                addMarkers(points);
                                //for (Point point : points)
                                //    addMarker(point);
                            }
                        }
                    }
                } else {
                    points = dbHelper.getPoints(-1);
                    if (points != null)
                        addMarkers(points);
                }
            }
        }

        if (requestCode == Const.INTENT_RESULT_TOKEN) {
            // Save token
            Settings.saveString(getActivity().getApplicationContext(), Const.PREFS_AUTH_TOKEN, data.getStringExtra("token"));
            checkUserStatus();
            loadPoints();
        }
    }



    public Context getContext() {
        Context tempContext = getActivity().getApplicationContext();
        return tempContext;
    }

    private void checkGraphUpdate() {
        String dataPath = Settings.getStorageDir(getActivity().getApplicationContext());

        if (dataPath == null) {
            DirUtil.StorageDirDesc[] contentPaths = DirUtil.getPrivateStorageDirs(getActivity());
            dataPath = contentPaths[0].path;
            Settings.saveString(getActivity().getApplicationContext(), Const.PREF_STORAGE_PATH, dataPath);
        }

        File unpackedRootDir = new File(Settings.getStorageDir(getActivity().getApplicationContext()), "/unpacked");
        if (!unpackedRootDir.exists())
            unpackedRootDir.mkdir();

        DownloadXmlTask downloadXmlTask = new DownloadXmlTask(Const.URL_ROOT_XML) {
            @Override
            protected void onPostExecute(String response) {
                if (response != null) {
                    XmlUtil xml = new XmlUtil(response);
                    final String newHash = xml.getHashByRegionId(Const.ID_REGION_KARELIA);
                    final String oldHash = Settings.getMapHash(getActivity().getApplicationContext());
                    final String fileName = xml.getFileNameByRegionId(Const.ID_REGION_KARELIA);
                    final String url = xml.getUrlByRegionId(Const.ID_REGION_KARELIA);

                    if (newHash == null)
                        return;

                    if (oldHash == null || !oldHash.equals(newHash) || !new File(Settings.getStorageDir(getActivity().getApplicationContext()),
                            fileName).exists()) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder
                                .setMessage(R.string.text_download_message)
                                .setCancelable(false)
                                .setPositiveButton(R.string.text_download_positive,
                                        new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialog, int id) {
                                                Toast.makeText(getActivity().getApplicationContext(), R.string.toast_download_start, Toast.LENGTH_SHORT).show();
                                                DownloadGraphTask downloadGraphTask = new DownloadGraphTask(url, fileName, getActivity().getApplicationContext()) {
                                                    @Override
                                                    protected void onPostExecute(Boolean success) {
                                                        if (success) {
                                                            Toast.makeText(getActivity().getApplicationContext(), R.string.toast_download_success, Toast.LENGTH_SHORT).show();
                                                            Settings.saveMapHash(getActivity().getApplicationContext(), newHash);
                                                        }
                                                        else Toast.makeText(getActivity().getApplicationContext(), R.string.toast_download_error, Toast.LENGTH_SHORT).show();
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
                    else if (new File(Settings.getStorageDir(getActivity().getApplicationContext()), fileName).exists())
                        DirUtil.unzipInStorage(getActivity().getApplicationContext(), fileName);

                }
            }
        };
        downloadXmlTask.execute();
    }


    public boolean isAuthorized() {
        return Settings.getToken(getActivity().getApplicationContext()) != null;
    }

    private void setMapPannable(boolean b) {
        mMapboxMap.getUiSettings().setZoomGesturesEnabled(b);
        mMapboxMap.getUiSettings().setScrollGesturesEnabled(b);
    }

    @UiThread
    public void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            if (!locationServices.areLocationPermissionsGranted()) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
            } else {
                enableLocation(true);
                mIsFollowingEnabled = true;
                setMapPannable(false);
                menu.findItem(R.id.follow_location).getIcon().
                        setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);
            }
        } else {
            enableLocation(false);
            mIsFollowingEnabled = false;
            setMapPannable(true);
            menu.findItem(R.id.follow_location).getIcon().setColorFilter(null);
        }
    }

    private void enableLocation(boolean enabled) {
        if (enabled) {
            locationServices.addLocationListener(mLocationListener);
        } else {
            locationServices.removeLocationListener(mLocationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_LOCATION: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableLocation(true);
                }
            }
        }
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
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        dbHelper.close();
        dbHelperSend.close();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}
