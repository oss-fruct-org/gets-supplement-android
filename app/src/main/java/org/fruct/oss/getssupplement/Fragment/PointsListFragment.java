package org.fruct.oss.getssupplement.Fragment;/**
 * Created by Yaroslav21 on 27.07.16.
 */

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.fruct.oss.getssupplement.Adapter.ListArrayAdapter;
import org.fruct.oss.getssupplement.Api.PointsAdd;
import org.fruct.oss.getssupplement.Const;
import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.BasicContainerForPoints;
import org.fruct.oss.getssupplement.Model.DatabaseType;
import org.fruct.oss.getssupplement.Model.Point;
import org.fruct.oss.getssupplement.Model.PointsResponse;
import org.fruct.oss.getssupplement.R;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PointsListFragment extends Fragment {

    private GetsDbHelper dbHelper;
    private GetsDbHelper dbHelperSend;

    private ListArrayAdapter adapter;
    private View v;
    private ArrayList<BasicContainerForPoints> dataForShow;

    public PointsListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new ListArrayAdapter(getActivity(), getPointsContainer());

        // настраиваем список
        ListView lvMain = (ListView) v.findViewById(R.id.lvMain);
        lvMain.setAdapter(adapter);
    }

    private ArrayList<BasicContainerForPoints> getPointsContainer () {

        dataForShow = new ArrayList<>();

        dbHelper = new GetsDbHelper(getActivity().getApplicationContext(), DatabaseType.DATA_FROM_API);
        dbHelperSend = new GetsDbHelper(getActivity().getApplicationContext(), DatabaseType.DATA_FROM_API);
        SQLiteDatabase db = dbHelper.getRdData();
        Cursor cursor = db.query(true, Const.DB_TEMP_POINTS, null, null, null, null, null, null, null);

        //ArrayList<String> dataPoints = new ArrayList<>();

        while (cursor.moveToNext()) {

            BasicContainerForPoints tempCont = new BasicContainerForPoints();

            tempCont.id = cursor.getInt(cursor.getColumnIndex("_id"));
            tempCont.category = cursor.getInt(cursor.getColumnIndex("categoryId"));
            tempCont.title = cursor.getString(cursor.getColumnIndex("title"));
            tempCont.rating = cursor.getFloat(cursor.getColumnIndex("rating"));
            tempCont.latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            tempCont.longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            tempCont.time = cursor.getString(cursor.getColumnIndex("time"));
            tempCont.box = false;

            //dataPoints.add(tempCont.title);
            dataForShow.add(tempCont);
        }

        return dataForShow;
    }

    public void deletePoints () {
        String result = "Точки удалены";

        SQLiteDatabase db = dbHelper.getRdData();
        ArrayList<BasicContainerForPoints> deletePoints = adapter.getDataForDelete();

        for (BasicContainerForPoints deletePoint : deletePoints)
            db.delete(Const.DB_TEMP_POINTS, "_id = ?", new String[] {Integer.toString(deletePoint.id)});

        adapter.deleteFromAdapterList(deletePoints);

        adapter.notifyDataSetChanged();
        Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_menu_points, menu);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                        Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_layout_points, null);

        Button buttonConfirm = (Button) v.findViewById(R.id.buttonForDelete);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePoints();
            }
        });

        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.reload) {
            PointsAdd p = new PointsAdd(dbHelperSend);
            p.execute();
            adapter.reloadDb(getPointsContainer());
        }

        adapter.notifyDataSetChanged();
        Toast.makeText(getActivity(), "Reloaded", Toast.LENGTH_LONG).show();
        return super.onOptionsItemSelected(item);
    }
}
