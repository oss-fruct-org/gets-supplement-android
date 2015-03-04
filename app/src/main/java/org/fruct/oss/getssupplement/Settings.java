package org.fruct.oss.getssupplement;

import android.content.Context;
import android.content.SharedPreferences;

import org.fruct.oss.getssupplement.Const;

/**
 * Created by alexander on 05.09.14.
 */
public class Settings {

/*
    Context context;
    static SharedPreferences sharedPreferences;

    public Settings(Context _context) {
        context = _context;
        sharedPreferences = context.getSharedPreferences(Const.PREFS_NAME, 0);
    }
*/

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
