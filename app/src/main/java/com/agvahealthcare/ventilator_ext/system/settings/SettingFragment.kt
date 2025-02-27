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
import androidx.constraintlayout.widget.Constraints
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
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import com.agvahealthcare.ventilator_ext.utility.*
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.github.angads25.toggle.model.ToggleableView
import com.github.angads25.toggle.widget.LabeledSwitch
import com.scichart.drawing.utility.ColorUtil
import kotlinx.android.synthetic.main.color_selection.view.*
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.knob_progress_view.view.param_progress_bar


class SettingFragment : Fragment(), OnKnobPressListener, onDropDownSelectionListener,
    OnDismissDialogListener,
    OnLimitChangeListener,
    OnToggledListener {

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

            0 -> includeButtonLoudness.buttonView.callOnClick()
            1 -> includeButtonChange.buttonView.callOnClick()
            2 -> includeColorChange.buttonView.callOnClick()
            3 -> if (layoutPanelLoudness.isVisible) progressBarLoudness.param_progress_bar.callOnClick() else if (layoutPanelTubeComp.isVisible) toggle_knob.isOn =
                !(toggle_knob as LabeledSwitch).isOn else pressureLayout.callOnClick()

            4 -> if (layoutPanelLoudness.isVisible) includeButtonTest.buttonView.callOnClick() else if (layoutPanelTubeComp.isVisible) toggle_iE_tile.isOn =
                !(toggle_iE_tile as LabeledSwitch).isOn else volumeLayout.callOnClick()

            5 -> flowLayout.callOnClick()
        }
    }

    fun highlightAdapterPosition(highlightedIndex: Int, data: String?) {
        clearPreviousConstraints()
        getViewForFocus(highlightedIndex, data)?.let {

            if (highlightedIndex in 0..2) changeConstraintsOfFocusLayout(
                it,
                focusLayoutSettings,
                mainViewPanelSettings
            )
            else {
                if (layoutPanelLoudness.isVisible) changeConstraintsOfFocusLayout(
                    it,
                    focusLayoutLoudness,
                    layoutPanelLoudness
                ) else if (layoutPanelTubeComp.isVisible) changeConstraintsOfFocusLayout(
                    it,
                    focusLayoutSwitch,
                    switchLayout
                ) else changeConstraintsOfFocusLayout(
                    it,
                    focusLayoutColorChange,
                    layoutPanelColorChange
                )
            }
        } ?: run {
            clearPreviousConstraints()
        }
    }

    fun clearPreviousConstraints() {
        try {
            var constraintSet = ConstraintSet()
            constraintSet.clone(mainViewPanelSettings)
            constraintSet.clear(focusLayoutSettings.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutSettings.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutSettings.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutSettings.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelSettings)

            constraintSet = ConstraintSet()
            constraintSet.clone(layoutPanelLoudness)
            constraintSet.clear(focusLayoutLoudness.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutLoudness.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutLoudness.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutLoudness.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(layoutPanelLoudness)

            constraintSet = ConstraintSet()
            constraintSet.clone(switchLayout)
            constraintSet.clear(focusLayoutSwitch.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutSwitch.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutSwitch.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutSwitch.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(switchLayout)

            constraintSet = ConstraintSet()
            constraintSet.clone(layoutPanelColorChange)
            constraintSet.clear(focusLayoutColorChange.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutColorChange.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutColorChange.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutColorChange.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(layoutPanelColorChange)
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

                0 -> includeButtonLoudness
                1 -> includeButtonChange
                2 -> includeColorChange
                3 -> if (layoutPanelLoudness.isVisible) progressBarLoudness else if (layoutPanelTubeComp.isVisible) toggle_knob else pressureLayout
                4 -> if (layoutPanelLoudness.isVisible) includeButtonTest else if (layoutPanelTubeComp.isVisible) toggle_iE_tile else volumeLayout
                5 -> flowLayout

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

        colorRecyclerView.visibility = View.GONE
        mAdapter = null

        when (clickedTile) {

            Configs.GraphType_Change.TYPE_PRESSURE -> {
                cbLayoutPressure.setBackgroundColor(colorInt)
                pressureText.text = text
                prefManager?.setCurrentGraphColor("PRESSURE", colorInt)
            }

            Configs.GraphType_Change.TYPE_VOLUME -> {
                cbLayoutVolume.setBackgroundColor(colorInt)
                volumeText.text = text
                prefManager?.setCurrentGraphColor("VOLUME", colorInt)
            }

            Configs.GraphType_Change.TYPE_FLOW -> {
                cbLayoutFlow.setBackgroundColor(colorInt)
                flowText.text = text
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
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        view.setOnClickListener {
            colorRecyclerView.visibility = View.GONE
        }
        return view
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

        includeButtonChange.buttonView.setTextColor(resources.getColor(R.color.white))
        includeColorChange.buttonView.setTextColor(resources.getColor(R.color.white))
        includeButtonLoudness.buttonView.setTextColor(resources.getColor(R.color.white))
        includeButtonTest.buttonView.setTextColor(resources.getColor(R.color.white))

        setDataViaPreference()
        setUpLoudness()
        setOnClickListener()
    }

    private fun setDataViaPreference() {
        prefManager?.apply {
            progressBarLoudness.param_progress_bar.maxProgress = VOLUME_MAX_VALUE.toDouble()

            progressBarLoudness.param_progress_bar.setCurrentProgress(
                readVolume().toInt().toDouble()
            )

            progressBarLoudness.textView.text = "" + readVolume().toInt()

            cbLayoutFlow.setBackgroundColor(readCurrentGraphColor("FLOW"))
            cbLayoutPressure.setBackgroundColor(readCurrentGraphColor("PRESSURE"))
            cbLayoutVolume.setBackgroundColor(readCurrentGraphColor("VOLUME"))

            pressureText.text =
                commonList.filter { it.colorInt == readCurrentGraphColor("PRESSURE") }[0].text
            volumeText.text =
                commonList.filter { it.colorInt == readCurrentGraphColor("VOLUME") }[0].text
            flowText.text =
                commonList.filter { it.colorInt == readCurrentGraphColor("FLOW") }[0].text

//            toggle_pressure.isOn = readPressureFilledStatus()
//            toggle_volume.isOn = readVolumeilledStatus()
//            toggle_flow.isOn = readflowFilledStatus()

        }
    }

    private fun setOnClickListener() {

        includeButtonLoudness.buttonView.text = getString(R.string.hint_loudness)
        includeButtonApply.buttonView.text = getString(R.string.hint_apply)
        includeButtonAutomatic.buttonView.text = getString(R.string.hint_automatic)
        includeButtonTest.buttonView.text = getString(R.string.hint_test)
        includeButtonChange.buttonView.text = getString(R.string.hint_change)
        includeColorChange.buttonView.text = "Graph Color"


        includeButtonTest.buttonView.setBackgroundResource(R.drawable.background_dark_grey)
        includeButtonAutomatic.buttonView.setBackgroundResource(R.drawable.background_dark_grey)
        includeButtonApply.buttonView.setBackgroundResource(R.drawable.background_dark_grey)

        includeButtonTest.buttonView.setPadding(50, 0, 50, 0)


        pressureLayout.setOnClickListener {

            clickedTile = Configs.GraphType_Change.TYPE_PRESSURE
            colorRecyclerView.visibility = View.VISIBLE
            changeConstraintsOfLayout(it)
            setupDropDownAdapter(colorRecyclerView)
        }

//        toggle_pressure.setOnToggledListener { toggleableView, isOn ->
//            if(isOn){
//                prefManager?.setPressureFilledStatus(true)
//            }else{
//                prefManager?.setPressureFilledStatus(false)
//            }
//        }

        volumeLayout.setOnClickListener {
            clickedTile = Configs.GraphType_Change.TYPE_VOLUME
            colorRecyclerView.visibility = View.VISIBLE
            changeConstraintsOfLayout(it)
            setupDropDownAdapter(colorRecyclerView)
        }

//        toggle_volume.setOnToggledListener { toggleableView, isOn ->
//            if(isOn){
//                prefManager?.setVolumeFilledStatus(true)
//            }else{
//                prefManager?.setVolumeFilledStatus(false)
//            }
//        }


        flowLayout.setOnClickListener {
            clickedTile = Configs.GraphType_Change.TYPE_FLOW
            colorRecyclerView.visibility = View.VISIBLE
            changeConstraintsOfLayout(it)
            setupDropDownAdapter(colorRecyclerView)
        }


//        toggle_flow.setOnToggledListener { toggleableView, isOn ->
//            if(isOn){
//                prefManager?.setFlowFilledStatus(true)
//            }else{
//                prefManager?.setFlowFilledStatus(false)
//            }
//        }


        includeButtonLoudness.buttonView.setOnClickListener {
            setUpLoudness()
        }

        toggle_knob?.setOnToggledListener { _, isOn ->
            if (prefManager?.readKnobStatus() == false) {
                prefManager?.setKnobStatus(true)
//                    ToastFactory.custom(context,"Knob status is " + prefManager?.readKnobStatus().toString())
            } else {
                prefManager?.setKnobStatus(false)
//                    ToastFactory.custom(context,"Knob status is " + prefManager?.readKnobStatus().toString())
            }
        }

        toggle_iE_tile?.setOnToggledListener { _, isOn ->
            if (prefManager?.readIETileStatus() == false) {
                prefManager?.setIETileStatus(true)
            } else {
                prefManager?.setIETileStatus(false)
            }
        }

        includeButtonChange.buttonView.setOnClickListener {
            setUpChange()
        }

        includeColorChange.buttonView.setOnClickListener {
            setUpColor()
        }

        progressBarLoudness.param_progress_bar.setOnClickListener {

            it.let {
                (it?.param_progress_bar as? CircularProgressIndicator)?.background =
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

        includeButtonTest.buttonView.setOnClickListener {

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
        toggle_knob?.isOn = prefManager?.readKnobStatus() ?: false
        toggle_iE_tile?.isOn = prefManager?.readIETileStatus() ?: false
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

        includeButtonLoudness.buttonView.setPadding(15, 10, 15, 10)
        includeColorChange.buttonView.setPadding(15, 10, 15, 10)
        includeButtonChange.buttonView.setPadding(15, 10, 15, 10)

        includeButtonTest.buttonView.setPadding(50, 0, 50, 0)
        includeButtonAutomatic.buttonView.setPadding(35, 0, 35, 0)
        includeButtonApply.buttonView.setPadding(55, 10, 55, 10)

    }

    private fun setUpChange() {

        (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 3

        includeButtonLoudness.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeColorChange.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonChange.buttonView.setBackgroundResource(R.drawable.background_green_border)

        layoutPanelLoudness.visibility = View.GONE
        layoutPanelTubeComp.visibility = View.VISIBLE
        layoutPanelColorChange.visibility = View.GONE


        setPaddingData()
    }

    private fun setUpColor() {

        (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 5

        includeColorChange.buttonView.setBackgroundResource(R.drawable.background_green_border)
        includeButtonChange.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonLoudness.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        layoutPanelLoudness.visibility = View.GONE
        layoutPanelTubeComp.visibility = View.GONE

        layoutPanelColorChange.visibility = View.VISIBLE

        setPaddingData()
    }

    private fun setUpLoudness() {

        (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 4
        includeButtonLoudness.buttonView.setBackgroundResource(R.drawable.background_green_border)
        includeButtonChange.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeColorChange.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)


        layoutPanelLoudness.visibility = View.VISIBLE
        layoutPanelTubeComp.visibility = View.GONE
        layoutPanelColorChange.visibility = View.GONE


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
        if (layoutPanelLoudness.isVisible) {
            Log.i("LOUDNESSCHECK", "Set value = $newValue")
            progressBarLoudness.param_progress_bar.maxProgress = VOLUME_MAX_VALUE.toDouble()
            progressBarLoudness.param_progress_bar.setCurrentProgress(newValue.toInt().toDouble())
            progressBarLoudness.textView.setText("" + newValue.toInt())
            prefManager?.setVolume(newValue)
            onLoudnessAdjustmentListener?.onCheckLoudness()
            startTimer()

            progressBarLoudness.let {
                (it?.param_progress_bar as? CircularProgressIndicator)?.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.progresscircle
                    )
            }
        } else {
        }

        // change here 13 feb
        customProgressDialog?.takeIf { it.isVisible }?.apply {
            this.dismiss()

        }
        customProgressDialog = null

    }


    override fun handleDialogClose() {

        progressBarLoudness.let {
            (it?.param_progress_bar as? CircularProgressIndicator)?.background =
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.progresscircle
                )
        }

        prefManager?.apply {
            progressBarLoudness.param_progress_bar.maxProgress = VOLUME_MAX_VALUE.toDouble()
            progressBarLoudness.param_progress_bar.setCurrentProgress(
                readVolume().toInt().toDouble()
            )
            progressBarLoudness.textView.setText("" + readVolume().toInt())
        }

        customProgressDialog?.takeIf { it.isVisible }?.dismiss()
        customProgressDialog = null
    }


    override fun onLimitChange(previousValue: Float, newValue: Float) {
        if (layoutPanelLoudness.isVisible) {
            view.let {
                (progressBarLoudness?.param_progress_bar as? CircularProgressIndicator)?.apply {
                    this.setCurrentProgress(newValue.toInt().toDouble())
                }

                (progressBarLoudness?.textView as? TextView)?.apply {
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
        constraintSet.clone(layoutPanelColorChange)
        constraintSet.connect(
            colorRecyclerView.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            colorRecyclerView.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )

        constraintSet.connect(
            colorRecyclerView.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(layoutPanelColorChange)
    }
}
