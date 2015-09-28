package org.fruct.oss.getssupplement.Api;

import android.os.AsyncTask;

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

public class AuthStepOne extends AsyncTask<String, String, LoginResponse> {


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

            String postData = "<request><params></params></request>";

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

                loginResponse.code = Integer.parseInt(element.getElementsByTagName("code").item(0).getTextContent());
                loginResponse.message = element.getElementsByTagName("message").item(0).getTextContent();
            }

            nodeList = doc.getElementsByTagName("content");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);

                loginResponse.id = element.getElementsByTagName("id").item(0).getTextContent();
                loginResponse.redirectUrl = element.getElementsByTagName("redirect_url").item(0).getTextContent();
            }


            return loginResponse;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
