package com.reactnativebaactivitymonitor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

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
  public static final String CHANNEL_ID = "AndroidForegroundService";

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
    // Create a notification channel for devices running Android Oreo and later
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Cardata", NotificationManager.IMPORTANCE_DEFAULT);
      channel.setDescription("Cardata is tracking your motion activity.");
      channel.enableLights(true);
      channel.setLightColor(Color.BLUE);
      channel.setShowBadge(true);
      NotificationManager manager = getSystemService(NotificationManager.class);
      manager.createNotificationChannel(channel);
    }

    // Start the service in the foreground with a notification
    startForeground(DETECTED_ACTIVITY_NOTIFICATION_ID, createNotification());
    requestActivityUpdates();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    removeActivityUpdates();
    stopForeground(true);
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

  private Notification createNotification() {
    ApplicationInfo applicationInfo = getApplicationContext().getApplicationInfo();
    int appIconResId = applicationInfo.icon;

    return new NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Cardata")
      .setContentText("Cardata is tracking your trips automatically.")
      .setSmallIcon(appIconResId)
      .build();
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
