package ir.morteza_aghighi.chargingalert;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Html;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.suke.widget.SwitchButton;

import java.text.DecimalFormat;
import java.util.Objects;

import ir.morteza_aghighi.chargingalert.tools.QuestionDialog;
import ir.morteza_aghighi.chargingalert.tools.ServiceMonitor;
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs;
import me.tankery.lib.circularseekbar.CircularSeekBar;


public class MainActivity extends AppCompatActivity implements QuestionDialog.QuestionListener {

    private static final DecimalFormat voltageDecimalFormat = new DecimalFormat("0.000");
    private static final DecimalFormat temperatureDecimalFormat = new DecimalFormat("0.0");
    private QuestionDialog questionDialog;
    private TextView tvBatPercent, tvBatVoltage, tvBatHealth, tvBatType,
            tvBatChargingStat, tvBatTemp;
    // code to post/handler request for permission
    private final int OVERLAY_REQUEST_CODE = 69;
    private final int BATTERY_OPTIMIZATION_REQUEST_CODE = 70;
    private final String overlayTag = "overlayTag", batteryOptimizationTag = "batteryOptimizationTag";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!SharedPrefs.getBoolean("notFirstRun", this)) {
            SharedPrefs.setBoolean("notFirstRun", true, this);
            SharedPrefs.setInt("chargingLimit", 90, this);
        }
        if (savedInstanceState == null && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q))
            checkDrawOverlayPermission();
        uiThings();
    }

    private void batteryOptimizationRequest() {
        if (!SharedPrefs.getBoolean("isBatteryOptimizationAsked", MainActivity.this)) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                questionDialog = new QuestionDialog(getString(R.string.warning), getString(R.string.explenationBattery));
                questionDialog.setCancelable(false);
                questionDialog.show(getSupportFragmentManager(), batteryOptimizationTag);
            }
        }
    }

    public void checkDrawOverlayPermission() {

        // check if we already  have permission to draw over other apps
        if (!Settings.canDrawOverlays(this)) {
            // if not construct intent to request permission

            // request permission via start activity for result
            questionDialog = new QuestionDialog(getString(R.string.warning), getString(R.string.explenation));
            questionDialog.setCancelable(false);
            questionDialog.show(getSupportFragmentManager(), overlayTag);
        } else batteryOptimizationRequest();
    }

    @SuppressLint("SetTextI18n")
    private void uiThings() {

        Window w = this.getWindow();
        w.setStatusBarColor(ContextCompat.getColor(this, R.color.activity_background));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            w.setNavigationBarColor(ContextCompat.getColor(this, R.color.activity_background));
        }

        final Button btnChargingAlert = findViewById(R.id.btnChargingAlert);
        final CircularSeekBar csbPT = findViewById(R.id.limitCircle);

        final TextView tvThreshold = findViewById(R.id.tvThreshold);
        tvThreshold.setText(SharedPrefs.getInt("chargingLimit", this) + "%");

        csbPT.setProgress(SharedPrefs.getInt("chargingLimit", this) - 5);

        if (SharedPrefs.getBoolean("isAlertEnabled", this)) {
            btnChargingAlert.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_ripple_reject));
            btnChargingAlert.setText(getResources().getString(R.string.disable_charging_alert));
            tvThreshold.setTextColor(getColor(R.color.white));
            csbPT.setEnabled(true);
            csbPT.setCircleProgressColor(getColor(R.color.Green));
            csbPT.setCircleColor(getColor(R.color.VeryLightGreen));
            csbPT.setPointerColor(getColor(R.color.Green));
            csbPT.setPointerHaloColor(getColor(R.color.VeryLightGreen));
        } else {
            btnChargingAlert.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_ripple_confirm));
            btnChargingAlert.setText(getResources().getString(R.string.enable_charging_alert));
            tvThreshold.setTextColor(getColor(R.color.color_light_gray));
            csbPT.setEnabled(false);
            csbPT.setCircleProgressColor(getColor(R.color.color_dark_gray));
            csbPT.setCircleColor(getColor(R.color.color_light_gray));
            csbPT.setPointerColor(getColor(R.color.color_dark_gray));
            csbPT.setPointerHaloColor(getColor(R.color.color_light_gray));
        }

        btnChargingAlert.setOnClickListener(view -> {
            if (SharedPrefs.getBoolean("isAlertEnabled", MainActivity.this)) {
                SharedPrefs.setBoolean("isAlertEnabled", false, MainActivity.this);
                btnChargingAlert.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.custom_ripple_confirm));
                btnChargingAlert.setText(getResources().getString(R.string.enable_charging_alert));
                tvThreshold.setTextColor(getColor(R.color.color_light_gray));
                csbPT.setEnabled(false);
                csbPT.setCircleProgressColor(getColor(R.color.color_dark_gray));
                csbPT.setCircleColor(getColor(R.color.color_light_gray));
                csbPT.setPointerColor(getColor(R.color.color_dark_gray));
                csbPT.setPointerHaloColor(getColor(R.color.color_light_gray));
            } else {
                SharedPrefs.setBoolean("isAlertEnabled", true, MainActivity.this);
                btnChargingAlert.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.custom_ripple_reject));
                btnChargingAlert.setText(getResources().getString(R.string.disable_charging_alert));
                tvThreshold.setTextColor(getColor(R.color.white));
                csbPT.setEnabled(true);
                csbPT.setCircleProgressColor(getColor(R.color.Green));
                csbPT.setCircleColor(getColor(R.color.VeryLightGreen));
                csbPT.setPointerColor(getColor(R.color.Green));
                csbPT.setPointerHaloColor(getColor(R.color.VeryLightGreen));
            }
        });


        final CountDownTimer coolDownTimer = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                SharedPrefs.setBoolean("isAlertEnabled", true, MainActivity.this);
            }
        };

        csbPT.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
                if (fromUser) {
                    tvThreshold.setText((int) progress + 5 + "%");
                    SharedPrefs.setInt("chargingLimit", (int) progress + 5, MainActivity.this);
                }
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                coolDownTimer.start();
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {
                try {
                    coolDownTimer.cancel();
                } catch (Exception ignored) {
                }
                SharedPrefs.setBoolean("isAlertEnabled", false, MainActivity.this);
            }
        });

        tvBatPercent = findViewById(R.id.tvBatPercent);
        tvBatPercent.setText(Html.fromHtml("<b>Percentage:</b> " + SharedPrefs.getInt("BatPercent", this) + "%"));

        tvBatVoltage = findViewById(R.id.tvBatVoltage);
        tvBatVoltage.setText(Html.fromHtml("<b>Voltage:</b> " + voltageDecimalFormat.format(SharedPrefs.getInt("BatVoltage", this) * 0.001) + "V"));

        tvBatHealth = findViewById(R.id.tvBatHealth);
        tvBatHealth.setText(Html.fromHtml("<b>Health:</b> " + SharedPrefs.getString("BatHealth", this)));

        tvBatType = findViewById(R.id.tvBatType);
        tvBatType.setText(Html.fromHtml("<b>Type:</b> " + SharedPrefs.getString("BatType", this)));

        tvBatChargingStat = findViewById(R.id.tvBatChargingStat);
        if (SharedPrefs.getString("BatChargingStat", this).
                equals("Unknown")) tvBatChargingStat.setText(Html.fromHtml("<b>Not Charging</b>"));
        else
            tvBatChargingStat.setText(Html.fromHtml("<b>Charging via:</b> " + SharedPrefs.getString("BatChargingStat", this)));

        tvBatTemp = findViewById(R.id.tvBatTemp);
        tvBatTemp.setText(Html.fromHtml("<b>Temperature:</b> " + temperatureDecimalFormat.format(SharedPrefs.getInt("BatTemp", this) * 0.1) + "°C"));

        SwitchButton swBoot = findViewById(R.id.swBoot);

        if (SharedPrefs.getBoolean("bootFlag", MainActivity.this))
            swBoot.setChecked(true); //Turn on switch if startup flag is true

        swBoot.toggle();     //switch state
        swBoot.toggle(true);//switch without animation
        swBoot.setShadowEffect(true);//disable shadow effect
        swBoot.setEnabled(true);//disable button
        swBoot.setEnableEffect(true);//disable the switch animation
        swBoot.setOnCheckedChangeListener((view, isChecked) -> SharedPrefs.setBoolean("bootFlag", isChecked, MainActivity.this));
    }

    BroadcastReceiver exitReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            stopService(new Intent(MainActivity.this, ChargingMonitorService.class));
            finish();
        }

    };
    private final BroadcastReceiver batReceiver = new BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            tvBatPercent.setText(Html.fromHtml("<b>Percentage:</b> " + intent.getIntExtra("BatPercent", 0) + "%"));
            tvBatVoltage.setText(Html.fromHtml("<b>Voltage:</b> " + voltageDecimalFormat.format((intent.getIntExtra("BatVoltage", 0) * 0.001)) + "V"));
            tvBatHealth.setText(Html.fromHtml("<b>Health:</b> " + intent.getStringExtra("BatHealth")));
            tvBatType.setText(Html.fromHtml("<b>Type:</b> " + intent.getStringExtra("BatType")));
            if (Objects.equals(intent.getStringExtra("BatChargingStat"), "Unknown"))
                tvBatChargingStat.setText(Html.fromHtml("<b>Not Charging</b>"));
            else
                tvBatChargingStat.setText(Html.fromHtml("<b>Charging via:</b> " + intent.getStringExtra("BatChargingStat")));
            tvBatTemp.setText(Html.fromHtml("<b>Temperature:</b> " + temperatureDecimalFormat.format((intent.getIntExtra("BatTemp", 0) * 0.1)) + "°C"));
        }

    };


    @Override
    protected void onResume() {
        super.onResume();
        if (!(new ServiceMonitor().isMyServiceRunning(ChargingMonitorService.class, this))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(new Intent(this, ChargingMonitorService.class));
            } else {
                this.startService(new Intent(this, ChargingMonitorService.class));
            }
        }
        IntentFilter exitIntentFilter = new IntentFilter("android.intent.CLOSE_ACTIVITY");
        IntentFilter batIntentFilter = new IntentFilter("android.intent.BATTERY_STATUS");
        registerReceiver(exitReceiver, exitIntentFilter);
        registerReceiver(batReceiver, batIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(exitReceiver);
        unregisterReceiver(batReceiver);
        super.onDestroy();
    }

    @Override
    public void onButtonClicked(boolean result) {
        SharedPrefs.setBoolean("isDeviceRotated", false, MainActivity.this);
        assert questionDialog.getTag() != null;
        if (questionDialog.getTag().equals(overlayTag) && result) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivityForResult(intent, OVERLAY_REQUEST_CODE);
        } else if (questionDialog.getTag().equals(batteryOptimizationTag) && result) {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            startActivityForResult(intent, BATTERY_OPTIMIZATION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_REQUEST_CODE) batteryOptimizationRequest();
        else if (requestCode == BATTERY_OPTIMIZATION_REQUEST_CODE)
            SharedPrefs.setBoolean("isBatteryOptimizationAsked", true, MainActivity.this);
    }
}