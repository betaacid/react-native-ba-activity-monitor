import * as React from 'react';

import { StyleSheet, View, Text, Alert, Button } from 'react-native';
import ActivityMonitor, { Activity } from 'react-native-ba-activity-monitor';

export default function App() {
  const [started, setStarted] = React.useState(false);
  const [activity, setActivity] = React.useState<Activity | null>(null);

  return (
    <View style={styles.container}>
      <Text>
        Activity: {activity?.type ?? '...'} - Confidence:{' '}
        {activity?.confidence ?? '...'}
      </Text>

      <Text style={{ marginTop: 10 }}>State: {started ? 'ON' : 'OFF'}</Text>

      <View
        style={{
          display: 'flex',
          flexDirection: 'row',
          width: 200,
          justifyContent: 'space-between',
          marginTop: 20,
        }}
      >
        <Button
          title="Start"
          disabled={started}
          onPress={async () => {
            const permission = await ActivityMonitor.askPermission();
            if (permission !== 'granted') {
              Alert.alert('Permission not granted: ' + permission);
              return;
            }

            await ActivityMonitor.start();
            setStarted(true);

            console.debug('[ActivityMonitor] just started');
            ActivityMonitor.onActivities((activities) => {
              console.debug('[ActivityMonitor] activities:');
              console.debug('[ActivityMonitor]' + JSON.stringify(activities));
              setActivity(activities[0] || null);
            });
          }}
        />

        <Button
          title="Stop"
          disabled={!started}
          onPress={() => {
            ActivityMonitor.stop();
            console.debug('[ActivityMonitor] just stopped');
            setStarted(false);
          }}
        />

        <Button
          title="Mock"
          disabled={!started}
          onPress={() => {
            ActivityMonitor.mockActivities([
              {
                type: 'in-vehicle',
                confidence: 95,
              },
              {
                type: 'walking',
                confidence: 31,
              },
            ]);
          }}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'white',
  },
});
