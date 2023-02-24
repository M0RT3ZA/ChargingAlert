package ir.morteza_aghighi.chargingalert

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ir.morteza_aghighi.chargingalert.databinding.ActivityAlertBinding
import ir.morteza_aghighi.chargingalert.model.BatteryStatsModel

class AlertActivity : AppCompatActivity() {
    private lateinit var ringtone: MediaPlayer
    private lateinit var cancelTimer: CountDownTimer
    private lateinit var activityAlertBinding: ActivityAlertBinding

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
        activityAlertBinding.btnSnooze.setOnClickListener {
            Toast.makeText(
                this@AlertActivity,
                "You will be alerted after 5 minutes if your phone is still on Charger",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
        try {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI)
            ringtone.start()
            cancelTimer = object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (BatteryStatsModel().getBatChargingType() == "Unplugged") {
                        cancelTimer.cancel()
                        finish()
                    }
                }

                override fun onFinish() {
                    Toast.makeText(
                        this@AlertActivity,
                        "You will be alerted after 5 minutes if your phone is still on Charger",
                        Toast.LENGTH_LONG
                    ).show()
                    ringtone.release()
                    finish()
                }
            }.start()
        } catch (ignored: Exception) {
        }
    }

    override fun onDestroy() {
        try {
            cancelTimer.cancel()
            ringtone.release()
        } catch (ignored: Exception) {
        }
        super.onDestroy()
    }

}