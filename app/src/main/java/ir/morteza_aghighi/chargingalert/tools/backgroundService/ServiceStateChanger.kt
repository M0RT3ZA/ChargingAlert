package ir.morteza_aghighi.chargingalert.tools.backgroundService

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build

class ServiceStateChanger(private val context: Context) : ContextWrapper(context) {
    fun actionOnService(action: ServiceActions) {
        if (getServiceState(context) == ServiceState.STOPPED && action == ServiceActions.STOP) return
        Intent(context, DataReceiverService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(it)
                return
            }
            context.startService(it)
        }
    }
}