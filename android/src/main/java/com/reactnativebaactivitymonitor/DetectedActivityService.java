package com.reactnativebaactivitymonitor;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class DetectedActivityService extends Service {

  public static final int DETECTED_ACTIVITY_NOTIFICATION_ID = 10;
  public static final int DETECTED_PENDING_INTENT_REQUEST_CODE = 100;

  private boolean activityTrackingEnabled = false;

  final class LocalBinder extends Binder {

  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return new LocalBinder();
  }

  @Override
  public void onCreate() {
    super.onCreate();

    requestActivityUpdates();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    removeActivityUpdates();
    NotificationManagerCompat.from(this).cancel(DETECTED_ACTIVITY_NOTIFICATION_ID);
  }

  private void requestActivityUpdates() {
    if(activityTrackingEnabled) {
      return;
    }

    List<ActivityTransition> transitions = new ArrayList<>();
    ActivityUtils.addAllRelevantTransitions(transitions);
    ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

    Task<Void> task = ActivityRecognition.getClient(this).requestActivityTransitionUpdates(request, TransitionsReceiver.getPendingIntent(this));

    task.addOnSuccessListener(
      result -> {
        activityTrackingEnabled = true;
      });

    task.addOnFailureListener(
      e -> {
        activityTrackingEnabled = false;
      });
  }

  private void removeActivityUpdates() {
    if(!activityTrackingEnabled) {
      return;
    }

    Task<Void> task = ActivityRecognition.getClient(this).removeActivityTransitionUpdates(TransitionsReceiver.getPendingIntent(this));
    task.addOnSuccessListener(
      result -> {
        activityTrackingEnabled = false;
      });

    task.addOnFailureListener(
      e -> {
        activityTrackingEnabled = true;
      });
  }

}
