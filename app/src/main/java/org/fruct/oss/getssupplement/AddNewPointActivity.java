package org.fruct.oss.getssupplement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.MapEventsOverlay;
import com.mapbox.mapboxsdk.overlay.MapEventsReceiver;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;

import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.DatabaseType;

/**
 * Created by Andrey on 18.07.2015.
 */
public class AddNewPointActivity extends Activity {

    RatingBar rbRating;
    private int category = -1;
    Button btCategory;

    ImageButton btLocation;
    ImageButton btZoomIn;
    ImageButton btZoomOut;

    TextView mCategoryDescription;

    private MapView mMap;

    public Marker getChoosedLocation() {
        return choosedLocation;
    }

    public void setChoosedLocation(Marker _choosedLocation) { this.choosedLocation = _choosedLocation; }

    Marker choosedLocation = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewpoint);

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

        prepareMap();
    }
    private void prepareMap() {
        mMap = (MapView) findViewById(R.id.activity_addpoint_mapview);

        mMap.setClickable(true);
        mMap.setUserLocationEnabled(true);

        Intent intent = getIntent();
        float optimalZoom = intent.getFloatExtra("zoomLevel", 16);

        if (mMap.getMaxZoomLevel() < optimalZoom)
            mMap.getController().setZoom(mMap.getMaxZoomLevel());
        else
            mMap.getController().setZoom(optimalZoom);

        mMap.setUseDataConnection(true);

        if (MapActivity.getLocation() != null) {
            double latitude =  MapActivity.getLocation().getLatitude();
            double longitude =  MapActivity.getLocation().getLongitude();

            LatLng myLocation = new LatLng(latitude, longitude);

            mMap.setCenter(myLocation);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_addnewpoint, menu);
        return true;
    }


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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent intent = new Intent();

        if (id == R.id.action_done){
            EditText Point_name = (EditText) findViewById(R.id.activity_addpoint_name);
            String pointName = Point_name.getText().toString();

            float ratingValue = rbRating.getRating();

            if (ratingValue == 0f || getCategory() == -1) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_name_description_url), Toast.LENGTH_SHORT).show();
                return false;
            }

            LatLng markerLocation = getChoosedLocation().getPoint();
            GetsDbHelper DbHelp = new GetsDbHelper(getApplicationContext(), DatabaseType.DATA_FROM_API);;
            if (!(pointName == null || pointName.equals("") || pointName.isEmpty()))
                intent.putExtra("name", pointName);
            else {
                String name = DbHelp.getCategoryName(getCategory());
                intent.putExtra("name", name);
            }


            intent.putExtra("latitude", markerLocation.getLatitude());
            intent.putExtra("longitude", markerLocation.getLongitude());
            intent.putExtra("category", getCategory());
            intent.putExtra("rating", ratingValue);
            setResult(Const.INTENT_RESULT_CODE_OK, intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public int getCategory(){
        return this.category;
    }
    public void setCategory(int category) {
        this.category = category;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Const.INTENT_RESULT_CODE_OK || data == null) {
            return;
        }

        int categoryId = data.getIntExtra("category", -1);
        String name = data.getStringExtra("name");
        String description = data.getStringExtra("description");
        setCategory(categoryId);

        btCategory.setText(getString(R.string.category) + " " + name);
        mCategoryDescription.setText(description);

    }
}
