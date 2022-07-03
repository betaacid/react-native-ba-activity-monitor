# react-native-ba-activity-monitor

Native Activity Monitor for RN apps

## Installation

```sh
yarn add react-native-ba-activity-monitor
```

## Usage

```js
import ActivityMonitor from 'react-native-ba-activity-monitor';

// ...

ActivityMonitor.start()
  .then(() => console.log('Success'))
  .catch((e) => console.error('Activity manager not possible to start: ' + e));
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

Unlicensed code. For exclusive usage of BetaAcid LLC.

---

Boostraped with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
