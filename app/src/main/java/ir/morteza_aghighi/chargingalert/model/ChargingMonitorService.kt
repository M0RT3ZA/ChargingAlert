package ir.morteza_aghighi.chargingalert.model

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ir.morteza_aghighi.chargingalert.AlertActivity
import ir.morteza_aghighi.chargingalert.MainActivity
import ir.morteza_aghighi.chargingalert.R
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** unique notification channel id.
 * in this app we just have one channel*/
private const val CHANNEL_ID = "ChargingAlertForegroundServiceChannel"

/** our service class which extends android service class*/
class ChargingMonitorService : Service() {

    /** coroutine exception handler*/
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    /** two coroutines, one for charging alert cool down and one for discharge alert cool down.
     * we using cool down timers so when the alert triggered service wait until cool down time is over,
     * the if user still does not plug/unplug the charger the service triggers alert again.*/
    val chargeAlterCoolDownJob = CoroutineScope(Dispatchers.Default + errorHandler)
    val disChargeAlterCoolDownJob = CoroutineScope(Dispatchers.Default + errorHandler)

    /** battery data model to set and get battery info*/
    private val batteryInfoModel = BatteryInfoModel()

    /** with below intent filter and inside broadcast receiver,
     * we get battery info every time that battery info is changed.
     * when service is started the first thing it does is to call registerReceiver and pass these two
     * variables to that.*/
    private var batIFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    private var batteryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            /** filtering the received data because we only need ACTION_BATTERY_CHANGED intents.
             * probably the if statement below is not necessary because
             * when we registered receiver we passed:
             * batIFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)*/
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                /** setting battery parameters below.*/
                batteryInfoModel.setBatLevel(intent.getIntExtra("level", 0))

                batteryInfoModel.setBatPercentage("${batteryInfoModel.getBatLevel()}%")

                batteryInfoModel.setBatVoltage(
                    "${intent.getIntExtra("voltage", 0).toFloat() / 1000}V"
                )

                batteryInfoModel.setBatHealth(
                    /** the default value is set to -1 because none of "health" values are -1
                     * so -1 means "Unspecified Failure"*/
                    when (intent.getIntExtra("health", -1)) {
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

                /** the default value is set to -1 because none of "plugged" values are -1
                 * so -1 means "Unspecified Failure"*/
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

                batteryInfoModel.setBatTemp("${intent.getIntExtra("temperature", 0) / 10}°C")

                /** if the alert is enabled, and charging type is not "Unplugged",
                 * and battery level is equal or greater than the limit set by user,
                 * it means we should play alarm. so if alarm is not already playing
                 * the alert progress starts.*/
                if (SharedPrefs.getBoolean("isAlertEnabled", context)
                    && batteryInfoModel.getBatChargingType() != "Unplugged"
                    && SharedPrefs.getInt("chargingLimit", context) <= batteryInfoModel.getBatLevel()
                    && !SharedPrefs.getBoolean("isAlarmPlaying", context)
                ) {
                    /** storing the "isAlarmPlaying" value in shared preferences
                     * so next time the if is not satisfied and this if body does not execute,
                     * until the cool down timer finishes.*/
                    SharedPrefs.setBoolean("isAlarmPlaying", true, context)

                    /** launching alert activity*/
                    context.startActivity(
                        Intent(context, AlertActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )

                    /** waiting 5 minute then reset the value of "isAlarmPlaying" back to false,
                     * so if user still needs to plug/unplug the charger alert plays again.*/
                    chargeAlterCoolDownJob.launch {
                        delay(300000)
                        SharedPrefs.setBoolean("isAlarmPlaying", false, context)
                    }
                }

                /** if the alert is enabled, and charging type is "Unplugged",
                 * and battery level is equal or lower than the limit set by user,
                 * it means we should play alarm. so if alarm is not already playing
                 * the alert progress starts.*/
                if (SharedPrefs.getBoolean("isAlertEnabled", context)
                    && batteryInfoModel.getBatChargingType() == "Unplugged"
                    && SharedPrefs.getInt("disChargingLimit", context) >= batteryInfoModel.getBatLevel()
                    && !SharedPrefs.getBoolean("isAlarmPlaying", context)
                ) {
                    SharedPrefs.setBoolean("isAlarmPlaying", true, context)
                    context.startActivity(
                        Intent(
                            context, AlertActivity::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                       true for discharge alert and false for charging alert
                            .putExtra("alertType", true)
                    )
                    disChargeAlterCoolDownJob.launch {
                        delay(300000)
                        SharedPrefs.setBoolean("isAlarmPlaying", false, context)
                    }
                }
                val batteryStatus = Intent("android.intent.BATTERY_STATUS")
                sendBroadcast(batteryStatus)
            }
        }
    }

    /** the service is not a bound service so onBind function returns null*/
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        registerReceiver(batteryReceiver, batIFilter)
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //This is the intent of PendingIntent
        val exitIntent = Intent("android.intent.CLOSE_ACTIVITY")
        val pendingExitIntent = PendingIntent.getBroadcast(
            applicationContext, 0, exitIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Monitoring Battery Status...").setPriority(Notification.PRIORITY_MIN)
            .setContentText("Touch to open Application").setSmallIcon(R.drawable.ic_stat_name)
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
            if (chargeAlterCoolDownJob.isActive) chargeAlterCoolDownJob.cancel()
            if (disChargeAlterCoolDownJob.isActive) disChargeAlterCoolDownJob.cancel()
        } catch (ignored: Exception) {}
    }

    override fun onCreate() {
        val filter = IntentFilter("android.intent.CLOSE_ACTIVITY")
        registerReceiver(exitSignalReceiver, filter)
        SharedPrefs.setBoolean("isAlarmPlaying", false, this)
        super.onCreate()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Battery Monitor Service Channel", NotificationManager.IMPORTANCE_NONE
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