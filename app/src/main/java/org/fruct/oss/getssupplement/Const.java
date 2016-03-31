package org.fruct.oss.getssupplement;

/**
 * Created by Andrey on 18.07.2015.
 */
public class Const {


    /**
     * Debug
     */
    public static final String TAG = "1337";


    /**
     * URLs
     */
    private static final String GETS_SERVER_PREFIX = "http://gets.cs.petrsu.ru/obstacle/service/";

    public static final String URL_AUTH = GETS_SERVER_PREFIX + "userLogin.php";
    public static final String URL_INFO = GETS_SERVER_PREFIX + "userInfo.php";

    public static final String URL_POINTS_LOAD = GETS_SERVER_PREFIX + "loadPoints.php";
    public static final String URL_POINTS_ADD = GETS_SERVER_PREFIX + "addPoint.php";
    public static final String URL_POINTS_UPDATE = GETS_SERVER_PREFIX + "updatePoint.php";
    public static final String URL_POINTS_DELETE = GETS_SERVER_PREFIX + "deletePoint.php";


    public static final String URL_CATEGORIES_GET = GETS_SERVER_PREFIX + "getCategories.php";
    public static final String URL_CATEGORIES_ADD = GETS_SERVER_PREFIX + "addCategory.php";

    public static final String URL_CHANNEL_PUBLISH = GETS_SERVER_PREFIX + "publish.php";
    public static final String URL_CHANNEL_UNPUBLISH = GETS_SERVER_PREFIX + "unpublish.php";


    /**
     * Shared preferences
     */
    public static final String PREFS_NAME = "gets_shared";
    public static final String PREFS_NAME_CATEGORIES_CHECKED = "checked_categories";
    public static final String PREFS_AUTH_TOKEN = "auth_token";
    public static final String PREFS_IS_TRUSTED_USER = "is_trusted_user";
    public static final String PREFS_CATEGORY = "category_";
    public static final String PREF_STORAGE_PATH = "pref_storage_path";
    /**
     * Database
     */
    public static final String DB_INTERNAL_NAME = "internal_db";
    public static final String DB_INTERNAL_POINTS = "internal_points";
    public static final String DB_INTERNAL_CATEGORIES = "internal_categories";


    /**
     * API constants
     */
    public static final boolean API_PUBLISH = true;
    public static final boolean API_UNPUBLISH = false;
    public static int API_POINTS_RADIUS = 5;// in km
    public static int ALL_CATEGORIES = -1;


    /**
     * Intents for result
     */
    public static final int INTENT_RESULT_NEW_POINT = 1;
    public static final int INTENT_RESULT_TOKEN = 2;
    public static final int INTENT_RESULT_CATEGORY = 3;
    public static final int INTENT_RESULT_PUBLISH = 4;
    public static final int INTENT_RESULT_UNPUBLISH = 5;
    public static final int INTENT_RESULT_CATEGORY_ACTIONS = 6;
    public static final int INTENT_RESULT_APP_INFO = 7;



    public static final int INTENT_RESULT_CODE_OK = 1;
    public static final int INTENT_RESULT_CODE_NOT_OK = 2;


    /**
     * Service
     */
    public static final boolean SERVICE_ACTIONS = true;
    public static final boolean SERVICE_NOT_ACTIONS = false;
}
