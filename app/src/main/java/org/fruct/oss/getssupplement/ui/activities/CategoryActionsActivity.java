package org.fruct.oss.getssupplement.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.fruct.oss.getssupplement.ui.adapters.CategoryArrayAdapter;
import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.Category;
import org.fruct.oss.getssupplement.R;
import org.fruct.oss.getssupplement.Utils.Const;

import java.util.ArrayList;

/**
 * Created by Andrey on 28.10.2015.
 */
public class CategoryActionsActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_actions);

        setToolbar();

        //GetsDbHelper dbHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.DATA_FROM_API);
        GetsDbHelper dbHelper = GetsDbHelper.getInstance(getApplicationContext());
        final ArrayList<Category> categories = dbHelper.getCategories();

        ArrayList<String> names = new ArrayList<String>();
        ArrayList<Integer> id = new ArrayList<Integer>();

        try {
            for (Category category : categories) {
                names.add(category.getName());
                id.add(category.id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final CategoryArrayAdapter adapter = new CategoryArrayAdapter(this, names, id, true);

        ListView listView = (ListView) findViewById(R.id.activity_categoryactions_listview);
        listView.setAdapter(adapter);

    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_category_actions));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        setResult(Const.INTENT_RESULT_CODE_OK, intent);
        finish();
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
