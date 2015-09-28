package org.fruct.oss.getssupplement.Api;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.fruct.oss.getssupplement.Const;
import org.fruct.oss.getssupplement.Model.PointsResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by ArtyomShorin on 21.07.2015.
 */
public class PointsAdd extends AsyncTask<String, String, PointsResponse> {

    String params = "";

    public PointsAdd(String token, int category, String title, float rating,
                     double latitude, double longitude, long unixTime) {

        params = "<request><params>";
        params += "<auth_token>" + token + "</auth_token>";

        params += "<category_id>" + category + "</category_id>";
        params += "<title>" + title + "</title>";

        // Put rating //
        String ratingField = "{\"description\":\"" + "{}" + "\",\"rating\":" + rating + "}";
        params += "<description>" + ratingField + "</description>";

        /*params += "<link><![CDATA[" + "{}" + "]]></link>";*/

        params += "<latitude>" + latitude + "</latitude>";
        params += "<longitude>" + longitude + "</longitude>";
        /*params += "<altitude>" + 0.0 + "</altitude>";*/

        // Convert unixTime to a desirable format 'dd MM yyyy HH:mm:ss.SSS"
        Date date = new Date(unixTime);
        String formatedDate = new SimpleDateFormat("dd MM yyyy HH:mm:s.000").format(date);
        params += "<time>" + formatedDate + "</time>";

        params += "</params></request>";

    }

    @Override
    protected PointsResponse doInBackground(String... params) {

        PointsResponse loginResponse = new PointsResponse();

        if (isCancelled()) {
            return null;
        }

        try {
            // Do request, get response
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Const.URL_POINTS_ADD);
            httppost.setHeader("Content-Type", "application/xml");

            String postData = this.params;
            Log.d(Const.TAG, "PointsAdd postData: " + postData);

            httppost.setEntity(new StringEntity(postData, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httppost);

            HttpEntity responseEntity = response.getEntity();

            // Parse
            String strResponse = EntityUtils.toString(responseEntity);
            Log.d(Const.TAG, "PointsAdd response: " + strResponse);

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
