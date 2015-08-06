package com.estimote.examples.demos;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.estimote.sdk.BeaconManager.MonitoringListener;

/**
 * Demo that shows how to use region monitoring. Two important steps are:
 * <ul>
 * <li>start monitoring region, in example in {@link #onResume()}</li>
 * <li>respond to monitoring changes by registering {@link MonitoringListener} in {@link BeaconManager}</li>
 * </ul>
 *
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
public class NotifyDemoActivity extends Activity {

  private static final String TAG = NotifyDemoActivity.class.getSimpleName();
    private static final String MINIKAST_PROXIMITY_UUID = "13817c83-7d22-4d5f-9f29-7a54f0ad7fac";

    private BeaconManager beaconManager;
    private NotificationManager notificationManager;
    private Region region;
    private Beacon oldBeacon;
    private LeDeviceListAdapter adapter;

    @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.notify_demo);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    // Configure device list.
    adapter = new LeDeviceListAdapter(this);
    ListView list = (ListView) findViewById(R.id.device_list);
    list.setAdapter(adapter);

    region = new Region("rid", MINIKAST_PROXIMITY_UUID, null, null);
    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    beaconManager = new BeaconManager(this);
    beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);
    beaconManager.setForegroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);
    // Default values are 5s of scanning and 25s of waiting time to save CPU cycles.
    // In order for this demo to be more responsive and immediate we lower down those values.
      beaconManager.setRangingListener(new BeaconManager.RangingListener() {
          @Override
          public void onBeaconsDiscovered(Region region,final List<Beacon> beacons) {
              if (!beacons.isEmpty()) {
                  runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          // Note that beacons reported here are already sorted by estimated
                          // distance between device and beacon.
                          getActionBar().setSubtitle("Found beacons: " + beacons.size());
                          adapter.replaceWith(beacons);
                      }
                  });

                  Beacon beacon = beacons.get(0);
                  //If this is our first time
                  if (oldBeacon == null) {
                      //If the beacon is near enough
                      if ((Utils.computeAccuracy(beacon) <= 4)) {
                          oldBeacon = beacon;
                          onEnteringRegion(oldBeacon);
                      }
                      return;
                  }
                  if (beacon.getMajor() != oldBeacon.getMajor()) { //If the nearest beacon if a new one
                      //If the new beacon its more close than the old one, then update it
                      if (Utils.computeAccuracy(beacon) < Utils.computeAccuracy(oldBeacon)) {
                          if ((Utils.computeAccuracy(beacon) <= 4)) {
                              onLeavingRegion(oldBeacon);
                              onEnteringRegion(beacon);
                              oldBeacon = beacon;
                          }
                      }
                  } else {
                      //If the current beacon It's far away, then you leaved the zone (your headzone :D:D:D)
                      if (Utils.computeAccuracy(beacon) > 4) {
                          oldBeacon = beacon;
                          onLeavingRegion(oldBeacon);
                      }
                  }

              }

          }

          private void onLeavingRegion(Beacon beacon) {
              postNotification(getMessageExitFor(beacon), beacon.getMajor());
          }

          private void onEnteringRegion(Beacon beacon) {
              postNotification(getMessageFor(beacon), beacon.getMajor());
          }
      });
  }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(region);
                } catch (RemoteException e) {
                    Log.d(TAG, "Error while starting monitoring");
                }
            }
        });
    }

    private String getMessageExitFor(Beacon b) {
        switch (b.getMajor()) {
            case 6767:
                return "Leaving the Mobile island.";
            case 7171:
                return "Leaving the Arzion meeting room.";
            case 2323:
                return "Leaving the Arzion food court. Don't feed the zombie!";
        }
        return "Unknown place";
    }

    private String getMessageFor(Beacon b) {
        switch (b.getMajor()) {
            case 6767:
                return "Entering into the Mobile island.";
            case 7171:
                return "Entering into Arzion meeting room";
            case 2323:
                return "Entering into the Arzion food court. Dont feed the zombie!";
        }
        return "Unknown place";
    }

    @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }


  @Override
  protected void onDestroy() {
    beaconManager.disconnect();
    super.onDestroy();
  }

  private void postNotification(String msg, int id) {
//    Intent notifyIntent = new Intent(NotifyDemoActivity.this, NotifyDemoActivity.class);
//    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//    PendingIntent pendingIntent = PendingIntent.getActivities(
//        NotifyDemoActivity.this,
//        0,
//        new Intent[]{notifyIntent},
//        PendingIntent.FLAG_UPDATE_CURRENT);
//    Notification notification = new Notification.Builder(NotifyDemoActivity.this)
//        .setSmallIcon(R.drawable.beacon_gray)
//        .setContentTitle("Notify Demo")
//        .setContentText(msg)
//        .setAutoCancel(true)
//        .setContentIntent(pendingIntent)
//        .build();
//    notification.defaults |= Notification.DEFAULT_SOUND;
//    notification.defaults |= Notification.DEFAULT_LIGHTS;
//    notificationManager.notify(id, notification);

    TextView statusTextView = (TextView) findViewById(R.id.status);
    statusTextView.setText(msg);
  }
}
