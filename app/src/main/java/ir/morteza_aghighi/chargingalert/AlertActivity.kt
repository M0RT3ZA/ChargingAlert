package ir.morteza_aghighi.chargingalert

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
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
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var cancelTimer: CountDownTimer
    private lateinit var activityAlertBinding: ActivityAlertBinding
    private lateinit var audioManager: AudioManager
    private var currentVolume = 0
    private var isDNDoff = (Settings.Global.getInt(contentResolver, "zen_mode") == 0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityAlertBinding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(activityAlertBinding.root)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
        val dismissMessage = if (intent.getBooleanExtra("alertType", false)){
            getString(R.string.disChargeDismissMessage)
        }else getString(R.string.chargeDismissMessage)
        activityAlertBinding.btnSnooze.setOnClickListener {
            Toast.makeText(
                this@AlertActivity,
                dismissMessage,
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
        try {
            if (isDNDoff || SharedPrefs.getBoolean("bypassDND", applicationContext)){
                audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI)
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                mediaPlayer = MediaPlayer.create(
                    applicationContext,
                    Settings.System.DEFAULT_ALARM_ALERT_URI
                )
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
                    if (BatteryInfoModel().getBatChargingType() == "Unplugged"
                        && !intent.getBooleanExtra("alertType",false)) {
                        cancelTimer.cancel()
                        finish()
                    }
                }

                override fun onFinish() {
                    Toast.makeText(
                        this@AlertActivity,
                        dismissMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    if (isDNDoff || SharedPrefs.getBoolean("bypassDND", applicationContext)){
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
                        mediaPlayer.release()
                    }
                    finish()
                }
            }.start()
        } catch (ignored: Exception) {
        }
    }

    override fun onDestroy() {
        try {
            cancelTimer.cancel()
            if (isDNDoff || SharedPrefs.getBoolean("bypassDND", applicationContext)){
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
                mediaPlayer.release()
            }
        } catch (ignored: Exception) {
        }
        super.onDestroy()
    }

}