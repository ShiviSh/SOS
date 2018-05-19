package com.sarts.shivi.sos.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;
import com.sarts.shivi.sos.MainActivity;
import com.sarts.shivi.sos.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by Shivi on 15-01-2018.
 */

public class ParseCustomBroadcastReceiver extends ParsePushBroadcastReceiver {

    String notificationTitle;
    String notificationAlert;
    String notificationURI;
    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            Log.d("push data alert", json.getString("alert").toString());

            if (json.has("title")) {
                notificationTitle = json.getString("title").toString();
            }
            if (json.has("alert")) {
                notificationAlert = json.getString("alert").toString();
            }

            if(json.has("uri")) {
                notificationURI = json.getString("uri");
            }

            Intent resultIntent = null;
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);


            resultIntent = new Intent(context, MainActivity.class);
            stackBuilder.addParentStack(MainActivity.class);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            final Intent openintent = new Intent(context, MainActivity.class);
            openintent.putExtra("source","notification");
            openintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            openintent.setAction(Intent.ACTION_MAIN);
            openintent.addCategory(Intent.CATEGORY_LAUNCHER);

//Customize your notification - sample code
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context,"parse_notification")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setTicker(String.format(Locale.getDefault(), "%s: %s", notificationTitle, notificationAlert))
                            .setContentTitle(notificationTitle)
                            .setContentText(notificationAlert)
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setContentIntent(PendingIntent.getActivity(context,0,openintent,0));
            if (notificationAlert != null
                    && notificationAlert.length() > ParsePushBroadcastReceiver.SMALL_NOTIFICATION_MAX_CHARACTER_LIMIT) {
                builder.setStyle(new NotificationCompat.BigTextStyle()
                                 .bigText(notificationAlert)
                                 .setBigContentTitle(notificationTitle));
            }

            int mNotificationId = 001;
            NotificationManager mNotifyMgr =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyMgr.notify(mNotificationId, builder.build());


        } catch (JSONException e) {
            Log.d("json error", e.getMessage());
        }

    }
}
