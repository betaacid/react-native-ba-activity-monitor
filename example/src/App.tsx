import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import ActivityMonitor from 'react-native-ba-activity-monitor';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();

  React.useEffect(() => {
    ActivityMonitor.askPermission().then((granted) => {
      if (granted) {
        ActivityMonitor.start()
          .then(() => console.log('Activity monitor started.'))
          .catch((e) =>
            console.error('Error starting the activity monitor: ' + e)
          );
      }
    });
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
