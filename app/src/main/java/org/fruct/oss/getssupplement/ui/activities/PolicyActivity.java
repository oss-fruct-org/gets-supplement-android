package org.fruct.oss.getssupplement.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;

import org.fruct.oss.getssupplement.R;
import org.fruct.oss.getssupplement.Utils.Const;

/**
 * Created by Andrey on 12.03.2017.
 */

public class PolicyActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView nvMain;

    private WebView wvPolicy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);

        setToolbar();
        setNavigation();

        wvPolicy = (WebView) findViewById(R.id.wvPolicy);

        if (getResources().getConfiguration().locale.getLanguage().equals("ru"))
            wvPolicy.loadUrl(Const.PRIVACY_POLICY_URL_RU);
        else
            wvPolicy.loadUrl(Const.PRIVACY_POLICY_URL_EN);

    }


    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_privacy_policy));
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
                        i.setClass(PolicyActivity.this, MapActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_profile:
                        i.setClass(PolicyActivity.this, LoginActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_queue:
                        i.setClass(PolicyActivity.this, QueueActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_policy:
                        break;
                    case R.id.nav_info:
                        i.setClass(PolicyActivity.this, AppInfoActivity.class);
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
        nvMain.getMenu().getItem(3).setChecked(true);
    }
}
