package ir.morteza_aghighi.chargingalert.tools.backgroundService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs

class StartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (SharedPrefs.getBoolean("runOnBoot", context)
            && SharedPrefs.getBoolean("remembered", context)
            && intent.action == Intent.ACTION_BOOT_COMPLETED
            && getServiceState(context) == ServiceState.STARTED) {
            Intent(context, DataReceiverService::class.java).also {
                it.action = ServiceActions.START.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(it)
                    return
                }
                context.startService(it)
            }
        }
    }
}