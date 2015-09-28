package org.fruct.oss.getssupplement.Api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import org.fruct.oss.getssupplement.IconHolder;
import org.fruct.oss.getssupplement.Model.CategoriesResponse;
import org.fruct.oss.getssupplement.Model.Category;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Andrey on 19.07.2015.
 */
public class CategoriesGet extends AsyncTask<Void, Void, CategoriesResponse> {

    private String authToken;

    public CategoriesGet(String token) {
        authToken = token;
    }

    @Override
    public CategoriesResponse doInBackground(Void... arg0) {

        CategoriesResponse response = new CategoriesResponse();

        if (isCancelled()) {
            return null;
        }

        try {
            // Do request, get response
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Const.URL_CATEGORIES_GET);

            String postData = "<request><params><auth_token>" + authToken + "</auth_token></params></request>";

            // Server can't work this fast
            httppost.setEntity(new StringEntity(postData));
            HttpResponse httpResponse = httpclient.execute(httppost);

            HttpEntity responseEntity = httpResponse.getEntity();

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

                response.code = Integer.parseInt(element.getElementsByTagName("code").item(0).getTextContent());
                response.message = element.getElementsByTagName("message").item(0).getTextContent();
            }

            nodeList = (NodeList) doc.getElementsByTagName("categories");

            ArrayList<Category> list = new ArrayList<Category>();

            // Go through all <category>
            for (int i = 0; i < nodeList.getLength(); i++) {
                NodeList categoryInner = (NodeList) nodeList.item(i).getChildNodes();

                // Go trough all items of <category>
                for (int j = 0; j < categoryInner.getLength(); j++) {

                    Element element = (Element) categoryInner.item(j);

                    Category category = new Category();
                    category.id = Integer.parseInt(element.getElementsByTagName("id").item(0).getTextContent());
                    category.name = element.getElementsByTagName("name").item(0).getTextContent();
                    category.description = getDescription(element.getElementsByTagName("description").item(0).getTextContent());
                    category.urlIcon = getIcon(element.getElementsByTagName("url").item(0).getTextContent());

                    // Download and save bitmap for later usage
                    if (category.urlIcon != null && !category.urlIcon.equals("")) {
                        IconHolder.getInstance().addBitmap(category.id, downloadBitmap(category.urlIcon));
                    }

                    Log.d(Const.TAG, category.description + " " + category.id);

                    //Log.d(Const.TAG, category.id + "login response " + category.name);


                    list.add(category);
                }
            }

            response.categories = list;

            return response;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String _getDescription(String jsonString) {
        return getDescription(jsonString);
    }
    public String _getIcon(String jsonString) {
        return getIcon(jsonString);
    }

    private String getDescription(String jsonString) {
        return getField(jsonString, "description");
    }

    private String getIcon(String jsonString) {
        return getField(jsonString, "icon");
    }

    private static String getField(String jsonString, String field) {
        String description = null;

        try {
            JSONObject json = new JSONObject(jsonString);
            description = json.getString(field);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return description;
    }


    protected Bitmap downloadBitmap(String url) {
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        return mIcon11;
    }

    @Override
    public void onPostExecute(CategoriesResponse response) {

    }
}
