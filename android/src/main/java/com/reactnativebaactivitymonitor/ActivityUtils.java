package com.reactnativebaactivitymonitor;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.DetectedActivity;

public final class ActivityUtils {

  public static int remapActivityType(String activityType) {
    switch(activityType) {
      case "in-vehicle": return DetectedActivity.IN_VEHICLE;
      case "walking": return DetectedActivity.WALKING;
      case "running": return DetectedActivity.RUNNING;
      case "still": return DetectedActivity.STILL;
      case "on-bicycle": return DetectedActivity.ON_BICYCLE;
      case "on-foot": return DetectedActivity.ON_FOOT;
      default: return -1;
    }
  }

  public static int remapTransitionType(String activityType) {
    switch(activityType) {
      case "enter": return ActivityTransition.ACTIVITY_TRANSITION_ENTER;
      case "exit": return ActivityTransition.ACTIVITY_TRANSITION_EXIT;
      default: return -1;
    }
  }

  public static String mapActivityType(int activityType) {
    switch(activityType) {
      case DetectedActivity.IN_VEHICLE: return "in-vehicle";
      case DetectedActivity.WALKING: return "walking";
      case DetectedActivity.RUNNING: return "running";
      case DetectedActivity.STILL: return "still";
      case DetectedActivity.ON_BICYCLE: return "on-bicycle";
      case DetectedActivity.ON_FOOT: return "on-foot";
      default: return "unknown";
    }
  }

  public static String mapTransitionType(int activityType) {
    switch(activityType) {
      case ActivityTransition.ACTIVITY_TRANSITION_ENTER: return "enter";
      case ActivityTransition.ACTIVITY_TRANSITION_EXIT: return "exit";
      default: return "unknown";
    }
  }

}
