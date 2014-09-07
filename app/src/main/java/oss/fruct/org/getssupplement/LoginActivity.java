package oss.fruct.org.getssupplement;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.apache.http.util.EncodingUtils;

import oss.fruct.org.getssupplement.Api.AuthStepOne;
import oss.fruct.org.getssupplement.Api.AuthStepTwo;
import oss.fruct.org.getssupplement.Model.LoginResponse;
import oss.fruct.org.getssupplement.R;

public class LoginActivity extends Activity {

    private WebView webView;

    private String responseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        webView = (WebView) findViewById(R.id.login_acitivty_webview);
        webView.setWebViewClient(authWebViewClient);
        webView.getSettings().setJavaScriptEnabled(true);

        AuthStepOne authStepOne = new AuthStepOne() {
            @Override
            public void onPostExecute(LoginResponse loginResponse) {
                if (loginResponse == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_authorization), Toast.LENGTH_SHORT).show();
                    finish();
                }

                setResponseId(loginResponse.id);

                webView.loadUrl(loginResponse.redirectUrl);
            }
        };

        authStepOne.execute();
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
                        finish();
                    }

                    // Save token
                    Log.d(Const.TAG, "Login response token: " + loginResponse.token);
                    Settings.saveString(getApplicationContext(), Const.PREFS_AUTH_TOKEN, loginResponse.token);

                    // Close activity after obtaining token
                    finish();
                }

            };

            authStepTwo.execute();

            //finish();
            // return true; //Indicates WebView to NOT load the url;
            return false; //Allow WebView to load url
        }

    };

    private String getResponseId() {
        return this.responseId;
    }

    private void setResponseId(String newValue) {
        this.responseId = newValue;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
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
