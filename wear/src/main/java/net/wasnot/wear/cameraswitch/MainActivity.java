package net.wasnot.wear.cameraswitch;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

public class MainActivity extends Activity {

    private static final int FIND_PHONE_NOTIFICATION_ID = 2;
    private static NotificationCompat.Builder sNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        // Create a notification with an action to toggle an alarm on the phone.
        Intent toggleAlarmOperation = new Intent(this, SwitchIntentService.class);
        toggleAlarmOperation.setAction(SwitchIntentService.ACTION_TOGGLE_ALARM);
        PendingIntent toggleAlarmIntent = PendingIntent.getService(this, 0, toggleAlarmOperation,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action alarmAction = new NotificationCompat.Action(
                android.R.drawable.ic_menu_camera, "camera", toggleAlarmIntent);
        // This intent turns off the alarm if the user dismisses the card from the wearable.
        Intent cancelAlarmOperation = new Intent(this, SwitchIntentService.class);
        cancelAlarmOperation.setAction(SwitchIntentService.ACTION_CANCEL_ALARM);
        PendingIntent cancelAlarmIntent = PendingIntent.getService(this, 0, cancelAlarmOperation,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // Use a spannable string for the notification title to resize it.
        SpannableString title = new SpannableString(getString(R.string.app_name));
        title.setSpan(new RelativeSizeSpan(0.85f), 0, title.length(), Spannable.SPAN_POINT_MARK);
        sNotification = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(getString(R.string.turn_alarm_on))
                .setSmallIcon(R.drawable.ic_launcher)
                .setVibrate(new long[]{0, 50})  // Vibrate to bring card to top of stream.
                .setDeleteIntent(cancelAlarmIntent)
//                .extend(new NotificationCompat.WearableExtender()
//                        .addAction(alarmAction)
////                        .setContentAction(0)
//                        .setHintHideIcon(true))
                .addAction(alarmAction)
                .setLocalOnly(true)
                .setPriority(Notification.PRIORITY_MAX);

        NotificationManagerCompat.from(this)
                .notify(FIND_PHONE_NOTIFICATION_ID, sNotification.build());

        finish();
    }

    /**
     * Updates the text on the wearable notification. This is used so the notification reflects the
     * current state of the alarm on the phone. For instance, if the alarm is turned on, the
     * notification text indicates that the user can tap it to turn it off, and vice-versa.
     *
     * @param notificationText The new text to display on the wearable notification.
     */
    public static void updateNotification(Context context, String notificationText) {
        sNotification.setContentText(notificationText);
        NotificationManagerCompat.from(context)
                .notify(FIND_PHONE_NOTIFICATION_ID, sNotification.build());
    }
}
