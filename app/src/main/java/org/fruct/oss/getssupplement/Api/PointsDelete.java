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
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.fruct.oss.getssupplement.Const;
import org.fruct.oss.getssupplement.Model.BasicResponse;
import org.fruct.oss.getssupplement.Model.PointsResponse;

/**
 * Created by alexander on 04.09.14.
 */
public class PointsDelete extends AsyncTask<String, String, BasicResponse> {

    String params = "";

    public PointsDelete(String token, int categoryId, String uuid) {
        /*
    public PointsAdd(String token, int category, String title, String description,
        String link, double latitude, double longitude, double altitude, long unixTime) {*/

        params = "<request><params>";
        params += "<auth_token>" + token + "</auth_token>";

        params += "<category_id>" + categoryId + "</category_id>";
        params += "<uuid>" + uuid + "</uuid>";

        params += "</params></request>";

    }

    @Override
    protected BasicResponse doInBackground(String... params) {

        PointsResponse loginResponse = new PointsResponse();

        if (isCancelled()) {
            return null;
        }

        try {
            // Do request, get response
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Const.URL_POINTS_ADD);

            String postData = this.params;
            Log.d(Const.TAG, "PointsAdd postData: " + postData);

            httppost.setEntity(new StringEntity(postData));
            HttpResponse response = httpclient.execute(httppost);

            HttpEntity responseEntity = response.getEntity();

            // Parse
            String strResponse = EntityUtils.toString(responseEntity);
            Log.d(Const.TAG, "PointsDelete response: " + strResponse);

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

            return loginResponse;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
