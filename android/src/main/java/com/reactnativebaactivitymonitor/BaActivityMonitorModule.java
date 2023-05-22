package com.reactnativebaactivitymonitor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;
import com.google.android.gms.common.internal.safeparcel.SafeParcelableSerializer;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ReactModule(name = BaActivityMonitorModule.NAME)
public class BaActivityMonitorModule extends ReactContextBaseJavaModule implements PermissionListener {

  private final static String TAG = "BaActivityModule";
  public static final String NAME = "BaActivityMonitor";

  private TransitionsReceiver mTransitionsReceiver = new TransitionsReceiver(this);
  private PendingIntent mActivityTransitionsPendingIntent;
  private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 18923671;
  private boolean activityTrackingEnabled;

  private boolean runningQOrLater =
    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;

  public static final String TRANSITIONS_RECEIVER_ACTION =
    "BA_ACTIVITY_MONITOR_TRANSITIONS_RECEIVER_ACTION";

  private final String GRANTED = "granted";
  private final String DENIED = "denied";
  private final String BLOCKED = "blocked";

  private Request mPermissionRequest;


  public BaActivityMonitorModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  @Override
  public void onCatalystInstanceDestroy() {
    this.stop();
  }

  public boolean isAllowedToTrackActivities() {
    if (runningQOrLater) {
      try {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
          getReactApplicationContext(),
          Manifest.permission.ACTIVITY_RECOGNITION
        );
      } catch (Exception e) {
        return true;
      }
    } else {
      return true;
    }
  }

  private void startTracking(Promise promise) {
    try {
      if(mActivityTransitionsPendingIntent == null) {
        getReactApplicationContext().registerReceiver(mTransitionsReceiver, new IntentFilter(TRANSITIONS_RECEIVER_ACTION));
        mActivityTransitionsPendingIntent = TransitionsReceiver.getPendingIntent(getReactApplicationContext());
      }

      ActivityRecognition.getClient(getReactApplicationContext())
        .requestActivityUpdates(1000L, mActivityTransitionsPendingIntent)
        .addOnSuccessListener(
          result -> {
            activityTrackingEnabled = true;
            promise.resolve(true);
          })
        .addOnFailureListener(
          e -> {
            activityTrackingEnabled = false;
            promise.reject(e);
          });
    } catch (SecurityException secException) {
      promise.reject(secException);
    } catch (Exception exception) {
      promise.reject(exception);
    }

  }

  @ReactMethod
  public void sendMockActivities(ReadableArray activities) {
    Intent intent = new Intent(TRANSITIONS_RECEIVER_ACTION);

    List<DetectedActivity> events = new ArrayList();

    for (Object activityMap : activities.toArrayList()) {
      HashMap activity = (HashMap) activityMap;
      DetectedActivity detectedActivity = new DetectedActivity(
        ActivityUtils.remapActivityType((String) activity.get("type")),
        (int) activity.get("confidence")
      );
      events.add(detectedActivity);
    }

    ActivityRecognitionResult result = new ActivityRecognitionResult(events, 1000L, SystemClock.elapsedRealtimeNanos());
    SafeParcelableSerializer.serializeToIntentExtra(result, intent, "com.google.android.location.internal.EXTRA_ACTIVITY_RESULT");
    getReactApplicationContext().sendBroadcast(intent);
  }

  @ReactMethod
  public void addListener(String eventName) {

  }

  @ReactMethod
  public void removeListeners(Integer count) {

  }

  @ReactMethod
  public void isStarted(Promise promise) {
    promise.resolve(activityTrackingEnabled);
  }

  @ReactMethod
  public void askPermissionAndroid(Promise promise) {
    if (isAllowedToTrackActivities()) {
      promise.resolve(GRANTED);
      return;
    }

    String permission = Manifest.permission.ACTIVITY_RECOGNITION;
    PermissionAwareActivity activity = getPermissionAwareActivity();
    boolean[] rationaleStatuses = new boolean[1];
    rationaleStatuses[0] = activity.shouldShowRequestPermissionRationale(permission);

    mPermissionRequest = new Request(
      rationaleStatuses,
      new Callback() {
        @SuppressLint("ApplySharedPref")
        @Override
        public void invoke(Object... args) {
          int[] results = (int[]) args[0];

          if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            promise.resolve(GRANTED);
          } else {
            PermissionAwareActivity activity = (PermissionAwareActivity) args[1];
            boolean[] rationaleStatuses = (boolean[]) args[2];

            if (rationaleStatuses[0] &&
              !activity.shouldShowRequestPermissionRationale(permission)) {
              promise.resolve(BLOCKED);
            } else {
              promise.resolve(DENIED);
            }
          }
        }
      });

    activity.requestPermissions(new String[]{permission}, PERMISSION_REQUEST_ACTIVITY_RECOGNITION, this);
  }

  @ReactMethod
  public void start(Promise promise) {
    if (activityTrackingEnabled) {
      return;
    }
    try {
      if (isAllowedToTrackActivities()) {
        startTracking(promise);
        Intent serviceIntent = new Intent(getReactApplicationContext(), DetectedActivityService.class);
        ContextCompat.startForegroundService(getReactApplicationContext(), serviceIntent);
        promise.resolve(true);
      } else {
        promise.reject("invalid_permission_status", "Permission needed.");
      }
    } catch (Exception exception) {
      promise.reject(exception.getMessage(),exception.getMessage());
    }
  }

  @SuppressLint("MissingPermission")
  @ReactMethod
  public void stop() {
    if (!activityTrackingEnabled) {
      return;
    }
    try {
      ActivityRecognition.getClient(getReactApplicationContext())
        .removeActivityUpdates(mActivityTransitionsPendingIntent);
      activityTrackingEnabled = false;
      Intent intent = new Intent(getReactApplicationContext(), DetectedActivityService.class);
      getReactApplicationContext().stopService(intent);
    } catch (Exception exception) {
      return;
    }
  }

  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode != PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
      return false;
    }
    mPermissionRequest.callback.invoke(grantResults, getPermissionAwareActivity(), mPermissionRequest.rationaleStatuses);
    mPermissionRequest = null;
    return true;
  }

  public void sendJSEvent(String eventName,
                          @Nullable Object params) {
    getReactApplicationContext()
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  private PermissionAwareActivity getPermissionAwareActivity() {
    Activity activity = getCurrentActivity();
    if (activity == null) {
      throw new IllegalStateException(
        "Tried to use permissions API while not attached to an Activity.");
    } else if (!(activity instanceof PermissionAwareActivity)) {
      throw new IllegalStateException(
        "Tried to use permissions API but the host Activity doesn't implement PermissionAwareActivity.");
    }
    return (PermissionAwareActivity) activity;
  }

  private class Request {

    public boolean[] rationaleStatuses;
    public Callback callback;

    public Request(boolean[] rationaleStatuses, Callback callback) {
      this.rationaleStatuses = rationaleStatuses;
      this.callback = callback;
    }
  }

}
