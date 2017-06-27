package org.fruct.oss.getssupplement.Model;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Andrey on 19.07.2015.
 */
public class Category {

    public int id;
    private String name;
    private Map<String, String> localNames;

    private String description;
    private Map<String, String> localDescriptions;

    public String urlIcon;

    public void setName(String newName) {
        name = newName;
        localNames = null;
    }

    public String getName() {
        if (localNames != null) {
            //log.debug("Point key = " + "name_" + Locale.getDefault().getLanguage());
            if (localNames.containsKey("name_" + Locale.getDefault().getLanguage()))
                return localNames.get("name_" + Locale.getDefault().getLanguage());
            else
                return localNames.get("name");
        }

        // если не распарсено, то парсим и в хеш
        localNames = new HashMap();
        try {
            JSONObject json = new JSONObject(name);
            Iterator<String> temp = json.keys();
            while (temp.hasNext()) {
                String key = temp.next();
                localNames.put(key, json.get(key).toString());
            }
        } catch (Exception e) {
            //log.debug("Catch exception: " + e.getMessage());
            localNames.put("name", name);
        }
        return name;
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
        localDescriptions = new HashMap();
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
        return description;
    }

    public String getAllDescriptions() {
        return description;
    }
}