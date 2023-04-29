package ir.morteza_aghighi.chargingalert

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ir.morteza_aghighi.chargingalert.databinding.ActivityAlertBinding
import ir.morteza_aghighi.chargingalert.model.BatteryInfoModel
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs

class AlertActivity : AppCompatActivity() {
    /** declaring variables,
     * the descriptions for each one is in the code.*/
    private lateinit var dismissMessage: String
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var chargeDischargeAlertCancelTimer: CountDownTimer
    private lateinit var audioManager: AudioManager
    private var bypassDND = false
    private var isUnplugged = false
    private var isDischargeAlert = false
    private var isDNDoff = false
    private var defaultVolume = 0
    private lateinit var batteryInfoModel: BatteryInfoModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initVariables()
    }

    private fun initVariables() {
        /**
         * initialize battery data model to get battery info.
         * this is useful when we want to check whether battery is unplugged or plugged.*/
        batteryInfoModel = BatteryInfoModel()


        /**
         * checking whether alert is for charging limit reached or discharging limit reached.*/
        isDischargeAlert = intent.getBooleanExtra("alertType", false)


        /**
         * check to see DND mode is on or off.
         * by default app respects the DND mode settings unless user tells it to play alarm even on DND.*/
        isDNDoff = Settings.Global.getInt(contentResolver, "zen_mode") == 0


        /**
         * setting dismiss message according to the type of alert (charge or discharge).*/
        dismissMessage = if (isDischargeAlert) {
            getString(R.string.disChargeDismissMessage)
        } else getString(R.string.chargeDismissMessage)


        /**
         * check to see if user want to play alert on DND or not.*/
        bypassDND = SharedPrefs.getBoolean(applicationContext, "bypassDND")


        /**
         * check to see is charger connected on creating activity.
         * this is just an initialization and later we update this variable.*/
        isUnplugged = batteryInfoModel.getBatChargingType() == "Unplugged"


        /**
         * setting up audio manager and media player to be able to set user preferred volume.
         * you will see the logic in the {@link #initFunctions() initFunctions} function*/
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        defaultVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        mediaPlayer = MediaPlayer.create(
            applicationContext, Settings.System.DEFAULT_ALARM_ALERT_URI
        )

        initUI()
    }

    private fun initUI() {

        /**
         * using view binding*/
        val activityAlertBinding: ActivityAlertBinding =
            ActivityAlertBinding.inflate(layoutInflater)
        setContentView(activityAlertBinding.root)

        /**
         * turning on the screen*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
            setShowWhenLocked(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }

        /**
         * onclick listener for btn snooze*/
        activityAlertBinding.btnSnooze.setOnClickListener {
            Toast.makeText(
                this@AlertActivity, dismissMessage, Toast.LENGTH_LONG
            ).show()
            finish()
        }

        initFunctions()
    }

    /**
     * this function sets up alarm sound and mechanism to check if user connects/disconnects charger
     * and if user connects charger on low battery or disconnects charger when upper charge limit reached
     * this function stops alert and closes activity*/
    private fun initFunctions() {

        /** check if DND is off or if it is on the bypass DND option is enabled by the user. */
        if ((isDNDoff || bypassDND)


            /** checking 2 different condition and one of them must return true.
             * first check if the alert type is for discharge and the battery is still unplugged.
             * second one checks if the alert type is for charge and charger is still plugged in.*/
            && ((isDischargeAlert && isUnplugged) || (!isDischargeAlert && !isUnplugged))) {

            /** if the above conditions are met first getting the default alarm music for the phone.*/
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            /** then temporary increase the volume of music playback to max level because the amount of volume that user set for alarm is
             * according to the max volume. if we do not increase the playback volume, the alarm volume is probably lower than
             * what user wanted.*/
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            )

            /** setting user preferred volume for both channels*/
            mediaPlayer.setVolume(
                SharedPrefs.getInt(applicationContext, "volume")
                    .toFloat() / (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 10),
                SharedPrefs.getInt(applicationContext, "volume")
                    .toFloat() / (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 10)
            )

            /** playing alarm*/
            mediaPlayer.start()
        }

        /** the bellow code is a timer that has 2 jobs:*/
        chargeDischargeAlertCancelTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                /** first: checks every second for if on discharge alert user connected charger
                 * of on charging alert user is disconnected from charger.
                 * and if conditions are met stops alert, countdown timer and finishes activity,
                 * and restore default device volume.*/
                isUnplugged = batteryInfoModel.getBatChargingType() == "Unplugged"

                if ((isUnplugged && !isDischargeAlert) || (!isUnplugged && isDischargeAlert)) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultVolume, 0)
                    chargeDischargeAlertCancelTimer.cancel()
                    finish()
                }

            }

            override fun onFinish() {
                /** second: if the talked conditions were not met for 60 seconds stops alarm,
                 * shows a message that the alert will repeat if you the user do not connect/disconnect charger
                 * in next 5 minutes, and finishes this activity,
                 * and restore default device volume.*/
                Toast.makeText(
                    this@AlertActivity, dismissMessage, Toast.LENGTH_LONG
                ).show()
                if (isDNDoff || bypassDND) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultVolume, 0)
                    mediaPlayer.release()
                }
                finish()
            }
        }.start()
    }

    override fun onDestroy() {
        try {
            /** onDestroy cancel countdown timer and alarm, and restore default device volume.*/
            chargeDischargeAlertCancelTimer.cancel()
            if (isDNDoff || bypassDND) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultVolume, 0)
                mediaPlayer.release()
            }
        } catch (ignored: Exception) {
        }
        super.onDestroy()
    }

}