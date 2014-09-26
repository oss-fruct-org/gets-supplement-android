package oss.fruct.org.getssupplement;

import android.app.Activity;
import android.graphics.drawable.Drawable;
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
    }

    private void prepareSpinner() {
        ArrayList<Category> categories = mDbHelper.getCategories();
        if (categories == null)
            return;

        List<String> spinnerItems = new ArrayList<String>();
        for (Category category : categories) {
            spinnerItems.add(category.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPointCategory.setAdapter(adapter);
    }

    private int findCategoryIdByName(String categoryName) {

        return -1;
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

        if (choosedLocation != null)
            mMap.getOverlays().remove(1); // FIXME: doesn' work

        Marker choosedLocation = new Marker(mMap, "Ala", "lala", position);
        choosedLocation.setIcon(new Icon(getApplicationContext(), Icon.Size.LARGE, "marker-stroked", "000000"));
        mMap.addMarker(choosedLocation);
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
                // TODO animations, whistles and so on
                return false;
            }

            String pointDescription = etPointDescription.getText().toString();
            String pointUrl = etPointUrl.getText().toString();
            int categoryId = findCategoryIdByName(spPointCategory.getSelectedItem().toString());

            // Get texts
            // Get location


            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
