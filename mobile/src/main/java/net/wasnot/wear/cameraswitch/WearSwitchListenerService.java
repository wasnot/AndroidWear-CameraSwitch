package net.wasnot.wear.cameraswitch;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.WearableListenerService;

public class WearSwitchListenerService extends WearableListenerService {

    private final static String TAG = WearSwitchListenerService.class.getSimpleName();

    public WearSwitchListenerService() {
    }


    private static final String FIELD_CHANGING = "changing";
    private static final String FIELD_ALARM_ON = "alarm_on";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (Log.isLoggable("mobile", Log.DEBUG)) {
            Log.d(TAG, "onDataChanged: " + dataEvents + " for " + getPackageName());
        }
        for (DataEvent event : dataEvents) {
            Log.i(TAG, event + " test");
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.i(TAG, event + " deleted.." + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                Boolean changing = DataMap.fromByteArray(item.getData()).get(FIELD_CHANGING);
                Boolean alarmOn = DataMap.fromByteArray(item.getData()).get(FIELD_ALARM_ON);
                if (alarmOn) {
                    Intent i = new Intent(this, CameraActivity.class);
                    i.setAction(CameraActivity.ACTION_SHUTTER);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } else {
                    Intent i = new Intent(this, CameraActivity.class);
                    i.setAction(CameraActivity.ACTION_FINISH);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            }
        }
        dataEvents.close();
    }

}
