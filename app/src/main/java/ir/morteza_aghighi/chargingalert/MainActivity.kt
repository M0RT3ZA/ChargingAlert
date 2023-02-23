package ir.morteza_aghighi.chargingalert

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PowerManager
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.suke.widget.SwitchButton
import ir.morteza_aghighi.chargingalert.databinding.ActivityMainBinding
import ir.morteza_aghighi.chargingalert.tools.QuestionDialog
import ir.morteza_aghighi.chargingalert.tools.QuestionDialog.QuestionListener
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs
import ir.morteza_aghighi.chargingalert.viewModel.UiAndServiceController
import me.tankery.lib.circularseekbar.CircularSeekBar
import me.tankery.lib.circularseekbar.CircularSeekBar.OnCircularSeekBarChangeListener
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), QuestionListener {
    private var questionDialog: QuestionDialog? = null
    private lateinit var tvBatPercent: TextView
    private lateinit var tvBatVoltage: TextView
    private lateinit var tvBatHealth: TextView
    private lateinit var tvBatType: TextView
    private lateinit var tvBatChargingStat: TextView
    private lateinit var tvBatTemp: TextView

    // code to post/handler request for permission
    private val OVERLAY_REQUEST_CODE = 69
    private val BATTERY_OPTIMIZATION_REQUEST_CODE = 70
    private val overlayTag = "overlayTag"
    private val batteryOptimizationTag = "batteryOptimizationTag"
    private lateinit var mainActivityBinding: ActivityMainBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)
        if (!SharedPrefs.getBoolean("notFirstRun", this)) {
            SharedPrefs.setBoolean("notFirstRun", true, this)
            SharedPrefs.setInt("chargingLimit", 90, this)
        }
        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) checkDrawOverlayPermission()
        uiThings()
    }

    private fun batteryOptimizationRequest() {
        if (!SharedPrefs.getBoolean("isBatteryOptimizationAsked", this@MainActivity)) {
            val packageName = packageName
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                questionDialog = QuestionDialog(
                    getString(R.string.warning),
                    getString(R.string.explenationBattery)
                )
                questionDialog!!.isCancelable = false
                questionDialog!!.show(supportFragmentManager, batteryOptimizationTag)
            }
        }
    }

    private fun checkDrawOverlayPermission() {

        // check if we already  have permission to draw over other apps
        if (!Settings.canDrawOverlays(this)) {
            // if not construct intent to request permission

            // request permission via start activity for result
            questionDialog =
                QuestionDialog(getString(R.string.warning), getString(R.string.explenation))
            questionDialog!!.isCancelable = false
            questionDialog!!.show(supportFragmentManager, overlayTag)
        } else batteryOptimizationRequest()
    }

    @SuppressLint("SetTextI18n")
    private fun uiThings() {
        val w = this.window
        w.statusBarColor = ContextCompat.getColor(this, R.color.activity_background)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            w.navigationBarColor = ContextCompat.getColor(this, R.color.activity_background)
        }
        val btnChargingAlert = mainActivityBinding.btnChargingAlert
        val csbPT = mainActivityBinding.limitCircle
        val tvThreshold = mainActivityBinding.tvThreshold
        tvThreshold.text = SharedPrefs.getInt("chargingLimit", this).toString() + "%"
        csbPT.progress = (SharedPrefs.getInt("chargingLimit", this) - 5).toFloat()
        if (SharedPrefs.getBoolean("isAlertEnabled", this)) {
            btnChargingAlert.background =
                ContextCompat.getDrawable(this, R.drawable.custom_ripple_reject)
            btnChargingAlert.text = resources.getString(R.string.disable_charging_alert)
            tvThreshold.setTextColor(getColor(R.color.white))
            csbPT.isEnabled = true
            csbPT.circleProgressColor = getColor(R.color.Green)
            csbPT.circleColor = getColor(R.color.VeryLightGreen)
            csbPT.pointerColor = getColor(R.color.Green)
            csbPT.pointerHaloColor = getColor(R.color.VeryLightGreen)
        } else {
            btnChargingAlert.background =
                ContextCompat.getDrawable(this, R.drawable.custom_ripple_confirm)
            btnChargingAlert.text = resources.getString(R.string.enable_charging_alert)
            tvThreshold.setTextColor(getColor(R.color.color_light_gray))
            csbPT.isEnabled = false
            csbPT.circleProgressColor = getColor(R.color.color_dark_gray)
            csbPT.circleColor = getColor(R.color.color_light_gray)
            csbPT.pointerColor = getColor(R.color.color_dark_gray)
            csbPT.pointerHaloColor = getColor(R.color.color_light_gray)
        }
        btnChargingAlert.setOnClickListener {
            if (SharedPrefs.getBoolean("isAlertEnabled", this@MainActivity)) {
                SharedPrefs.setBoolean("isAlertEnabled", false, this@MainActivity)
                btnChargingAlert.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.custom_ripple_confirm)
                btnChargingAlert.text = resources.getString(R.string.enable_charging_alert)
                tvThreshold.setTextColor(getColor(R.color.color_light_gray))
                csbPT.isEnabled = false
                csbPT.circleProgressColor = getColor(R.color.color_dark_gray)
                csbPT.circleColor = getColor(R.color.color_light_gray)
                csbPT.pointerColor = getColor(R.color.color_dark_gray)
                csbPT.pointerHaloColor = getColor(R.color.color_light_gray)
            } else {
                SharedPrefs.setBoolean("isAlertEnabled", true, this@MainActivity)
                btnChargingAlert.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.custom_ripple_reject)
                btnChargingAlert.text = resources.getString(R.string.disable_charging_alert)
                tvThreshold.setTextColor(getColor(R.color.white))
                csbPT.isEnabled = true
                csbPT.circleProgressColor = getColor(R.color.Green)
                csbPT.circleColor = getColor(R.color.VeryLightGreen)
                csbPT.pointerColor = getColor(R.color.Green)
                csbPT.pointerHaloColor = getColor(R.color.VeryLightGreen)
            }
        }
        val coolDownTimer: CountDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                SharedPrefs.setBoolean("isAlertEnabled", true, this@MainActivity)
            }
        }
        csbPT.setOnSeekBarChangeListener(object : OnCircularSeekBarChangeListener {
            override fun onProgressChanged(
                circularSeekBar: CircularSeekBar,
                progress: Float,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    tvThreshold.text = (progress.toInt() + 5).toString() + "%"
                    SharedPrefs.setInt("chargingLimit", progress.toInt() + 5, this@MainActivity)
                }
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar) {
                coolDownTimer.start()
            }

            override fun onStartTrackingTouch(seekBar: CircularSeekBar) {
                try {
                    coolDownTimer.cancel()
                } catch (ignored: Exception) {
                }
                SharedPrefs.setBoolean("isAlertEnabled", false, this@MainActivity)
            }
        })
        /*tvBatPercent = findViewById(R.id.tvBatPercent)
        tvBatPercent.text = Html.fromHtml(
            "<b>Percentage:</b> " + SharedPrefs.getInt(
                "BatPercent",
                this
            ) + "%"
        )
        tvBatVoltage = findViewById(R.id.tvBatVoltage)
        tvBatVoltage.text = Html.fromHtml(
            "<b>Voltage:</b> " + voltageDecimalFormat.format(
                SharedPrefs.getInt("BatVoltage", this) * 0.001
            ) + "V"
        )
        tvBatHealth = findViewById(R.id.tvBatHealth)
        tvBatHealth.text = Html.fromHtml(
            "<b>Health:</b> " + SharedPrefs.getString(
                "BatHealth",
                this
            )
        )
        tvBatType = findViewById(R.id.tvBatType)
        tvBatType.text = Html.fromHtml("<b>Type:</b> " + SharedPrefs.getString("BatType", this))
        tvBatChargingStat = findViewById(R.id.tvBatChargingStat)
        if (SharedPrefs.getString("BatChargingStat", this) == "Unknown") tvBatChargingStat.text =
            Html.fromHtml("<b>Not Charging</b>") else tvBatChargingStat.text = Html.fromHtml(
            "<b>Charging via:</b> " + SharedPrefs.getString(
                "BatChargingStat",
                this
            )
        )
        tvBatTemp = findViewById(R.id.tvBatTemp)
        tvBatTemp.text = Html.fromHtml(
            "<b>Temperature:</b> " + temperatureDecimalFormat.format(
                SharedPrefs.getInt("BatTemp", this) * 0.1
            ) + "°C"
        )*/
        val swBoot = mainActivityBinding.swBoot
        if (SharedPrefs.getBoolean("bootFlag", this@MainActivity)) swBoot.isChecked =
            true //Turn on switch if startup flag is true
        swBoot.toggle() //switch state
        swBoot.toggle(true) //switch without animation
        swBoot.setShadowEffect(true) //disable shadow effect
        swBoot.isEnabled = true //disable button
        swBoot.setEnableEffect(true) //disable the switch animation
        swBoot.setOnCheckedChangeListener { _: SwitchButton?, isChecked: Boolean ->
            SharedPrefs.setBoolean(
                "bootFlag",
                isChecked,
                this@MainActivity
            )
        }
    }


    /*private val batReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            tvBatPercent.text =
                Html.fromHtml("<b>Percentage:</b> " + intent.getIntExtra("BatPercent", 0) + "%")
            tvBatVoltage.text = Html.fromHtml(
                "<b>Voltage:</b> " + voltageDecimalFormat.format(
                    intent.getIntExtra(
                        "BatVoltage",
                        0
                    ) * 0.001
                ) + "V"
            )
            tvBatHealth.text =
                Html.fromHtml("<b>Health:</b> " + intent.getStringExtra("BatHealth"))
            tvBatType.text = Html.fromHtml("<b>Type:</b> " + intent.getStringExtra("BatType"))
            if (intent.getStringExtra("BatChargingStat") == "Unknown") tvBatChargingStat.text =
                Html.fromHtml("<b>Not Charging</b>") else tvBatChargingStat.text =
                Html.fromHtml("<b>Charging via:</b> " + intent.getStringExtra("BatChargingStat"))
            tvBatTemp.text = Html.fromHtml(
                "<b>Temperature:</b> " + temperatureDecimalFormat.format(
                    intent.getIntExtra(
                        "BatTemp",
                        0
                    ) * 0.1
                ) + "°C"
            )
        }
    }*/

    override fun onResume() {
        super.onResume()

//        val batIntentFilter = IntentFilter("android.intent.BATTERY_STATUS")
//        registerReceiver(batReceiver, batIntentFilter)
        UiAndServiceController(this, mainActivityBinding).readData()
    }

    override fun onDestroy() {
//        unregisterReceiver(batReceiver)
        UiAndServiceController(this).unreadData()
        super.onDestroy()
    }

    override fun onButtonClicked(result: Boolean) {
        SharedPrefs.setBoolean("isDeviceRotated", false, this@MainActivity)
        assert(questionDialog!!.tag != null)
        if (questionDialog!!.tag == overlayTag && result) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + applicationContext.packageName)
            )
            startActivityForResult(intent, OVERLAY_REQUEST_CODE)
        } else if (questionDialog!!.tag == batteryOptimizationTag && result) {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            startActivityForResult(intent, BATTERY_OPTIMIZATION_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_REQUEST_CODE) batteryOptimizationRequest() else if (requestCode == BATTERY_OPTIMIZATION_REQUEST_CODE) SharedPrefs.setBoolean(
            "isBatteryOptimizationAsked",
            true,
            this@MainActivity
        )
    }

    companion object {
        private val voltageDecimalFormat = DecimalFormat("0.000")
        private val temperatureDecimalFormat = DecimalFormat("0.0")
    }
}