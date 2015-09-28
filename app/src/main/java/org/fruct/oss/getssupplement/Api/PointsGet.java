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
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.fruct.oss.getssupplement.Const;
import org.fruct.oss.getssupplement.Model.Point;
import org.fruct.oss.getssupplement.Model.PointsResponse;

public class PointsGet extends AsyncTask<String, String, PointsResponse> {

    private String authToken;
    private double latitude;
    private double longitude;
    private int radius;
    private int categoryId;

    // TODO: implement 'space' parameter
    public PointsGet(String token, double latitude, double longitude, int radius, int categoryId) {
        authToken = token;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.categoryId = categoryId;
    }

    public PointsGet(String token, int categoryId) {
        authToken = token;
        this.categoryId = categoryId;
    }

    public PointsGet(String token, double latitude, double longitude, int radius) {
        authToken = token;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    @Override
    public PointsResponse doInBackground(String... params) {

        PointsResponse response = new PointsResponse();

        if (isCancelled()) {
            return null;
        }

        try {
            // Do request, get response
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Const.URL_POINTS_LOAD);

            String postData = "<request><params><auth_token>" + authToken + "</auth_token>"; //</params></request>";

            if (categoryId != 0)
                postData += "<category_id>" + categoryId + "</category_id>";

            if (radius != 0)
                postData += "<latitude>" + latitude + "</latitude>";
                postData += "<longitude>" + longitude + "</longitude>";
                postData += "<radius>" + radius + "</radius>";

            postData += "</params></request>";
            Log.d(Const.TAG, postData + " ");


            httppost.setEntity(new StringEntity(postData));
            HttpResponse httpResponse = httpclient.execute(httppost);

            HttpEntity responseEntity = httpResponse.getEntity();

            // Parse
            //String strResponse = EntityUtils.toString(responseEntity);
            String strResponse = EntityUtils.toString(responseEntity, "UTF-8");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(strResponse.getBytes("UTF-8"));
            Document doc = dBuilder.parse(is);

            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("status");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(  i);

                response.code = Integer.parseInt(element.getElementsByTagName("code").item(0).getTextContent());
                response.message = element.getElementsByTagName("message").item(0).getTextContent();
            }

            nodeList = doc.getElementsByTagName("Document");

            ArrayList<Point> list = new ArrayList<Point>();

            // Go through all <Placemark>
            for (int i = 0; i < nodeList.getLength(); i++) {
                NodeList categoryInner = nodeList.item(i).getChildNodes();

                // Go trough all items of every <Placemark>
                for (int j = 0; j < categoryInner.getLength(); j++) {

                    Element element = (Element) categoryInner.item(j);

                    Point point = new Point();

                    try {
                        point.name = element.getElementsByTagName("name").item(0).getTextContent();
                        point.description = element.getElementsByTagName("description").item(0).getTextContent();

                        NodeList extendedData = element.getElementsByTagName("ExtendedData").item(0).getChildNodes();

                        for (int k = 0; k < extendedData.getLength(); k++) {
                            Element dataNode = (Element) extendedData.item(k);

                            if (dataNode.getAttribute("name").equals("link")) {
                                String url = dataNode.getChildNodes().item(0).getTextContent();

                                // Check for empty url
                                if (url.replace(" ", "").equals(""))
                                    point.url = null;
                                else point.url = url;
                            }

                            if (dataNode.getAttribute("name").equals("time"))
                                point.time = dataNode.getChildNodes().item(0).getTextContent();

                            if (dataNode.getAttribute("name").equals("access"))
                                point.access = dataNode.getChildNodes().item(0).getTextContent();

                            if (dataNode.getAttribute("name").equals("uuid"))
                                point.uuid = dataNode.getChildNodes().item(0).getTextContent();

                            if (dataNode.getAttribute("name").equals("rating"))
                                point.rating = Float.parseFloat(dataNode.getChildNodes().item(0).getTextContent());

                            if (dataNode.getAttribute("name").equals("category_id"))
                                point.categoryId = Integer.parseInt(dataNode.getChildNodes().item(0).getTextContent());

                        }

                        String coordinates = element.getElementsByTagName("Point").item(0).getTextContent();
                        point.longitude = Float.parseFloat(coordinates.split(",")[0]);
                        point.latitude = Float.parseFloat(coordinates.split(",")[1]);

                    } catch (Exception e) {
                        Log.d(Const.TAG + "xml", "Error parsing XML " + e.toString());
                    }

                    //Log.d(Const.TAG, point.id + "login response " + point.name + " " + point.url);

                    list.add(point);
                }
            }
            response.points = list;

            return response;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onPostExecute(PointsResponse response) {

    }
}
