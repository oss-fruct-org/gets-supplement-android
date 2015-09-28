package org.fruct.oss.getssupplement;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.HashMap;

/**
 * Created by Andrey on 19.07.2015.
 */
public class IconHolder {

    private static IconHolder instance;

    private IconHolder() {
        bitmaps = new HashMap<>();
    }

    public static IconHolder getInstance() {
        if (instance == null)
            instance = new IconHolder();

        return instance;
    }

    // CategoryId -> Bitmap
    private HashMap<Integer, Bitmap> bitmaps;

    public void addBitmap(int categoryId, Bitmap bitmap) {
        if (bitmaps.containsKey(categoryId))
            return;

        bitmaps.put(categoryId, bitmap);
    }

    public Bitmap getBitmapByCategoryId(int categoryId) {
        if (!bitmaps.containsKey(categoryId))
            return null;

        return bitmaps.get(categoryId);
    }

    public Drawable getDrawableByCategoryId(Resources res, int categoryId) {
        return new BitmapDrawable(res, getBitmapByCategoryId(categoryId));

    }

}