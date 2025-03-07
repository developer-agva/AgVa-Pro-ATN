package com.agvahealthcare.ventilator_ext.maneuvers

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.callback.SimpleCallbackListener
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.databinding.FragmentManeuversDialogBinding
import com.agvahealthcare.ventilator_ext.manager.DataStoreManager
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.maneuvers.hold.HoldFragment
import com.agvahealthcare.ventilator_ext.maneuvers.utilities.NebulizerFragment
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.utility.ToastFactory
import com.agvahealthcare.ventilator_ext.utility.replaceFragment
import com.agvahealthcare.ventilator_ext.utility.setHeightWidthPercent
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_AND
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_MINUS
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_PLUS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class ManeuversDialogFragment : DialogFragment(), SimpleCallbackListener {

    companion object {

        const val TAG = "ManeuversDialog"
        private const val KEY_HEIGHT = "KEY_HEIGHT"
        private const val KEY_WIDTH = "KEY_WIDTH"
        private const val KEY_IS_KNOB = "KEY_KNOB"
        fun newInstance(
            height: Int?,
            width: Int?,
            isKnob: String?,
            closeListener: OnDismissDialogListener?,
            communicationService: CommunicationService?
        ): ManeuversDialogFragment {
            val args = Bundle()
            height?.let { args.putInt(KEY_HEIGHT, it) }
            width?.let { args.putInt(KEY_WIDTH, it) }
            isKnob?.let { args.putString(KEY_IS_KNOB, it) }

            val fragment = ManeuversDialogFragment()
            fragment.arguments = args
            fragment.closeListener = closeListener
            fragment.communicationService = communicationService
            return fragment
        }
    }

    private lateinit var binding : FragmentManeuversDialogBinding
    private var closeListener: OnDismissDialogListener? = null
    private var communicationService: CommunicationService? = null
    private var prefManager: PreferenceManager? = null
    private var dataStoreManager: DataStoreManager? = null
    var fragmentHold: HoldFragment? = null
    var fragmentUtilities: NebulizerFragment? = null
    private var dashBoardViewModel: DashBoardViewModel? = null


    // knob highlight logic starts here
    var sizeOfCurrentArray = 0

    var highlightedIndex = -1
    private var visibilityTimeout: CountDownTimer? = null

    private fun highlightAdapters(highlightedIndex: Int,data: String?) {
        if (fragmentHold != null) fragmentHold?.highlightAdapterPosition(highlightedIndex,data)
        else if (fragmentUtilities != null) fragmentUtilities?.highlightAdapterPosition(
            highlightedIndex
        )
    }

    private fun handleAdaptersClick(highlightedIndex: Int) {
        if (fragmentHold != null) fragmentHold?.handleClick(highlightedIndex)
        else if (fragmentUtilities != null) fragmentUtilities?.handleClick(highlightedIndex)
    }

    private fun makeAllFragmentsNull() {
        fragmentHold = null
        fragmentUtilities = null
    }

    @SuppressLint("NotifyDataSetChanged")
    fun highlightViewWithFocus(data: String) {


        if (fragmentUtilities?.customProgressDialog?.isVisible == true) {
            fragmentUtilities?.updateKnobData(data)
        } else {

            Log.i("value_check_bonds", "index : $highlightedIndex ,size : $sizeOfCurrentArray")

            clearPreviousConstraints()
            startTimeoutWithDebounce()

            when (data) {
                PREFIX_PLUS -> {
                    if (highlightedIndex < (sizeOfCurrentArray + 3)) highlightedIndex++
                    else highlightedIndex = 0

                    getViewForFocus(false)?.let {
                        highlightAdapters(-1,data)
                        changeConstraintsOfFocusLayout(it.second)
                    } ?: kotlin.run {
                        highlightAdapters(highlightedIndex,data)
                    }
                }

                PREFIX_MINUS -> {

                    if (highlightedIndex > 0) highlightedIndex--
                    else highlightedIndex = (sizeOfCurrentArray + 3)

                    getViewForFocus(true)?.let {
                        highlightAdapters(-1,data)
                        changeConstraintsOfFocusLayout(it.second)
                    } ?: kotlin.run {
                        highlightAdapters(highlightedIndex,data)
                    }
                }

                PREFIX_AND -> {
                    getViewForFocus(null)?.let {
                        if (highlightedIndex == sizeOfCurrentArray + 1) {
                            it.first.callOnClick()
                        } else {
                            it.first.callOnClick()

                            // reset highlight index to starting position after clicking on any fragments
                            highlightedIndex = -1
                        }
                    } ?: kotlin.run {
                        handleAdaptersClick(highlightedIndex)
                    }

                    clearPreviousConstraints()
                }
            }
        }
    }

    private fun clearPreviousConstraints() {
        try {
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.mainViewPanelManeuvers)
            constraintSet.clear(binding.focusLayoutManeuvers.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutManeuvers.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutManeuvers.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutManeuvers.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.mainViewPanelManeuvers)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.mainViewPanelManeuvers)
        constraintSet.connect(
            binding.focusLayoutManeuvers.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.focusLayoutManeuvers.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            binding.focusLayoutManeuvers.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            binding.focusLayoutManeuvers.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.mainViewPanelManeuvers)
    }

    private fun getViewForFocus(isMinus: Boolean?): Pair<View,View>? {

        return when (highlightedIndex) {

            in 0..sizeOfCurrentArray -> {
                if (sizeOfCurrentArray == 0) {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                } else null
            }

            sizeOfCurrentArray + 1 -> {
                val pair = Pair(binding.imageViewCrossManeuvers,binding.imageViewCrossManeuvers)
                pair
            }
            sizeOfCurrentArray + 2 -> {
                val pair = Pair(binding.includeButtonHold.buttonView,binding.includeButtonHold.root)
                pair
            }

            sizeOfCurrentArray + 3 -> {
                val pair = Pair(binding.includeButtonNebuliser.buttonView,binding.includeButtonNebuliser.root)
                pair
            }

            else -> null
        }
    }

    fun startTimeoutWithDebounce() {

        cancelTimeout()

        visibilityTimeout = object : CountDownTimer(10000, 2000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                highlightAdapters(-1, data = null)
                clearPreviousConstraints()
                cancelTimeout()
            }
        }
        visibilityTimeout?.start()
    }

    fun cancelTimeout() {
        if (visibilityTimeout != null) {
            visibilityTimeout?.cancel()
            visibilityTimeout = null
        }
    }

    // knob highlight logic ends here

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentManeuversDialogBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)

        dashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]
        prefManager = PreferenceManager(requireContext())
        dataStoreManager = DataStoreManager(requireContext())

        if (prefManager?.readCurrentUid() == Configs.PatientProfile.TYPE_NEONAT) binding.includeButtonNebuliser.root.visibility =
            View.GONE
        else binding.includeButtonNebuliser.root.visibility = View.VISIBLE


        if (tag == Configs.NEBULIZER) {
            setUpUtilities()
            Log.i("TAG_CHECK", "this is initiated${tag}")
        } else {
            setUpHold()
        }
        setOnClickListener()
    }


    // clickListener on Buttons
    private fun setOnClickListener() {

        binding.includeButtonHold.buttonView.text = getString(R.string.hint_hold)
        binding.includeButtonNebuliser.buttonView.text = getString(R.string.hint_Nebuliser)
        //  includeButtonNebuliser.buttonView.alpha=0.3f

        /*  includeButtonNebuliser.buttonView.isClickable = false
          includeButtonNebuliser.buttonView.isFocusable = false
          includeButtonNebuliser.buttonView.isEnabled = false*/
        binding.imageViewCrossManeuvers.setOnClickListener {

            requireActivity().supportFragmentManager
                .beginTransaction()
                .remove(this)
                .commitNow()
//            requireActivity().supportFragmentManager.popBackStack()

            closeListener?.handleDialogClose()

            /* closeListener?.handleDialogClose()
             dismiss()*/
        }


        binding.includeButtonHold.buttonView.setOnClickListener {
            setUpHold()
        }

        binding.includeButtonNebuliser.buttonView.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                if (dataStoreManager?.getNebulizerCheck()?.first() == true)
                    setUpUtilities()
                else ToastFactory.custom(context, "No Oxygen Supply Connected")
            }

        }
    }

    //By Default Fragment
    private fun setUpHold() {

        sizeOfCurrentArray = 1
        makeAllFragmentsNull()

        if (fragmentHold == null) {
            fragmentHold =
                HoldFragment(communicationService, this, arguments?.getString(KEY_IS_KNOB))
            fragmentHold?.apply {
                replaceFragment(
                    this,
                    this::class.java.javaClass.simpleName,
                    R.id.maneuvers_nav_container
                )
            }

            binding.includeButtonHold.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
            binding.includeButtonHold.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.includeButtonNebuliser.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
            binding.includeButtonNebuliser.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )

            setPaddingButton()
        }
    }

    private fun setUpUtilities() {

        sizeOfCurrentArray = 1
        makeAllFragmentsNull()

        if (fragmentUtilities == null) {
            fragmentUtilities = NebulizerFragment(communicationService)
            fragmentUtilities?.apply {
                replaceFragment(
                    this,
                    this::class.java.javaClass.simpleName,
                    R.id.maneuvers_nav_container
                )
            }
            Log.i("LOG_CHECKed", "This is Data${tag}")
            binding.includeButtonNebuliser.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
            binding.includeButtonNebuliser.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.includeButtonHold.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
            binding.includeButtonHold.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            setPaddingButton()
        }

    }

    private fun setPaddingButton() {
        binding.includeButtonHold.buttonView.setPadding(30, 10, 30, 10)
        binding.includeButtonNebuliser.buttonView.setPadding(25, 10, 25, 10)
    }

    override fun onStart() {
        super.onStart()
        val heightDialog = arguments?.getInt(KEY_HEIGHT)
        val widthDialog = arguments?.getInt(KEY_WIDTH)

        setHeightWidthPercent(heightDialog, widthDialog, true)
    }


    override fun doAction() {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .remove(this)
            .commitNow()
//            requireActivity().supportFragmentManager.popBackStack()

        closeListener?.handleDialogClose()
    }

}