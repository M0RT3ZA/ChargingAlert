package ir.morteza_aghighi.chargingalert

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import ir.morteza_aghighi.chargingalert.databinding.ActivityMainBinding
import ir.morteza_aghighi.chargingalert.model.BatteryInfoModel
import ir.morteza_aghighi.chargingalert.tools.QuestionDialog
import ir.morteza_aghighi.chargingalert.tools.QuestionDialog.QuestionListener
import ir.morteza_aghighi.chargingalert.tools.SharedPrefs
import ir.morteza_aghighi.chargingalert.tools.ToastMaker
import ir.morteza_aghighi.chargingalert.tools.backgroundService.ServiceActions
import ir.morteza_aghighi.chargingalert.tools.backgroundService.ServiceStateChanger
import ir.morteza_aghighi.chargingalert.viewModel.UiAndServiceController
import kotlin.math.roundToInt

// code to post/handler request for permission
private const val OVERLAY_REQUEST_CODE = 69
private const val BATTERY_OPTIMIZATION_REQUEST_CODE = 70

class MainActivity : AppCompatActivity(), QuestionListener {
    private var questionDialog: QuestionDialog? = null
    private lateinit var audioManager: AudioManager
    private var currentVolume = 0
    private lateinit var mediaPlayer: MediaPlayer
    private val overlayTag = "overlayTag"
    private val batteryOptimizationTag = "batteryOptimizationTag"
    private lateinit var mainActivityBinding: ActivityMainBinding
    private lateinit var uiAndServiceController: UiAndServiceController
    private lateinit var cancelTimer: CountDownTimer

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)
        if (!SharedPrefs.getBoolean(this@MainActivity, "notFirstRun")) {
            SharedPrefs.setBoolean(this@MainActivity, "notFirstRun", true)
            SharedPrefs.setInt(this@MainActivity, "chargingLimit", 90)
        }
        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) checkDrawOverlayPermission()
        uiThings()
    }

    private fun initTour() {
        if (SharedPrefs.getBoolean(this@MainActivity, "tourComplete")) return

        TapTargetSequence(this@MainActivity).targets(
            TapTarget.forView(
                mainActivityBinding.clTVs,
                getString(R.string.batInfoTitle),
                getString(R.string.batInfoDescription)
            ).tintTarget(true),
            TapTarget.forView(
                mainActivityBinding.iosPbChargeThreshold,
                getString(R.string.chargeThresholdTitle),
                getString(R.string.chargeThresholdDescription)
            ).tintTarget(false),
            TapTarget.forView(
                mainActivityBinding.iosPbDischargeThreshold,
                getString(R.string.disChargeThresholdTitle),
                getString(R.string.disChargeThresholdDescription)
            ).tintTarget(false),
            TapTarget.forView(
                mainActivityBinding.iosPbVolume,
                getString(R.string.alertVolumeTitle),
                getString(R.string.alertVolumeDescription)
            ).tintTarget(false),
            TapTarget.forView(
                mainActivityBinding.swDND,
                getString(R.string.ignoreDNDTitle),
                getString(R.string.ignoreDNDDescription)
            ).tintTarget(false),
            TapTarget.forView(
                mainActivityBinding.swBoot,
                getString(R.string.runOnBootTitle),
                getString(R.string.runOnBootDescription)
            ).tintTarget(false),
            TapTarget.forView(
                mainActivityBinding.btnChargingAlert,
                getString(R.string.enableServiceTitle),
                getString(R.string.enableServiceDescription)
            ).tintTarget(false)
        ).continueOnCancel(true)
            .listener(object : TapTargetSequence.Listener {
                override fun onSequenceFinish() {
                    SharedPrefs.setBoolean(this@MainActivity, "tourComplete", true)
                }

                override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}

                override fun onSequenceCanceled(lastTarget: TapTarget?) {}

            }).start()
    }

    private fun batteryOptimizationRequest() {
        val packageName = packageName
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            questionDialog = QuestionDialog(
                getString(R.string.warning), getString(R.string.explenationBattery)
            )
            questionDialog!!.isCancelable = false
            questionDialog!!.show(supportFragmentManager, batteryOptimizationTag)
        } else initTour()
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
        val iosPbChargeThreshold = mainActivityBinding.iosPbChargeThreshold
        val iosPbDischargeThreshold = mainActivityBinding.iosPbDischargeThreshold
        val iosPbVolume = mainActivityBinding.iosPbVolume
        val swBoot = mainActivityBinding.swBoot
        val swDND = mainActivityBinding.swDND
        swBoot.apply {
//            this.setEnableEffect(true) //disable the switch animation
            this.setOnCheckedChangeListener { _, isChecked ->
                SharedPrefs.setBoolean(
                    this@MainActivity, "bootFlag", isChecked
                )
            }
        }

        swDND.apply {
            //            this.setEnableEffect(true) //disable the switch animation
            this.setOnCheckedChangeListener { _, isChecked ->
                SharedPrefs.setBoolean(
                    this@MainActivity, "bypassDND", isChecked
                )
            }
        }
        if (SharedPrefs.getBoolean(this@MainActivity, "isAlertEnabled")) {
            btnChargingAlert.background =
                ContextCompat.getDrawable(this, R.drawable.custom_ripple_reject)
            btnChargingAlert.text = resources.getString(R.string.disable_charging_alert)
            iosPbChargeThreshold.apply {
                this.isEnabled = true
                this.progressPaint.color = getColor(R.color.Green)
                this.backgroundPaint.color = getColor(R.color.LightGreen)
                this.getProgress()
            }
            iosPbDischargeThreshold.apply {
                this.isEnabled = true
                this.progressPaint.color = getColor(R.color.Green)
                this.backgroundPaint.color = getColor(R.color.LightGreen)
                this.getProgress()
            }
            iosPbVolume.apply {
                this.isEnabled = true
                this.progressPaint.color = getColor(R.color.Green)
                this.backgroundPaint.color = getColor(R.color.LightGreen)
                this.getProgress()
            }
            swBoot.apply {
                this.isEnabled = true
                this.setCheckedNoEvent(SharedPrefs.getBoolean(this@MainActivity, "bootFlag"))
            }
            swDND.apply {
                this.isEnabled = true
                this.setCheckedNoEvent(SharedPrefs.getBoolean(this@MainActivity, "bypassDND"))
            }
        } else {
            btnChargingAlert.background =
                ContextCompat.getDrawable(this, R.drawable.custom_ripple_confirm)
            btnChargingAlert.text = resources.getString(R.string.enable_charging_alert)
            iosPbChargeThreshold.apply {
                this.isEnabled = false
                this.progressPaint.color = getColor(R.color.gray)
                this.backgroundPaint.color = getColor(R.color.light_gray)
                this.getProgress()
            }
            iosPbDischargeThreshold.apply {
                this.isEnabled = false
                this.progressPaint.color = getColor(R.color.gray)
                this.backgroundPaint.color = getColor(R.color.light_gray)
                this.getProgress()
            }
            iosPbVolume.apply {
                this.isEnabled = false
                this.progressPaint.color = getColor(R.color.gray)
                this.backgroundPaint.color = getColor(R.color.light_gray)
                this.getProgress()
            }
            swBoot.apply {
                this.isEnabled = false
                this.setCheckedNoEvent(false)
            }
            swBoot.isChecked = false
            swDND.apply {
                this.isEnabled = false
                this.setCheckedNoEvent(false)
            }
        }
        btnChargingAlert.setOnClickListener {
            if (SharedPrefs.getBoolean(this@MainActivity, "isAlertEnabled")) {
                SharedPrefs.setBoolean(this@MainActivity, "isAlertEnabled", false)
                btnChargingAlert.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.custom_ripple_confirm)
                btnChargingAlert.text = resources.getString(R.string.enable_charging_alert)
                iosPbChargeThreshold.apply {
                    this.isEnabled = false
                    this.progressPaint.color = getColor(R.color.gray)
                    this.backgroundPaint.color = getColor(R.color.light_gray)
                    this.getProgress()
                }
                iosPbDischargeThreshold.apply {
                    this.isEnabled = false
                    this.progressPaint.color = getColor(R.color.gray)
                    this.backgroundPaint.color = getColor(R.color.light_gray)
                    this.getProgress()
                }
                iosPbVolume.apply {
                    this.isEnabled = false
                    this.progressPaint.color = getColor(R.color.gray)
                    this.backgroundPaint.color = getColor(R.color.light_gray)
                    this.getProgress()
                }
                swBoot.apply {
                    this.isEnabled = false
                    this.setCheckedNoEvent(false)
                }
                swBoot.isChecked = false
                swDND.apply {
                    this.isEnabled = false
                    this.setCheckedNoEvent(false)
                }
            } else {
                SharedPrefs.setBoolean(this@MainActivity, "isAlertEnabled", true)
                btnChargingAlert.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.custom_ripple_reject)
                btnChargingAlert.text = resources.getString(R.string.disable_charging_alert)
                iosPbChargeThreshold.apply {
                    this.isEnabled = true
                    this.progressPaint.color = getColor(R.color.Green)
                    this.backgroundPaint.color = getColor(R.color.LightGreen)
                    this.getProgress()
                }
                iosPbDischargeThreshold.apply {
                    this.isEnabled = true
                    this.progressPaint.color = getColor(R.color.Green)
                    this.backgroundPaint.color = getColor(R.color.LightGreen)
                    this.getProgress()
                }
                iosPbVolume.apply {
                    this.isEnabled = true
                    this.progressPaint.color = getColor(R.color.Green)
                    this.backgroundPaint.color = getColor(R.color.LightGreen)
                    this.getProgress()
                }
                swBoot.apply {
                    this.isEnabled = true
                    this.setCheckedNoEvent(SharedPrefs.getBoolean(this@MainActivity, "bootFlag"))
                }
                swDND.apply {
                    this.isEnabled = true
                    this.setCheckedNoEvent(SharedPrefs.getBoolean(this@MainActivity, "bypassDND"))
                }
            }
        }

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI)
        cancelTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
                mediaPlayer.release()
                mediaPlayer =
                    MediaPlayer.create(applicationContext, Settings.System.DEFAULT_ALARM_ALERT_URI)
            }
        }

        iosPbChargeThreshold.setProgress(SharedPrefs.getInt(this@MainActivity, "chargingLimit"))
        iosPbChargeThreshold.setOnProgressChangeListener { _, progress, _, _, actionUp ->
            if (actionUp) if (SharedPrefs.getInt(
                    this@MainActivity, "disChargingLimit"
                ) < progress
            ) SharedPrefs.setInt(this@MainActivity, "chargingLimit", progress)
            else {
                ToastMaker(applicationContext, getString(R.string.lowerAlert), true).sh()
                iosPbChargeThreshold.setProgress(iosPbDischargeThreshold.getProgress() + 1)
                SharedPrefs.setInt(
                    this@MainActivity, "chargingLimit", iosPbDischargeThreshold.getProgress() + 1
                )
            }
        }

        iosPbDischargeThreshold.setProgress(
            SharedPrefs.getInt(
                this@MainActivity, "disChargingLimit"
            )
        )
        iosPbDischargeThreshold.setOnProgressChangeListener { _, progress, _, _, actionUp ->
            if (actionUp) if (SharedPrefs.getInt(
                    this@MainActivity, "chargingLimit"
                ) > progress
            ) SharedPrefs.setInt(this@MainActivity, "disChargingLimit", progress)
            else {
                ToastMaker(applicationContext, getString(R.string.lowerAlert), true).sh()
                iosPbDischargeThreshold.setProgress(iosPbChargeThreshold.getProgress() - 1)
                SharedPrefs.setInt(
                    this@MainActivity, "disChargingLimit", iosPbChargeThreshold.getProgress() - 1
                )
            }
        }
        iosPbVolume.maxProgress = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 10
        iosPbVolume.minProgress = 0
        iosPbVolume.setProgress(SharedPrefs.getInt(this@MainActivity, "volume") * 10)
        iosPbVolume.setOnProgressChangeListener { _, progress, _, _, actionUp ->
            if (actionUp) {
                SharedPrefs.setInt(this@MainActivity, "volume", (progress / 10F).roundToInt())
                if (!mediaPlayer.isPlaying) {
//                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    mediaPlayer = MediaPlayer.create(
                        applicationContext, Settings.System.DEFAULT_ALARM_ALERT_URI
                    )
                    mediaPlayer.setVolume(
                        SharedPrefs.getInt(this@MainActivity, "volume")
                            .toFloat() / iosPbVolume.maxProgress,
                        SharedPrefs.getInt(this@MainActivity, "volume")
                            .toFloat() / iosPbVolume.maxProgress
                    )
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                        0
                    )
                    mediaPlayer.start()
                    cancelTimer.start()
                } else {
//                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    cancelTimer.cancel()
                    mediaPlayer.release()
                    mediaPlayer = MediaPlayer.create(
                        applicationContext, Settings.System.DEFAULT_ALARM_ALERT_URI
                    )
                    mediaPlayer.setVolume(
                        SharedPrefs.getInt(this@MainActivity, "volume")
                            .toFloat() / iosPbVolume.maxProgress,
                        SharedPrefs.getInt(this@MainActivity, "volume")
                            .toFloat() / iosPbVolume.maxProgress
                    )
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                        0
                    )
                    mediaPlayer.start()
                    cancelTimer.start()
                }
            }
        }

        uiAndServiceController = UiAndServiceController(this, mainActivityBinding)
        uiAndServiceController.readData()
    }

    override fun onResume() {
        super.onResume()
        ServiceStateChanger(this).actionOnService(ServiceActions.START)
        reloadUIAndVars()
    }

    override fun onDestroy() {
        cancelTimer.cancel()
        mediaPlayer.release()
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        uiAndServiceController.unreadData()
        super.onDestroy()
    }

    override fun onButtonClicked(result: Boolean) {
//        SharedPrefs.setBoolean("isDeviceRotated", false, this@MainActivity)
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
        } else if (questionDialog!!.tag == batteryOptimizationTag && !result) initTour()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_REQUEST_CODE) batteryOptimizationRequest()
        else if (requestCode == BATTERY_OPTIMIZATION_REQUEST_CODE) {
            SharedPrefs.setBoolean(
                this@MainActivity, "isBatteryOptimizationAsked", true
            )
            initTour()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun reloadUIAndVars() {
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        val batteryInfoModel = BatteryInfoModel()
        mainActivityBinding.tvBatPercent.text =
            "${getString(R.string.batPercent)}${batteryInfoModel.getBatPercentage()}"

        mainActivityBinding.tvBatVoltage.text =
            "${getString(R.string.batVoltage)}${batteryInfoModel.getBatVoltage()}"

        mainActivityBinding.tvBatHealth.text =
            "${getString(R.string.batHealth)}${batteryInfoModel.getBatHealth()}"

        mainActivityBinding.tvBatType.text =
            "${getString(R.string.batType)}${batteryInfoModel.getBatType()}"

        mainActivityBinding.tvBatTemp.text =
            "${getString(R.string.batTemp)}${batteryInfoModel.getBatTemp()}"

        mainActivityBinding.tvBatChargingStat.text =
            "${getString(R.string.batCharging)}${batteryInfoModel.getBatChargingType()}"
    }
}