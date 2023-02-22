package ir.morteza_aghighi.chargingalert.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import ir.morteza_aghighi.chargingalert.ChargingMonitorService;


public class /*tittle = getString(R.string.warning);
            message = getString(R.string.explenation);*/BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (SharedPrefs.getBoolean("bootFlag", context) &&
                Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) &&
                !(new ServiceMonitor().isMyServiceRunning(ChargingMonitorService.class, context))) {
            new ToastMaker(context).msg("Starting Charging Monitor Service...");
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, ChargingMonitorService.class));
                } else {
                    context.startService(new Intent(context, ChargingMonitorService.class));
                }
            } catch (Exception ignored) {
            }
        }
    }
}
