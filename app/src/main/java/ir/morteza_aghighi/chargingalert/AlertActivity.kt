package ir.morteza_aghighi.chargingalert;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class AlertActivity extends AppCompatActivity {
    private MediaPlayer ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        Button btnSnooze = findViewById(R.id.btnSnooze);
        btnSnooze.setOnClickListener(view -> {
            Toast.makeText(AlertActivity.this,"You will be alerted after 5 minutes if your phone is still on Charger",Toast.LENGTH_LONG).show();
            finish();
        });
        try {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            ringtone = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
            ringtone.start();

            new CountDownTimer(60000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    Toast.makeText(AlertActivity.this,"You will be alerted after 5 minutes if your phone is still on Charger",Toast.LENGTH_LONG).show();
                    ringtone.release();
                    finish();
                }
            }.start();


        } catch (Exception ignored) {}
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter batIntentFilter = new IntentFilter("android.intent.BATTERY_STATUS");
        registerReceiver(batReceiver,batIntentFilter);
    }

    @Override
    protected void onDestroy() {
        try {
            ringtone.release();
            unregisterReceiver(batReceiver);
        }catch (Exception ignored){}
        super.onDestroy();
    }

    BroadcastReceiver batReceiver = new BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getStringExtra("BatChargingStat"), "Unknown")) finish();
        }
    };

}