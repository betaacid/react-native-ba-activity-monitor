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
 * This fails if used on a non supported platform. Currently it only supports 'ios' and 'android'.
 *
 * @returns a promise that resolves if it was initialized correctly
 */
export function start(): Promise<PermissionResult | void> {
  if (Platform.OS !== 'android' && Platform.OS !== 'ios') {
    throw new Error(`Platform '${Platform.OS}' not supported`);
  }

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

export type PermissionResult = 'granted' | 'denied' | 'unavailable' | 'blocked';

/**
 * Asks for permission. Can be used for both android and ios platforms and WILL fail if tries to use another platform.
 *
 * @returns a promise that resolves whether the notification was accepted or not <boolean>.
 */
export function askPermission(): Promise<PermissionResult> {
  if (Platform.OS === 'ios') {
    return BaActivityMonitor.askPermissionIOS();
  } else if (Platform.OS === 'android') {
    return BaActivityMonitor.askPermissionAndroid();
  } else {
    throw new Error(`Platform '${Platform.OS}' not supported`);
  }
}

export default {
  start,
  stop,
  askPermission,
};
