package org.fruct.oss.getssupplement;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.fruct.oss.getssupplement.Api.AuthStepOne;
import org.fruct.oss.getssupplement.Api.AuthStepTwo;
import org.fruct.oss.getssupplement.Api.UserInfoGet;
import org.fruct.oss.getssupplement.Model.LoginResponse;
import org.fruct.oss.getssupplement.Model.UserInfoResponse;

public class LoginActivity extends AppCompatActivity {

    private WebView webView;
    private TextView tvUsername, tvEmail, tvTrusted;
    private LinearLayout llContainer;

    private String responseId;

    private Toolbar toolbar;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setToolbar();
        setNavigation();

        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        tvTrusted = (TextView) findViewById(R.id.tvTrusted);
        llContainer = (LinearLayout) findViewById(R.id.llContainer);

        webView = (WebView) findViewById(R.id.login_activity_webview);
        webView.setWebViewClient(authWebViewClient);
        webView.getSettings().setJavaScriptEnabled(true);

        String token = Settings.getToken(this);
        if (token == null || token.equals("")) {
            AuthStepOne authStepOne = new AuthStepOne() {
                @Override
                public void onPostExecute(LoginResponse loginResponse) {
                    if (loginResponse == null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_authorization), Toast.LENGTH_SHORT).show();
                        recreate();
                        return;
                    }

                    setResponseId(loginResponse.id);

                    webView.loadUrl(loginResponse.redirectUrl);
                }
            };

            authStepOne.execute();
        } else {

            loadUserInfo(token);
        }
    }

    private void loadUserInfo(String token) {
        webView.setVisibility(View.GONE);
        llContainer.setVisibility(View.VISIBLE);

        UserInfoGet userInfoGet = new UserInfoGet(token) {
            @Override
            protected void onPostExecute(UserInfoResponse userInfoResponse) {
                super.onPostExecute(userInfoResponse);

                if (userInfoResponse != null && userInfoResponse.isTrustedUser) {
                    Settings.saveBoolean(getApplicationContext(), Const.PREFS_IS_TRUSTED_USER, true);
                } else {
                    Settings.saveBoolean(getApplicationContext(), Const.PREFS_IS_TRUSTED_USER, false);
                }

                setUserInfoViews(userInfoResponse);
            }
        };
        userInfoGet.execute();
    }

    private void setUserInfoViews(UserInfoResponse user) {
        if (user.name != null)
            tvUsername.setText(String.format(getString(R.string.user_info_name), user.name));
        else
            tvUsername.setVisibility(View.GONE);
        if (user.email != null)
            tvEmail.setText(String.format(getString(R.string.user_info_email), user.email));
        else
            tvEmail.setVisibility(View.GONE);
        if (user.isTrustedUser)
            tvTrusted.setText(String.format(getString(R.string.user_info_trusted), "Да"));
        else
            tvTrusted.setText(String.format(getString(R.string.user_info_trusted), "Нет"));

    }


    // When redirected, make next query
    WebViewClient authWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            AuthStepTwo authStepTwo = new AuthStepTwo(getResponseId()) {
                @Override
                public void onPostExecute(LoginResponse loginResponse) {
                    if (loginResponse == null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_authorization), Toast.LENGTH_SHORT).show();
                        recreate();
                        return;
                    }

                    if (loginResponse.code != 0) {
                        return;
                    }

                    // Save token
                    Settings.saveString(getApplicationContext(), Const.PREFS_AUTH_TOKEN, loginResponse.token);
                    // Close activity after obtaining token
                    Intent intent = new Intent();
                    intent.putExtra("token", loginResponse.token);
                    setResult(Const.INTENT_RESULT_CODE_OK, intent);
                    loadUserInfo(loginResponse.token);
                }

            };

            if (!url.startsWith("https://accounts.google.com/"))
                authStepTwo.execute();

            //finish();
            // return true; //Indicates WebView to NOT load the url;
            return false; //Allow WebView to load url
        }

    };

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.menu_title_profile));
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

        NavigationView nvMain = (NavigationView) findViewById(R.id.nvMain);
        nvMain.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                item.setChecked(true);

                Intent i = new Intent();
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                switch (item.getItemId()) {
                    case R.id.nav_map:
                        i.setClass(LoginActivity.this, MapActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_profile:
                        break;
                    case R.id.nav_queue:
                        i.setClass(LoginActivity.this, QueueActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_policy:
                        i.setClass(LoginActivity.this, PolicyActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_info:
                        i.setClass(LoginActivity.this, AppInfoActivity.class);
                        startActivity(i);
                        break;
                    default:
                        dl.closeDrawers();
                }

                dl.closeDrawers();
                return true;
            }
        });

        nvMain.getMenu().getItem(1).setChecked(true);
    }


    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private String getResponseId() {
        return this.responseId;
    }

    private void setResponseId(String newValue) {
        this.responseId = newValue;
    }
}
