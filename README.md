# react-native-ba-activity-monitor

Native Activity Monitor for RN apps

## Installation

```sh
yarn add react-native-ba-activity-monitor
```

### Android

Add this to the bottommost part of your manifest `<application>` tag.

```xml
<service android:name="com.reactnativebaactivitymonitor.DetectedActivityService" />
<receiver
    android:name="com.reactnativebaactivitymonitor.TransitionsReceiver"
    android:exported="false"
    android:permission="com.google.android.gms.permission.ACTIVITY_RECOGNITION">
    <intent-filter>
      <action android:name="action.TRANSITIONS_DATA" />
    </intent-filter>
</receiver>
```

## Usage

```js
import ActivityMonitor from 'react-native-ba-activity-monitor';

// ...

const permission = await ActivityMonitor.askPermission();
if (permission === 'granted') {
  await ActivityMonitor.start();
  ActivityMonitor.onActivities((activities) => {
    console.debug(
      '[ActivityMonitor] activities: ' + JSON.stringify(activities)
    );
  });
}

// ...
```

### Considerations

Fully typed with TypeScript. Feel free to aknowledge all the types on the `index.tsx` file and import them as needed in your project.

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

Unlicensed code. For exclusive usage of BetaAcid LLC.

---

Boostraped with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
