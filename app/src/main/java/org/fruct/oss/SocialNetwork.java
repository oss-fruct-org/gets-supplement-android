package org.fruct.oss;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.jraska.falcon.Falcon;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.model.VKWallPostResult;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import org.fruct.oss.getssupplement.R;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by kulakov on 27.06.17.
 */

public class SocialNetwork {

    private static Bitmap mBitMap;

    private static final String[] sMyScope = new String[]{
            VKScope.FRIENDS,
            VKScope.WALL,
            VKScope.PHOTOS,
            VKScope.NOHTTPS,
            VKScope.MESSAGES,
            VKScope.DOCS
    };

    public static void sendImage(String mMessage, Context context) {
        File screenshotFile = takeScreenshot(context);

        mBitMap = BitmapFactory.decodeFile(screenshotFile.getAbsolutePath());
        loadPhotoToMyVKWall(mBitMap, mMessage, context);

    }

    public static void loadPhotoToMyVKWall(final Bitmap photo, final String message, final Context mContext) {
        if (!VKSdk.isLoggedIn()) {
            showLogin(mContext);
            return;
        }
        VKRequest request = VKApi.uploadWallPhotoRequest(new VKUploadImage(photo,
                VKImageParameters.jpgImage(0.9f)), getMyId(), 0);
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                // recycle bitmap
                VKApiPhoto photoModel = ((VKPhotoArray) response.parsedModel).get(0);
                makeVKPost(new VKAttachments(photoModel), message, getMyId(), mContext);
                mBitMap = null;
            }
            @Override
            public void onError(VKError error) {
                // error
                Log.d(getClass().getSimpleName(), "VK error: " + error.toString());
            }
        });
    }

    public static File takeScreenshot(Context mContext) {

        File screenshotFile = getScreenshotFile(mContext);

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        try {
            Falcon.takeScreenshot(getActivity(), screenshotFile);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        String message = "Screenshot captured to " + screenshotFile.getAbsolutePath();
        Toast.makeText(mContext.getApplicationContext(), message, Toast.LENGTH_LONG).show();

        return screenshotFile;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static Activity getActivity() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        // https://androidreclib.wordpress.com/2014/11/22/getting-the-current-activity/
        Class activityThreadClass = Class.forName("android.app.ActivityThread");
        Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
        Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
        activitiesField.setAccessible(true);
        ArrayMap activities = (ArrayMap) activitiesField.get(activityThread);
        for(Object activityRecord:activities.values()){
            Class activityRecordClass = activityRecord.getClass();
            Field pausedField = activityRecordClass.getDeclaredField("paused");
            pausedField.setAccessible(true);
            if(!pausedField.getBoolean(activityRecord)) {
                Field activityField = activityRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);
                return activity;
            }
        }
        return null;
    }

    protected static File getScreenshotFile(Context mContext) {
        File screenshotDirectory;
        try {
            screenshotDirectory = getScreenshotsDirectory(mContext.getApplicationContext());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS", Locale.getDefault());

        String screenshotName = dateFormat.format(new Date()) + ".png";
        return new File(screenshotDirectory, screenshotName);
    }

    private static File getScreenshotsDirectory(Context context) throws IllegalAccessException {
        String dirName = "screenshots_" + context.getPackageName();

        File rootDir;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            rootDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else {
            rootDir = context.getDir("screens", Context.MODE_PRIVATE);
        }

        File directory = new File(rootDir, dirName);

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IllegalAccessException("Unable to create screenshot directory " + directory.getAbsolutePath());
            }
        }

        return directory;
    }

    private static void showLogin(Context mContext) {
//        mActivity.getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.container, new LoginFragment())
//                .commit();
        Intent mLogin = new Intent(mContext, LoginFragment.class);
        mContext.startActivity(mLogin);

    }

    private static void makeVKPost(VKAttachments att, String msg, final int ownerId, final Context mContext) {
        VKParameters parameters = new VKParameters();
        parameters.put(VKApiConst.OWNER_ID, String.valueOf(ownerId));
        parameters.put(VKApiConst.ATTACHMENTS, att);
        parameters.put(VKApiConst.MESSAGE, msg);
        VKRequest post = VKApi.wall().post(parameters);
        post.setModelClass(VKWallPostResult.class);
        post.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                // post was added
                Toast.makeText(mContext, "Image was sent", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(VKError error) {
                // error
                Toast.makeText(mContext, "Error: " + error.errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  static int getMyId() {
        final VKAccessToken vkAccessToken = VKAccessToken.currentToken();
        return vkAccessToken != null ? Integer.parseInt(vkAccessToken.userId) : 0;
    }

    public static class LoginFragment extends android.support.v4.app.Fragment {
        private CallbackManager callbackManager;
        private LoginButton loginButton;

        public LoginFragment() {
            super();
        }



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_login, container, false);

            v.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VKSdk.login(getActivity(), sMyScope);
                }
            });


            return v;
        }

    }

}
