package org.fruct.oss.getssupplement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.fruct.oss.getssupplement.Api.PublishChannel;
import org.fruct.oss.getssupplement.Model.BasicResponse;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Andrey on 28.10.2015.
 */
public class CategoryArrayAdapter extends ArrayAdapter {
    private final Context context;
    private final ArrayList<String> names;
    private final ArrayList<Integer> id;
    private final boolean isActions;

    public CategoryArrayAdapter(Context context, ArrayList<String> names, ArrayList<Integer> id, boolean isActions) {
        super(context, R.layout.list_category, R.id.list_text, names);
        this.context = context;
        this.names = names;
        this.id = id;
        this.isActions = isActions;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;


        if (isActions) rowView = inflater.inflate(R.layout.list_categoryactions, parent, false);
        else rowView = inflater.inflate(R.layout.list_category, parent, false);

        TextView textView = (TextView) rowView.findViewById(R.id.list_text);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.list_icon);
        CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.list_check);

        imageView.setVisibility(View.VISIBLE);
        textView.setText(names.get(position));

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Settings.saveBoolean(context, Const.PREFS_CATEGORY + id.get(position), true);
                }
                else {
                    Settings.saveBoolean(context, Const.PREFS_CATEGORY + id.get(position), false);
                }
            }
        });

        if (IconHolder.getInstance().getDrawableByCategoryId(context.getResources(), id.get(position)) != null)
            imageView.setImageDrawable(IconHolder.getInstance().getDrawableByCategoryId(context.getResources(), id.get(position)));

        if (isActions) {
            if (Settings.getIsTrusted(context)) {
                final ImageButton imageButton = (ImageButton) rowView.findViewById(R.id.list_publish);
                imageButton.setVisibility(View.VISIBLE);

                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int category = id.get(position);

                        PublishChannel publishChannel = new PublishChannel(Settings.getToken(context), category, Const.API_PUBLISH) {
                            @Override
                            protected void onPostExecute(BasicResponse response) {
                                super.onPostExecute(response);

                                if (response.code == 0)
                                    Toast.makeText(context, context.getString(R.string.successful_publish), Toast.LENGTH_SHORT).show();
                                else if (response.code == 2)
                                    Toast.makeText(context, context.getString(R.string.repeated_publish), Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(context, context.getString(R.string.unsuccessful_publish), Toast.LENGTH_SHORT).show();
                            }
                        };

                        publishChannel.execute();
                    }
                });
            }
        }
        return rowView;
    }
}
