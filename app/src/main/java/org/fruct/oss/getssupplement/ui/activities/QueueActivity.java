package org.fruct.oss.getssupplement.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.fruct.oss.getssupplement.R;
import org.fruct.oss.getssupplement.ui.adapters.ExpandableCategoriesAdapter;

/**
 * Created by Andrey on 12.03.2017.
 */

public class QueueActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView nvMain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        setToolbar();
        setNavigation();
        setUpRecycler();
    }

    private void setUpRecycler() {
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rvCategories);
//        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ExpandableCategoriesAdapter());
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.menu_title_queue));
    }

    private void setNavigation() {
        final DrawerLayout dl = (DrawerLayout) findViewById(R.id.dlMain);
        mDrawerToggle = new ActionBarDrawerToggle(this,
                dl,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close);

        dl.addDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

        nvMain = (NavigationView) findViewById(R.id.nvMain);
        nvMain.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                item.setChecked(true);

                Intent i = new Intent();
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                switch (item.getItemId()) {
                    case R.id.nav_map:
                        i.setClass(QueueActivity.this, MapActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_profile:
                        i.setClass(QueueActivity.this, LoginActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_queue:
                        break;
                    case R.id.nav_policy:
                        i.setClass(QueueActivity.this, PolicyActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_info:
                        i.setClass(QueueActivity.this, AppInfoActivity.class);
                        startActivity(i);
                        break;
                    default:
                        dl.closeDrawers();
                }

                dl.closeDrawers();

                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        nvMain.getMenu().getItem(2).setChecked(true);
    }
}
