package ir.morteza_aghighi.chargingalert.tools

import android.app.ActivityManager
import android.content.Context

/*Class to Check if background service is running*/
class ServiceMonitor {
    fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}