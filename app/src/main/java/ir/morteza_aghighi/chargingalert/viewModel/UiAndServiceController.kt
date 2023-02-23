package ir.morteza_aghighi.chargingalert.viewModel

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import ir.morteza_aghighi.chargingalert.R
import ir.morteza_aghighi.chargingalert.databinding.ActivityMainBinding
import ir.morteza_aghighi.chargingalert.model.BatteryStatsModel
import ir.morteza_aghighi.chargingalert.model.ChargingMonitorService
import ir.morteza_aghighi.chargingalert.tools.ServiceMonitor

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


    private val batIntentFilter = IntentFilter("android.intent.BATTERY_STATUS")
    private val exitIntentFilter = IntentFilter("android.intent.CLOSE_ACTIVITY")
    private val batteryStatsModel = BatteryStatsModel()
    private val batReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            binding.tvBatPercent.text = "${context.getString(R.string.batPercent)} ${batteryStatsModel.getBatPercentage()}"

            binding.tvBatVoltage.text =
                batteryStatsModel.getBatVoltage()
            binding.tvBatHealth.text =
                batteryStatsModel.getBatHealth()
            binding.tvBatType.text =
                batteryStatsModel.getBatType()
            binding.tvBatTemp.text =
                batteryStatsModel.getBatTemp()
            binding.tvBatChargingStat.text =
                batteryStatsModel.getBatChargingType()
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
        activity.applicationContext.unregisterReceiver(batReceiver)
        activity.applicationContext.unregisterReceiver(exitReceiver)

    }
}