package com.amsavarthan.hify.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.MainActivity;
import com.amsavarthan.hify.ui.activities.friends.FriendProfile;
import com.amsavarthan.hify.ui.activities.notification.NotificationActivity;
import com.amsavarthan.hify.ui.activities.notification.NotificationImage;
import com.amsavarthan.hify.ui.activities.notification.NotificationImageReply;
import com.amsavarthan.hify.ui.activities.notification.NotificationReplyActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by amsavarthan on 10/3/18.
 */

public class NotificationUtil {

    private static String TAG = NotificationUtil.class.getSimpleName();

    private Context mContext;

    public NotificationUtil(Context mContext) {
        this.mContext = mContext;
    }

    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void clearNotificationsById(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    public static long getTimeMilliSec(String timeStamp) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void showNotificationMessage(int id, String timeStamp, String click_action, String channelName, String channelDesc, String user_image, String title, String message, Intent intent) {
        showNotificationMessage(id, timeStamp, click_action, channelName, channelDesc, user_image, title, message, intent, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager, String channelName, String channelDesc) {
        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(Config.ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(channelDesc);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.BLUE);
        adminChannel.canShowBadge();
        adminChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    public void showNotificationMessage(int id, String timeStamp, String click_action, String channelName, String channelDesc, final String user_image, final String title, final String message, Intent intent, String imageUrl) {

        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return;

        // notification icon
        final int icon = R.mipmap.ic_launcher;

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        0,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                mContext, Config.ADMIN_CHANNEL_ID);


        try {
            if (!TextUtils.isEmpty(imageUrl)) {

                Bitmap bitmap = getBitmapFromURL(imageUrl);


                if (bitmap != null) {
                    showBigNotification(id, timeStamp, click_action, channelName, channelDesc, user_image, bitmap, mBuilder, icon, title, message, resultPendingIntent);
                } else {
                    showSmallNotification(id, timeStamp, click_action, channelName, channelDesc, user_image, mBuilder, icon, title, message, resultPendingIntent);
                }

            } else {
                showSmallNotification(id, timeStamp, click_action, channelName, channelDesc, user_image, mBuilder, icon, title, message, resultPendingIntent);
            }
        } catch (Exception e) {
            Log.e("showNotificationMessage", e.getMessage());
        }


    }

    public Intent getIntent(String click_action) {

        Intent resultIntent;

        switch (click_action) {
            case "com.amsavarthan.hify.TARGETNOTIFICATION":
                resultIntent = new Intent(mContext, NotificationActivity.class);
                break;
            case "com.amsavarthan.hify.TARGETNOTIFICATIONREPLY":
                resultIntent = new Intent(mContext, NotificationReplyActivity.class);
                break;
            case "com.amsavarthan.hify.TARGETNOTIFICATION_IMAGE":
                resultIntent = new Intent(mContext, NotificationImage.class);
                break;
            case "com.amsavarthan.hify.TARGETNOTIFICATIONREPLY_IMAGE":
                resultIntent = new Intent(mContext, NotificationImageReply.class);
                break;
            case "com.amsavarthan.hify.TARGET_FRIENDREQUEST":
                resultIntent = new Intent(mContext, FriendProfile.class);
                break;
            default:
                resultIntent = new Intent(mContext, MainActivity.class);
                break;
        }
        return resultIntent;

    }

    private void showSmallNotification(int id, String timeStamp, String click_action, String channelName, String channelDesc, String user_image, NotificationCompat.Builder mBuilder, int icon, String title, String message, PendingIntent resultPendingIntent) {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(message);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager, channelName, channelDesc);
        }

        Notification notification;
        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setColorized(true)
                .setWhen(getTimeMilliSec(timeStamp))
                .setShowWhen(true)
                .setSound(Uri.parse("android.resource://"+mContext.getPackageName()+"/"+R.raw.hify_sound))
                .setColor(Color.parseColor("#2591FC"))
                .setStyle(bigTextStyle)
                //.addPerson(user_image)
                //.setLargeIcon(getBitmapFromURL(user_image))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(message)
                .build();

        notificationManager.notify(id, notification);
    }

    private void showBigNotification(int id, String timeStamp, String click_action, String channelName, String channelDesc, String user_image, Bitmap bitmap, NotificationCompat.Builder mBuilder, int icon, String title, String message, PendingIntent resultPendingIntent) {

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.bigPicture(bitmap);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager, channelName, channelDesc);
        }

        Notification notification;

        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setColorized(true)
                .setShowWhen(true)
                .setSound(Uri.parse("android.resource://"+mContext.getPackageName()+"/"+R.raw.hify_sound))
                .setWhen(getTimeMilliSec(timeStamp))
                .setColor(Color.parseColor("#2591FC"))
                .setStyle(bigPictureStyle)
                //.setLargeIcon(getBitmapFromURL(user_image))
                .setSmallIcon(R.mipmap.ic_launcher)
                //.addPerson(user_image)
                .setContentText(message)
                .build();

        notificationManager.notify(id, notification);
    }

    private Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
