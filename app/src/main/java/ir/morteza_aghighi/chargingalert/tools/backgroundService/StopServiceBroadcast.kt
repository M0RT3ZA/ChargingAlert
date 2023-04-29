package ir.morteza_aghighi.chargingalert.tools.backgroundService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopServiceBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getStringExtra("action").equals("stopAction")) {
            isServiceStoppedByUser = true
            ServiceStateChanger(context).actionOnService(ServiceActions.STOP)
        }
    }
}
