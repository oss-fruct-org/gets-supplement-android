package org.fruct.oss.getssupplement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.Category;

import java.util.ArrayList;

/**
 * Created by Andrey on 18.07.2015.
 */
public class CategoryActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        //GetsDbHelper dbHelper = new GetsDbHelper(getApplicationContext(), DatabaseType.DATA_FROM_API);
        GetsDbHelper dbHelper = GetsDbHelper.getApiHelper(getApplicationContext());
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

        final CategoryArrayAdapter adapter = new CategoryArrayAdapter(this, names, id, false);

        ListView listView = (ListView) findViewById(R.id.activity_category_listview);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category category = categories.get(position);

                Intent i = new Intent();
                i.putExtra("name", category.name);
                i.putExtra("category", category.id);
                i.putExtra("description", category.description);
                setResult(Const.INTENT_RESULT_CODE_OK, i);

                finish();
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent i = new Intent();
            setResult(Const.INTENT_RESULT_CODE_NOT_OK, i);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
