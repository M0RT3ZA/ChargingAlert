package ir.morteza_aghighi.chargingalert.viewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import ir.morteza_aghighi.chargingalert.AlertActivity
import ir.morteza_aghighi.chargingalert.R
import ir.morteza_aghighi.chargingalert.databinding.ActivityMainBinding
import ir.morteza_aghighi.chargingalert.model.BatteryStatsModel
import ir.morteza_aghighi.chargingalert.model.ChargingMonitorService
import ir.morteza_aghighi.chargingalert.tools.ServiceMonitor
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs
import kotlinx.coroutines.*

class UiAndServiceController {

    private lateinit var activity: Activity
    constructor(activity: Activity) : super() {
        this.activity = activity
    }

    private lateinit var binding: ActivityMainBinding
    constructor(activity: Activity, binding: ActivityMainBinding) : super() {
        this.activity = activity
        this.binding = binding
    }

    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
    val alterCoolDownJob = CoroutineScope(Dispatchers.Default + errorHandler)

    private val batIntentFilter = IntentFilter("android.intent.BATTERY_STATUS")
    private val exitIntentFilter = IntentFilter("android.intent.CLOSE_ACTIVITY")

    private val batteryStatsModel = BatteryStatsModel()
    private val batReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            binding.tvBatPercent.text =
                "${context.getString(R.string.batPercent)} ${batteryStatsModel.getBatPercentage()}"

            binding.tvBatVoltage.text =
                "${context.getString(R.string.batVoltage)} ${batteryStatsModel.getBatVoltage()}"

            binding.tvBatHealth.text =
                "${context.getString(R.string.batHealth)} ${batteryStatsModel.getBatHealth()}"

            binding.tvBatType.text =
                "${context.getString(R.string.batType)} ${batteryStatsModel.getBatType()}"

            binding.tvBatTemp.text =
                "${context.getString(R.string.batTemp)} ${batteryStatsModel.getBatTemp()}"

            binding.tvBatChargingStat.text =
                "${context.getString(R.string.batCharging)} ${batteryStatsModel.getBatChargingType()}"

            if (SharedPrefs.getBoolean("isAlertEnabled", context) &&
                batteryStatsModel.getBatChargingType() != "Unplugged" && SharedPrefs.getInt(
                    "chargingLimit",
                    context
                ) <= batteryStatsModel.getBatLevel() &&
                !SharedPrefs.getBoolean("isAlarmPlaying", context)
            ) {
                SharedPrefs.setBoolean("isAlarmPlaying", true, context)
                context.startActivity(
                    Intent(context, AlertActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                alterCoolDownJob.launch {
                    delay(300000)
                    SharedPrefs.setBoolean("isAlarmPlaying", false, context)
                }
            }
        }
    }

    private val exitReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            context.stopService(Intent(context, ChargingMonitorService::class.java))
            activity.finish()
        }
    }

    fun readData() {
        if (!ServiceMonitor().isMyServiceRunning(ChargingMonitorService::class.java, activity.applicationContext)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.startForegroundService(Intent(activity.applicationContext, ChargingMonitorService::class.java))
            } else {
                activity.startService(Intent(activity.applicationContext, ChargingMonitorService::class.java))
            }
        }
        activity.applicationContext.registerReceiver(batReceiver, batIntentFilter)
        activity.applicationContext.registerReceiver(exitReceiver, exitIntentFilter)
    }

    fun unreadData() {
/*        try {
*//*            activity.applicationContext.unregisterReceiver(batReceiver)
            activity.applicationContext.unregisterReceiver(exitReceiver)*//*
        }catch (ignored: java.lang.Exception){}*/
        if (alterCoolDownJob.isActive)
            alterCoolDownJob.cancel()
    }
}