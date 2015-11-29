package org.fruct.oss.getssupplement;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by Andrey on 29.11.2015.
 */
public class AppInfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appinfo);

        TextView textView = null;
        textView = (TextView) findViewById(R.id.app_info_text);

        if (textView != null) {
            String text;
            text = (String) textView.getText();

            try {
                text += " ";
                text += getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            }
            catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            textView.setText(text);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        setResult(Const.INTENT_RESULT_CODE_OK, intent);
        finish();
        return true;
    }
}

