import * as React from 'react';

import { StyleSheet, View, Text, Alert } from 'react-native';
import ActivityMonitor from 'react-native-ba-activity-monitor';

export default function App() {
  // @ts-ignore-error
  const [activity, setActivity] = React.useState<string>('-');

  React.useEffect(() => {
    ActivityMonitor.askPermission()
      .then(async (result) => {
        if (result !== 'granted') {
          throw new Error('Permission not granted');
        }

        await ActivityMonitor.start();
        await ActivityMonitor;
      })
      .catch((e) => {
        Alert.alert('Not allowed: ' + e.message);
      });
  }, []);

  return (
    <View style={styles.container}>
      <Text>Activity: {activity}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
