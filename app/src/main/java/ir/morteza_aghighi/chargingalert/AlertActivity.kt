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
    private lateinit var dismissMessage: String
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var cancelTimer: CountDownTimer
    private lateinit var audioManager: AudioManager
    private var bypassDND = false
    private var isDischargeAlert = false
    private var isDNDoff = false
    private var currentVolume = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initVariables()
    }

    private fun initVariables() {
        isDischargeAlert = intent.getBooleanExtra("alertType", false)
        isDNDoff = Settings.Global.getInt(contentResolver, "zen_mode") == 0
        dismissMessage = if (isDischargeAlert) {
            getString(R.string.disChargeDismissMessage)
        } else getString(R.string.chargeDismissMessage)
        bypassDND = SharedPrefs.getBoolean("bypassDND", applicationContext)
        isDischargeAlert = intent.getBooleanExtra("alertType", false)
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        mediaPlayer = MediaPlayer.create(
            applicationContext, Settings.System.DEFAULT_ALARM_ALERT_URI
        )
        initUI()
    }

    private fun initUI() {

        val activityAlertBinding: ActivityAlertBinding =
            ActivityAlertBinding.inflate(layoutInflater)
        setContentView(activityAlertBinding.root)

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

        activityAlertBinding.btnSnooze.setOnClickListener {
            Toast.makeText(
                this@AlertActivity, dismissMessage, Toast.LENGTH_LONG
            ).show()
            finish()
        }
        initFunctions()
    }

    private fun initFunctions() {

        if ((isDNDoff || bypassDND)
            && ((isDischargeAlert && BatteryInfoModel().getBatChargingType() == "Unplugged")
                    || (!isDischargeAlert && BatteryInfoModel().getBatChargingType() != "Unplugged"))) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            )
            mediaPlayer.setVolume(
                SharedPrefs.getInt("volume", applicationContext)
                    .toFloat() / (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 10),
                SharedPrefs.getInt("volume", applicationContext)
                    .toFloat() / (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 10)
            )
            mediaPlayer.start()
        }
        cancelTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (BatteryInfoModel().getBatChargingType() == "Unplugged" && !isDischargeAlert) {
                    cancelTimer.cancel()
                    finish()
                }else if (BatteryInfoModel().getBatChargingType() != "Unplugged" && isDischargeAlert){
                    cancelTimer.cancel()
                    finish()
                }
            }

            override fun onFinish() {
                Toast.makeText(
                    this@AlertActivity, dismissMessage, Toast.LENGTH_LONG
                ).show()
                if (isDNDoff || bypassDND) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
                    mediaPlayer.release()
                }
                finish()
            }
        }.start()
    }

    override fun onDestroy() {
        try {
            cancelTimer.cancel()
            if (isDNDoff || bypassDND) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
                mediaPlayer.release()
            }
        } catch (ignored: Exception) {
        }
        super.onDestroy()
    }

}