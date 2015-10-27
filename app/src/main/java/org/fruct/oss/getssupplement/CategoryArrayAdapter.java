package org.fruct.oss.getssupplement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Andrey on 28.10.2015.
 */
public class CategoryArrayAdapter extends ArrayAdapter {
    private final Context context;
    private final ArrayList<String> names;
    private final ArrayList<Integer> id;

    public CategoryArrayAdapter(Context context, ArrayList<String> names, ArrayList<Integer> id) {
        super(context, R.layout.list_categories, R.id.list_text, names);
        this.context = context;
        this.names = names;
        this.id = id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_categories, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.list_text);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.list_icon);

        imageView.setVisibility(View.VISIBLE);
        textView.setText(names.get(position));
        if (IconHolder.getInstance().getDrawableByCategoryId(context.getResources(), id.get(position)) != null)
            imageView.setImageDrawable(IconHolder.getInstance().getDrawableByCategoryId(context.getResources(), id.get(position)));

        return rowView;
    }
}
