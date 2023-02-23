package ir.morteza_aghighi.chargingalert.viewModel

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import ir.morteza_aghighi.chargingalert.databinding.ActivityMainBinding
import ir.morteza_aghighi.chargingalert.model.ChargingMonitorService
import ir.morteza_aghighi.chargingalert.tools.ServiceMonitor

class UiStuff(val activity: Activity) {
    private val batIntentFilter = IntentFilter("android.intent.BATTERY_STATUS")
    private val exitIntentFilter = IntentFilter("android.intent.CLOSE_ACTIVITY")
    private var mainActivityBinding: ActivityMainBinding = ActivityMainBinding.inflate(activity.layoutInflater)
    private val chargingMonitorService = ChargingMonitorService()
    private var batReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("onReceive", "ACTION_BATTERY_CHANGED ${ChargingMonitorService().getBatPercentage()}")
            mainActivityBinding.tvBatPercent.text =
                chargingMonitorService.getBatPercentage()
            mainActivityBinding.tvBatVoltage.text =
                chargingMonitorService.getBatVoltage()
            mainActivityBinding.tvBatHealth.text =
                chargingMonitorService.getBatHealth()
            mainActivityBinding.tvBatType.text =
                chargingMonitorService.getBatType()
            mainActivityBinding.tvBatTemp.text =
                chargingMonitorService.getBatTemp()
            mainActivityBinding.tvBatChargingStat.text =
                chargingMonitorService.getBatChargingType()
        }
    }

    private var exitReceiver: BroadcastReceiver = object : BroadcastReceiver() {
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