package ir.morteza_aghighi.chargingalert.viewModel

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import ir.morteza_aghighi.chargingalert.databinding.ActivityMainBinding
import ir.morteza_aghighi.chargingalert.model.ChargingMonitorService

class UiStuff {
    private val batIntentFilter = IntentFilter("android.intent.BATTERY_STATUS")
    private lateinit var mainActivityBinding: ActivityMainBinding
    private val chargingMonitorService = ChargingMonitorService()
    private var batReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
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
        }
    }

    public fun readData(context: Context) {
        context.registerReceiver(batReceiver, batIntentFilter)
    }
    public fun unreadData(context: Context){
        context.registerReceiver(batReceiver, batIntentFilter)
    }
}