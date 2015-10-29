package org.fruct.oss.getssupplement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.Category;
import org.fruct.oss.getssupplement.Model.DatabaseType;

import java.util.ArrayList;

/**
 * Created by Andrey on 28.10.2015.
 */
public class CategoryActionsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categoryactions);

        GetsDbHelper dbHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.DATA_FROM_API);
        final ArrayList<Category> categories = dbHelper.getCategories();

        ArrayList<String> names = new ArrayList<String>();
        ArrayList<Integer> id = new ArrayList<Integer>();

        try {
            for (Category category : categories) {
                names.add(category.name);
                id.add(category.id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final CategoryArrayAdapter adapter = new CategoryArrayAdapter(this, names, id, Const.SERVICE_ACTIONS);

        ListView listView = (ListView) findViewById(R.id.activity_categoryactions_listview);
        listView.setAdapter(adapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
