package ir.morteza_aghighi.chargingalert.viewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import ir.morteza_aghighi.chargingalert.R
import ir.morteza_aghighi.chargingalert.databinding.ActivityMainBinding
import ir.morteza_aghighi.chargingalert.model.BatteryInfoModel
import ir.morteza_aghighi.chargingalert.tools.backgroundService.ServiceActions
import ir.morteza_aghighi.chargingalert.tools.backgroundService.ServiceStateChanger
import kotlinx.coroutines.*

class UiAndServiceController(
    private var activity: Activity, private var binding: ActivityMainBinding
) {

    private val batIntentFilter = IntentFilter("android.intent.BATTERY_STATUS")
    private val exitIntentFilter = IntentFilter("android.intent.CLOSE_ACTIVITY")

    private val batteryInfoModel = BatteryInfoModel()
    private val batReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            if (context.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                binding.tvBatPercent.text =
                    "${context.getString(R.string.batPercent) + "\n"}${batteryInfoModel.getBatPercentage()}"

                binding.tvBatVoltage.text =
                    "${context.getString(R.string.batVoltage) + "\n"}${batteryInfoModel.getBatVoltage()}"

                binding.tvBatHealth.text =
                    "${context.getString(R.string.batHealth) + "\n"}${batteryInfoModel.getBatHealth()}"

                binding.tvBatType.text =
                    "${context.getString(R.string.batType) + "\n"}${batteryInfoModel.getBatType()}"

                binding.tvBatTemp.text =
                    "${context.getString(R.string.batTemp) + "\n"}${batteryInfoModel.getBatTemp()}"

                binding.tvBatChargingStat.text =
                    "${context.getString(R.string.batCharging) + "\n"}${batteryInfoModel.getBatChargingType()}"
            } else {
                binding.tvBatPercent.text =
                    "${context.getString(R.string.batPercent) + "\t"}${batteryInfoModel.getBatPercentage()}"

                binding.tvBatVoltage.text =
                    "${context.getString(R.string.batVoltage) + "\t"}${batteryInfoModel.getBatVoltage()}"

                binding.tvBatHealth.text =
                    "${context.getString(R.string.batHealth) + "\t"}${batteryInfoModel.getBatHealth()}"

                binding.tvBatType.text =
                    "${context.getString(R.string.batType) + "\t"}${batteryInfoModel.getBatType()}"

                binding.tvBatTemp.text =
                    "${context.getString(R.string.batTemp) + "\t"}${batteryInfoModel.getBatTemp()}"

                binding.tvBatChargingStat.text =
                    "${context.getString(R.string.batCharging) + "\t"}${batteryInfoModel.getBatChargingType()}"
            }
        }
    }

    private val exitReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            /*context.stopService(Intent(context, ChargingMonitorService::class.java))*/
            ServiceStateChanger(context).actionOnService(ServiceActions.STOP)
            activity.finish()
        }
    }

    fun readData() {
        activity.applicationContext.registerReceiver(batReceiver, batIntentFilter)
        activity.applicationContext.registerReceiver(exitReceiver, exitIntentFilter)
    }

    fun unreadData() {
        activity.applicationContext.unregisterReceiver(batReceiver)
        activity.applicationContext.unregisterReceiver(exitReceiver)
    }
}