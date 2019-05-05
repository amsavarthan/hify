package com.amsavarthan.hify.feature_ai.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.lang3.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.MediaType;

public class Utils {

    // WolframAlpha API

    public static final String WOLFRAM_APP_ID = "2THKVH-GR58KTPHAU";
    public static final String WOLFRAM_BASE_URL = "http://api.wolframalpha.com/v2/query?";
    public static final String WOLFRAM_POD_STATE_BASE_URL = "&podstate=";

    public static final String PACKAGE_NAME = "com.amsavarthan.hify";
    public static final String TESS_DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/Hify/AI/";
    public static final String tess_lang = "eng";

    private static int screenWidth = 0;
    private static int screenHeight = 0;


    public static String calculateMD5(String wikiImageFileName) {
        MessageDigest md = null;
        StringBuffer stringBuffer = null;

        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        md.update(wikiImageFileName.getBytes());

        byte byteData[] = md.digest();

        //convert the byte to hex format
        stringBuffer = new StringBuffer();

        for (int i = 0; i < byteData.length; i++) {
            stringBuffer.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return stringBuffer.toString();
    }

    // Check network connectivity

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenHeight(Context c) {
        if (screenHeight == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
        }

        return screenHeight;
    }

    public static int getScreenWidth(Context c) {
        if (screenWidth == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
        }

        return screenWidth;
    }

}
