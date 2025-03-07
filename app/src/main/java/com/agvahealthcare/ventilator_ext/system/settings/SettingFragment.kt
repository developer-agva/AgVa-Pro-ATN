package com.agvahealthcare.ventilator_ext.system.settings

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.alarm.limit_one.EncoderValue
import com.agvahealthcare.ventilator_ext.alarm.limit_one.KnobParameterModel
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.callback.OnKnobPressListener
import com.agvahealthcare.ventilator_ext.callback.OnLimitChangeListener
import com.agvahealthcare.ventilator_ext.callback.OnLoudnessAdjustmentListener
import com.agvahealthcare.ventilator_ext.databinding.FragmentSettingsBinding
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import com.agvahealthcare.ventilator_ext.utility.*
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.github.angads25.toggle.model.ToggleableView
import com.github.angads25.toggle.widget.LabeledSwitch
import com.scichart.drawing.utility.ColorUtil


class SettingFragment : Fragment(), OnKnobPressListener, onDropDownSelectionListener,
    OnDismissDialogListener,
    OnLimitChangeListener,
    OnToggledListener {
    private lateinit var binding: FragmentSettingsBinding
    var customProgressDialog: KnobDialog? = null
    private var prefManager: PreferenceManager? = null
    var onLoudnessAdjustmentListener: OnLoudnessAdjustmentListener? = null
    var isViewClicked: Boolean = false
    private var mAdapter: CommonDropDownAdapter? = null
    private var commonList = ArrayList<CommonItemData>()
    var clickedTile: Configs.GraphType_Change? = null


    companion object {
        const val TAG = "SettingFragment"
        fun newInstance(
            onLoudnessAdjustmentListener: OnLoudnessAdjustmentListener?,
        ): SettingFragment {
            val args = Bundle()
            val fragment = SettingFragment()
            fragment.arguments = args
            fragment.onLoudnessAdjustmentListener = onLoudnessAdjustmentListener
            return fragment
        }
    }


    // logic knob highlight starts here

    fun handleClick(highlightedIndex: Int) {
        clearPreviousConstraints()

        when (highlightedIndex) {

            0 -> binding.includeButtonLoudness.buttonView.callOnClick()
            1 -> binding.includeButtonChange.buttonView.callOnClick()
            2 -> binding.includeColorChange.buttonView.callOnClick()
            3 -> if (binding.layoutPanelLoudness.isVisible) binding.progressBarLoudness.paramProgressBar.callOnClick() else if (binding.layoutPanelTubeComp.isVisible) binding.toggleKnob.isOn =
                !(binding.toggleKnob as LabeledSwitch).isOn else binding.pressureLayout.callOnClick()

            4 -> if (binding.layoutPanelLoudness.isVisible) binding.includeButtonTest.buttonView.callOnClick() else if (binding.layoutPanelTubeComp.isVisible) binding.toggleIETile.isOn =
                !(binding.toggleIETile as LabeledSwitch).isOn else binding.volumeLayout.callOnClick()

            5 -> binding.flowLayout.callOnClick()
        }
    }

    fun highlightAdapterPosition(highlightedIndex: Int, data: String?) {
        clearPreviousConstraints()
        getViewForFocus(highlightedIndex, data)?.let {

            if (highlightedIndex in 0..2) changeConstraintsOfFocusLayout(
                it,
                binding.focusLayoutSettings,
                binding.mainViewPanelSettings
            )
            else {
                if (binding.layoutPanelLoudness.isVisible) changeConstraintsOfFocusLayout(
                    it,
                    binding.focusLayoutLoudness,
                    binding.layoutPanelLoudness
                ) else if (binding.layoutPanelTubeComp.isVisible) changeConstraintsOfFocusLayout(
                    it,
                    binding.focusLayoutSwitch,
                    binding.switchLayout
                ) else changeConstraintsOfFocusLayout(
                    it,
                    binding.focusLayoutColorChange,
                    binding.layoutPanelColorChange
                )
            }
        } ?: run {
            clearPreviousConstraints()
        }
    }

    fun clearPreviousConstraints() {
        try {
            var constraintSet = ConstraintSet()
            constraintSet.clone(binding.mainViewPanelSettings)
            constraintSet.clear(binding.focusLayoutSettings.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutSettings.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutSettings.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutSettings.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.mainViewPanelSettings)

            constraintSet = ConstraintSet()
            constraintSet.clone(binding.layoutPanelLoudness)
            constraintSet.clear(binding.focusLayoutLoudness.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutLoudness.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutLoudness.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutLoudness.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.layoutPanelLoudness)

            constraintSet = ConstraintSet()
            constraintSet.clone(binding.switchLayout)
            constraintSet.clear(binding.focusLayoutSwitch.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutSwitch.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutSwitch.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutSwitch.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.switchLayout)

            constraintSet = ConstraintSet()
            constraintSet.clone(binding.layoutPanelColorChange)
            constraintSet.clear(binding.focusLayoutColorChange.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutColorChange.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutColorChange.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutColorChange.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.layoutPanelColorChange)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(
        view: View,
        focusView: View,
        mainView: ConstraintLayout
    ) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainView)
        constraintSet.connect(
            focusView.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusView.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusView.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusView.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainView)
    }

    //
    private fun getViewForFocus(highlightedIndex: Int, data: String?): View? {

        data?.let {

            return when (highlightedIndex) {

                0 -> binding.includeButtonLoudness.root
                1 -> binding.includeButtonChange.root
                2 -> binding.includeColorChange.root
                3 -> if (binding.layoutPanelLoudness.isVisible) binding.progressBarLoudness.root else if (binding.layoutPanelTubeComp.isVisible) binding.toggleKnob else binding.pressureLayout
                4 -> if (binding.layoutPanelLoudness.isVisible) binding.includeButtonTest.root else if (binding.layoutPanelTubeComp.isVisible) binding.toggleIETile else binding.volumeLayout
                5 -> binding.flowLayout

                else -> null
            }
        } ?: run {
            return null
        }
    }
    // logic knob highlight ends here

    private fun setupDropDownAdapter(recyclerView: RecyclerView) {

        mAdapter = CommonDropDownAdapter(requireContext(), commonList, this@SettingFragment)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }

    override fun onItemSelect(text: String, colorInt: Int) {

        binding.colorRecyclerView.visibility = View.GONE
        mAdapter = null

        when (clickedTile) {

            Configs.GraphType_Change.TYPE_PRESSURE -> {
                binding.cbLayoutPressure.setBackgroundColor(colorInt)
                binding.pressureText.text = text
                prefManager?.setCurrentGraphColor("PRESSURE", colorInt)
            }

            Configs.GraphType_Change.TYPE_VOLUME -> {
                binding.cbLayoutVolume.setBackgroundColor(colorInt)
                binding.volumeText.text = text
                prefManager?.setCurrentGraphColor("VOLUME", colorInt)
            }

            Configs.GraphType_Change.TYPE_FLOW -> {
                binding.cbLayoutFlow.setBackgroundColor(colorInt)
                binding.flowText.text = text
                prefManager?.setCurrentGraphColor("FLOW", colorInt)
            }
        }

        clickedTile = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)
        binding.root.setOnClickListener {
            binding.colorRecyclerView.visibility = View.GONE
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(requireContext())

        commonList.clear()
        commonList.add(CommonItemData(ColorUtil.White, "WHITE"))
        commonList.add(CommonItemData(ColorUtil.Wheat, "WHEAT"))
        commonList.add(CommonItemData(ColorUtil.argb(255, 255, 204, 0), "AMBER"))
        commonList.add(CommonItemData(ColorUtil.argb(255, 0, 255, 255), "BLUE"))
        commonList.add(CommonItemData(ColorUtil.argb(255, 240, 148, 15), "BROWN"))
        commonList.add(CommonItemData(ColorUtil.argb(255, 51, 153, 102), "GREEN"))

        binding.includeButtonChange.buttonView.setTextColor(resources.getColor(R.color.white))
        binding.includeColorChange.buttonView.setTextColor(resources.getColor(R.color.white))
        binding.includeButtonLoudness.buttonView.setTextColor(resources.getColor(R.color.white))
        binding.includeButtonTest.buttonView.setTextColor(resources.getColor(R.color.white))

        setDataViaPreference()
        setUpLoudness()
        setOnClickListener()
    }

    private fun setDataViaPreference() {
        prefManager?.apply {
            binding.progressBarLoudness.paramProgressBar.maxProgress = VOLUME_MAX_VALUE.toDouble()

            binding.progressBarLoudness.paramProgressBar.setCurrentProgress(
                readVolume().toInt().toDouble()
            )

            binding.progressBarLoudness.textView.text = "" + readVolume().toInt()

            binding.cbLayoutFlow.setBackgroundColor(readCurrentGraphColor("FLOW"))
            binding.cbLayoutPressure.setBackgroundColor(readCurrentGraphColor("PRESSURE"))
            binding.cbLayoutVolume.setBackgroundColor(readCurrentGraphColor("VOLUME"))

            binding.pressureText.text =
                commonList.filter { it.colorInt == readCurrentGraphColor("PRESSURE") }[0].text
            binding.volumeText.text =
                commonList.filter { it.colorInt == readCurrentGraphColor("VOLUME") }[0].text
            binding.flowText.text =
                commonList.filter { it.colorInt == readCurrentGraphColor("FLOW") }[0].text

//            toggle_pressure.isOn = readPressureFilledStatus()
//            toggle_volume.isOn = readVolumeilledStatus()
//            toggle_flow.isOn = readflowFilledStatus()

        }
    }

    private fun setOnClickListener() {

        binding.includeButtonLoudness.buttonView.text = getString(R.string.hint_loudness)
        binding.includeButtonApply.buttonView.text = getString(R.string.hint_apply)
        binding.includeButtonAutomatic.buttonView.text = getString(R.string.hint_automatic)
        binding.includeButtonTest.buttonView.text = getString(R.string.hint_test)
        binding.includeButtonChange.buttonView.text = getString(R.string.hint_change)
        binding.includeColorChange.buttonView.text = "Graph Color"


        binding.includeButtonTest.buttonView.setBackgroundResource(R.drawable.background_dark_grey)
        binding.includeButtonAutomatic.buttonView.setBackgroundResource(R.drawable.background_dark_grey)
        binding.includeButtonApply.buttonView.setBackgroundResource(R.drawable.background_dark_grey)

        binding.includeButtonTest.buttonView.setPadding(50, 0, 50, 0)


        binding.pressureLayout.setOnClickListener {

            clickedTile = Configs.GraphType_Change.TYPE_PRESSURE
            binding.colorRecyclerView.visibility = View.VISIBLE
            changeConstraintsOfLayout(it)
            setupDropDownAdapter(binding.colorRecyclerView)
        }

//        toggle_pressure.setOnToggledListener { toggleableView, isOn ->
//            if(isOn){
//                prefManager?.setPressureFilledStatus(true)
//            }else{
//                prefManager?.setPressureFilledStatus(false)
//            }
//        }

        binding.volumeLayout.setOnClickListener {
            clickedTile = Configs.GraphType_Change.TYPE_VOLUME
            binding.colorRecyclerView.visibility = View.VISIBLE
            changeConstraintsOfLayout(it)
            setupDropDownAdapter(binding.colorRecyclerView)
        }

//        toggle_volume.setOnToggledListener { toggleableView, isOn ->
//            if(isOn){
//                prefManager?.setVolumeFilledStatus(true)
//            }else{
//                prefManager?.setVolumeFilledStatus(false)
//            }
//        }


        binding.flowLayout.setOnClickListener {
            clickedTile = Configs.GraphType_Change.TYPE_FLOW
            binding.colorRecyclerView.visibility = View.VISIBLE
            changeConstraintsOfLayout(it)
            setupDropDownAdapter(binding.colorRecyclerView)
        }


//        toggle_flow.setOnToggledListener { toggleableView, isOn ->
//            if(isOn){
//                prefManager?.setFlowFilledStatus(true)
//            }else{
//                prefManager?.setFlowFilledStatus(false)
//            }
//        }


        binding.includeButtonLoudness.buttonView.setOnClickListener {
            setUpLoudness()
        }

        binding.toggleKnob.setOnToggledListener { _, isOn ->
            if (prefManager?.readKnobStatus() == false) {
                prefManager?.setKnobStatus(true)
//                    ToastFactory.custom(context,"Knob status is " + prefManager?.readKnobStatus().toString())
            } else {
                prefManager?.setKnobStatus(false)
//                    ToastFactory.custom(context,"Knob status is " + prefManager?.readKnobStatus().toString())
            }
        }

        binding.toggleIETile.setOnToggledListener { _, isOn ->
            if (prefManager?.readIETileStatus() == false) {
                prefManager?.setIETileStatus(true)
            } else {
                prefManager?.setIETileStatus(false)
            }
        }

        binding.includeButtonChange.buttonView.setOnClickListener {
            setUpChange()
        }

        binding.includeColorChange.buttonView.setOnClickListener {
            setUpColor()
        }

        binding.progressBarLoudness.paramProgressBar.setOnClickListener {

            it.let {
                binding.progressBarLoudness.paramProgressBar.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.progresscircle_with_selection_yellow
                    )
            }

            val encoder = EncoderValue(VOLUME_MIN_VALUE.toFloat(), VOLUME_MAX_VALUE.toFloat(), 2.0f)
            prefManager?.readVolume()?.let { it1 ->
                KnobParameterModel(
                    Configs.LBL_Volume_KEY,
                    Configs.LBL_Volume_KEY,
                    1,
                    it1,
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

                    customProgressDialog?.let { dialog ->
                        dialog.show(childFragmentManager, SettingFragment.TAG)
                        dialog.startTimeoutWithDebounce()
                    }
                }
            }
        }
        //Need to be replaced and the

        binding.includeButtonTest.buttonView.setOnClickListener {

            if (!isViewClicked) {
                isViewClicked = true;
                onLoudnessAdjustmentListener?.onCheckLoudness()
                startTimer()
            } else {

            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.toggleKnob.isOn = prefManager?.readKnobStatus() ?: false
        binding.toggleIETile.isOn = prefManager?.readIETileStatus() ?: false
    }


    private fun startTimer() {

        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                isViewClicked = false
            }
        }.start()


    }

    private fun setPaddingData() {

        binding.includeButtonLoudness.buttonView.setPadding(15, 10, 15, 10)
        binding.includeColorChange.buttonView.setPadding(15, 10, 15, 10)
        binding.includeButtonChange.buttonView.setPadding(15, 10, 15, 10)

        binding.includeButtonTest.buttonView.setPadding(50, 0, 50, 0)
        binding.includeButtonAutomatic.buttonView.setPadding(35, 0, 35, 0)
        binding.includeButtonApply.buttonView.setPadding(55, 10, 55, 10)

    }

    private fun setUpChange() {

        (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 3

        binding.includeButtonLoudness.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        binding.includeColorChange.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        binding.includeButtonChange.buttonView.setBackgroundResource(R.drawable.background_green_border)

        binding.layoutPanelLoudness.visibility = View.GONE
        binding.layoutPanelTubeComp.visibility = View.VISIBLE
        binding.layoutPanelColorChange.visibility = View.GONE


        setPaddingData()
    }

    private fun setUpColor() {

        (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 5

        binding.includeColorChange.buttonView.setBackgroundResource(R.drawable.background_green_border)
        binding.includeButtonChange.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        binding.includeButtonLoudness.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        binding.layoutPanelLoudness.visibility = View.GONE
        binding.layoutPanelTubeComp.visibility = View.GONE

        binding.layoutPanelColorChange.visibility = View.VISIBLE

        setPaddingData()
    }

    private fun setUpLoudness() {

        (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 4
        binding.includeButtonLoudness.buttonView.setBackgroundResource(R.drawable.background_green_border)
        binding.includeButtonChange.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        binding.includeColorChange.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)


        binding.layoutPanelLoudness.visibility = View.VISIBLE
        binding.layoutPanelTubeComp.visibility = View.GONE
        binding.layoutPanelColorChange.visibility = View.GONE

        setPaddingData()
    }


    fun updateKnobSetting(data: String) {

        if (clickedTile != null) {
            when (data) {
                Configs.PREFIX_PLUS -> mAdapter?.forwardIndex()
                Configs.PREFIX_MINUS -> mAdapter?.backwardIndex()
                Configs.PREFIX_AND -> mAdapter?.clickOnSelectedIndex()
            }
        } else {
            customProgressDialog?.updateWithTimeoutDebounce(data)
        }
    }

    override fun onKnobPress(previousValue: Float, newValue: Float) {
        if (binding.layoutPanelLoudness.isVisible) {
            Log.i("LOUDNESSCHECK", "Set value = $newValue")
            binding.progressBarLoudness.paramProgressBar.maxProgress = VOLUME_MAX_VALUE.toDouble()
            binding.progressBarLoudness.paramProgressBar.setCurrentProgress(
                newValue.toInt().toDouble()
            )
            binding.progressBarLoudness.textView.setText("" + newValue.toInt())
            prefManager?.setVolume(newValue)
            onLoudnessAdjustmentListener?.onCheckLoudness()
            startTimer()

            binding.progressBarLoudness.paramProgressBar.background =
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.progresscircle
                )

        } else {
        }

        // change here 13 feb
        customProgressDialog?.takeIf { it.isVisible }?.apply {
            this.dismiss()

        }
        customProgressDialog = null

    }


    override fun handleDialogClose() {

        binding.progressBarLoudness.paramProgressBar.background =
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.progresscircle
                )


        prefManager?.apply {
            binding.progressBarLoudness.paramProgressBar.maxProgress = VOLUME_MAX_VALUE.toDouble()
            binding.progressBarLoudness.paramProgressBar.setCurrentProgress(
                readVolume().toInt().toDouble()
            )
            binding.progressBarLoudness.textView.setText("" + readVolume().toInt())
        }

        customProgressDialog?.takeIf { it.isVisible }?.dismiss()
        customProgressDialog = null
    }


    override fun onLimitChange(previousValue: Float, newValue: Float) {
        if (binding.layoutPanelLoudness.isVisible) {
            view.let {
                (binding.progressBarLoudness.paramProgressBar as? CircularProgressIndicator)?.apply {
                    this.setCurrentProgress(newValue.toInt().toDouble())
                }

                (binding.progressBarLoudness.textView as? TextView)?.apply {
                    this.text = newValue.toInt().toString()
                }
            }
        } else {

        }


    }

    override fun onSwitched(toggleableView: ToggleableView?, isOn: Boolean) {
        if (isOn) {

        } else { //progressBarTubeComp?.isEnabled = false


        }
    }

    private fun changeConstraintsOfLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.layoutPanelColorChange)
        constraintSet.connect(
            binding.colorRecyclerView.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.colorRecyclerView.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )

        constraintSet.connect(
            binding.colorRecyclerView.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.layoutPanelColorChange)
    }
}
