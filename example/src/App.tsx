import * as React from 'react';

import { StyleSheet, View, Text, Alert } from 'react-native';
import ActivityMonitor, { Activity } from 'react-native-ba-activity-monitor';

export default function App() {
  const [activity, setActivity] = React.useState<Activity | null>(null);

  React.useEffect(() => {
    ActivityMonitor.askPermission()
      .then(async (result) => {
        if (result !== 'granted') {
          throw new Error('Permission not granted');
        }

        await ActivityMonitor.start();
        ActivityMonitor.onActivities((activities) => {
          setActivity(activities[0] || null);
        });
      })
      .catch((e) => {
        Alert.alert('Not allowed: ' + e.message);
      });
  }, []);

  return (
    <View style={styles.container}>
      <Text>
        Activity: {activity?.type ?? '...'} - {activity?.transitioType ?? '...'}{' '}
        - {activity?.timestamp ?? '...'}
      </Text>
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
