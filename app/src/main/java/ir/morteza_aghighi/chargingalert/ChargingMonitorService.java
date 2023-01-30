package ir.morteza_aghighi.chargingalert;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import ir.morteza_aghighi.chargingalert.tools.SharedPrefs;

public class ChargingMonitorService extends Service {

    public ChargingMonitorService() {}

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    CountDownTimer alertCoolDownTimer = new CountDownTimer(300000, 1000) {
        public void onTick(long millisUntilFinished) {}

        public void onFinish() {
            SharedPrefs.setBoolean("isAlarmPlaying",false,ChargingMonitorService.this);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    IntentFilter batIFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    Intent batteryStatus = new Intent("android.intent.BATTERY_STATUS");

    BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){

                batteryStatus.putExtra("BatPercent",intent.getIntExtra("level",0));
                SharedPrefs.setInt("BatPercent",intent.getIntExtra("level",0),context);

                batteryStatus.putExtra("BatVoltage",intent.getIntExtra("voltage",0));
                SharedPrefs.setInt("BatVoltage",intent.getIntExtra("voltage",0),context);

                String Health = "Good";
                int health = intent.getIntExtra("health",0);
                switch (health){
                    case BatteryManager.BATTERY_HEALTH_COLD:
                        Health = "Cold";
                    break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        Health = "Dead";
                    break;
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        Health = "Good";
                    break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        Health = "Over Voltage";
                    break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        Health = "Overheat";
                    break;
                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        Health = "Unknown";
                    break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        Health = "Unspecified Failure";
                    break;
                }
                batteryStatus.putExtra("BatHealth",Health);
                SharedPrefs.setString("BatHealth",Health,context);

                batteryStatus.putExtra("BatType",intent.getStringExtra("technology"));
                SharedPrefs.setString("BatType",(intent.getStringExtra("technology")),context);

                String cType;
                int chargingType = intent.getIntExtra("plugged",-1);
                switch (chargingType){
                    case BatteryManager.BATTERY_PLUGGED_AC:
                        cType = "AC";
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        cType = "USB";
                        break;
                    case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                        cType = "Wireless";
                        break;
                    default:
                        cType = "Unknown";
                }

                batteryStatus.putExtra("BatChargingStat",cType);
                SharedPrefs.setString("BatChargingStat",cType,context);

                batteryStatus.putExtra("BatTemp",intent.getIntExtra("temperature",-1));
                SharedPrefs.setInt("BatTemp",intent.getIntExtra("temperature",-1),context);
                sendBroadcast(batteryStatus);

                if (SharedPrefs.getBoolean("isAlertEnabled",context) &&
                        !SharedPrefs.getString("BatChargingStat",context).equals("Unknown") &&
                        ((SharedPrefs.getInt("chargingLimit",(context)) <= intent.getIntExtra("level",0))) &&
                        !SharedPrefs.getBoolean("isAlarmPlaying",context)){
                    SharedPrefs.setBoolean("isAlarmPlaying",true,context);

                    startActivity(new Intent(context, AlertActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                    alertCoolDownTimer.start();
                }
            }
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(batteryReceiver,batIFilter);

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        //This is the intent of PendingIntent
        Intent exitIntent = new Intent("android.intent.CLOSE_ACTIVITY");
        PendingIntent pendingExitIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 0 , exitIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Monitoring Battery Status...")
                .setPriority(Notification.PRIORITY_MIN)
                .setContentText("Touch to open Application")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_stat_name, "Turn OFF Monitoring Service", pendingExitIntent)
                .build();

        startForeground(1, notification);
        //do heavy work on a background thread

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(batteryReceiver);
            unregisterReceiver(exitSignalReceiver);
            alertCoolDownTimer.cancel();
        }catch (Exception ignored){}
//        SharedPrefs.setBoolean("isAlarmPlaying",false,this);
    }


    @Override
    public void onCreate() {
        IntentFilter filter = new IntentFilter("android.intent.CLOSE_ACTIVITY");
        registerReceiver(exitSignalReceiver, filter);
        SharedPrefs.setBoolean("isAlarmPlaying",false,this);
        super.onCreate();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Battery Monitor Service Channel",
                    NotificationManager.IMPORTANCE_NONE
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.deleteNotificationChannel(serviceChannel.getId());
            manager.createNotificationChannel(serviceChannel);
        }
    }


    BroadcastReceiver exitSignalReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            stopService(new Intent(ChargingMonitorService.this, ChargingMonitorService.class));
        }

    };


}