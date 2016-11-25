package org.fruct.oss.getssupplement;

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
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.gms.appindexing.Action;
//import com.google.android.gms.appindexing.AppIndex;
//import com.google.android.gms.common.api.GoogleApiClient;
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

import org.fruct.oss.getssupplement.Adapter.SlidingMenuAdapter;
import org.fruct.oss.getssupplement.Api.CategoriesGet;
import org.fruct.oss.getssupplement.Api.PointsAdd;
import org.fruct.oss.getssupplement.Api.PointsGet;
import org.fruct.oss.getssupplement.Api.PointsDelete;
import org.fruct.oss.getssupplement.Api.PublishChannel;
import org.fruct.oss.getssupplement.Api.UserInfoGet;
import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Fragment.MapFragment;
import org.fruct.oss.getssupplement.Model.BasicResponse;
import org.fruct.oss.getssupplement.Fragment.InfoFragment;
import org.fruct.oss.getssupplement.Fragment.PointsListFragment;
import org.fruct.oss.getssupplement.Model.CategoriesResponse;
import org.fruct.oss.getssupplement.Model.Category;
import org.fruct.oss.getssupplement.Model.DatabaseType;
import org.fruct.oss.getssupplement.Model.ItemSlideMenu;
import org.fruct.oss.getssupplement.Model.Point;
import org.fruct.oss.getssupplement.Model.PointsResponse;
import org.fruct.oss.getssupplement.R;
import org.fruct.oss.getssupplement.Model.UserInfoResponse;
import org.fruct.oss.getssupplement.Utils.DirUtil;
import org.fruct.oss.getssupplement.Utils.DownloadGraphTask;
import org.fruct.oss.getssupplement.Utils.DownloadXmlTask;
import org.fruct.oss.getssupplement.Utils.XmlUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Yaroslav21 on 27.07.16.
 */
public class MainActivity extends Activity {//} implements LocationListener {

    private List<ItemSlideMenu> listSliding;
    private SlidingMenuAdapter adapter;
    private ListView listViewSliding;
    private DrawerLayout drawerLayout;

    private MapView mMapView;
    //private MapboxMap mMapboxMap;

    private LocationServices locationServices;
    private GetsDbHelper dbHelper, dbHelperSend;

    ProgressBar progressBar;
    private static Location sLocation;
    private static boolean successLoading = false;
    private static ArrayList<Category> categoryArrayList;
    private static ArrayList<Point> pointsArrayList;

    //private LocationManager locationManager;
    //private  LocationProvider gpsProvider;
    //private LocationProvider networkProvider;

    public static Location getLocation() {
        return sLocation;
    }
    private Menu menu;

    //private RelativeLayout mainContent;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        //dbHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.DATA_FROM_API);
        dbHelper = GetsDbHelper.getApiHelper(getApplicationContext());


        //mMapView = (MapView) findViewById(R.id.activity_map_mapview);
        //mMapView.onCreate(savedInstanceState);


        /*mMapView = (MapView) findViewById(R.id.activity_map_mapview);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                MainActivity.this.mMapboxMap = mapboxMap;
                //setUpMapView();


            }
        });*/
        locationServices = LocationServices.getLocationServices(this);

        if (!isAuthorized()) {
            if (isInternetConnectionAvailable()) {
                //Intent i =
                new Intent(MainActivity.this, LoginActivity.class);
                //startActivityForResult(i, Const.INTENT_RESULT_TOKEN);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.network_error_authorization), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        downloadPoints();

        //Init component
        listViewSliding = (ListView) findViewById(R.id.lv_sliding_menu);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //mainContent = (RelativeLayout)findViewById(R.id.main_content);
        listSliding = new ArrayList<>();
        //Add item for sliding list
        listSliding.add(new ItemSlideMenu(R.drawable.ic_action_more, "Карта"));
        listSliding.add(new ItemSlideMenu(R.drawable.ic_info, "О приложении"));
        listSliding.add(new ItemSlideMenu(R.mipmap.ic_launcher, "Точки оффлайн"));
        adapter = new SlidingMenuAdapter(this, listSliding);
        listViewSliding.setAdapter(adapter);

        //Display icon to open/close sliding list
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //Set title
        setTitle(listSliding.get(0).getTitle());
        //Item selected
        listViewSliding.setItemChecked(0, true);
        //Close menu
        drawerLayout.closeDrawer(listViewSliding);

        //Display fragment 1 when start
        replaceFragment(0);
        //Handle on item click

        listViewSliding.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Set title
                setTitle(listSliding.get(position).getTitle());
                //Item selected
                listViewSliding.setItemChecked(position, true);
                //Replace fragment
                replaceFragment(position);
                //Close menu
                drawerLayout.closeDrawer(listViewSliding);
            }
        });

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_opened, R.string.drawer_closed) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        //dbHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.DATA_FROM_API);
        dbHelper = GetsDbHelper.getApiHelper(getApplicationContext());
        //dbHelperSend = new GetsDbHelper(getApplicationContext(), DatabaseType.DATA_FROM_API);
        dbHelperSend = GetsDbHelper.getApiHelper(getApplicationContext());
        //downloadPoints();

        PointsAdd p = new PointsAdd(dbHelperSend);
        p.execute();
    }

    public boolean isAuthorized() {
        return Settings.getToken(getApplicationContext()) != null;
    }

    public boolean isInternetConnectionAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        return false;
    }

    public static boolean isSuccessLoading() {
        return successLoading;
    }

    public static ArrayList<Category> getCategoryArray() {
        return categoryArrayList;
    }

    public static ArrayList<Point> getPointsArray() {
        return pointsArrayList;
    }

    private void downloadPoints() {
        Location location = locationServices.getLastLocation(); //mMapboxMap.getMyLocation();

        if (location == null) {
            System.out.print("NULL LOCATION!!!!!!\n");
            return;
        }

        final PointsGet pointsGet = new PointsGet(Settings.getToken(getApplicationContext()),
                location.getLatitude(), location.getLongitude(),
                Const.API_POINTS_RADIUS) {
            @Override
            public void onPostExecute(final PointsResponse response) {
                if (response == null) {
                    return;
                }
                Toast.makeText(getApplicationContext(), getString(R.string.successful_download), Toast.LENGTH_SHORT).show();
                //progressBar.setVisibility(ProgressBar.INVISIBLE);
                successLoading = true;
                pointsArrayList = response.points;


                //System.out.print("14212412321\n");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dbHelper.clearDatabase();
                        dbHelper.addPoints(response.points);
                    }
                }).start();
                //if (mMapboxMap != null) {
                //}
            }
        };

        final CategoriesGet categoriesGet = new CategoriesGet(Settings.getToken(getApplicationContext())) {
            @Override
            public void onPostExecute(CategoriesResponse response) {
                if (response == null) {
                    return;
                }

                if (response.categories != null)

                categoryArrayList = response.categories;
                dbHelper.addCategories(response.categories);
                if (menu != null) {
                    MenuItem miActions = menu.findItem(R.id.action_category_actions);
                    if (miActions != null)
                        miActions.setEnabled(true);
                }
                //System.out.print("14212412320\n");
                pointsGet.execute();
            }
        };
        categoriesGet.execute();
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main_menu, menu);
        getMenuInflater().inflate(R.menu.menu_map, menu);
        this.menu = menu;
        return true;
    }*/


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    //Create method replace fragment

    private void replaceFragment(int pos) {
        Fragment fragment = null;
        switch (pos) {
            case 0:
                fragment = new MapFragment();
                break;
            case 1:
                fragment = new InfoFragment();
                break;
            case 2:
                fragment = new PointsListFragment();
                break;
            default:
                fragment = new MapFragment();
                break;
        }

        if (null != fragment) {
            android.app.FragmentManager fragmentManager = getFragmentManager();
            android.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.main_content, fragment);
            //transaction.addToBackStack(null);
            transaction.commit();
        }
    }
/*
    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://org.fruct.oss.getssupplement/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://org.fruct.oss.getssupplement/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
    */

    /* выводим информацию о корзине
    public void showResult () {
        Fragment fragmentListAdd = (FragmentListAdd) getFragmentManager().findFragmentById(R.layout.fragment_layout_points);
        fragmentListAdd.deletePoints();
    }*/


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
        dbHelperSend.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
