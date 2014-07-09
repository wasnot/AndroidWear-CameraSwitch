package net.wasnot.wear.cameraswitch;

import java.util.concurrent.TimeUnit;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in a service on a
 * separate handler thread. <p> TODO: Customize class - update intent actions, extra parameters and
 * static helper methods.
 */
public class SwitchIntentService extends IntentService
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = SwitchIntentService.class.getSimpleName();

    private static final String FIELD_CHANGING = "changing";
    private static final String FIELD_ALARM_ON = "alarm_on";
    private static final String PATH_SOUND_ALARM = "/sound_alarm";
    public static final String ACTION_TOGGLE_ALARM = "action_toggle_alarm";
    public static final String ACTION_CANCEL_ALARM = "action_alarm_off";

    // Timeout for making a connection to GoogleApiClient (in milliseconds).
    private static final long CONNECTION_TIME_OUT_MS = 100;
    private GoogleApiClient mGoogleApiClient;

    public SwitchIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "onHandleIntent");
        }
        if (mGoogleApiClient.isConnected()) {
            // Set the alarm off by default.
            boolean alarmOn = false;
            if (intent.getAction().equals(ACTION_TOGGLE_ALARM)) {
                // Get current state of the alarm.
                DataItemBuffer result = Wearable.DataApi.getDataItems(mGoogleApiClient).await();
                if (result.getStatus().isSuccess()) {
                    if (result.getCount() == 1) {
                        alarmOn = DataMap.fromByteArray(result.get(0).getData())
                                .getBoolean(FIELD_ALARM_ON, false);
                    } else {
                        Log.e(TAG, "Unexpected number of DataItems found.\n"
                                + "\tExpected: 1\n"
                                + "\tActual: " + result.getCount());
                    }
                } else if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "onHandleIntent: failed to get current alarm state");
                }
                result.close();
                // Toggle alarm.
                alarmOn = !alarmOn;
//                alarmOn = true;
                // Change notification text based on new value of alarmOn.
                String notificationText = alarmOn ? getString(R.string.turn_alarm_off)
                        : getString(R.string.turn_alarm_on);
                MainActivity.updateNotification(this, notificationText);
                Log.v(TAG, "onHandleIntent");
            }
            // Use alarmOn boolean to update the DataItem - phone will respond accordingly
            // when it receives the change.
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_SOUND_ALARM);
            putDataMapRequest.getDataMap().putBoolean(FIELD_CHANGING, alarmOn);
            putDataMapRequest.getDataMap().putBoolean(FIELD_ALARM_ON, true);
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataMapRequest.asPutDataRequest())
                    .await();
        } else {
            Log.e(TAG, "Failed to toggle alarm on phone - Client disconnected from Google Play "
                    + "Services");
        }
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }
}
