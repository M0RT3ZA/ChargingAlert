package ir.morteza_aghighi.chargingalert.tools.backgroundService

import android.content.Context
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs

enum class ServiceState {
    STARTED,
    STOPPED,
}

private const val key = "CA_SERVICE_STATE"

fun setServiceState(context: Context, state: ServiceState) {
    SharedPrefs.setString(context, key, state.name)
}

fun getServiceState(context: Context): ServiceState {

    val value = SharedPrefs.getString(context, key, ServiceState.STOPPED.name)
    return ServiceState.valueOf(value!!)
}
