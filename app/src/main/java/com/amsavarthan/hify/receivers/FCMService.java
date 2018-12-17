package com.amsavarthan.hify.receivers;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.amsavarthan.hify.feature_ai.activities.AnswersActivity;
import com.amsavarthan.hify.ui.activities.account.UpdateAvailable;
import com.amsavarthan.hify.ui.activities.MainActivity;
import com.amsavarthan.hify.ui.activities.friends.FriendProfile;
import com.amsavarthan.hify.ui.activities.lottie.FestivalActivity;
import com.amsavarthan.hify.ui.activities.notification.NotificationActivity;
import com.amsavarthan.hify.ui.activities.notification.NotificationImage;
import com.amsavarthan.hify.ui.activities.notification.NotificationImageReply;
import com.amsavarthan.hify.ui.activities.notification.NotificationReplyActivity;
import com.amsavarthan.hify.ui.activities.post.CommentsActivity;
import com.amsavarthan.hify.utils.Config;
import com.amsavarthan.hify.utils.NotificationUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by amsavarthan on 10/3/18.
 */

public class FCMService extends FirebaseMessagingService {

    private static final String TAG = FCMService.class.getSimpleName();

    private NotificationUtil notificationUtils;
    private String cDesc;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        handleDataMessage(remoteMessage);
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {

        final String title = remoteMessage.getData().get("title");
        final String body = remoteMessage.getData().get("body");
        String click_action = remoteMessage.getData().get("click_action");
        String message = remoteMessage.getData().get("message");
        String from_name = remoteMessage.getData().get("from_name");
        String from_image = remoteMessage.getData().get("from_image");
        String from_id = remoteMessage.getData().get("from_id");
        final String imageUrl = remoteMessage.getData().get("image");
        String reply_for = remoteMessage.getData().get("reply_for");
        long id = System.currentTimeMillis();
        String timeStamp = String.valueOf(remoteMessage.getData().get("timestamp"));

        //Friend Request Message data
        String friend_id = remoteMessage.getData().get("friend_id");
        String friend_name = remoteMessage.getData().get("friend_name");
        String friend_email = remoteMessage.getData().get("friend_email");
        String friend_image = remoteMessage.getData().get("friend_image");
        String friend_token = remoteMessage.getData().get("friend_token");

        //CommentData and Like Data
        String post_id=remoteMessage.getData().get("post_id");
        String admin_id=remoteMessage.getData().get("admin_id");
        String post_desc=remoteMessage.getData().get("post_desc");
        String channel=remoteMessage.getData().get("channel");

        //UpdateData
        String version=remoteMessage.getData().get("version");
        String improvements=remoteMessage.getData().get("improvements");
        String link=remoteMessage.getData().get("link");

        String question_id=remoteMessage.getData().get("question_id");
		boolean read= Boolean.parseBoolean(remoteMessage.getData().get("read"));
		String doc_id=remoteMessage.getData().get("doc_id");

        //Festival
        String festival_name=remoteMessage.getData().get("festival_name");
        String festival_text=remoteMessage.getData().get("festival_text");
        String send_text=remoteMessage.getData().get("send_text");
        String dev_id=remoteMessage.getData().get("dev_id");
        String reason=remoteMessage.getData().get("reason");

        final Intent resultIntent;

        switch (click_action) {
            case "com.amsavarthan.hify.TARGETNOTIFICATION":

                resultIntent = new Intent(getApplicationContext(), NotificationActivity.class);

                break;
            case "com.amsavarthan.hify.TARGETNOTIFICATIONREPLY":

                resultIntent = new Intent(getApplicationContext(), NotificationReplyActivity.class);

                break;
            case "com.amsavarthan.hify.TARGETNOTIFICATION_IMAGE":

                resultIntent = new Intent(getApplicationContext(), NotificationImage.class);

                break;
            case "com.amsavarthan.hify.TARGETNOTIFICATIONREPLY_IMAGE":

                resultIntent = new Intent(getApplicationContext(), NotificationImageReply.class);

                break;
            case "com.amsavarthan.hify.TARGET_FRIENDREQUEST":

                resultIntent = new Intent(getApplicationContext(), FriendProfile.class);

                break;
            case "com.amsavarthan.hify.TARGET_ACCEPTED":

                resultIntent = new Intent(getApplicationContext(), FriendProfile.class);

                break;
            case "com.amsavarthan.hify.TARGET_DECLINED":

                resultIntent = new Intent(getApplicationContext(), FriendProfile.class);

                break;
            case "com.amsavarthan.hify.TARGET_COMMENT":

                resultIntent = new Intent(getApplicationContext(), MainActivity.class).putExtra("openFragment","forComment");

                break;
            case "com.amsavarthan.hify.TARGET_UPDATE":

                resultIntent = new Intent(getApplicationContext(), UpdateAvailable.class);

                break;
            case "com.amsavarthan.hify.TARGET_LIKE":

                resultIntent = new Intent(getApplicationContext(), MainActivity.class).putExtra("openFragment","forLike");

                break;
            case "com.amsavarthan.hify.TARGET_FORUM":

                resultIntent = new Intent(getApplicationContext(), AnswersActivity.class);
                break;
            case "com.amsavarthan.hify.TARGET_FESTIVAL":

                resultIntent = new Intent(getApplicationContext(), FestivalActivity.class);
                break;
            default:

                resultIntent = new Intent(getApplicationContext(), MainActivity.class);

                break;
        }

        resultIntent.putExtra("title", title);
        resultIntent.putExtra("body", body);
        resultIntent.putExtra("name", from_name);
        resultIntent.putExtra("from_image", from_image);
        resultIntent.putExtra("message", message);
        resultIntent.putExtra("from_id", from_id);
        resultIntent.putExtra("notification_id", id);
        resultIntent.putExtra("timestamp", timeStamp);
        resultIntent.putExtra("reply_for", reply_for);
        resultIntent.putExtra("image", imageUrl);
        resultIntent.putExtra("reply_image", from_image);

        resultIntent.putExtra("f_id", friend_id);
        resultIntent.putExtra("f_name", friend_name);
        resultIntent.putExtra("f_email", friend_email);
        resultIntent.putExtra("f_image", friend_image);
        resultIntent.putExtra("f_token", friend_token);

        resultIntent.putExtra("user_id", admin_id);
        resultIntent.putExtra("post_id", post_id);
        resultIntent.putExtra("post_desc", post_desc);

        resultIntent.putExtra("channel", channel);
        resultIntent.putExtra("version", version);
        resultIntent.putExtra("improvements", improvements);
        resultIntent.putExtra("link", link);

        resultIntent.putExtra("question_id",question_id);
        resultIntent.putExtra("read",read);
        resultIntent.putExtra("doc_id",doc_id);

        resultIntent.putExtra("festival_name",festival_name);
        resultIntent.putExtra("festival_text",festival_text);
        resultIntent.putExtra("send_text",send_text);
        resultIntent.putExtra("dev_id",dev_id);
        resultIntent.putExtra("reason",reason);

        cDesc="Used to show "+channel+" Messages";

        try {
            boolean foreground=new ForegroundCheckTask().execute(getApplicationContext()).get();

            if(!foreground){

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {

                    if (!TextUtils.isEmpty(from_image)) {
                        showNotificationMessage((int) id, timeStamp, click_action, channel, cDesc, from_image, getApplicationContext(), title, body, resultIntent);
                    } else {
                        showNotificationMessage((int) id, timeStamp, click_action, channel, cDesc, friend_image, getApplicationContext(), title, body, resultIntent);
                    }

                } else {
                    // image is present, show notification with image
                    if (!TextUtils.isEmpty(from_image)) {
                        showNotificationMessageWithBigImage((int) id, timeStamp, click_action, channel, cDesc, from_image, getApplicationContext(), title, body, resultIntent, imageUrl);
                    } else {
                        showNotificationMessageWithBigImage((int) id, timeStamp, click_action, channel, cDesc, friend_image, getApplicationContext(), title, body, resultIntent, imageUrl);
                    }
                }

            }else{

                boolean active= getSharedPreferences("fcm_activity",MODE_PRIVATE).getBoolean("active",true);

                if(active) {
                    Intent intent = new Intent(Config.PUSH_NOTIFICATION);

                    intent.putExtra("title", title);
                    intent.putExtra("body", body);
                    intent.putExtra("name", from_name);
                    intent.putExtra("from_image", from_image);
                    intent.putExtra("message", message);
                    intent.putExtra("from_id", from_id);
                    intent.putExtra("notification_id", id);
                    intent.putExtra("timestamp", timeStamp);
                    intent.putExtra("reply_for", reply_for);
                    intent.putExtra("image", imageUrl);
                    intent.putExtra("reply_image", from_image);

                    intent.putExtra("f_id", friend_id);
                    intent.putExtra("f_name", friend_name);
                    intent.putExtra("f_email", friend_email);
                    intent.putExtra("f_image", friend_image);
                    intent.putExtra("f_token", friend_token);

                    intent.putExtra("user_id", admin_id);
                    intent.putExtra("post_id", post_id);
                    intent.putExtra("post_desc", post_desc);
                    intent.putExtra("click_action", click_action);

                    intent.putExtra("version", version);
                    intent.putExtra("improvements", improvements);
                    intent.putExtra("link", link);
                    intent.putExtra("channel",channel);
                    intent.putExtra("question_id",question_id);

                    intent.putExtra("read",read);
                    intent.putExtra("doc_id",doc_id);

                    intent.putExtra("festival_name",festival_name);
                    intent.putExtra("festival_text",festival_text);
                    intent.putExtra("send_text",send_text);
                    intent.putExtra("dev_id",dev_id);
                    intent.putExtra("reason",reason);

                    if (title.toLowerCase().contains("update")) {
                        showNotificationMessage((int) id, timeStamp, click_action, channel, cDesc, from_image, getApplicationContext(), title, body, resultIntent);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    } else {
                        showNotificationMessage((int) id, timeStamp, click_action, channel, cDesc, from_image, getApplicationContext(), title, body, resultIntent);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    }
                }else{

                    if (TextUtils.isEmpty(imageUrl)) {

                        if (!TextUtils.isEmpty(from_image)) {
                            showNotificationMessage((int) id, timeStamp, click_action, channel, cDesc, from_image, getApplicationContext(), title, body, resultIntent);
                        } else {
                            showNotificationMessage((int) id, timeStamp, click_action, channel, cDesc, friend_image, getApplicationContext(), title, body, resultIntent);
                        }

                    } else {
                        // image is present, show notification with image
                        if (!TextUtils.isEmpty(from_image)) {
                            showNotificationMessageWithBigImage((int) id, timeStamp, click_action, channel, cDesc, from_image, getApplicationContext(), title, body, resultIntent, imageUrl);
                        } else {
                            showNotificationMessageWithBigImage((int) id, timeStamp, click_action, channel, cDesc, friend_image, getApplicationContext(), title, body, resultIntent, imageUrl);
                        }
                    }


                }

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }



    }

    private void showNotificationMessage(int id, String timeStamp, String click_action, String channelName, String channelDesc, String user_image, Context context, String title, String message, Intent intent) {
        notificationUtils = new NotificationUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(id, timeStamp, click_action, channelName, channelDesc, user_image, title, message, intent, null);
    }

    private void showNotificationMessageWithBigImage(int id, String timeStamp, String click_action, String channelName, String channelDesc, String user_image, Context context, String title, String message, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(id, timeStamp, click_action, channelName, channelDesc, user_image, title, message, intent, imageUrl);
    }

    class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }

}
