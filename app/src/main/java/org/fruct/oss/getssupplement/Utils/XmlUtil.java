package org.fruct.oss.getssupplement.Utils;

import android.util.Log;
import android.util.Xml;

import org.fruct.oss.getssupplement.Const;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Andrey on 23.04.2016.
 */
public class XmlUtil {

    private String xml;
    private Document xmlDoc;

    public XmlUtil(String xml) {
        this.xml = xml;
        parseHashFromXml();
    }

    private void parseHashFromXml() {
        String strXml = this.xml;
        DocumentBuilder dBuilder = null;
        InputStream is = null;
        Document doc = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            dBuilder = dbFactory.newDocumentBuilder();

            is = new ByteArrayInputStream(strXml.getBytes("UTF-8"));
            doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        this.xmlDoc = doc;
    }

    private Element getElementByRegionId(String id) {
        String hash = null;
        Document doc = this.xmlDoc;
        if (doc != null) {
            NodeList nodeList = doc.getElementsByTagName("file");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);

                String type = element.getElementsByTagName("type").item(0).getTextContent();
                String region = element.getElementsByTagName("region-id").item(0).getTextContent();
                // Return null if map type != graphhopper
                if (type.equals(Const.TYPE_GRAPHOPPER) && region.equals(id)) {
                    return element;
                }
            }
        }
        return null;
    }

    public String getHashByRegionId(String id) {
        Element element = getElementByRegionId(id);
        String hash = null;
        if (element != null)
            hash = element.getElementsByTagName("hash").item(0).getTextContent();
        return hash;
    }

    public String getUrlByRegionId(String id) {
        Element element = getElementByRegionId(id);
        String url = null;
        if (element != null)
            url = element.getElementsByTagName("url").item(0).getTextContent();
        return url;
    }

    public String getFileNameByRegionId(String id) {
        Element element = getElementByRegionId(id);
        String name = null;
        if (element != null)
            name = element.getElementsByTagName("name").item(0).getTextContent();
        return name;
    }
}
