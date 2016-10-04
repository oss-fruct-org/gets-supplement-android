package org.fruct.oss.getssupplement;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.mapbox.mapboxsdk.MapboxAccountManager;

/**
 * Created by Andrey on 29.09.2016.
 */
public class GetsSupplementApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        MapboxAccountManager.start(this, getString(R.string.mapbox_token));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
