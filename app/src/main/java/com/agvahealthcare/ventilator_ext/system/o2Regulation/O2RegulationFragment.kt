package com.agvahealthcare.ventilator_ext.system.o2Regulation

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C.USAGE_ALARM
import androidx.media3.common.MediaItem.fromUri
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.agvahealthcare.ventilator_ext.MainActivityViewModel
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.alarm.limit_one.EncoderValue
import com.agvahealthcare.ventilator_ext.alarm.limit_one.KnobParameterModel
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.callback.OnKnobPressListener
import com.agvahealthcare.ventilator_ext.callback.OnLimitChangeListener
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import com.agvahealthcare.ventilator_ext.system.settings.SettingFragment
import com.agvahealthcare.ventilator_ext.utility.KnobDialog
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_o2_regulation.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class O2RegulationFragment(private var communicationService: CommunicationService?) : Fragment(),
    OnLimitChangeListener, OnKnobPressListener, OnDismissDialogListener,
    View.OnClickListener {
    private var mO2RegulationCheckViewModel: O2RegulationCheckViewModel? = null
    var clickHold = false
    var clickCalibrate = false
    var clickStateCheck = false
    private var delPressure = 0.0f
    private var pressureAverage = ArrayList<Float>()
    private var exoPlayer: ExoPlayer? = null
    private var userLimitForCalibrate = 2.4f
    private var holdCount = 0
    var customProgressDialog: KnobDialog? = null
    private var stepofCheckPoint = 10
    private var minCheckPoint = 20
    private var maxCheckPoint = 300
    private var maxLimit = 0.0f
    private var minLimit = 0.0f
    private var defaultLimit = 2.4f
    private var step = 0.1f
    private var isCalibrating = false

    private var checkPointDefault = "100"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_o2_regulation, container, false)
    }

    // logic knob highlight starts here

    fun handleClick(highlightedIndex: Int) {
        clearPreviousConstraints()

        when (highlightedIndex) {

            0 -> includeButtonRelease.buttonView.callOnClick()
            1 -> includeButtonCalibrate.buttonView.callOnClick()
            2 -> includeButtonHold.buttonView.callOnClick()
            3 -> includeButtonCheckPoint.buttonView.callOnClick()
            4 -> includeButtonCheckPointStart.buttonView.callOnClick()

        }
    }

    fun highlightAdapterPosition(highlightedIndex: Int, data: String?) {

        getViewForFocus(highlightedIndex, data)?.let {
            changeConstraintsOfFocusLayout(it)
        } ?: run {
            clearPreviousConstraints()
        }
    }

    fun clearPreviousConstraints() {
        try {
            val constraintSet = ConstraintSet()
            constraintSet.clone(mainViewPanelO2)
            constraintSet.clear(focusLayoutO2.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutO2.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutO2.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutO2.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelO2)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainViewPanelO2)
        constraintSet.connect(
            focusLayoutO2.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutO2.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutO2.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutO2.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainViewPanelO2)
    }

    //
    private fun getViewForFocus(highlightedIndex: Int, data: String?): View? {

        data?.let {

            return when (highlightedIndex) {

                0 -> includeButtonRelease
                1 -> includeButtonCalibrate
                2 -> includeButtonHold
                3 -> includeButtonCheckPoint
                4 -> includeButtonCheckPointStart

                else -> null
            }
        } ?: run {
            return null
        }
    }
    // logic knob highlight ends here

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mO2RegulationCheckViewModel =
            ViewModelProvider(requireActivity())[O2RegulationCheckViewModel::class.java]

        CoroutineScope(Dispatchers.IO).launch {
            delay(2000L)
            communicationService?.takeIf { it.isPortsConnected }
                ?.apply { send(getString(R.string.o2Regulation_start_cmd)) }
        }

        maxLimit = 2.8f
        minLimit = 2.2f

        observeData()
        setupView()
        setOnClickListener()
        defaultValueSetOnView()

    }

    fun updateValueOnKnobChange(data: String?){
        data?.let {
            customProgressDialog?.updateWithTimeoutDebounce(it)
        }
    }

    private fun startStateCheck() {
        checkPointDefault = includeButtonCheckPoint.buttonView.text.toString()
        includeButtonCheckPoint.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        includeButtonCheckPoint.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.black
            )
        )
        clickStateCheck = false
    }

    private fun startCalibration() {

        userLimitForCalibrate = txtDelPressureHold.text.toString().toFloat()
        isCalibrating = true
        onPlayAlarm(Configs.URI_BEEP)
        hideHoldLayout()
    }

    private fun observeData() {

        mO2RegulationCheckViewModel?.o2PressureData?.observe(viewLifecycleOwner) {
            val pressure = it.split(",")[0]
            val duty = it.split(",")[1]

            if (duty.toInt() > 0) {
                includeButtonRelease.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includeButtonRelease.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }

            txtO2PValue1.text = pressure
            txtCheckpointValue.text = duty

            if (clickHold) {
                Log.i(
                    "cheadaw",
                    "${userLimitForCalibrate - pressure.toFloat()} , $userLimitForCalibrate , ${pressure.toFloat()}"
                )
                if (holdCount <= 120) {
                    pressureAverage.add(userLimitForCalibrate - pressure.toFloat())
                    delPressure = pressureAverage.sum() / pressureAverage.size
                    txtDelPressureHold.text = String.format("%.1f", delPressure)
                    ++holdCount
                } else {
                    includeButtonHold.buttonView.callOnClick()
                }
            } else if (clickCalibrate) {
                if (pressure.toFloat() in (userLimitForCalibrate - 0.1f)..(userLimitForCalibrate + 0.1f)) exoPlayer?.setPlaybackSpeed(
                    1.0f
                )
                else if (pressure.toFloat() < userLimitForCalibrate) exoPlayer?.setPlaybackSpeed(
                    0.3f
                )
                else exoPlayer?.setPlaybackSpeed(2.0f)
            }
        }
    }

    private fun onPlayAlarm(uri: Uri) {
        exoPlayer?.stop()

        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        val mediaItem = fromUri(uri)

        val attrib: AudioAttributes = AudioAttributes.Builder()
            .setUsage(USAGE_ALARM)
            .build()
        exoPlayer?.apply {
            setMediaItem(mediaItem)
            setAudioAttributes(attrib, false)
            repeatMode = Player.REPEAT_MODE_ALL
            prepare()
            setPlaybackSpeed(1.0f)
            play()
        }
    }

    override fun onPause() {

        clickHold = false
        clickCalibrate = false
        pressureAverage.clear()
        holdCount = 0
        isCalibrating = false
        delPressure = 0.0f

        try {
            exoPlayer?.stop()
            exoPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // send command to stop data
        communicationService?.takeIf { it.isPortsConnected }?.apply {
            send(getString(R.string.o2Regulation_stop_cmd))
        }
        super.onPause()
    }


    private fun defaultValueSetOnView() {
        txtO2PValue1.text = "-"
        txtO2DelPValue.text = "-"
    }

    private fun setupView() {
        includeButtonRelease.buttonView.text = "Release"
        includeButtonCalibrate.buttonView.text = "Calibrate"
        includeButtonHold.buttonView.text = "Hold"
        includeButtonCheckPointStart.buttonView.text = "Start"
        includeButtonCheckPoint.buttonView.text = checkPointDefault
        includeButtonRelease.buttonView.setBackgroundResource(R.color.dolphin_grey)
        includeButtonRelease.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
    }

    private fun showCalibrateScreen() {
        holdLayout.visibility = View.VISIBLE
        txtDelPressureHold.text = defaultLimit.toString()
        txtTitle.text = "Limits for calibration"
    }

    private fun hideHoldLayout() {
        holdLayout.visibility = View.GONE
        txtDelPressureHold.text = "-"
    }

    private fun showHoldScreen() {
        holdLayout.visibility = View.VISIBLE
        txtDelPressureHold.text = String.format("%.1f", delPressure)
        txtTitle.text = "ΔPRESSURE"
    }

    private fun setOnClickListener() {
        includeButtonRelease.buttonView.setOnClickListener(this)
        includeButtonCalibrate.buttonView.setOnClickListener(this)
        includeButtonHold.buttonView.setOnClickListener(this)
        includeButtonCheckPoint.buttonView.setOnClickListener(this)
        includeButtonCheckPointStart.buttonView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {

            includeButtonCheckPoint.buttonView -> {
                clickStateCheck = true

                includeButtonCheckPoint.buttonView.setBackgroundResource(R.color.racing_green)
                includeButtonCheckPoint.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )

                val encoder = EncoderValue(minCheckPoint.toFloat(), maxCheckPoint.toFloat(), stepofCheckPoint.toFloat())

                KnobParameterModel(
                    "CheckPoint",
                    "CheckPoint",
                    1,
                    includeButtonCheckPoint.buttonView.text.toString().toFloat(),
                    ""
                ).also {
                    customProgressDialog = KnobDialog.newInstance(
                        onKnobPressListener = this,
                        onTimeoutListener = this,
                        onCloseListener = this,
                        parameterModel = it,
                        encoderValue = encoder,
                        onLimitChangeListener = this
                    )
                }

                customProgressDialog?.let { dialog ->
                    dialog.show(childFragmentManager, SettingFragment.TAG)
                    dialog.startTimeoutWithDebounce()
                }
            }

            includeButtonCheckPointStart.buttonView -> {

                includeButtonCheckPointStart.buttonView.setBackgroundResource(R.color.racing_green)
                includeButtonCheckPointStart.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )

                if (checkPointDefault.toInt() < 100) communicationService?.send("CM+OC0$checkPointDefault")
                else communicationService?.send("CM+OC$checkPointDefault")

            }

            includeButtonRelease.buttonView -> {
                includeButtonRelease.buttonView.setBackgroundResource(R.color.racing_green)
                includeButtonRelease.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.release_cmd))
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    includeButtonRelease.buttonView.setBackgroundResource(R.color.dolphin_grey)
                    includeButtonRelease.buttonView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    )
                }, 500)
            }

            //onClick Calibrate
            includeButtonCalibrate.buttonView -> {

                if (clickCalibrate) {
                    includeButtonCalibrate.buttonView.setBackgroundResource(R.color.dolphin_grey)
                    includeButtonCalibrate.buttonView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    )
                    clickCalibrate = false
                    exoPlayer?.stop()
                    hideHoldLayout()
                    handleDialogClose()
                    isCalibrating = false

                } else {

                    if (clickHold) includeButtonHold.buttonView.callOnClick()

                    includeButtonCalibrate.buttonView.setBackgroundResource(R.color.racing_green)
                    includeButtonCalibrate.buttonView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    isCalibrating = false
                    clickCalibrate = true
                    val encoder = EncoderValue(minLimit, maxLimit, step)

                    KnobParameterModel(
                        "Calibrate",
                        "Calibrate",
                        1,
                        defaultLimit,
                        ""
                    ).also {
                        customProgressDialog = KnobDialog.newInstance(
                            onKnobPressListener = this,
                            onTimeoutListener = this,
                            onCloseListener = this,
                            parameterModel = it,
                            encoderValue = encoder,
                            onLimitChangeListener = this
                        )
                    }

                    customProgressDialog?.let { dialog ->
                        dialog.show(childFragmentManager, SettingFragment.TAG)
                        dialog.startTimeoutWithDebounce()
                    }
                    showCalibrateScreen()
                }
            }

            includeButtonHold.buttonView -> {

                if (clickHold) {
                    txtO2DelPValue.text = String.format("%.1f", delPressure)
                    includeButtonHold.buttonView.setBackgroundResource(R.color.dolphin_grey)
                    includeButtonHold.buttonView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    )
                    hideHoldLayout()
                    clickHold = false
                    pressureAverage.clear()
                    holdCount = 0
                } else {

                    if (clickCalibrate) includeButtonCalibrate.buttonView.callOnClick()

                    includeButtonHold.buttonView.setBackgroundResource(R.color.racing_green)
                    includeButtonHold.buttonView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    showHoldScreen()
                    clickHold = true
                    txtO2DelPValue.text = "Calculating..."
                }
            }
        }
    }

    override fun onKnobPress(previousValue: Float, newValue: Float) {

        if (clickCalibrate) startCalibration()
        else if (clickStateCheck) startStateCheck()
        // change here 13 feb
        customProgressDialog?.takeIf { it.isVisible }?.apply {
            this.dismiss()
        }
        customProgressDialog = null
    }


    override fun handleDialogClose() {
        customProgressDialog?.takeIf { it.isVisible }?.dismiss()
        customProgressDialog = null

        if (clickCalibrate) includeButtonCalibrate.buttonView.callOnClick()
        else if (clickStateCheck) {
            includeButtonCheckPoint.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
            includeButtonCheckPoint.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.black
                )
            )
            clickStateCheck = false
        }

    }

    override fun onLimitChange(previousValue: Float, newValue: Float) {

        if (clickCalibrate) {
            txtDelPressureHold.text = newValue.toString()
        } else if (clickStateCheck) {
            includeButtonCheckPoint.buttonView.text = newValue.toInt().toString()
        }
    }

}