import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-ba-activity-monitor' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const BaActivityMonitor = NativeModules.BaActivityMonitor
  ? NativeModules.BaActivityMonitor
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

/**
 * Starts the activity monitoring service. If the permission was not yet granted, it will
 * ask the user automatically. For only asking permission, use the method `ActivityMonitor.askPermission()`.
 *
 * @returns true if starts correctly or a rejection with the error cause
 */
export function start(): Promise<boolean> {
  return BaActivityMonitor.start();
}

/**
 * Stops the activity monitoring service if its running.
 *
 * @returns whether the service stopped correctly.
 */
export function stop(): Promise<boolean> {
  return BaActivityMonitor.start();
}

export function askPermission(): Promise<boolean> {
  if (Platform.OS === 'ios') {
    return BaActivityMonitor.askPermissionIOS();
  } else if (Platform.OS === 'android') {
    return BaActivityMonitor.askPermissionAndroid();
  } else {
    throw new Error(`Platform ${Platform.OS} not supported`);
  }
}

export default {
  start,
  stop,
  askPermission,
};
