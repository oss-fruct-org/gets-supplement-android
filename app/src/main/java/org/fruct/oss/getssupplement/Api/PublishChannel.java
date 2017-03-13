package org.fruct.oss.getssupplement.Api;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.fruct.oss.getssupplement.Utils.Const;
import org.fruct.oss.getssupplement.Model.BasicResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Andrey on 15.10.2015.
 */
public class PublishChannel extends AsyncTask<String, String, BasicResponse> {

    String params = "";
    boolean isPublish;

    public PublishChannel(String token, int category, boolean _isPublish) {

        params = "<request><params>";
        params += "<auth_token>" + token + "</auth_token>";
        params += "<category_id>" + category + "</category_id>";
        params += "</params></request>";
        this.isPublish = _isPublish;
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
            HttpPost httppost = new HttpPost();

            if (isPublish) httppost.setURI(new URI(Const.URL_CHANNEL_PUBLISH));
            else httppost.setURI(new URI(Const.URL_CHANNEL_UNPUBLISH));

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
