package org.fruct.oss.getssupplement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.fruct.oss.getssupplement.Api.AuthStepOne;
import org.fruct.oss.getssupplement.Api.AuthStepTwo;
import org.fruct.oss.getssupplement.Model.LoginResponse;

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

            Log.d(Const.TAG, url + " ");

            AuthStepTwo authStepTwo = new AuthStepTwo(getResponseId()) {
                @Override
                public void onPostExecute(LoginResponse loginResponse) {
                    Log.d(Const.TAG, "Login response: " + loginResponse);
                    if (loginResponse == null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_authorization), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        setResult(Const.INTENT_RESULT_CODE_NOT_OK, intent);
                        finish();
                    }

                    // Save token
                    Log.d(Const.TAG, "Login response token: " + loginResponse.token);
                    Settings.saveString(getApplicationContext(), Const.PREFS_AUTH_TOKEN, loginResponse.token);

                    // Close activity after obtaining token
                    Intent intent = new Intent();
                    intent.putExtra("token", loginResponse.token);
                    setResult(Const.INTENT_RESULT_CODE_OK, intent);
                    finish();
                }

            };

            if (!url.startsWith("https://accounts.google.com/"))
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




}
