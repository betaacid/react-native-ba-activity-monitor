package com.reactnativebaactivitymonitor;

import static com.reactnativebaactivitymonitor.DetectedActivityService.DETECTED_PENDING_INTENT_REQUEST_CODE;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

public class TransitionsReceiver extends BroadcastReceiver {

  private final static String TAG = "TransitionsReceiver";

  private final BaActivityMonitorModule module;

  public TransitionsReceiver() {
    super();
    this.module = null;
  }

  public TransitionsReceiver(BaActivityMonitorModule module) {
    super();
    this.module = module;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "onReceive(): " + intent);
    if (!TextUtils.equals(BaActivityMonitorModule.TRANSITIONS_RECEIVER_ACTION, intent.getAction())) {
      Log.e(TAG, "Received an unsupported action in TransitionsReceiver: action = " +
        intent.getAction());
      return;
    }

    if (ActivityRecognitionResult.hasResult(intent)) {
      ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
      WritableArray activities = Arguments.createArray();

      for (DetectedActivity detectedActivity : result.getProbableActivities()) {
        WritableMap activity = Arguments.createMap();
        activity.putString("type", ActivityUtils.mapActivityType(detectedActivity.getType()));
        activity.putDouble("confidence", detectedActivity.getConfidence());
        activities.pushMap(activity);
      }

      if(module != null) {
        module.sendJSEvent("activities", activities);
      }
    }
  }

  public static PendingIntent getPendingIntent(Context context) {
    Intent intent = new Intent(BaActivityMonitorModule.TRANSITIONS_RECEIVER_ACTION);
    return PendingIntent.getBroadcast(context, DETECTED_PENDING_INTENT_REQUEST_CODE, intent,
      PendingIntent.FLAG_MUTABLE);
  }

}
