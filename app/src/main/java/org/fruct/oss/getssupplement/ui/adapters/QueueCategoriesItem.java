package org.fruct.oss.getssupplement.ui.adapters;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Andrey on 13.03.2017.
 */

public class QueueCategoriesItem {
    private int categoryId;
    private RecyclerView rvPoints;

    public QueueCategoriesItem(int categoryId, RecyclerView rvPoints) {
        this.categoryId = categoryId;
        this.rvPoints = rvPoints;
    }
}
