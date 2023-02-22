package ir.morteza_aghighi.chargingalert

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs.getBoolean
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs.getInt
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs.getString
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs.setBoolean
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs.setInt
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs.setString

class ChargingMonitorService : Service() {
    var alertCoolDownTimer: CountDownTimer = object : CountDownTimer(300000, 1000) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            setBoolean("isAlarmPlaying", false, this@ChargingMonitorService)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private var batIFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    var batteryStatus = Intent("android.intent.BATTERY_STATUS")
    private var batteryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Intent.ACTION_BATTERY_CHANGED == intent.action) {
                batteryStatus.putExtra("BatPercent", intent.getIntExtra("level", 0))
                setInt("BatPercent", intent.getIntExtra("level", 0), context)
                batteryStatus.putExtra("BatVoltage", intent.getIntExtra("voltage", 0))
                setInt("BatVoltage", intent.getIntExtra("voltage", 0), context)
                var batteryHealth = "Good"
                when (intent.getIntExtra("health", 0)) {
                    BatteryManager.BATTERY_HEALTH_COLD -> batteryHealth = "Cold"
                    BatteryManager.BATTERY_HEALTH_DEAD -> batteryHealth = "Dead"
                    BatteryManager.BATTERY_HEALTH_GOOD -> batteryHealth = "Good"
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> batteryHealth = "Over Voltage"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> batteryHealth = "Overheat"
                    BatteryManager.BATTERY_HEALTH_UNKNOWN -> batteryHealth = "Unknown"
                    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> batteryHealth =
                        "Unspecified Failure"
                }
                batteryStatus.putExtra("BatHealth", batteryHealth)
                setString("BatHealth", batteryHealth, context)
                batteryStatus.putExtra("BatType", intent.getStringExtra("technology"))
                setString("BatType", intent.getStringExtra("technology"), context)
                val cType: String
                val chargingType = intent.getIntExtra("plugged", -1)
                cType = when (chargingType) {
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                    else -> "Unknown"
                }
                batteryStatus.putExtra("BatChargingStat", cType)
                setString("BatChargingStat", cType, context)
                batteryStatus.putExtra("BatTemp", intent.getIntExtra("temperature", -1))
                setInt("BatTemp", intent.getIntExtra("temperature", -1), context)
                sendBroadcast(batteryStatus)
                if (getBoolean("isAlertEnabled", context) &&
                    getString("BatChargingStat", context) != "Unknown" && getInt(
                        "chargingLimit",
                        context
                    ) <= intent.getIntExtra("level", 0) &&
                    !getBoolean("isAlarmPlaying", context)
                ) {
                    setBoolean("isAlarmPlaying", true, context)
                    startActivity(
                        Intent(context, AlertActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    alertCoolDownTimer.start()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        registerReceiver(batteryReceiver, batIFilter)
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //This is the intent of PendingIntent
        val exitIntent = Intent("android.intent.CLOSE_ACTIVITY")
        val pendingExitIntent = PendingIntent.getBroadcast(
            applicationContext, 0, exitIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Monitoring Battery Status...")
            .setPriority(Notification.PRIORITY_MIN)
            .setContentText("Touch to open Application")
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_stat_name, "Turn OFF Monitoring Service", pendingExitIntent)
            .build()
        startForeground(1, notification)
        //do heavy work on a background thread
        return START_STICKY
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(batteryReceiver)
            unregisterReceiver(exitSignalReceiver)
            alertCoolDownTimer.cancel()
        } catch (ignored: Exception) {
        }
        //        SharedPrefs.setBoolean("isAlarmPlaying",false,this);
    }

    override fun onCreate() {
        val filter = IntentFilter("android.intent.CLOSE_ACTIVITY")
        registerReceiver(exitSignalReceiver, filter)
        setBoolean("isAlarmPlaying", false, this)
        super.onCreate()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Battery Monitor Service Channel",
                NotificationManager.IMPORTANCE_NONE
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.deleteNotificationChannel(serviceChannel.id)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private var exitSignalReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            stopService(Intent(this@ChargingMonitorService, ChargingMonitorService::class.java))
        }
    }

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }
}