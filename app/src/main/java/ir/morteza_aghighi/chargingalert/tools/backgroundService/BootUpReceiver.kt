package ir.morteza_aghighi.chargingalert.tools.backgroundService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import ir.morteza_aghighi.chargingalert.model.ChargingMonitorService
import ir.morteza_aghighi.chargingalert.tools.ServiceMonitor
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs
import ir.morteza_aghighi.chargingalert.tools.ToastMaker

class
/*tittle = getString(R.string.warning);
            message = getString(R.string.explenation);*/
BootUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (SharedPrefs.getBoolean(
                context, "bootFlag"
            ) && Intent.ACTION_BOOT_COMPLETED == intent.action &&
            !ServiceMonitor().isMyServiceRunning(ChargingMonitorService::class.java, context)
        ) {
            ToastMaker(context, "Starting Charging Monitor Service...").sh()
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(
                        Intent(
                            context,
                            ChargingMonitorService::class.java
                        )
                    )
                } else {
                    context.startService(Intent(context, ChargingMonitorService::class.java))
                }
            } catch (ignored: Exception) {
            }
        }
    }
}