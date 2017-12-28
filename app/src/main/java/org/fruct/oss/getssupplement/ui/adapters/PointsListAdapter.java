package org.fruct.oss.getssupplement.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.fruct.oss.getssupplement.Api.PointsAdd;
import org.fruct.oss.getssupplement.Database.GetsDbHelper;
import org.fruct.oss.getssupplement.Model.Point;
import org.fruct.oss.getssupplement.Model.PointsResponse;
import org.fruct.oss.getssupplement.R;
import org.fruct.oss.getssupplement.Utils.IconHolder;
import org.fruct.oss.getssupplement.Utils.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrey on 14.03.2017.
 */

public class PointsListAdapter extends RecyclerView.Adapter<PointsListAdapter.ViewHolder> {

    private Context context;
    private final List<Point> data;

    public PointsListAdapter(Context context, List<Point> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_points_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Drawable icon = IconHolder.getInstance().getDrawableByCategoryId(context.getResources(), data.get(position).categoryId);
        holder.ivCategoryIcon.setImageDrawable(icon);
        holder.tvPointName.setText(data.get(position).getName());
        holder.ibSendPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PointsAdd pointsAdd = new PointsAdd(data.get(position), Settings.getToken(context)) {
                    @Override
                    protected void onPostExecute(PointsResponse pointsResponse) {
                        ArrayList<Point> points = pointsResponse.points;
                        if (points != null && !points.isEmpty()) {
                            // TODO: add to DB_INTERNAL
                            GetsDbHelper.getInstance(context).deleteCachedPoint(data.get(position).id);
                            data.remove(position);

                            notifyDataSetChanged();

                            Toast.makeText(context, context.getString(R.string.successuflly_sent), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, context.getString(R.string.unsuccessuflly_sent), Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                pointsAdd.execute();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivCategoryIcon;
        private TextView tvPointName;
        private ImageButton ibSendPoint;

        public ViewHolder(View v) {
            super(v);
            ivCategoryIcon = (ImageView) v.findViewById(R.id.ivCategoryIcon);
            tvPointName = (TextView) v.findViewById(R.id.tvPointName);
            ibSendPoint = (ImageButton) v.findViewById(R.id.ibSendPoint);
        }
    }
}
