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

    public static final String URL_POINTS_LOAD = GETS_SERVER_PREFIX + "loadPoints.php";
    public static final String URL_POINTS_ADD = GETS_SERVER_PREFIX + "addPoint.php";
    public static final String URL_POINTS_UPDATE = GETS_SERVER_PREFIX + "updatePoint.php";
    public static final String URL_POINTS_DELETE = GETS_SERVER_PREFIX + "deletePoint.php";

    public static final String URL_CATEGORIES_GET = GETS_SERVER_PREFIX + "getCategories.php";
    public static final String URL_CATEGORIES_ADD = GETS_SERVER_PREFIX + "addCategory.php";


    /**
     * Shared preferences
     */
    public static final String PREFS_NAME = "gets_shared";
    public static final String PREFS_AUTH_TOKEN = "auth_token";

    /**
     * Database
     */
    public static final String DB_INTERNAL_NAME = "internal_db";
    public static final String DB_INTERNAL_POINTS = "internal_points";
    public static final String DB_INTERNAL_CATEGORIES = "internal_categories";


    /**
     * API constants
     */
    public static int API_POINTS_RADIUS = 5;// in km


    /**
     * Intents for result
     */
    public static final int INTENT_RESULT_NEW_POINT = 1;
    public static final int INTENT_RESULT_TOKEN = 2;
    public static final int INTENT_RESULT_CATEGORY = 3;


    public static final int INTENT_RESULT_CODE_OK = 1;
    public static final int INTENT_RESULT_CODE_NOT_OK = 2;
}
