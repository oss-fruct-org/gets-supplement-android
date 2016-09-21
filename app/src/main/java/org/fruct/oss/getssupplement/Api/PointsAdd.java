package org.fruct.oss.getssupplement.Api;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.fruct.oss.getssupplement.Const;
import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.Point;
import org.fruct.oss.getssupplement.Model.PointsResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by ArtyomShorin on 21.07.2015.
 */
public class PointsAdd extends AsyncTask<String, String, PointsResponse> {

    private GetsDbHelper dbHelper;
    private Document doc = null;
    private NodeList nodeList = null;
    private PointsResponse pointsResponse = null;

    public PointsAdd(String token, int category, String title, float rating,
                     double latitude, double longitude, int streetId, long unixTime, GetsDbHelper _dbHelper) {

        dbHelper = _dbHelper;

        // Convert unixTime to a desirable format 'dd MM yyyy HH:mm:ss.SSS"
        Date date = new Date(unixTime);
        String formatedDate = new SimpleDateFormat("dd MM yyyy HH:mm:s.000").format(date);

        /* ДОБАВИТЬ ЗАПИСЬ ПОЛЕЙ В ТАБЛИЦУ (params) */
        SQLiteDatabase db =  dbHelper.getWrData(); // ЗАПИХАТЬ ЭТО В GETSDBHELPER, А ПОТОМ ВЫЗВАТЬ ОТ СЮДА С ПАРАМЕТРАМИ
        ContentValues cv = new ContentValues();

        cv.put("categoryId", category);
        cv.put("title", title);
        cv.put("token", token); // final String url = xml.getUrlByRegionId(Const.ID_REGION_KARELIA);
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        cv.put("rating", rating);
        cv.put("streetId", streetId); // ?????
        cv.put("time", formatedDate); // curent time
        cv.put("status", "WAITING");
        cv.put("code", "404");
        cv.put("message", "ERROR: NOT SENT");
        db.insertWithOnConflict(Const.DB_TEMP_POINTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
    }

    private String AssemblingRequest(String token, int category, String title, float rating,
                                     double latitude, double longitude, int streetId, String formatedDate){
        String params = "";

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

        params += "<time>" + formatedDate + "</time>";

        if (streetId != -1)
            params += "<extended_data><street_id>" + streetId + "</street_id></extended_data>";

        params += "</params></request>";

        return params;
    }

    private boolean IsSuccessRequest (String postData) {

        pointsResponse = new PointsResponse();

        boolean isSuccessR = false; /* СДЕЛАТЬ УСЛОВИЯ, ЧТОБЫ ПЕРЕМЕННАЯ ПРОВЕРЯЛА УСПЕХ */

        try {

            // Do request, get response
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Const.URL_POINTS_ADD);
            httppost.setHeader("Content-Type", "application/xml");

            httppost.setEntity(new StringEntity(postData, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httppost);

            HttpEntity responseEntity = response.getEntity();

            // Parse
            String strResponse = EntityUtils.toString(responseEntity);

            //System.out.print("strResponse = ");
            //System.out.println(strResponse);

            /* strResponse проверить что хранит */
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(strResponse.getBytes("UTF-8"));
            doc = dBuilder.parse(is);

            doc.getDocumentElement().normalize();

            nodeList = doc.getElementsByTagName("status");

            pointsResponse.code = 404;
            pointsResponse.message = "Not found";

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);

                pointsResponse.code = Integer.parseInt(element.getElementsByTagName("code").item(0).getTextContent());
                pointsResponse.message = element.getElementsByTagName("message").item(0).getTextContent();


                //System.out.print("code = ");
                //System.out.println(pointsResponse.code);
                //System.out.print("message = ");
                //System.out.println(pointsResponse.message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (pointsResponse.code == 0) {
            //System.out.println("!!!!!!!!!!!!!!!!!!!!!!SUCCESS REQUEST!!!!!!!!!!!!!!!!!!!!!!!");
            isSuccessR = true;
        }/* else if (pointsResponse.code == 1) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!INCORRECT XML!!!!!!!!!!!!!!!!!!!!!!!");
        } else {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!SERVER OFFLINE!!!!!!!!!!!!!!!!!!!!!!!");
        }*/

        return isSuccessR;
    }

    @Override
    protected PointsResponse doInBackground(String... params) {

        SQLiteDatabase db =  dbHelper.getRdData();

        Cursor cursor = db.query(true, Const.DB_TEMP_POINTS, null, null, null, null, null, null, null);

        if (isCancelled()) {
            return null;
        }

        try {

            String postData = null;

            //Vector<int> tempDataToDelete;
            ArrayList<String> tempDataToDelete = new ArrayList<String>();

            while (cursor.moveToNext()) {

                String token = cursor.getString(cursor.getColumnIndex("token"));
                int category = cursor.getInt(cursor.getColumnIndex("categoryId"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                float rating = cursor.getFloat(cursor.getColumnIndex("rating"));
                double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
                int streetId = cursor.getInt(cursor.getColumnIndex("streetId"));
                String formatedDate = cursor.getString(cursor.getColumnIndex("time"));

                postData = AssemblingRequest(token, category, title, rating, latitude, longitude, streetId, formatedDate);

                if (IsSuccessRequest(postData)) {
                    tempDataToDelete.add(cursor.getString(cursor.getColumnIndex("_id")));
                    //System.out.println("!!!!!!!!!!!!!!!!!!!!!!POSTDATA SUCCESS!!!!!!!!!!!!!!!!!!!!!!!");
                //} else {
                    //System.out.println("!!!!!!!!!!!!!!!!!!!!!!POSTDATA NOT SUCCESS!!!!!!!!!!!!!!!!!!!!!!!");
                }
            }

            while (!tempDataToDelete.isEmpty()) {
                db.delete(Const.DB_TEMP_POINTS, "_id = ?", new String[] {tempDataToDelete.get(0)});
                tempDataToDelete.remove(0);
                //System.out.println("!!!!!!!!!!!!!!!!!!!!!!DELETED!!!!!!!!!!!!!!!!!!!!!!!");
            }

            cursor.close();
            db.close();

            nodeList = doc.getElementsByTagName("Document");

            ArrayList<Point> list = new ArrayList<Point>();

            // Go through all <Placemark>
            for (int i = 0; i < nodeList.getLength(); i++) {
                NodeList categoryInner = nodeList.item(i).getChildNodes();

                // Go trough all items of every <Placemark>
                for (int j = 0; j < categoryInner.getLength(); j++) {

                    Element element = (Element) categoryInner.item(j);


                    try {
                        Point point = new Point();

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

                        list.add(point);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Log.d(Const.TAG, point.id + "login response " + point.name + " " + point.url);


                }
            }
            pointsResponse.points = list;
            return pointsResponse;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
