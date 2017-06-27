package org.fruct.oss.getssupplement.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import org.fruct.oss.getssupplement.Api.AuthStepTwo;
import org.fruct.oss.getssupplement.Api.UserInfoGet;
import org.fruct.oss.getssupplement.Model.LoginResponse;
import org.fruct.oss.getssupplement.Model.UserInfoResponse;
import org.fruct.oss.getssupplement.R;
import org.fruct.oss.getssupplement.Utils.Const;
import org.fruct.oss.getssupplement.Utils.Settings;
import org.fruct.oss.getssupplement.Utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private TextView tvUsername, tvEmail, tvTrusted;
    private LinearLayout llContainer;
    private Button btnLogout;
    private ProgressBar pbLoadInfo;
    private NavigationView nvMain;

    private String responseId;

    private Toolbar toolbar;

    private ActionBarDrawerToggle mDrawerToggle;

    private GoogleApiClient googleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private ProgressDialog mProgressDialog;



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
        pbLoadInfo = (ProgressBar) findViewById(R.id.pbLoadInfo);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.saveToken(LoginActivity.this, null);
                checkIsLoggedInAndLoad();
            }
        });

        checkIsLoggedInAndLoad();
    }

    private void checkIsLoggedInAndLoad() {
        String token = Settings.getToken(this);
        Log.d(getLocalClassName(), "Saved token=" + token);
        if (token == null || token.equals("")) {
//            llContainer.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);

            // exchange token
            AsyncTask<Void, Void, String> exchangeTask = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params2) {
                    try {
                        String strResponse = Utils.downloadUrl(Const.URL_GET_AUTH_PARAMS, "<request><params/></request>");
                        Log.d(getLocalClassName(), "Server answer: " + strResponse);
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        InputStream is = new ByteArrayInputStream(strResponse.getBytes("UTF-8"));
                        Document doc = dBuilder.parse(is);

                        doc.getDocumentElement().normalize();

                        NodeList nodeList = doc.getElementsByTagName("status");

                        for (int i = 0; i < nodeList.getLength(); i++) {
                            Element element = (Element) nodeList.item(i);
                            int code = Integer.parseInt(element.getElementsByTagName("code").item(0).getTextContent());
                            String message = element.getElementsByTagName("message").item(0).getTextContent();
                            if (code > 0) {
                                Log.d(getLocalClassName(), "Server return error code " + code + " with message: " + message);
                                return null;
                            }
                        }

                        nodeList = doc.getElementsByTagName("content");

                        for (int i = 0; i < nodeList.getLength(); i++) {
                            Element element = (Element) nodeList.item(i);
                            return element.getElementsByTagName("client_id").item(0).getTextContent();
                        }

                        return null;
                    } catch (IOException | ParserConfigurationException | SAXException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    Log.d(getClass().getSimpleName(), "Client ID: " + s);
                    if (s != null && !s.isEmpty()) {
                        setResponseId(s);
                        signIn();
                    }
                }
            };
            exchangeTask.execute();

        } else {
            loadUserInfo(token);
        }
    }

    private void signIn() {
        Log.d(getLocalClassName(), "Try to get auth for client ID: " + getResponseId());
        if (getResponseId() == null)
            return;

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
                .requestServerAuthCode(getResponseId(), true)
                .requestScopes(new Scope("https://www.googleapis.com/auth/plus.me"),
                        new Scope("https://www.googleapis.com/auth/plus.login"),
                        new Scope("https://www.googleapis.com/auth/drive"),
                        new Scope("https://www.googleapis.com/auth/userinfo.email"))
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();


        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Toast.makeText(getApplicationContext(), R.string.signed_out, Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void loadUserInfo(String token) {
        pbLoadInfo.setVisibility(View.VISIBLE);

        UserInfoGet userInfoGet = new UserInfoGet(token) {
            @Override
            protected void onPostExecute(UserInfoResponse userInfoResponse) {
                super.onPostExecute(userInfoResponse);

                if (userInfoResponse.code != 0) {
                    Log.d(getLocalClassName(), "Got error code " + userInfoResponse.code + " with message " + userInfoResponse.message);
                    signIn();
                }

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
        btnLogout.setVisibility(View.VISIBLE);
        llContainer.setVisibility(View.VISIBLE);
        pbLoadInfo.setVisibility(View.GONE);

        if (user == null) {
            Toast.makeText(this, getString(R.string.internal_error), Toast.LENGTH_SHORT).show();
            return;
        }

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
//    WebViewClient authWebViewClient = new WebViewClient() {
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//
//            AuthStepTwo authStepTwo = new AuthStepTwo(getResponseId()) {
//                @Override
//                public void onPostExecute(LoginResponse loginResponse) {
//                    if (loginResponse == null) {
//                        Toast.makeText(LoginActivity.this, getString(R.string.error_authorization), Toast.LENGTH_SHORT).show();
//                        checkIsLoggedInAndLoad();
//                        return;
//                    }
//
//                    if (loginResponse.code != 0) {
//                        return;
//                    }
//
//                    // Save token
//                    Settings.saveToken(LoginActivity.this, loginResponse.token);
//                    // Close activity after obtaining token
//                    Intent intent = new Intent();
//                    intent.putExtra("token", loginResponse.token);
//                    setResult(Const.INTENT_RESULT_CODE_OK, intent);
//                    loadUserInfo(loginResponse.token);
//                }
//
//            };
//
//            if (!url.startsWith("https://accounts.google.com/"))
//                authStepTwo.execute();
//
//            //finish();
//            // return true; //Indicates WebView to NOT load the url;
//            return false; //Allow WebView to load url
//        }
//
//    };

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

        nvMain = (NavigationView) findViewById(R.id.nvMain);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        nvMain.getMenu().getItem(1).setChecked(true);
        hideProgressDialog();
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            Log.d(getLocalClassName(), "Activity started" + googleApiClient.toString());

            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
            if (opr.isDone()) {
                Log.d(getLocalClassName(), "Got cached sign-in");
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {

                showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(getLocalClassName(), "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            Toast.makeText(this,R.string.signed_in, Toast.LENGTH_LONG).show();

            String code = account.getServerAuthCode();
            Log.d(getLocalClassName(), "Google code: " + code);
            //this.setResponseId(code);

            AuthStepTwo authStepTwo = new AuthStepTwo(code) {
                @Override
                public void onPostExecute(LoginResponse loginResponse) {
                    if (loginResponse == null) {
                        Toast.makeText(LoginActivity.this, getString(R.string.error_authorization), Toast.LENGTH_SHORT).show();
                        checkIsLoggedInAndLoad();
                        return;
                    }

                    if (loginResponse.code != 0) {
                        Log.d(getLocalClassName(), "Got error from server: " + loginResponse.code + " message: " + loginResponse.message);
                        return;
                    }

                    // Save token
                    Settings.saveToken(LoginActivity.this, loginResponse.token);
                    Log.d(getLocalClassName(), "Got token from server: " + loginResponse.token);
                    // Close activity after obtaining token
                    Intent intent = new Intent();
                    intent.putExtra("token", loginResponse.token);
                    setResult(Const.INTENT_RESULT_CODE_OK, intent);
                    loadUserInfo(loginResponse.token);
                }

            };
            if (!code.isEmpty()) {
                authStepTwo.execute();
            }

//            // exchange token
//            AsyncTask<String, Void, String> exchangeTask = new AsyncTask<String, Void, String>() {
//                @Override
//                protected String doInBackground(String... params) {
//                    try {
//                        String strResponse = Utils.downloadUrl(Const.URL_EXCHANGE_TOKEN, "<request><params><exchange_token>" + params[0] + "</exchange_token></params></request>");
//                        Log.d(getLocalClassName(), "Server answer: " + strResponse);
//                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//                        InputStream is = new ByteArrayInputStream(strResponse.getBytes("UTF-8"));
//                        Document doc = dBuilder.parse(is);
//
//                        doc.getDocumentElement().normalize();
//
//                        NodeList nodeList = doc.getElementsByTagName("status");
//
//                        for (int i = 0; i < nodeList.getLength(); i++) {
//                            Element element = (Element) nodeList.item(i);
//                            int code = Integer.parseInt(element.getElementsByTagName("code").item(0).getTextContent());
//                            String message = element.getElementsByTagName("message").item(0).getTextContent();
//                            if (code > 0) {
//                                Log.d(getLocalClassName(), "Server return error code " + code + " with message: " + message);
//                                return null;
//                            }
//                        }
//
//                        nodeList = doc.getElementsByTagName("content");
//
//                        for (int i = 0; i < nodeList.getLength(); i++) {
//                            Element element = (Element) nodeList.item(i);
//                            return element.getElementsByTagName("token").item(0).getTextContent();
//                        }
//
//                        return null;
//                    } catch (IOException | ParserConfigurationException | SAXException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                }
//
//                @Override
//                protected void onPostExecute(String s) {
//                    super.onPostExecute(s);
//                    if (s != null && !s.isEmpty()) {
//                        Settings.saveToken(LoginActivity.this, s);
//                        loadUserInfo(s);
//                    }
//                }
//            };
//            exchangeTask.execute(code);

            //Picasso.with(this).load(urlIcon).into(userImage);

        } else {
            // Signed out, show unauthenticated UI.
        }
    }

}
