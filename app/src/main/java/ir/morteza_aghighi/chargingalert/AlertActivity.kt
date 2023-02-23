package ir.morteza_aghighi.chargingalert

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ir.morteza_aghighi.chargingalert.model.BatteryStatsModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

class AlertActivity : AppCompatActivity() {
    private lateinit var ringtone: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
        val btnSnooze = findViewById<Button>(R.id.btnSnooze)
        btnSnooze.setOnClickListener { view: View? ->
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
            object : CountDownTimer(6000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (BatteryStatsModel().getBatChargingType() == "Unplugged") finish()
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
        val flow = flow {
            delay(Duration.ZERO)
            while (true) {
                emit(Unit)
                delay(1000)
            }
        }

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        try {
            ringtone.release()
        } catch (ignored: Exception) {
        }
        super.onDestroy()
    }

}