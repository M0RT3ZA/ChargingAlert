package ir.morteza_aghighi.chargingalert.tools.backgroundService

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import ir.morteza_aghighi.chargingalert.MainActivity
import ir.morteza_aghighi.chargingalert.R
import ir.morteza_aghighi.chargingalert.R.*


private const val NOTIFICATION_CHANNEL_ID = "CA BACKGROUND SERVICE CHANNEL"
private const val IMAGE_NOTIFICATION_CHANNEL_ID = "CA IMAGE NOTIFICATION CHANNEL"
private const val MAIN_NOTIFICATION_REQUEST_CODE = 0
private const val MAIN_NOTIFICATION_NAME = "CA Service Notifications Channel"
private const val MAIN_NOTIFICATION_DESCRIPTION = "CA Service Channel"
private const val KILL_NOTIFICATION_REQUEST_CODE = 1
private const val IMAGE_NOTIFICATION_REQUEST_CODE = 2

class ServiceNotificationTools(private val context: Context) {
    // Register the channel with the system
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val builder: NotificationCompat.Builder = NotificationCompat.Builder(context,
        NOTIFICATION_CHANNEL_ID
    )
    private val notificationIntent = Intent(context, MainActivity::class.java)

    @SuppressLint("UnspecifiedImmutableFlag")
    fun createNotification():Notification {

        val pendingIntent: PendingIntent

        val exitIntent = Intent(context, StopServiceBroadcast::class.java)
            .putExtra("action", "stopAction")

        val pendingExitIntent: PendingIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, MAIN_NOTIFICATION_NAME, importance).apply {
                description = MAIN_NOTIFICATION_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
            pendingIntent = getActivity(
                context,
                MAIN_NOTIFICATION_REQUEST_CODE,
                notificationIntent,
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )
            pendingExitIntent = getBroadcast(context, KILL_NOTIFICATION_REQUEST_CODE,
                exitIntent,FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
        }else{
            pendingIntent = getActivity(
                context,
                MAIN_NOTIFICATION_REQUEST_CODE,
                notificationIntent,
                FLAG_UPDATE_CURRENT
            )
            //This is the intent of PendingIntent
            pendingExitIntent = getBroadcast(context, KILL_NOTIFICATION_REQUEST_CODE,
                exitIntent,FLAG_UPDATE_CURRENT)
        }
        builder.setSmallIcon(drawable.ic_stat_name)
            .setContentTitle(context.getString(R.string.monitoring))
            .setContentText(context.getString(R.string.running_description))
            .setContentIntent(pendingIntent)
            //Using this action button I would like to call logTest
            .addAction(
                drawable.cg,
                context.getString(R.string.stop_background_service), pendingExitIntent
            )
            .setOngoing(true)
        notificationManager.notify(KILL_NOTIFICATION_REQUEST_CODE, builder.build())
        return builder.build()
    }

/*    @SuppressLint("UnspecifiedImmutableFlag")
    fun showMessageNotification(notificationTitle: String, notificationDescription: String) {
        val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            IMAGE_NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(drawable.splash_logo)
            .setContentTitle(notificationTitle)
            .setContentText(notificationDescription)
            .setOngoing(false)
            .setAutoCancel(true)
            .setSound(alarmSound)
        val pendingIntent: PendingIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(string.new_message_channel_name)
            val descriptionText = context.getString(string.new_message_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =
                NotificationChannel(IMAGE_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
            notificationManager.createNotificationChannel(channel)
            pendingIntent = getActivity(
                context,
                IMAGE_NOTIFICATION_REQUEST_CODE,
                notificationIntent,
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )
        }else{
            // Register the channel with the system
            pendingIntent = getActivity(
                context,
                IMAGE_NOTIFICATION_REQUEST_CODE,
                notificationIntent,
                FLAG_UPDATE_CURRENT
            )
        }
        notificationManager.notify(
            IMAGE_NOTIFICATION_REQUEST_CODE,
            builder.setContentIntent(pendingIntent).build())
    }*/
}