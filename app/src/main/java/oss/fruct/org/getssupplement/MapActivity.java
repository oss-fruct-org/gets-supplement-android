package oss.fruct.org.getssupplement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import oss.fruct.org.getssupplement.Api.CategoriesGet;
import oss.fruct.org.getssupplement.Database.GetsDbHelper;
import oss.fruct.org.getssupplement.Model.CategoriesResponse;


public class MapActivity extends Activity {

    static Context context;

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        context = getApplicationContext();

        if (!isAutuhorized()) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        } else {
            CategoriesGet categoriesGet = new CategoriesGet(Settings.getToken(getApplicationContext())) {
                @Override
                public void onPostExecute(CategoriesResponse response) {
                    GetsDbHelper dbHelper = new GetsDbHelper(getApplicationContext());
                    dbHelper.addCategories(response.categories);
                }
            };
            //categoriesGet.execute();
        }

        mapView = (MapView) findViewById(R.id.map_view);
        mapView = new MapView(this, 256); //constructor
        setUpMapView();

    }

    private void setUpMapView() {
        mapView.setClickable(true);
        //mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        //setContentView(mapView);
        mapView.getController().setZoom(15);
        mapView.getController().setCenter(new GeoPoint(52.221, 6.893));
        mapView.setUseDataConnection(true); // TODO: offline mode

    }


    public static boolean isAutuhorized() {
        Log.d(Const.TAG + " token", Settings.getToken(context) + "  ");
        return Settings.getToken(context) != null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
