package ir.morteza_aghighi.chargingalert.model

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import ir.morteza_aghighi.chargingalert.AlertActivity
import ir.morteza_aghighi.chargingalert.MainActivity
import ir.morteza_aghighi.chargingalert.R
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs.setBoolean
import kotlinx.coroutines.*

private const val CHANNEL_ID = "ForegroundServiceChannel"

class ChargingMonitorService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
    val chargeAlterCoolDownJob = CoroutineScope(Dispatchers.Default + errorHandler)
    val disChargeAlterCoolDownJob = CoroutineScope(Dispatchers.Default + errorHandler)

    private val batteryInfoModel = BatteryInfoModel()
    private var batIFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    var batteryStatus = Intent("android.intent.BATTERY_STATUS")
    private var batteryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Intent.ACTION_BATTERY_CHANGED == intent.action) {
                batteryInfoModel.setBatLevel(intent.getIntExtra("level", 0))

                batteryInfoModel.setBatPercentage("${batteryInfoModel.getBatLevel()}%")

                batteryInfoModel.setBatVoltage(
                    "${intent.getIntExtra("voltage", 0).toFloat() / 1000}V"
                )

                batteryInfoModel.setBatHealth(
                    when (intent.getIntExtra("health", 0)) {
                        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                        BatteryManager.BATTERY_HEALTH_UNKNOWN -> "Unknown"
                        else -> {
                            "Unspecified Failure"
                        }
                    }
                )

                batteryInfoModel.setBatType(intent.getStringExtra("technology").toString())

                val chargingType = intent.getIntExtra("plugged", -1)
                batteryInfoModel.setBatChargingType(
                    when (chargingType) {
                        BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                        BatteryManager.BATTERY_PLUGGED_DOCK -> "Dock"
                        else -> "Unplugged"
                    }
                )

                batteryInfoModel.setBatTemp("${intent.getIntExtra("temperature", -1) / 10}°C")

                if (SharedPrefs.getBoolean("isAlertEnabled", context) &&
                    batteryInfoModel.getBatChargingType() != "Unplugged" && SharedPrefs.getInt(
                        "chargingLimit",
                        context
                    ) <= batteryInfoModel.getBatLevel() &&
                    !SharedPrefs.getBoolean("isAlarmPlaying", context)
                ) {
                    setBoolean("isAlarmPlaying", true, context)
                    context.startActivity(
                        Intent(context, AlertActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    chargeAlterCoolDownJob.launch {
                        delay(300000)
                        setBoolean("isAlarmPlaying", false, context)
                    }
                }
                if (SharedPrefs.getBoolean("isAlertEnabled", context) &&
                    batteryInfoModel.getBatChargingType() == "Unplugged" && SharedPrefs.getInt(
                        "disChargingLimit",
                        context
                    ) >= batteryInfoModel.getBatLevel() &&
                    !SharedPrefs.getBoolean("isAlarmPlaying", context)
                ) {
                    setBoolean("isAlarmPlaying", true, context)
                    context.startActivity(
                        Intent(context, AlertActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                       true for discharge alert and false for charging alert
                            .putExtra("alertType", true)
                    )
                    disChargeAlterCoolDownJob.launch {
                        delay(300000)
                        setBoolean("isAlarmPlaying", false, context)
                    }
                }
                sendBroadcast(batteryStatus)
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
            if (chargeAlterCoolDownJob.isActive)
                chargeAlterCoolDownJob.cancel()
        } catch (ignored: Exception) {}
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
}