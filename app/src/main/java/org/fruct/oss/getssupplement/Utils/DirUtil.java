package org.fruct.oss.getssupplement.Utils;

import android.util.Log;

import org.fruct.oss.getssupplement.Const;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by Andrey on 26.03.2016.
 */
public class DirUtil {

    public static void unzip(File zipFile, File outputDir) throws IOException {
        ZipInputStream zipInputStream = null;
        FileOutputStream fileOutputStream = null;
        byte[] buffer = new byte[4096];
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                File file = new File(outputDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    file.mkdir();
                } else {
                    fileOutputStream = new FileOutputStream(new File(outputDir, zipEntry.getName()));

                    int readed;
                    while ((readed = zipInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, readed);
                    }

                    fileOutputStream.close();
                    fileOutputStream = null;
                }
            }

            Log.d(Const.TAG, "Zip archive successfully extracted");
        } catch (IOException e) {
            Log.d(Const.TAG, "Can't extract archive");
            throw e;
        } finally {
            Utils.silentClose(zipInputStream);
            Utils.silentClose(fileOutputStream);
        }
    }
}
