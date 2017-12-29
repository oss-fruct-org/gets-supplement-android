package org.fruct.oss.getssupplement.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.fruct.oss.getssupplement.R;

import java.util.HashMap;

/**
 * Created by Andrey on 19.07.2015.
 */
public class IconHolder {
    private static String TAG = "IconHolder";

    private static IconHolder instance;
    private static Bitmap callout = null;

    private IconHolder() {
        bitmaps = new HashMap<>();
        cleanBitmaps = new HashMap<>();
    }

    public static IconHolder getInstance() {
        if (instance == null) {
            instance = new IconHolder();
        }

        return instance;
    }

    // CategoryId -> Bitmap
    private HashMap<Integer, Bitmap> bitmaps;
    private HashMap<Integer, Bitmap> cleanBitmaps;

    public void addBitmap(Context context, int categoryId, Bitmap bitmap) {
        if (bitmaps.containsKey(categoryId))
            return;

        if (callout == null) {
            callout = BitmapFactory.decodeResource(context.getResources(), R.drawable.callout);
        }
        int size = context.getResources().getDimensionPixelSize(R.dimen.marker_iconsize_normal);
        Log.d(TAG, "Icon size = " + size);
        Bitmap fixedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(fixedBitmap);
        canvas.drawBitmap(callout, null, new RectF(0,0,size,size), null);
        canvas.drawBitmap(bitmap, null, new RectF(size *15 / 80, size * 10 / 80, size * 60 / 80, size * 55 / 80), null);
        bitmaps.put(categoryId, fixedBitmap);
        cleanBitmaps.put(categoryId, bitmap);
    }

    public Bitmap getBitmapByCategoryId(int categoryId) {
        if (!bitmaps.containsKey(categoryId))
            return null;

        return bitmaps.get(categoryId);
    }

    public Bitmap getCleanBitmapByCategoryId(int categoryId) {
        if (!cleanBitmaps.containsKey(categoryId))
            return null;

        return cleanBitmaps.get(categoryId);
    }

    public Drawable getDrawableByCategoryId(Resources res, int categoryId) {
        return new BitmapDrawable(res, getCleanBitmapByCategoryId(categoryId));
    }
}