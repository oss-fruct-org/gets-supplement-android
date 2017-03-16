package org.fruct.oss.getssupplement.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Andrey on 19.07.2015.
 */
public class Settings {

    public static void saveString(Context context, String settingsName, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Const.PREFS_NAME, 0).edit();
        editor.putString(settingsName, value);
        editor.apply();
    }

    public static void saveBoolean(Context context, String settingsName, Boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Const.PREFS_NAME, 0).edit();
        editor.putBoolean(settingsName, value);
        editor.apply();
    }

    public static void saveCheckedStatus(Context context, int categoryId, boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Const.PREFS_NAME_CATEGORIES_CHECKED, 0).edit();
        editor.putBoolean(Integer.toString(categoryId), value);
        editor.apply();
    }

    public static void saveMapHash(Context context, String hash) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Const.PREFS_MAP_HASH, 0).edit();
        editor.putString(Const.PREFS_MAP_HASH, hash);
        editor.apply();
    }

    public static String getToken(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Const.PREFS_NAME, 0);
        return sp.getString(Const.PREFS_AUTH_TOKEN, null);
    }

    public static void saveToken(Context context, String token) {
        saveString(context, Const.PREFS_AUTH_TOKEN, token);
    }

    public static String getStorageDir(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Const.PREFS_NAME, 0);
        return sp.getString(Const.PREF_STORAGE_PATH, null);
    }

    public static boolean getIsTrusted(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Const.PREFS_NAME, 0);
        return sp.getBoolean(Const.PREFS_IS_TRUSTED_USER, false);
    }

    public static boolean getIsChecked(Context context, int categoryId) {
        SharedPreferences sp = context.getSharedPreferences(Const.PREFS_NAME_CATEGORIES_CHECKED, 0);
        return sp.getBoolean(Integer.toString(categoryId), true);
    }

    public static String getMapHash(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Const.PREFS_MAP_HASH, 0);
        return sp.getString(Const.PREFS_MAP_HASH, null);
    }

    public static int generateId(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Const.PREFS_GENERATOR, 0);
        int val = sp.getInt(Const.PREFS_LAST_GEN, 0);
        sp.edit().putInt(Const.PREFS_LAST_GEN, ++val).apply();
        return val;
    }
}
