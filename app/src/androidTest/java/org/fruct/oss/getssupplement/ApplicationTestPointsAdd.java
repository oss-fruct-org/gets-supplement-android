package org.fruct.oss.getssupplement;

import android.app.Application;
import android.test.ApplicationTestCase;

import org.fruct.oss.getssupplement.Api.PointsAdd;
import org.fruct.oss.getssupplement.Api.PointsGet;
import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.DatabaseType;
import org.fruct.oss.getssupplement.Model.PointsResponse;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTestPointsAdd extends ApplicationTestCase<Application> {
    public ApplicationTestPointsAdd() {
        super(Application.class);
    }

    GetsDbHelper getsDbHelper;

    @Override
    protected void setUp() throws Exception {
        getsDbHelper = new GetsDbHelper(getContext(), DatabaseType.DATA_FROM_API);
    }

    private String getToken() {
        return "g:1/yw3pnNRWu5ajD6AT0F2lie6Qxb5-ld3yFwQohoWwAok";
    }










}