package oss.fruct.org.getssupplement;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.MapEventsOverlay;
import com.mapbox.mapboxsdk.overlay.MapEventsReceiver;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oss.fruct.org.getssupplement.Api.PointsAdd;
import oss.fruct.org.getssupplement.Database.GetsDbHelper;
import oss.fruct.org.getssupplement.Model.Category;
import oss.fruct.org.getssupplement.Model.DatabaseType;


public class AddNewPointActivity extends Activity {

    EditText etPointName;
    EditText etPointDescription;
    EditText etPointUrl;

    Spinner spPointCategory;
    ImageButton btAddNewCategory;

    private MapView mMap;
    ImageButton btLocation;
    ImageButton btZoomIn;
    ImageButton btZoomOut;

    GetsDbHelper mDbHelper;

    public Marker getChoosedLocation() {
        return choosedLocation;
    }

    public void setChoosedLocation(Marker _choosedLocation) {
        this.choosedLocation = _choosedLocation;
    }

    Marker choosedLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewpoint);

        etPointName = (EditText) findViewById(R.id.activity_addpoint_name);
        etPointDescription = (EditText) findViewById(R.id.activity_addpoint_description);
        etPointUrl = (EditText) findViewById(R.id.activity_addpoint_url);

        spPointCategory = (Spinner) findViewById(R.id.activity_addpoint_category_spinner);
        btAddNewCategory = (ImageButton) findViewById(R.id.activity_addpoint_category_new);

        mMap = (MapView) findViewById(R.id.activity_addpoint_mapview);
        btLocation = (ImageButton) findViewById(R.id.activity_addpoint_location);
        btLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MapActivity.getLocation() != null) {
                    LatLng myLocation = new LatLng(MapActivity.getLocation().getLatitude(), MapActivity.getLocation().getLongitude());
                    mMap.getController().animateTo(myLocation);
                }
            }
        });

        btZoomIn = (ImageButton) findViewById(R.id.activity_addpoint_zoom_in);
        btZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.zoomIn();
            }
        });

        btZoomOut = (ImageButton) findViewById(R.id.activity_addpoint_zoom_out);
        btZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.zoomOut();
            }
        });

        mDbHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.DATA_FROM_API);

        prepareSpinner();

        prepareMap();

        // Check for Internet connection, show warning
        if (!isInternetConnectionAvailable()) {
            findViewById(R.id.activity_addpoint_no_network).setVisibility(View.VISIBLE);
        }

    }

    private void prepareSpinner() {
        ArrayList<Category> categories = mDbHelper.getCategories();
        Log.d(Const.TAG, "Spinner length: " + categories.size());

        if (categories == null)
            return;

        List<String> spinnerItems = new ArrayList<String>();
        for (Category category : categories) {
            spinnerItems.add(category.name);
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPointCategory.setAdapter(adapter);
    }

    ArrayAdapter<String> adapter;

    private int findCategoryIdByName(String categoryName) {

        return 1;
    }

    private void prepareMap() {
        mMap = (MapView) findViewById(R.id.activity_addpoint_mapview);

        mMap.setClickable(true);
        mMap.setUserLocationEnabled(true);

        mMap.getController().setZoom(15);
        mMap.setUseDataConnection(true);

        if (MapActivity.getLocation() != null) {
            double latitude =  MapActivity.getLocation().getLatitude();
            double longitude =  MapActivity.getLocation().getLongitude();

            LatLng myLocation = new LatLng(latitude, longitude);

            mMap.setCenter(myLocation);
            mMap.setZoom(17);

            addMaker(myLocation);
        }

        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapUpHelper(ILatLng iLatLng) {
                addMaker(new LatLng(iLatLng.getLatitude(), iLatLng.getLongitude()));
                //Toast.makeText(getApplicationContext(), "Single tap " + iLatLng, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean longPressHelper(ILatLng iLatLng) {
                Toast.makeText(getApplicationContext(), "Long press", Toast.LENGTH_SHORT).show();
                return false;
            }
        };

        mMap.addOverlay(new MapEventsOverlay(getApplicationContext(), mapEventsReceiver));

    }

    Overlay markerOverlay;

    private void addMaker(LatLng position) {

        //if (getChoosedLocation() != null)
        //    setChoosedLocation(null);
        if (choosedLocation != null)
            choosedLocation.setPoint(position);
        else {
            setChoosedLocation(new Marker(mMap, "", "", position));
            getChoosedLocation().setIcon(new Icon(getApplicationContext(), Icon.Size.LARGE, "marker-stroked", "000000"));
            mMap.addMarker(getChoosedLocation());
        }
    }

    private boolean isInternetConnectionAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
            return true;

        return false;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_new_point, menu);
        return true;
    }

    private boolean isStringEmpty(String stringToCheck) {
        if (stringToCheck == null || stringToCheck.equals("") || stringToCheck.isEmpty())
            return true;
        else
            return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            String pointName = etPointName.getText().toString();

            if (isStringEmpty(pointName)) {
                // TODO animations, bells, whistles etc
                return false;
            }

            String pointDescription = etPointDescription.getText().toString();
            String pointUrl = etPointUrl.getText().toString();
            int categoryId = findCategoryIdByName(spPointCategory.getSelectedItem().toString());

            Log.d(Const.TAG, "Choosed location: " + choosedLocation);
            LatLng markerLocation = getChoosedLocation().getPoint();


            // Save to local database when no Internet connection
            if (isInternetConnectionAvailable()) {
                GetsDbHelper dbHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.USER_GENERATED);
                dbHelper.addPoint(pointName, pointUrl, "???", System.currentTimeMillis()/1000L, markerLocation.getLatitude(), markerLocation.getLongitude()); // FIXME
                Toast.makeText(getApplicationContext(), getString(R.string.saved_to_local_db), Toast.LENGTH_SHORT).show();
                return true;
            }

            PointsAdd pointsAdd = new PointsAdd(Settings.getToken(getApplicationContext()),
                    categoryId,
                    pointName,
                    pointDescription,
                    pointUrl,
                    markerLocation.getLatitude(),
                    markerLocation.getLongitude(),
                    markerLocation.getAltitude(), // FIXME
                    System.currentTimeMillis() / 1000L
            );

            pointsAdd.execute();


            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
