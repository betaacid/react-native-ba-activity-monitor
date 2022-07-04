import { NativeEventEmitter, NativeModules, Platform } from 'react-native';

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

export type ActivityTransitionType = 'enter' | 'exit';
export type ActivityType =
  | 'in-vehicle'
  | 'on-bicycle'
  | 'still'
  | 'walking'
  | 'running';

export interface Activity {
  type: ActivityType;
  transitionType: ActivityTransitionType;
  timestamp: number;
}

export type OnActivityCallback = (activities: Activity[]) => void;
export type OnActivityUnregisterCallback = () => void;

/**
 * the callbacks called on activity
 */
let registeredCallbacks: OnActivityCallback[] = [];

/**
 * native module event listener destructor
 */
let eventListenerDestructor: (() => void) | null = null;

/**
 * Starts the activity monitoring service. If the permission was not yet granted, it will
 * ask the user automatically. For only asking permission, use the method `ActivityMonitor.askPermission()`.
 * This fails if used on a non supported platform. Currently it only supports 'ios' and 'android'.
 *
 * @returns a promise that resolves if it was initialized correctly
 */
export async function start(): Promise<PermissionResult | Boolean> {
  if (Platform.OS !== 'android' && Platform.OS !== 'ios') {
    throw new Error(`Platform '${Platform.OS}' not supported`);
  }

  const result: PermissionResult | Boolean = await BaActivityMonitor.start();

  if (result !== 'granted' && result !== true) {
    return result;
  }

  const eventEmitter = new NativeEventEmitter(NativeModules.BaActivityMonitor);
  const eventListener = eventEmitter.addListener(
    'activities',
    (event: Activity[]) => {
      registeredCallbacks.forEach((c) => c(event));
    }
  );

  eventListenerDestructor = eventListener.remove;

  return result;
}

/**
 * Stops the activity monitoring service if its running.
 *
 * @returns whether the service stopped correctly.
 */
export function stop(): Promise<boolean> {
  if (eventListenerDestructor) {
    eventListenerDestructor();
  }
  return BaActivityMonitor.stop();
}

export type PermissionResult = 'granted' | 'denied' | 'blocked';

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

/**
 * Sends mock activities to simulate the native incoming activity transition. Used for testing.
 * @param activities the activities to mock
 */
export function mockActivities(activities: Activity[]) {
  BaActivityMonitor.sendMockActivities(activities);
}

/**
 * Registers a listener that receives a list of probable, with the most likely one as the first result.
 * @param callback
 */
export function onActivities(
  callback: OnActivityCallback
): OnActivityUnregisterCallback {
  registeredCallbacks.push(callback);
  return () => {
    registeredCallbacks = registeredCallbacks.filter((rc) => rc !== callback);
  };
}

export function unregisterOnActivitiesListener(
  callback: OnActivityCallback
): void {
  registeredCallbacks = registeredCallbacks.filter((rc) => rc !== callback);
}

export default {
  start,
  stop,
  askPermission,
  onActivities,
  mockActivities,
};
