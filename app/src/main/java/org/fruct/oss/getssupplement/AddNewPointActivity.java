package org.fruct.oss.getssupplement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.DatabaseType;
import org.fruct.oss.getssupplement.Model.Point;
import org.fruct.oss.getssupplement.Utils.GHUtil;

import java.io.File;

/**
 * Created by Andrey on 18.07.2015.
 */
public class AddNewPointActivity extends Activity {

    private MapView mMapView;
    private MapboxMap mMapboxMap;

    private boolean isInEdit;
    private String deleteUuid;
    private int deleteCategoryId;
    private RatingBar rbRating;
    private int category = -1;
    private Button btCategory;

    private ImageButton btLocation, btZoomIn, btZoomOut;
    private TextView mCategoryDescription;
    private CheckBox cbMagnet;
    private TextView tvMagnet;

    private GHUtil gu;
    private GetsDbHelper mDbHelper;
    private int closestStreetId = -1;
    private Marker choosedLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewpoint);

        mDbHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.DATA_FROM_API);
        mCategoryDescription = (TextView) findViewById(R.id.activity_addpoint_category_description);
        rbRating = (RatingBar) findViewById(R.id.activity_addpoint_ratingbar);
        btCategory = (Button) findViewById(R.id.activity_addpoint_category);

        btCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CategoryActivity.class);
                startActivityForResult(i, Const.INTENT_RESULT_CATEGORY);
            }
        });

        btLocation = (ImageButton) findViewById(R.id.activity_addpoint_location);
        btZoomIn = (ImageButton) findViewById(R.id.activity_addpoint_zoom_in);
        btZoomOut = (ImageButton) findViewById(R.id.activity_addpoint_zoom_out);
        cbMagnet = (CheckBox) findViewById(R.id.activity_addpoint_magnet_check);
        tvMagnet = (TextView) findViewById(R.id.activity_addpoint_magnet_text);

        mMapView = (MapView) findViewById(R.id.activity_addpoint_mapview);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                AddNewPointActivity.this.mMapboxMap = mapboxMap;
                setListeners();
                initGh();
                prepareMap();
            }
        });
    }

    private void initGh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File unpackedRootDir = new File(Settings.getStorageDir(getApplicationContext()), "/unpacked");
                gu = new GHUtil(unpackedRootDir.getAbsolutePath());
            }
        }).start();
    }

    private void prepareMap() {
        // TODO: was ist das?
//        mMapView.setUserLocationEnabled(true);
        Intent intent = getIntent();
        double optimalZoom = intent.getDoubleExtra("zoomLevel", 16);
        isInEdit = intent.getBooleanExtra("isInEdit", false);
        double latitude;
        double longitude;
        LatLng myLocation;
        float ratingValue;
        String pointName;
        String description;
        String token;
        String categoryName;

        // If activity is opened as edit form
        if (isInEdit) {
            latitude = intent.getDoubleExtra("latitude", 0);
            longitude = intent.getDoubleExtra("longitude", 0);

            myLocation = new LatLng(latitude, longitude);
            //TODO: maybe fix by new mapbox feature
//            mMapView.setCenter(myLocation);
            mMapboxMap.setCameraPosition(new CameraPosition.Builder().target(myLocation).build());
            addMaker(myLocation);

            deleteCategoryId = intent.getIntExtra("categoryId", 0);
            ratingValue = intent.getFloatExtra("rating", 0);
            pointName = intent.getStringExtra("name");
            description = intent.getStringExtra("description");
            deleteUuid = intent.getStringExtra("uuid");
            token = intent.getStringExtra("token");


            EditText Point_name = (EditText) findViewById(R.id.activity_addpoint_name);
            categoryName = mDbHelper.getCategoryName(deleteCategoryId);

            // Set values
            Point_name.setText(pointName);
            rbRating.setRating(ratingValue);
            if (description != null && !description.equals("{}"))
                mCategoryDescription.setText(description);
            btCategory.setText(getString(R.string.category) + " " + categoryName);
            setCategory(deleteCategoryId);
        } else if (MapActivity.getLocation() != null) {
            latitude = MapActivity.getLocation().getLatitude();
            longitude = MapActivity.getLocation().getLongitude();
            myLocation = new LatLng(latitude, longitude);
//            mMapView.setCenter(myLocation);
            //TODO: maybe fix by new mapbox feature
            mMapboxMap.setCameraPosition(new CameraPosition.Builder().target(myLocation).build());
            addMaker(myLocation);
        }

        mMapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng point) {
                addMaker(point);
            }
        });
/*
        if (mMapView.getMaxZoomLevel() < optimalZoom)
            mMapView.getController().setZoom(mMapView.getMaxZoomLevel());
        else
            mMapView.getController().setZoom(optimalZoom);

        mMapView.setUseDataConnection(true);

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

        mMapView.addOverlay(new MapEventsOverlay(getApplicationContext(), mapEventsReceiver));
*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_addnewpoint, menu);
        return true;
    }


    private void addMaker(LatLng position) {

        if (cbMagnet.isChecked())
            position = attract(position);

        if (choosedLocation != null)
            choosedLocation.setPosition(position);
        else {
            IconFactory iconFactory = IconFactory.getInstance(AddNewPointActivity.this);
            Marker marker = mMapboxMap.addMarker(new MarkerOptions()
                    .position(position)
                    .icon(iconFactory.defaultMarker()));
            setChoosedLocation(marker);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = new Intent();

        if (id == R.id.action_done_adding) {
            EditText Point_name = (EditText) findViewById(R.id.activity_addpoint_name);
            String pointName = Point_name.getText().toString();
            float ratingValue = rbRating.getRating();

            if (ratingValue == 0f || getCategory() == -1) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_name_description_url), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!pointName.isEmpty())
                intent.putExtra("name", pointName);
            else {
                String name = mDbHelper.getCategoryName(getCategory());
                intent.putExtra("name", name);
            }

            LatLng markerLocation = getChoosedLocation().getPosition();
            intent.putExtra("latitude", markerLocation.getLatitude());
            intent.putExtra("longitude", markerLocation.getLongitude());
            intent.putExtra("category", getCategory());
            intent.putExtra("rating", ratingValue);
            intent.putExtra("streetId", closestStreetId);
            if (isInEdit) {
                intent.putExtra("deleteUuid", deleteUuid);
                intent.putExtra("deleteCategoryId", deleteCategoryId);
            }
            setResult(Const.INTENT_RESULT_CODE_OK, intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setListeners() {
        btLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MapActivity.getLocation() != null) {
                    LatLng myLocation = new LatLng(MapActivity.getLocation().getLatitude(), MapActivity.getLocation().getLongitude());
                    CameraPosition position = new CameraPosition.Builder()
                            .target(myLocation)
                            .build();
                    mMapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), 2000);
                }
            }
        });

        btZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double currentZoom = mMapboxMap.getCameraPosition().zoom;
                CameraPosition position = new CameraPosition.Builder()
                        .zoom(currentZoom + 1).build();
                mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 500);
            }
        });

        btZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double currentZoom = mMapboxMap.getCameraPosition().zoom;
                CameraPosition position = new CameraPosition.Builder()
                        .zoom(currentZoom - 1).build();
                mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 500);
            }
        });

        cbMagnet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (gu == null || gu.getGH() == null) {
                    cbMagnet.setChecked(false);
                    return;
                }
                if (isChecked) {
                    Point point = mDbHelper.getPointByMarkerId(getChoosedLocation().getId());
                    if (point != null) {
                        LatLng coords = attract(new LatLng(point.latitude, point.longitude));
                        if (coords != null)
                            addMaker(coords);
                        else
                            closestStreetId = -1;
                    }
                } else
                    closestStreetId = -1;
            }
        });

        tvMagnet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cbMagnet.setChecked(!cbMagnet.isChecked());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED) {
            if (resultCode != Const.INTENT_RESULT_CODE_OK || data == null) {
                return;
            }
            int categoryId = data.getIntExtra("category", -1);
            String name = data.getStringExtra("name");
            String description = data.getStringExtra("description");
            setCategory(categoryId);

            if (btCategory != null) {
                if (name != null)
                    btCategory.setText(getString(R.string.category) + " " + name);
                else
                    btCategory.setText(getString(R.string.category));
            }
            if (mCategoryDescription != null) {
                if (description != null)
                    mCategoryDescription.setText(description);
                else
                    mCategoryDescription.setText("");
            }
        }
    }

    private LatLng attract(LatLng point) {
        if (gu != null && gu.getGH() != null) {
            point = gu.getClosestPoint(point);
            if (gu.getClosestStreet() != null && !gu.getClosestStreet().isEmpty() && !gu.getClosestStreet().startsWith(" "))
                Toast.makeText(getApplicationContext(), gu.getClosestStreet(), Toast.LENGTH_SHORT).show();
            this.closestStreetId = gu.getClosestStreetId();
        }
        return point;
    }

    public int getCategory() {
        return this.category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public Marker getChoosedLocation() {
        return choosedLocation;
    }

    public void setChoosedLocation(Marker _choosedLocation) {
        this.choosedLocation = _choosedLocation;
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
        mDbHelper.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
}
