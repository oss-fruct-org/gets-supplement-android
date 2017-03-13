package org.fruct.oss.getssupplement.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import org.fruct.oss.getssupplement.R;

/**
 * Created by Andrey on 29.11.2015.
 */
public class AppInfoActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView nvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);

        setToolbar();
        setNavigation();

        TextView textView = (TextView) findViewById(R.id.app_info_text);

        if (textView != null) {
            String text;
            text = (String) textView.getText();

            try {
                text += " ";
                text += getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            }
            catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            textView.setText(text);
        }
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.menu_title_info));
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
                        i.setClass(AppInfoActivity.this, MapActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_profile:
                        i.setClass(AppInfoActivity.this, LoginActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_queue:
                        i.setClass(AppInfoActivity.this, QueueActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_policy:
                        i.setClass(AppInfoActivity.this, PolicyActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_info:
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
        nvMain.getMenu().getItem(4).setChecked(true);
    }
}

