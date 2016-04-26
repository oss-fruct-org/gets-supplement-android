package org.fruct.oss.getssupplement.Utils;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by Andrey on 21.04.2016.
 */
public class DownloadXmlTask extends AsyncTask<Void, Void, String> {
    private String url;
    public DownloadXmlTask(String url) {
        this.url = url;
    }

    @Override
    protected String doInBackground(Void... params) {

        URL link = null;
        String response = null;

        try {
            link = new URL(this.url);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (link != null) {
            try {
                InputStream in = new BufferedInputStream(link.openStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while (-1 != (n = in.read(buf))) {
                    out.write(buf, 0, n);
                }
                out.close();
                in.close();
                response = out.toString();
            }
            catch (Exception e)
            { e.printStackTrace(); }
        }
        return response;
    }

}
