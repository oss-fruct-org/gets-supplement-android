package org.fruct.oss.getssupplement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
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

import org.fruct.oss.getssupplement.Api.PointsAdd;
import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.Category;
import org.fruct.oss.getssupplement.Model.DatabaseType;


public class AddNewPointActivity extends Activity {

    EditText etPointName;
    //EditText etPointDescription;
    //EditText etPointUrl;
    RatingBar rbRating;

    //Spinner spPointCategory;
    //ImageButton btAddNewCategory;

    private MapView mMap;
    ImageButton btLocation;
    ImageButton btZoomIn;
    ImageButton btZoomOut;

    Button btCategory;

    GetsDbHelper mDbHelper;

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    private int category = -1;

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
        //etPointDescription = (EditText) findViewById(R.id.activity_addpoint_description);
        //etPointUrl = (EditText) findViewById(R.id.activity_addpoint_url);


        //spPointCategory = (Spinner) findViewById(R.id.activity_addpoint_category_spinner);
        //btAddNewCategory = (ImageButton) findViewById(R.id.activity_addpoint_category_new); // TODO
        btCategory = (Button) findViewById(R.id.activity_addpoint_category);
        btCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CategoryActivity.class);
                startActivityForResult(i, Const.INTENT_RESULT_CATEGORY);
            }
        });

        rbRating = (RatingBar) findViewById(R.id.activity_addpoint_ratingbar);

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


        hideKeyboard();

        //prepareSpinner();

        prepareMap();

        // Check for Internet connection, show warning
        if (!MapActivity.isInternetConnectionAvailable()) {
            findViewById(R.id.activity_addpoint_no_network).setVisibility(View.VISIBLE);
        }

    }
/*

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
*/

    ArrayAdapter<String> adapter;
/*
    private int findCategoryIdByName(String categoryName) {

        return 1;
    }*/

    private void prepareMap() {
        mMap = (MapView) findViewById(R.id.activity_addpoint_mapview);

        mMap.setClickable(true);
        mMap.setUserLocationEnabled(true);

        mMap.getController().setZoom(mMap.getMaxZoomLevel());
        mMap.setUseDataConnection(true);

        if (MapActivity.getLocation() != null) {
            double latitude =  MapActivity.getLocation().getLatitude();
            double longitude =  MapActivity.getLocation().getLongitude();

            LatLng myLocation = new LatLng(latitude, longitude);

            mMap.setCenter(myLocation);
            mMap.setZoom(mMap.getMaxZoomLevel());

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
                //Toast.makeText(getApplicationContext(), "Long press", Toast.LENGTH_SHORT).show();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_new_point, menu);
        return true;
    }

    public static boolean isStringEmpty(String stringToCheck) {
        if (stringToCheck == null || stringToCheck.equals("") || stringToCheck.isEmpty())
            return true;
        else
            return false;
    }

/*
    private boolean isUrl(String text) {
        return Patterns.WEB_URL.matcher(text).matches();
    }
*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent intent = new Intent();
            setResult(Const.INTENT_RESULT_CODE_NOT_OK, intent);
            //NavUtils.navigateUpFromSameTask(this);
            finish();
        }

        if (id == R.id.action_done) {
            String pointName = etPointName.getText().toString();

            float ratingValue = rbRating.getRating();

            if (ratingValue == 0f || getCategory() == -1) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_name_description_url), Toast.LENGTH_SHORT).show();
                return false;
            }


            //String pointDescription = etPointDescription.getText().toString();
            //String pointUrl = etPointUrl.getText().toString();
            /*if (pointUrl == null || pointUrl.equals("") || !isUrl(pointUrl)) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_valid_url), Toast.LENGTH_SHORT).show();
                return false;
            }*/


            //int categoryId = findCategoryIdByName(spPointCategory.getSelectedItem().toString());

            LatLng markerLocation = getChoosedLocation().getPoint();

            Intent intent = new Intent();

            if (!isStringEmpty(pointName))
                intent.putExtra("name", pointName);
            else {
                String name = mDbHelper.getCategoryName(getCategory());
                intent.putExtra("name", name);
            }

            //intent.putExtra("description", pointDescription);
            //intent.putExtra("url", pointUrl);
            intent.putExtra("latitude", markerLocation.getLatitude());
            intent.putExtra("longitude", markerLocation.getLongitude());
            //intent.putExtra("altitude", markerLocation.getAltitude());
            intent.putExtra("category", getCategory());
            intent.putExtra("rating", ratingValue);

            setResult(Const.INTENT_RESULT_CODE_OK, intent);
            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        etPointName.clearFocus();
        imm.hideSoftInputFromWindow(etPointName.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Const.INTENT_RESULT_CODE_OK) {
            // TODO
            return;
        }

        int categoryId = data.getIntExtra("category", -1);
        String name = data.getStringExtra("name");
        setCategory(categoryId);

        btCategory.setText("Category: " + name);

    }



}
