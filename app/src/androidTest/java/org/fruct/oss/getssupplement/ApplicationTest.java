package org.fruct.oss.getssupplement;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.MoreAsserts;
import android.util.Log;

import org.fruct.oss.getssupplement.Api.PointsAdd;
import org.fruct.oss.getssupplement.Api.PointsGet;
import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.Category;
import org.fruct.oss.getssupplement.Model.DatabaseType;
import org.fruct.oss.getssupplement.Model.Point;
import org.fruct.oss.getssupplement.Model.PointsResponse;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    GetsDbHelper getsDbHelper;

    @Override
    protected void setUp() throws Exception {
        getsDbHelper = new GetsDbHelper(getContext(), DatabaseType.DATA_FROM_API);
    }
/*
    public void testCategoriesGet() {
        CategoriesGet categoriesGet = new CategoriesGet("not_a_token");
        assertNotNull(categoriesGet.doInBackground()); // Should return a response

        CategoriesGet categoriesGet0 = new CategoriesGet("REALTOKEN"); // TODO real token
        assertNotNull(categoriesGet.doInBackground());

        // No field, return null
        assertNull(categoriesGet0._getDescription("incorrect_string"));
        assertNull(categoriesGet0._getIcon("incorrect_string"));
        assertNull(categoriesGet0._getIcon("{\"icon\":null}"));
    }*/

    private String getToken() {
        return "g:1/yw3pnNRWu5ajD6AT0F2lie6Qxb5-ld3yFwQohoWwAok";
    }

    public void testPointsGet1() {
        PointsGet pointsGet = new PointsGet(getToken(), 35.0d, 61.0d, 500) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 0);
                assertNotNull(response.points);
            }
        };

        pointsGet.execute();
    }


    public void testPointsGet2() {
        PointsGet pointsGet = new PointsGet(null, 35.0d, 61.0d, 500) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
                assertNull(response.points);
            }
        };

        pointsGet.execute();
    }

    public void testPointsGet3() {
        PointsGet pointsGet = new PointsGet("abc123", 35.0d, 61.0d, 500) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
                assertNull(response.points);
            }
        };

        pointsGet.execute();
    }

    public void testPointsGet4() {
        PointsGet pointsGet = new PointsGet("", 35.0d, 61.0d, 500) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
                assertNull(response.points);
            }
        };

        pointsGet.execute();
    }

    public void testPointsGet5() {
        PointsGet pointsGet = new PointsGet("/\\&1$%^>строка γραμμή", 35.0d, 61.0d, 500) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
                assertNull(response.points);
            }
        };

        pointsGet.execute();
    }

    // Error
    /*public void testPointsGet6() {
        String token = "";
        for (int i = 0; i < 500; i ++)
            token += "строка строка строка ";

        PointsGet pointsGet = new PointsGet(token, 35.0d, 61.0d, 500) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
                assertNull(response.points);
            }
        };

        pointsGet.execute();
    }*/

    public void testPointsGet7() {
        PointsGet pointsGet = new PointsGet(getToken(), -999.0d, 61.0d, 500) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
                assertNull(response.points);
            }
        };

        pointsGet.execute();
    }

    public void testPointsGet8() {

        PointsGet pointsGet = new PointsGet(getToken(), 999, 61.0d, 500) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
                assertNull(response.points);
            }
        };

        pointsGet.execute();
    }
    public void testPointsGet9() {

        PointsGet pointsGet = new PointsGet(getToken(), 35.0d, -999.0d, 500) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
                assertNull(response.points);
            }
        };

        pointsGet.execute();
    }
    public void testPointsGet10() {

        PointsGet pointsGet = new PointsGet(getToken(), 35.0d, 999.0d, 500) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
                assertNull(response.points);
            }
        };

        pointsGet.execute();
    }
    public void testPointsGet11() {

        PointsGet pointsGet = new PointsGet(getToken(), 35.0d, 61.0d, 0) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
                assertNull(response.points);
            }
        };

        pointsGet.execute();
    }

    public void testPointsGet12() {
        PointsGet pointsGet = new PointsGet(getToken(), 35.0d, 61.0d, -500) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
                assertNull(response.points);
            }
        };

        pointsGet.execute();
    }

    public void testPointsGet13() {
        PointsGet pointsGet = new PointsGet(getToken(), 35.0d, 61.0d, 9999999) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
                assertNull(response.points);
            }
        };

        pointsGet.execute();
    }


    /**
     *
     *
     *          Points add
     *
     *
     *
     */
    public void testPointsAdd1() {
        PointsAdd pointsAdd = new PointsAdd(getToken(), 23, "title test", 0.5f, 12.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                Log.d(Const.TAG, "testPointsAdd:_" + response.code + "_" + response.message);
                assertEquals(response.code, 0);
            }
        };

        pointsAdd.execute();
    }

    public void testPointsAdd2() {
        PointsAdd pointsAdd = new PointsAdd(null, 23, "title test", 0.5f, 12.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }

    public void testPointsAdd3() {
        PointsAdd pointsAdd = new PointsAdd(getToken(), 23, null, 0.5f, 12.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }


    public void testPointsAdd4() {
        PointsAdd pointsAdd = new PointsAdd("abc123", 23, "title test", 0.5f, 12.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }

    public void testPointsAdd5() {
        PointsAdd pointsAdd = new PointsAdd("", 23, "title test", 0.5f, 12.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }

    public void testPointsAdd6() {
        PointsAdd pointsAdd = new PointsAdd(getToken(), 23, "", 0.5f, 12.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }

    public void testPointsAdd7() {
        PointsAdd pointsAdd = new PointsAdd("token#$%^&*(>//γραμμή токен\n\r\t", 23, "", 0.5f, 12.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }

    public void testPointsAdd8() {
        PointsAdd pointsAdd = new PointsAdd(getToken(), 23, "token#$%^&*(>//γραμμή токен\n\r\t", 0.5f, 12.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 0);
            }
        };

        pointsAdd.execute();
    }


    public void testPointsAdd9() {
        String token = "";
        for (int i = 0; i < 250; i ++)
            token += "строка строка строка ";

        PointsAdd pointsAdd = new PointsAdd(token, 23, "title test", 0.5f, 12.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }

    public void testPointsAdd10() {
        String token = "";
        for (int i = 0; i < 250; i ++)
            token += "строка строка строка ";

        PointsAdd pointsAdd = new PointsAdd(getToken(), 23, token, 0.5f, 12.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 0);
            }
        };

        pointsAdd.execute();
    }

    public void testPointsAdd11() {
        PointsAdd pointsAdd = new PointsAdd(getToken(), 23, "title test", -99.0f, 12.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }

    public void testPointsAdd12() {
        PointsAdd pointsAdd = new PointsAdd(getToken(), 23, "title test", 99.0f, 12.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }


    public void testPointsAdd13() {
        PointsAdd pointsAdd = new PointsAdd(getToken(), 23, "title test", 4.0f, -999.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }

    public void testPointsAdd14() {
        PointsAdd pointsAdd = new PointsAdd(getToken(), 23, "title test", 4.0f, 999.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }

    public void testPointsAdd15() {
        PointsAdd pointsAdd = new PointsAdd(getToken(), 23, "title test", 4.0f, 12.0d, -999.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }

    public void testPointsAdd16() {
        PointsAdd pointsAdd = new PointsAdd(getToken(), 23, "title test", 4.0f, 12.0d, 999.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }


    public void testPointsAdd17() {
        PointsAdd pointsAdd = new PointsAdd(getToken(), 23, "title test", 4.0f, 12.0d, 12.0d, -1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }


    public void testPointsAdd18() {
        PointsAdd pointsAdd = new PointsAdd(getToken(), -9, "title test", 4.0f, 12.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }

    public void testPointsAdd19() {
        PointsAdd pointsAdd = new PointsAdd(getToken(), 9999999, "title test", 4.0f, 12.0d, 12.0d, 1231231) {
            @Override
            public void onPostExecute(PointsResponse response) {
                assertEquals(response.code, 1);
            }
        };

        pointsAdd.execute();
    }


    /**
     *
     *
     *
     *
     *          GetsDbHelper
     *
     *
     *
     *
     *
     *
     */
        public void testDb1_1() {
            Point point = new Point();
            point.name = "name";
            point.latitude = -21.0;
            point.longitude = 21.0;
            point.rating = 4.5f;

            GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
            dbHelper.clearDatabase();
            dbHelper.addPoint(point);

            ArrayList<Point> points = dbHelper.getPoints();
            Point pointFromDb = dbHelper.getPoints().get(0);

            assertEquals(points.size(), 1);

            assertEquals(pointFromDb.name, point.name);
            assertEquals(pointFromDb.latitude, point.latitude);
            assertEquals(pointFromDb.longitude, point.longitude);
            assertEquals(pointFromDb.rating, point.rating);
        }

    public void testDb1_2() {
        Point point = new Point();
        point.name = "name";
        point.latitude = -210.0;
        point.longitude = 21.0;
        point.rating = 4.5f;

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoint(point);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(points.size(), 0);
    }

    public void testDb1_3() {
        Point point = new Point();
        point.name = "name";
        point.latitude = 221.0;
        point.longitude = 21.0;
        point.rating = 4.5f;

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoint(point);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(points.size(), 0);
    }

    public void testDb1_4() {
        Point point = new Point();
        point.name = "name";
        point.latitude = -21.0;
        point.longitude = 221.0;
        point.rating = 4.5f;

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoint(point);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(points.size(), 0);
    }


    public void testDb1_5() {
        Point point = new Point();
        point.name = "name";
        point.latitude = -21.0;
        point.longitude = -221.0;
        point.rating = 4.5f;

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoint(point);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(points.size(), 0);
    }


    public void testDb1_6() {
        Point point = new Point();
        point.name = "name";
        point.latitude = -21.0;
        point.longitude = 21.0;
        point.rating = -99f;

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoint(point);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(points.size(), 0);
    }



    public void testDb1_7() {
        Point point = new Point();
        point.name = "name";
        point.latitude = -21.0;
        point.longitude = 21.0;
        point.rating = 99f;

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoint(point);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(points.size(), 0);
    }




    public void testDb1_8() {
        Point point = new Point();
        point.name = null;
        point.latitude = -21.0;
        point.longitude = 21.0;
        point.rating = 4.5f;

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoint(point);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(points.size(), 0);
    }

    public void testDb1_9() {
        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoint(null);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(points.size(), 0);
    }


    public void testDb1_10() {
        Point point = new Point();
        point.name = "name";
        point.latitude = -21.0;
        point.longitude = 21.0;
        point.rating = 4.5f;

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoint(point);
        dbHelper.addPoint(point);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(points.size(), 1);
    }


    public void testDb1_11() {
        Point point = new Point();
        point.name = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001";
        point.latitude = -21.0;
        point.longitude = 21.0;
        point.rating = 4.5f;

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoint(point);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(points.size(), 1);
    }

    public void testDb1_12() {
        Point point = new Point();
        point.name = "name";
        point.latitude = -21.0;
        point.longitude = 21.0;
        point.rating = 4.5f;
        point.uuid = "abc11";

        Point point1 = new Point();
        point.name = "new name";
        point.longitude = -12.0;
        point.latitude = 12.0;
        point.rating = 4.2f;
        point.uuid = "abc11";

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoint(point);
        dbHelper.addPoint(point1);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(points.size(), 1);
    }

    public void testDb1_13() {
        Point point = new Point();
        point.name = "name@#$%^&*(словоపదం";
        point.latitude = -21.0;
        point.longitude = 21.0;
        point.rating = 4.5f;

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoint(point);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(points.size(), 1);
    }

    public void testDb1_14() {
        Point point = new Point();
        point.name = " DROP TABLE * ";
        point.latitude = -21.0;
        point.longitude = 21.0;
        point.rating = 4.5f;

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoint(point);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(points.size(), 1);
        assertEquals(points.get(0).name, point.name);
    }














    public void testDb2_1() {

        ArrayList<Point> originalPoints = new ArrayList<Point>();

        for (int i = 0; i < 50; i++) {
            Point point = new Point();
            point.name = "name " + i;
            point.latitude = -21.0;
            point.longitude = 21.0;
            point.rating = 4.5f;

            originalPoints.add(point);
        }

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoints(originalPoints);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(originalPoints.size(), points.size());
    }


    public void testDb2_2() {

        ArrayList<Point> originalPoints = new ArrayList<Point>();

        for (int i = 0; i < 50; i++) {
            Point point = new Point();
            point.name = "name " + i;
            point.latitude = 991.0;
            point.longitude = -91.0;
            point.rating = 49.5f;

            originalPoints.add(point);
        }

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoints(originalPoints);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(0, points.size());
    }

    public void testDb2_3() {
        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoints(null);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(0, points.size());
    }

    public void testDb2_4() {

        ArrayList<Point> originalPoints = new ArrayList<Point>();

        for (int i = 0; i < 50; i++) {
            Point point = new Point();
            point.name = "name ";
            point.latitude = -21.0;
            point.longitude = 21.0;
            point.rating = 4.5f;

            originalPoints.add(point);
        }

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoints(originalPoints);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(1, points.size());
    }

    public void testDb2_5() {

        ArrayList<Point> originalPoints = new ArrayList<Point>();
        Point point1 = new Point();
        point1.name = "name ";
        point1.latitude = -21.0;
        point1.longitude = 21.0;
        point1.rating = 4.5f;

        for (int i = 0; i < 50; i++) {
            Point point = new Point();
            point.name = "name ";
            point.latitude = -21.0;
            point.longitude = 21.0;
            point.rating = 4.5f;

            originalPoints.add(point);
        }

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoint(point1);
        dbHelper.addPoints(originalPoints);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(1, points.size());
    }



    public void testDb3_1() {

        ArrayList<Point> originalPoints = new ArrayList<Point>();

        for (int i = 0; i < 50; i++) {
            Point point = new Point();
            point.name = "name ";
            point.latitude = -21.0 + i;
            point.longitude = 21.0;
            point.rating = 4.5f;

            originalPoints.add(point);
        }

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();
        dbHelper.addPoints(originalPoints);

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(originalPoints.size(), points.size());
    }


    public void testDb3_3() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        ArrayList<Point> points = dbHelper.getPoints();

        assertEquals(new ArrayList<Point>(), points);
    }







    public void testDb4_1() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        Category category = new Category();
        category.name = "abc";
        category.id = 0;
        category.description = "no";
        category.urlIcon = "no";

        dbHelper.addCategory(
                category.id,
                category.name,
                category.description,
                category.urlIcon
        );

        assertEquals(dbHelper.getCategories().size(), 1);
    }


    public void testDb4_2() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        Category category = new Category();
        category.name = "abc";
        category.id = -90;
        category.description = "no";
        category.urlIcon = "no";

        dbHelper.addCategory(
                category.id,
                category.name,
                category.description,
                category.urlIcon
        );

        assertEquals(dbHelper.getCategories().size(), 0);
    }

    public void testDb4_3() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        Category category = new Category();
        category.name = null;
        category.id = 1;
        category.description = "no";
        category.urlIcon = "no";

        dbHelper.addCategory(
                category.id,
                category.name,
                category.description,
                category.urlIcon
        );

        assertEquals(dbHelper.getCategories().size(), 0);
    }


    public void testDb4_4() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        Category category = new Category();
        category.name = "abc";
        category.id = 1;
        category.description = "no";
        category.urlIcon = "no";

        dbHelper.addCategory(
                category.id,
                category.name,
                category.description,
                category.urlIcon
        );

        dbHelper.addCategory(
                category.id,
                category.name,
                category.description,
                category.urlIcon
        );

        assertEquals(dbHelper.getCategories().size(), 1);
    }



    public void testDb4_5() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        Category category = new Category();
        category.name = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001";
        category.id = 1;
        category.description = "no";
        category.urlIcon = "no";

        dbHelper.addCategory(
                category.id,
                category.name,
                category.description,
                category.urlIcon
        );

        assertEquals(dbHelper.getCategories().size(), 1);
    }






    public void testDb4_6() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        Category category = new Category();
        category.name = "; DROP TABLE *;";
        category.id = 1;
        category.description = "no";
        category.urlIcon = "no";

        dbHelper.addCategory(
                category.id,
                category.name,
                category.description,
                category.urlIcon
        );

        assertEquals(dbHelper.getCategories().size(), 1);
    }


    public void testDb4_7() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        Category category = new Category();
        category.name = "abc";
        category.id = 1;
        category.description = "; DROP TABLE *;";
        category.urlIcon = "no";

        dbHelper.addCategory(
                category.id,
                category.name,
                category.description,
                category.urlIcon
        );

        assertEquals(dbHelper.getCategories().size(), 1);
    }

    public void testDb4_8() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        Category category = new Category();
        category.name = "abc";
        category.id = 1;
        category.description = "no";
        category.urlIcon = "; DROP TABLE *;";

        dbHelper.addCategory(
                category.id,
                category.name,
                category.description,
                category.urlIcon
        );

        assertEquals(dbHelper.getCategories().size(), 1);
    }











    public void testDb5_1() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        Category category = new Category();
        category.name = "abc";
        category.id = 1;
        category.description = "no";
        category.urlIcon = " no";

        dbHelper.addCategory(
                category.id,
                category.name,
                category.description,
                category.urlIcon
        );

        assertEquals(dbHelper.getCategories().size(), 1);
    }




    public void testDb5_3() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        Category category = new Category();
        category.name = "abc";
        category.id = 1;
        category.description = "no";
        category.urlIcon = "nope";

        dbHelper.addCategory(
                category.id,
                category.name,
                category.description,
                category.urlIcon
        );

        assertEquals(dbHelper.getCategories(), new ArrayList<Category>());
    }







    public void testDb6_1() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        Category category = new Category();
        category.name = "abc";
        category.id = 1;
        category.description = "no";
        category.urlIcon = "nope";

        dbHelper.addCategory(
                category.id,
                category.name,
                category.description,
                category.urlIcon
        );

        assertEquals(dbHelper.getCategoryName(1), "abc");
    }



    public void testDb6_2() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        assertNull(dbHelper.getCategoryName(1));
    }





    public void testDb6_3() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        Category category = new Category();
        category.name = "abc";
        category.id = 1;
        category.description = "no";
        category.urlIcon = "nope";

        dbHelper.addCategory(
                category.id,
                category.name,
                category.description,
                category.urlIcon
        );

        assertNull(dbHelper.getCategoryName(100));
    }


    public void testDb6_4() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        Category category = new Category();
        category.name = "abc";
        category.id = 1;
        category.description = "no";
        category.urlIcon = "nope";

        dbHelper.addCategory(
                category.id,
                category.name,
                category.description,
                category.urlIcon
        );

        assertNull(dbHelper.getCategoryName(-90));
    }








    public void testStringEmpty_1() {
        boolean isStringEmpty = AddNewPointActivity.isStringEmpty("string");

        assertFalse(isStringEmpty);
    }

    public void testStringEmpty_2() {
        boolean isStringEmpty = AddNewPointActivity.isStringEmpty("строка string #$%^&*( పదం\n\r\t");

        assertFalse(isStringEmpty);
    }

    public void testStringEmpty_3() {
        boolean isStringEmpty = AddNewPointActivity.isStringEmpty(null);

        assertFalse(isStringEmpty);
    }

    public void testStringEmpty_4() {
        boolean isStringEmpty = AddNewPointActivity.isStringEmpty(" ");

        assertFalse(isStringEmpty);
    }

    public void testStringEmpty_5() {
        boolean isStringEmpty = AddNewPointActivity.isStringEmpty("");

        assertTrue(isStringEmpty);
    }

    public void testStringEmpty_6() {
        boolean isStringEmpty = AddNewPointActivity.isStringEmpty("\n\r\t");

        assertFalse(isStringEmpty);
    }








    public void testStressDb() {

        GetsDbHelper dbHelper = new GetsDbHelper(getContext(), DatabaseType.USER_GENERATED);
        dbHelper.clearDatabase();

        ArrayList<Point> points = new ArrayList<Point>();
        for (int i = 0; i < 500; i++){
            Point point = new Point();
            point.name = "name" + i;
            point.latitude = -210.0;
            point.longitude = 21.0;
            point.rating = 4.5f;

            points.add(point);
        }

        dbHelper.addPoints(points);


        //ArrayList<Point> points = dbHelper.getPoints();
        MoreAsserts.assertNotEqual(1, 0);
    }








    /*public void testDbAddPoint() {
        ArrayList<Point> points = new ArrayList<Point>();
        points.add(new Point(1, "123", "uRL", "r","0", 12.34d, 12.34d, 4.5f, "nouuid"));
        getsDbHelper.clearDatabase();
        getsDbHelper.addPoints(points);
        assertEquals(getsDbHelper.getPoints(), points);
    }*/


    /*public void testDbClear() {
        getsDbHelper.clearDatabase();
        assertNull(getsDbHelper.getPoints());
    }*/




}