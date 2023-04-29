package ir.morteza_aghighi.chargingalert.tools.backgroundService

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.app.PendingIntent.getBroadcast
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import ir.morteza_aghighi.chargingalert.MainActivity


private const val NOTIFICATION_CHANNEL_ID = "Charging Alert BACKGROUND SERVICE CHANNEL"
private const val MAIN_NOTIFICATION_REQUEST_CODE = 0
private const val MAIN_NOTIFICATION_NAME = "Charging Alert Service Notifications Channel"
private const val MAIN_NOTIFICATION_DESCRIPTION = "Charging Alert Service Channel"
private const val KILL_NOTIFICATION_REQUEST_CODE = 1

class ServiceNotificationTools(private val context: Context) {
    // Register the channel with the system
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val builder: NotificationCompat.Builder = NotificationCompat.Builder(
        context,
        NOTIFICATION_CHANNEL_ID
    )
    private val notificationIntent = Intent(context, MainActivity::class.java)

    fun createServiceNotification(
        notificationTitle: String, notificationDescription: String, notificationIcon: Int,
        exit_text: String, exit_icon: Int
    ): Notification {

        val pendingIntent: PendingIntent

        val exitIntent = Intent(context, StopServiceBroadcast::class.java)
            .putExtra("action", "stopAction")

        val pendingExitIntent: PendingIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                MAIN_NOTIFICATION_NAME,
                importance
            ).apply {
                description = MAIN_NOTIFICATION_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
            pendingIntent = getActivity(
                context,
                MAIN_NOTIFICATION_REQUEST_CODE,
                notificationIntent,
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )
            pendingExitIntent = getBroadcast(
                context, KILL_NOTIFICATION_REQUEST_CODE,
                exitIntent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )
        } else {
            pendingIntent = getActivity(
                context,
                MAIN_NOTIFICATION_REQUEST_CODE,
                notificationIntent,
                FLAG_UPDATE_CURRENT
            )
            //This is the intent of PendingIntent
            pendingExitIntent = getBroadcast(
                context, KILL_NOTIFICATION_REQUEST_CODE,
                exitIntent, FLAG_UPDATE_CURRENT
            )
        }
        builder.setSmallIcon(notificationIcon)
            .setContentTitle(notificationTitle)
            .setContentText(notificationDescription)
            .setContentIntent(pendingIntent)
            //Using this action button I would like to call logTest
            .addAction(
                exit_icon,
                exit_text, pendingExitIntent
            )
            .setOngoing(true)
        notificationManager.notify(KILL_NOTIFICATION_REQUEST_CODE, builder.build())
        return builder.build()
    }
}