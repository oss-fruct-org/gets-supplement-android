package org.fruct.oss.getssupplement.Model;

import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Andrey on 19.07.2015.
 */
public class Category {
    private static String TAG = "Category";

    public int id;
    private String name;
    private Map<String, String> localNames = null;

    private String description;
    private Map<String, String> localDescriptions;

    public String urlIcon;

    public void setName(String newName) {
        name = newName;
        localNames = null;
    }

    public String getName() {
        if (localNames != null) {
            Log.d(TAG,"Point key = " + "name_" + Locale.getDefault().getLanguage());
            if (localNames.containsKey("name_" + Locale.getDefault().getLanguage()))
                return localNames.get("name_" + Locale.getDefault().getLanguage());
            else
                return localNames.get("name");
        }

        // если не распарсено, то парсим и в хеш
        localNames = new HashMap<String, String>();
        try {
            JSONObject json = new JSONObject(name);
            Iterator<String> temp = json.keys();
            while (temp.hasNext()) {
                String key = temp.next();
                localNames.put(key, json.get(key).toString());
            }
        } catch (Exception e) {
            Log.d(TAG, "Name: " + name + " not parsed: " + e.getMessage());
            localNames.put("name", name);
        }
        if (localNames.containsKey("name_" + Locale.getDefault().getLanguage()))
            return localNames.get("name_" + Locale.getDefault().getLanguage());
        else
            return localNames.get("name");
    }

    public String getAllNames() {
        return name;
    }

    public void setDescription(String value) {
        description = value;
        localDescriptions = null;
    }

    public String getDescription() {
        if (localDescriptions != null) {
            //log.debug("Point key = " + "name_" + Locale.getDefault().getLanguage());
            if (localDescriptions.containsKey("name_" + Locale.getDefault().getLanguage()))
                return localDescriptions.get("name_" + Locale.getDefault().getLanguage());
            else
                return localDescriptions.get("name");
        }

        // если не распарсено, то парсим и в хеш
        localDescriptions = new HashMap<String, String>();
        try {
            JSONObject json = new JSONObject(description);
            Iterator<String> temp = json.keys();
            while (temp.hasNext()) {
                String key = temp.next();
                localDescriptions.put(key, json.get(key).toString());
            }
        } catch (Exception e) {
            //log.debug("Catch exception: " + e.getMessage());
            localDescriptions.put("description", description);
        }
        if (localDescriptions.containsKey("name_" + Locale.getDefault().getLanguage()))
            return localDescriptions.get("name_" + Locale.getDefault().getLanguage());
        else
            return localDescriptions.get("name");
    }

    public String getAllDescriptions() {
        return description;
    }
}