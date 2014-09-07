package oss.fruct.org.getssupplement.Api;

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

import oss.fruct.org.getssupplement.Const;
import oss.fruct.org.getssupplement.Model.BasicResponse;
import oss.fruct.org.getssupplement.Model.CategoriesResponse;
import oss.fruct.org.getssupplement.Model.Category;
import oss.fruct.org.getssupplement.Model.LoginResponse;

/**
 * Created by alexander on 04.09.14.
 */
public class CategoriesGet extends AsyncTask<String, String, CategoriesResponse> {

   private String authToken;

    public CategoriesGet(String token) {
        authToken = token;
    }

    @Override
    protected CategoriesResponse doInBackground(String... params) {

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
                Element element = (Element) nodeList.item(  i);

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
                    category.description = element.getElementsByTagName("description").item(0).getTextContent();
                    category.url = element.getElementsByTagName("url").item(0).getTextContent();

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

    @Override
    public void onPostExecute(CategoriesResponse response) {

    }
}
