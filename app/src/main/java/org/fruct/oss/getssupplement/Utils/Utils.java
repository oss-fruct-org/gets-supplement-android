package org.fruct.oss.getssupplement.Utils;

import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.zip.ZipFile;

/**
 * Created by Andrey on 26.03.2016.
 */
public class Utils {


    public static void silentClose(@Nullable Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * Workaround for "java.lang.IncompatibleClassChangeError: interface not implemented" in Samsung Galaxy S4
     */
    public static void silentClose(@Nullable ZipFile zipFile) {
        try {
            if (zipFile != null) {
                zipFile.close();
            }
        } catch (IOException ignored) {
        }
    }
}