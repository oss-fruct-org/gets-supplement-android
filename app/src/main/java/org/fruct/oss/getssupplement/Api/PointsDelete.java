package org.fruct.oss.getssupplement.Api;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.fruct.oss.getssupplement.Const;
import org.fruct.oss.getssupplement.Model.BasicResponse;
import org.fruct.oss.getssupplement.Model.Point;
import org.fruct.oss.getssupplement.Model.PointsResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Andrey on 14.10.2015.
 */
public class PointsDelete extends AsyncTask<String, String, BasicResponse>{

    String params = "";

    public PointsDelete(String token, Point point) {

        params = "<request><params>";
        params += "<auth_token>" + token + "</auth_token>";

        params += "<category_id>" + point.categoryId + "</category_id>";
        params += "<uuid>" + point.uuid + "</uuid>";

        //params += "<latitude>" + point.latitude + "</latitude>";
        //params += "<longitude>" + point.longitude + "</longitude>";
        //params += "<time>" + point.time + "</time>";
        //params += "<description>" + point.description + "</description>";

        params += "</params></request>";
    }
    @Override
    protected BasicResponse doInBackground(String... params) {

        BasicResponse basicResponse = new BasicResponse();

        if (isCancelled()) {
            return null;
        }

        try {
            // Do request, get response
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Const.URL_POINTS_DELETE);

            String postData = this.params;

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

                basicResponse.code = Integer.parseInt(element.getElementsByTagName("code").item(0).getTextContent());
                basicResponse.message = element.getElementsByTagName("message").item(0).getTextContent();
            }

            return basicResponse;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
