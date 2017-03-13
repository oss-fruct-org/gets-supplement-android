package org.fruct.oss.getssupplement.ui.adapters;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
import com.github.aakira.expandablelayout.ExpandableLinearLayout;
import com.github.aakira.expandablelayout.Utils;

import org.fruct.oss.getssupplement.R;

import java.util.List;

/**
 * Created by Andrey on 13.03.2017.
 */
public class ExpandableCategoriesAdapter extends RecyclerView.Adapter<ExpandableCategoriesAdapter.ViewHolder> {

    private final List<QueueCategoriesItem> data;

    private SparseBooleanArray expandState = new SparseBooleanArray();

    public ExpandableCategoriesAdapter(final List<QueueCategoriesItem> data) {
        this.data = data;
        for (int i = 0; i < data.size(); i++) {
            expandState.append(i, false);
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_queue_list, parent, false));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.expandableLayout.setInRecyclerView(true);
        holder.expandableLayout.setInterpolator(new AccelerateDecelerateInterpolator());
        holder.expandableLayout.setExpanded(expandState.get(position));
        holder.expandableLayout.setListener(new ExpandableLayoutListenerAdapter() {
            @Override
            public void onPreOpen() {
                createRotateAnimator(holder.buttonLayout, 0f, 180f).start();
                expandState.put(position, true);
            }

            @Override
            public void onPreClose() {
                createRotateAnimator(holder.buttonLayout, 180f, 0f).start();
                expandState.put(position, false);
            }
        });
        holder.buttonLayout.setRotation(expandState.get(position) ? 180f : 0f);
        holder.expandableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                holder.expandableLayout.toggle();
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout buttonLayout;
        private ExpandableLinearLayout expandableLayout;

        public ViewHolder(View v) {
            super(v);
            buttonLayout = (RelativeLayout) v.findViewById(R.id.btnExpand);
            expandableLayout = (ExpandableLinearLayout) v.findViewById(R.id.expandableLayout);
        }
    }

    private ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "rotation", from, to);
        animator.setDuration(300);
        animator.setInterpolator(Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR));
        return animator;
    }
}
