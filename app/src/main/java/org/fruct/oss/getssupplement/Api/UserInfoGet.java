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
import org.fruct.oss.getssupplement.Const;
import org.fruct.oss.getssupplement.Model.UserInfoResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Andrey on 12.10.2015.
 */
public class UserInfoGet extends AsyncTask<String, String, UserInfoResponse> {

    private String token;

    public UserInfoGet(String _token) {
        this.token = _token;
    }

    @Override
    protected UserInfoResponse doInBackground(String... params) {

        UserInfoResponse userInfoResponse = new UserInfoResponse();

        if (isCancelled()) {
            return null;
        }

        try {
            // Do request, get response
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Const.URL_INFO);

            String postData = "<request><params><auth_token>" + token + "</auth_token></params></request>";
            Log.d(Const.TAG, "UserInfoGet token data: " + postData);

            httppost.setEntity(new StringEntity(postData));
            HttpResponse response = httpclient.execute(httppost);

            HttpEntity responseEntity = response.getEntity();

            // Parse
            String strResponse = EntityUtils.toString(responseEntity);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(strResponse.getBytes("UTF-8"));
            Document doc = dBuilder.parse(is);

            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("status");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                userInfoResponse.code = Integer.parseInt(element.getElementsByTagName("code").item(0).getTextContent());
                userInfoResponse.message = element.getElementsByTagName("message").item(0).getTextContent();
            }

            nodeList = doc.getElementsByTagName("content");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                userInfoResponse.isTrustedUser = Boolean.parseBoolean(element.getElementsByTagName("isTrustedUser").item(0).getTextContent());
            }

            return userInfoResponse;

        } catch (Exception e){
            e.printStackTrace();
        };
        return null;
    }
}
