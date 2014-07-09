package net.wasnot.wear.cameraswitch;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Camera;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by aidaakihiro on 2014/07/09.
 */
public class NotificationUtil {

    private final static String TAG = NotificationUtil.class.getSimpleName();

    public static void showNotification(Context con) {
        Log.d(TAG, "showNotification");
        NotificationManagerCompat nm = NotificationManagerCompat.from(con);
        NotificationCompat.Builder b = new NotificationCompat.Builder(con);
        b.setSmallIcon(R.drawable.ic_launcher);
        b.setContentIntent(getIntent(con));
        b.setContentTitle(con.getString(R.string.app_name));
        b.setContentText("testtest");

        // Create a WearableExtender to add functionality for wearables
        WearableExtender wearableExtender =
                new WearableExtender()
                        .setHintHideIcon(true);
        b.extend(wearableExtender);

// Create the action
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_camera, "camera", getShutterIntent(con)).build();
//        b.extend(new WearableExtender().addAction(action));

// Create a big text style for the second page
        BigTextStyle secondPageStyle = new BigTextStyle();
        secondPageStyle.setBigContentTitle("Page 2").bigText("A lot of text...");
// Create second page notification
        Notification secondPageNotification = new NotificationCompat.Builder(con).setStyle(
                secondPageStyle).build();
// Add second page with wearable extender and extend the main notification
        Notification twoPageNotification = new WearableExtender().addPage(
                secondPageNotification).extend(b).addAction(action).build();

        nm.notify(0, twoPageNotification);
    }

    private static PendingIntent getShutterIntent(Context con) {
        Intent i = new Intent(con, CameraActivity.class);
        i.setAction(CameraActivity.ACTION_SHUTTER);
        return PendingIntent.getActivity(con, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getIntent(Context con) {
        Intent i = new Intent(con, CameraActivity.class);
        return PendingIntent.getActivity(con, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
