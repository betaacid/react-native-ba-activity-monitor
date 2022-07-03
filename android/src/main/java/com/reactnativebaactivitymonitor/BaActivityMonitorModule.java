package com.reactnativebaactivitymonitor;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.facebook.react.BuildConfig;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

@ReactModule(name = BaActivityMonitorModule.NAME)
public class BaActivityMonitorModule extends ReactContextBaseJavaModule {

    private final static String TAG = "BaActivityModule";
    public static final String NAME = "BaActivityMonitor";

    private TransitionsReceiver mTransitionsReceiver = new TransitionsReceiver();
    private PendingIntent mActivityTransitionsPendingIntent;
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 45;
    private boolean activityTrackingEnabled;

    private boolean runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;
    private final String TRANSITIONS_RECEIVER_ACTION =
        "BA_ACTIVITY_MONITOR_TRANSITIONS_RECEIVER_ACTION";


    public BaActivityMonitorModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    private void addFullTransition(List<ActivityTransition> transitions, int activity) {
        transitions.add(
                new ActivityTransition.Builder()
                .setActivityType(activity)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());

        transitions.add(
                new ActivityTransition.Builder()
                .setActivityType(activity)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
    }

    public boolean isAllowedToTrackActivities() {
        if (runningQOrLater) {
            return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    getReactApplicationContext().getCurrentActivity(),
                    Manifest.permission.ACTIVITY_RECOGNITION
            );
        } else {
            return true;
        }
    }

    private void startTracking(Promise promise) {
        List<ActivityTransition> transitions = new ArrayList<>();

        addFullTransition(transitions, DetectedActivity.IN_VEHICLE);
        addFullTransition(transitions, DetectedActivity.WALKING);
        addFullTransition(transitions, DetectedActivity.RUNNING);
        addFullTransition(transitions, DetectedActivity.STILL);
        addFullTransition(transitions, DetectedActivity.WALKING);

        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        getReactApplicationContext().getCurrentActivity().registerReceiver(mTransitionsReceiver, new IntentFilter(TRANSITIONS_RECEIVER_ACTION));
        mActivityTransitionsPendingIntent = PendingIntent.getBroadcast(getReactApplicationContext().getCurrentActivity(), 0, new Intent(TRANSITIONS_RECEIVER_ACTION), 0);

        Task<Void> task = ActivityRecognition.getClient(getReactApplicationContext().getCurrentActivity())
          .requestActivityTransitionUpdates(request, mActivityTransitionsPendingIntent);

        task.addOnSuccessListener(
          new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                activityTrackingEnabled = true;
                promise.resolve(true);

            }
        });

        task.addOnFailureListener(
          new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                activityTrackingEnabled = false;
                promise.reject(e);
            }
        });
    }

    @ReactMethod
    public void askPermissionAndroid() {
      ActivityCompat.requestPermissions(
        getReactApplicationContext().getCurrentActivity(),
        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
        PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
    }

    @ReactMethod
    public void start(Promise promise) {
        if (isAllowedToTrackActivities()) {
            startTracking(promise);
            promise.resolve(true);
        } else {
            askPermissionAndroid();
        }
    }

    @ReactMethod
    public void stop() {
      getReactApplicationContext().getCurrentActivity().unregisterReceiver(mTransitionsReceiver);
    }

    public class TransitionsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive(): " + intent);

            if (!TextUtils.equals(TRANSITIONS_RECEIVER_ACTION, intent.getAction())) {

                Log.e(TAG, "Received an unsupported action in TransitionsReceiver: action = " +
                        intent.getAction());
                return;
            }
        }
    }

}
