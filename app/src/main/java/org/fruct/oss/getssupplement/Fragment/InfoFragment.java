package org.fruct.oss.getssupplement.Fragment;/**
 * Created by Yaroslav21 on 27.07.16.
 */

import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.fruct.oss.getssupplement.R;

public class InfoFragment extends Fragment {
    public InfoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.info_fragment, container, false);

        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            TextView versionView = (TextView) rootView.findViewById(R.id.textViewVersion);
            versionView.setText(versionView.getText() + " " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException ignore) {
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_fragment_menu, menu);



        super.onCreateOptionsMenu(menu, inflater);
    }
}
