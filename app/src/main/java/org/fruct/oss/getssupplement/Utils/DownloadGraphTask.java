package org.fruct.oss.getssupplement.Utils;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by Andrey on 27.04.2016.
 */
public class DownloadGraphTask extends AsyncTask<Void, Void, Boolean> {

    private String url;
    private String fileName;
    private Context context;

    public DownloadGraphTask(String url, String fileName, Context context) {
        this.url = url;
        this.fileName = fileName;
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success;
        try {
            GHUtil.downloadGHMap(url, fileName, context);
            DirUtil.unzipInStorage(context, fileName);
            success = true;
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }
}
