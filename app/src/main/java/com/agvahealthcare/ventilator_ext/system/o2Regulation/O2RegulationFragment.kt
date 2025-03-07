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
import com.agvahealthcare.ventilator_ext.databinding.FragmentO2RegulationBinding
import com.agvahealthcare.ventilator_ext.databinding.FragmentWiFiBinding
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import com.agvahealthcare.ventilator_ext.system.settings.SettingFragment
import com.agvahealthcare.ventilator_ext.utility.KnobDialog
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class O2RegulationFragment(private var communicationService: CommunicationService?) : Fragment(),
    OnLimitChangeListener, OnKnobPressListener, OnDismissDialogListener,
    View.OnClickListener {

    private lateinit var binding:FragmentO2RegulationBinding
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
        binding = FragmentO2RegulationBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    // logic knob highlight starts here

    fun handleClick(highlightedIndex: Int) {
        clearPreviousConstraints()

        when (highlightedIndex) {

            0 -> binding.includeButtonRelease.buttonView.callOnClick()
            1 -> binding.includeButtonCalibrate.buttonView.callOnClick()
            2 -> binding.includeButtonHold.buttonView.callOnClick()
            3 -> binding.includeButtonCheckPoint.buttonView.callOnClick()
            4 -> binding.includeButtonCheckPointStart.buttonView.callOnClick()

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
            constraintSet.clone(binding.mainViewPanelO2)
            constraintSet.clear(binding.focusLayoutO2.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutO2.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutO2.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutO2.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.mainViewPanelO2)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.mainViewPanelO2)
        constraintSet.connect(
            binding.focusLayoutO2.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.focusLayoutO2.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            binding.focusLayoutO2.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            binding.focusLayoutO2.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.mainViewPanelO2)
    }

    //
    private fun getViewForFocus(highlightedIndex: Int, data: String?): View? {

        data?.let {

            return when (highlightedIndex) {

                0 -> binding.includeButtonRelease.root
                1 -> binding.includeButtonCalibrate.root
                2 -> binding.includeButtonHold.root
                3 -> binding.includeButtonCheckPoint.root
                4 -> binding.includeButtonCheckPointStart.root

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
        checkPointDefault = binding.includeButtonCheckPoint.buttonView.text.toString()
        binding.includeButtonCheckPoint.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.includeButtonCheckPoint.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.black
            )
        )
        clickStateCheck = false
    }

    private fun startCalibration() {

        userLimitForCalibrate = binding.txtDelPressureHold.text.toString().toFloat()
        isCalibrating = true
        onPlayAlarm(Configs.URI_BEEP)
        hideHoldLayout()
    }

    private fun observeData() {

        mO2RegulationCheckViewModel?.o2PressureData?.observe(viewLifecycleOwner) {
            val pressure = it.split(",")[0]
            val duty = it.split(",")[1]

            if (duty.toInt() > 0) {
                binding.includeButtonRelease.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includeButtonRelease.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }

            binding.txtO2PValue1.text = pressure
            binding.txtCheckpointValue.text = duty

            if (clickHold) {
                Log.i(
                    "cheadaw",
                    "${userLimitForCalibrate - pressure.toFloat()} , $userLimitForCalibrate , ${pressure.toFloat()}"
                )
                if (holdCount <= 120) {
                    pressureAverage.add(userLimitForCalibrate - pressure.toFloat())
                    delPressure = pressureAverage.sum() / pressureAverage.size
                    binding.txtDelPressureHold.text = String.format("%.1f", delPressure)
                    ++holdCount
                } else {
                    binding.includeButtonHold.buttonView.callOnClick()
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
        binding.txtO2PValue1.text = "-"
        binding.txtO2DelPValue.text = "-"
    }

    private fun setupView() {
        binding.includeButtonRelease.buttonView.text = "Release"
        binding.includeButtonCalibrate.buttonView.text = "Calibrate"
        binding.includeButtonHold.buttonView.text = "Hold"
        binding.includeButtonCheckPointStart.buttonView.text = "Start"
        binding.includeButtonCheckPoint.buttonView.text = checkPointDefault
        binding.includeButtonRelease.buttonView.setBackgroundResource(R.color.dolphin_grey)
        binding.includeButtonRelease.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
    }

    private fun showCalibrateScreen() {
        binding.holdLayout.visibility = View.VISIBLE
        binding.txtDelPressureHold.text = defaultLimit.toString()
        binding.txtTitle.text = "Limits for calibration"
    }

    private fun hideHoldLayout() {
        binding.holdLayout.visibility = View.GONE
        binding.txtDelPressureHold.text = "-"
    }

    private fun showHoldScreen() {
        binding.holdLayout.visibility = View.VISIBLE
        binding.txtDelPressureHold.text = String.format("%.1f", delPressure)
        binding.txtTitle.text = "ΔPRESSURE"
    }

    private fun setOnClickListener() {
        binding.includeButtonRelease.buttonView.setOnClickListener(this)
        binding.includeButtonCalibrate.buttonView.setOnClickListener(this)
        binding.includeButtonHold.buttonView.setOnClickListener(this)
        binding.includeButtonCheckPoint.buttonView.setOnClickListener(this)
        binding.includeButtonCheckPointStart.buttonView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {

            binding.includeButtonCheckPoint.buttonView -> {
                clickStateCheck = true

                binding.includeButtonCheckPoint.buttonView.setBackgroundResource(R.color.racing_green)
                binding.includeButtonCheckPoint.buttonView.setTextColor(
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
                    binding.includeButtonCheckPoint.buttonView.text.toString().toFloat(),
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

            binding.includeButtonCheckPointStart.buttonView -> {

                binding.includeButtonCheckPointStart.buttonView.setBackgroundResource(R.color.racing_green)
                binding.includeButtonCheckPointStart.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )

                if (checkPointDefault.toInt() < 100) communicationService?.send("CM+OC0$checkPointDefault")
                else communicationService?.send("CM+OC$checkPointDefault")

            }

            binding.includeButtonRelease.buttonView -> {
                binding.includeButtonRelease.buttonView.setBackgroundResource(R.color.racing_green)
                binding.includeButtonRelease.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.release_cmd))
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    binding.includeButtonRelease.buttonView.setBackgroundResource(R.color.dolphin_grey)
                    binding.includeButtonRelease.buttonView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    )
                }, 500)
            }

            //onClick Calibrate
            binding.includeButtonCalibrate.buttonView -> {

                if (clickCalibrate) {
                    binding.includeButtonCalibrate.buttonView.setBackgroundResource(R.color.dolphin_grey)
                    binding.includeButtonCalibrate.buttonView.setTextColor(
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

                    if (clickHold) binding.includeButtonHold.buttonView.callOnClick()

                    binding.includeButtonCalibrate.buttonView.setBackgroundResource(R.color.racing_green)
                    binding.includeButtonCalibrate.buttonView.setTextColor(
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

            binding.includeButtonHold.buttonView -> {

                if (clickHold) {
                    binding.txtO2DelPValue.text = String.format("%.1f", delPressure)
                    binding.includeButtonHold.buttonView.setBackgroundResource(R.color.dolphin_grey)
                    binding.includeButtonHold.buttonView.setTextColor(
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

                    if (clickCalibrate) binding.includeButtonCalibrate.buttonView.callOnClick()

                    binding.includeButtonHold.buttonView.setBackgroundResource(R.color.racing_green)
                    binding.includeButtonHold.buttonView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    showHoldScreen()
                    clickHold = true
                    binding.txtO2DelPValue.text = "Calculating..."
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

        if (clickCalibrate) binding.includeButtonCalibrate.buttonView.callOnClick()
        else if (clickStateCheck) {
            binding.includeButtonCheckPoint.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
            binding.includeButtonCheckPoint.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.black
                )
            )
            clickStateCheck = false
        }

    }

    override fun onLimitChange(previousValue: Float, newValue: Float) {

        if (clickCalibrate) {
            binding.txtDelPressureHold.text = newValue.toString()
        } else if (clickStateCheck) {
            binding.includeButtonCheckPoint.buttonView.text = newValue.toInt().toString()
        }
    }

}