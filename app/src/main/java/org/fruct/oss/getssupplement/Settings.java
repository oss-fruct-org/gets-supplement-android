package org.fruct.oss.getssupplement;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Andrey on 19.07.2015.
 */
public class Settings {
    public static void saveString(Context context, String settingsName, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Const.PREFS_NAME, 0).edit();
        editor.putString(settingsName, value);
        editor.commit();
    }

    public static String getToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Const.PREFS_NAME, 0);
        return sharedPreferences.getString(Const.PREFS_AUTH_TOKEN, null);
    }
}
