package org.fruct.oss.getssupplement.Api;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.fruct.oss.getssupplement.Const;
import org.fruct.oss.getssupplement.Model.LoginResponse;

public class AuthStepTwo extends AsyncTask<String, String, LoginResponse> {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AuthStepTwo(String _id) {
        setId(_id);
    }

    @Override
    protected LoginResponse doInBackground(String... params) {

        LoginResponse loginResponse = new LoginResponse();

        if (isCancelled()) {
            return null;
        }

        try {
            // Do request, get response
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Const.URL_AUTH);

            String postData = "<request><params><id>" + id + "</id></params></request>";
            Log.d(Const.TAG, "AuthStepTwo postData: " + postData);

            // Server can't work this fast
            for (int attempts = 0; attempts  < 5; attempts++) {

                httppost.setEntity(new StringEntity(postData));
                HttpResponse response = httpclient.execute(httppost);

                HttpEntity responseEntity = response.getEntity();

                // Parse
                String strResponse = EntityUtils.toString(responseEntity);
                Log.d(Const.TAG, "AuthStepTwo response: " + strResponse);

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                InputStream is = new ByteArrayInputStream(strResponse.getBytes("UTF-8"));
                Document doc = dBuilder.parse(is);

                doc.getDocumentElement().normalize();

                NodeList nodeList = doc.getElementsByTagName("status");

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);

                    loginResponse.code = Integer.parseInt(element.getElementsByTagName("code").item(0).getTextContent());
                    loginResponse.message = element.getElementsByTagName("message").item(0).getTextContent();
                }

                nodeList = doc.getElementsByTagName("content");

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);


                    loginResponse.token = element.getElementsByTagName("auth_token").item(0).getTextContent();
                    Log.d(Const.TAG, loginResponse.token + "login response " + loginResponse.message);

                    // Return if aith_token found (else next attemp)
                    return loginResponse;
                }

                Thread.sleep(1000);
            }

            return loginResponse;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
