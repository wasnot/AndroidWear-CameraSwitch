package net.wasnot.wear.cameraswitch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CameraShutterReceiver extends BroadcastReceiver {

    private final static String TAG = CameraShutterReceiver.class.getSimpleName();
    public final static String ACTION_SHUTTER = "net.wasnot.wear.cameraswitch.SHUTTER";

    public CameraShutterReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        if (intent == null) {
            return;
        }
        Context con = context.getApplicationContext();
        if (ACTION_SHUTTER.equals(intent.getAction())) {
//            Intent i = new Intent
        }
    }
}
