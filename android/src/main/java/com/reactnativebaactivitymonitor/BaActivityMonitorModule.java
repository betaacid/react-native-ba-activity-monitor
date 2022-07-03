package com.reactnativebaactivitymonitor;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;

@ReactModule(name = BaActivityMonitorModule.NAME)
public class BaActivityMonitorModule extends ReactContextBaseJavaModule {
    public static final String NAME = "BaActivityMonitor";



    public BaActivityMonitorModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    private void addFullTransition(List<ActivityTransition> transitions, DetectedActivity activity) {
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

    @ReactMethod
    public void start(Promise promise) {
        List<ActivityTransition> transitions = new ArrayList<>();

        addFullTransition(transitions, DetectedActivity.IN_VEHICLE);
        addFullTransition(transitions, DetectedActivity.WALKING);
        addFullTransition(transitions, DetectedActivity.RUNNING);
        addFullTransition(transitions, DetectedActivity.STILL);
        addFullTransition(transitions, DetectedActivity.WALKING);

        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        Task<Void> task = ActivityRecognition.getClient(this)
          .requestActivityTransitionUpdates(request, myPendingIntent);
    }

}
