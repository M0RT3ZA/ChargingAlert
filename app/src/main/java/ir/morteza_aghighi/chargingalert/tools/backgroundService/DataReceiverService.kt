package ir.morteza_aghighi.chargingalert.tools.backgroundService

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.os.SystemClock.sleep
import android.telephony.SmsMessage
import android.util.Log
import ir.morteza_aghighi.chargingalert.R
import ir.morteza_aghighi.chargingalert.tools.*
import kotlinx.coroutines.*


var isServiceStoppedByUser = true

class DataReceiverService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private var repeatInterval = 5000L
    private lateinit var messageReceiver: DataReceiverService.MessageReceiver


    override fun onBind(intent: Intent): IBinder? {
        Log.d("DataReceiverServiceLog", "Some component want to bind with the service")
        // We don't provide binding, so return null
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("DataReceiverServiceLog", "onStartCommand executed with startId: $startId")
        if (intent != null) {
            val action = intent.action
            Log.d("DataReceiverServiceLog", "using an intent with action $action")
            when (action) {
                ServiceActions.START.name -> startService()
                ServiceActions.STOP.name -> stopService()
                else -> Log.d(
                    "DataReceiverServiceLog",
                    "This should never happen. No action in the received intent"
                )
            }
        } else {
            Log.d("DataReceiverServiceLog",
                "with a null intent. It has been probably restarted by the system."
            )
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1, ServiceNotificationTools(this).createNotification())
        val receiveFilter = IntentFilter()
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED")
        receiveFilter.priority = 100
        messageReceiver = MessageReceiver()
        registerReceiver(messageReceiver, receiveFilter)
        Log.d("DataReceiverServiceLog", "The service has been created")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceStoppedByUser) {
            ToastMaker(this, getString(R.string.service_stopped)).sh()
//            networkMonitoringUtil.unRegisterNetworkCallbackEvents()
        }
        unregisterReceiver(messageReceiver)
        Log.d("DataReceiverServiceLog", "The service has been destroyed")
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(this, DataReceiverService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent =
            PendingIntent.getService(this, 1, restartServiceIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        this.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent)
    }



    @OptIn(DelicateCoroutinesApi::class)
    private fun startService() {
        if (isServiceStarted) return

        Log.d("DataReceiverServiceLog", "Starting the foreground service task")
        if (isServiceStoppedByUser)
            ToastMaker(this, getString(R.string.service_started)).sh()
        isServiceStarted = true
        setServiceState(this,
            ServiceState.STARTED
        )

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DataReceiverService::lock").apply {
                    acquire(60 * 10 * 1000L /*10 minutes*/)
                }
            }
        // we're starting a loop in a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    fetchDataFromServer()
                }
                sleep(repeatInterval)
            }
            Log.d("DataReceiverServiceLog", "End of the loop for the service")
        }
    }

    private fun stopService() {
        Log.d("DataReceiverServiceLog", "Stopping the foreground service")
        if (isServiceStoppedByUser)
            ToastMaker(this, getString(R.string.stopping_service)).sh()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Log.d("DataReceiverServiceLog", "Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        setServiceState(
            this,
            ServiceState.STOPPED
        )
    }

    private fun fetchDataFromServer() {
//        rQueue.add(imageApiRequest)
    }

    internal inner class MessageReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

        }
    }
}
