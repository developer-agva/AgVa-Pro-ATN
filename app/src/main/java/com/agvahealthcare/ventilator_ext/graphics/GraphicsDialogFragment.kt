package com.agvahealthcare.ventilator_ext.graphics

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
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.callback.OnGraphSelectListener
import com.agvahealthcare.ventilator_ext.dashboard.GraphLayoutFragment
import com.agvahealthcare.ventilator_ext.dashboard.chart.parentType
import com.agvahealthcare.ventilator_ext.dashboard.duo_graph.DuoFragmentGraph
import com.agvahealthcare.ventilator_ext.dashboard.loops_graph.LoopsFragmentGraph
import com.agvahealthcare.ventilator_ext.dashboard.quad_graph.DivideQuadFragmentGraph
import com.agvahealthcare.ventilator_ext.dashboard.trio_graph.DivideTrioFragmentGraph
import com.agvahealthcare.ventilator_ext.dashboard.trio_graph.TrioFragmentGraph
import com.agvahealthcare.ventilator_ext.databinding.FragmentGraphicsDialogBinding
import com.agvahealthcare.ventilator_ext.graph.divide_trends.QuadTrendsFragment
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.setHeightWidthPercent
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_AND
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_MINUS
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_PLUS


class GraphicsDialogFragment(
    private val onGraphSelectListener: OnGraphSelectListener?,
    val closeListener: OnDismissDialogListener?,
    val graphLayoutListener: GraphLayoutListener?
) : DialogFragment() {

    private lateinit var binding: FragmentGraphicsDialogBinding
    private var prefManager: PreferenceManager? = null

    interface GraphLayoutListener {
        fun getCurrentLayoutFragment(): GraphLayoutFragment?
    }

    companion object {
        const val TAG = "GraphicsDialog"
        private const val KEY_HEIGHT = "KEY_HEIGHT"
        private const val KEY_WIDTH = "KEY_WIDTH"
    }

    // knob highlight logic starts here

    private var highlightedIndex = 0
    private var visibilityTimeout: CountDownTimer? = null

    @SuppressLint("NotifyDataSetChanged")
    fun highlightViewWithFocus(data: String) {

        startTimeoutWithDebounce()
        Log.i("value_check_tiles", "$highlightedIndex")

        when (data) {
            PREFIX_PLUS -> {
                if (highlightedIndex < 8) highlightedIndex++
                else highlightedIndex = 1

                getViewForFocus()?.let { changeConstraintsOfFocusLayout(it.second) }
            }

            PREFIX_MINUS -> {
                if (highlightedIndex > 1) highlightedIndex--
                else highlightedIndex = 8

                getViewForFocus()?.let { changeConstraintsOfFocusLayout(it.second) }
            }

            PREFIX_AND -> {

                if (highlightedIndex != 8) {
                    getViewForFocus()?.first?.callOnClick()
                } else {
                    getViewForFocus()?.first?.callOnClick()
                }
            }
        }
    }

    private fun clearPreviousConstraints() {
        try {
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.mainViewPanelGraphics)
            constraintSet.clear(binding.focusLayoutGraphics.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutGraphics.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutGraphics.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutGraphics.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.mainViewPanelGraphics)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.mainViewPanelGraphics)
        constraintSet.connect(
            binding.focusLayoutGraphics.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.focusLayoutGraphics.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            binding.focusLayoutGraphics.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            binding.focusLayoutGraphics.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.mainViewPanelGraphics)
    }

    private fun getViewForFocus(): Pair<View,View>? {

        return when (highlightedIndex) {
            1 -> Pair(binding.imageViewCrossGraphics,binding.imageViewCrossGraphics)
            2 -> Pair(binding.layoutPanelTrio,binding.layoutPanelTrio)
            3 -> Pair(binding.layoutPanelDuo,binding.layoutPanelDuo)
            4 -> Pair(binding.layoutPanelQuadTrends,binding.layoutPanelQuadTrends)
            5 -> Pair(binding.layoutPanelDivideQuad,binding.layoutPanelDivideQuad)
            6 -> Pair(binding.layoutPanelDivideTrio,binding.layoutPanelDivideTrio)
            7 -> Pair(binding.layoutPanelDividePent,binding.layoutPanelDividePent)
            8 -> Pair(binding.includeButtonDefault.buttonView,binding.includeButtonDefault.root)

            else -> null
        }
    }

    fun startTimeoutWithDebounce() {

        cancelTimeout()

        visibilityTimeout = object : CountDownTimer(10000, 2000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
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
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGraphicsDialogBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
        prefManager = PreferenceManager(requireContext())

        setupClickListener()
        initView()
    }

    private fun initView() {
        binding.includeButtonLayout0.buttonView.text = getString(R.string.hint_layout_default)
        binding.includeButtonLayout1.buttonView.text = getString(R.string.hint_layout_1)
        binding.includeButtonLayout2.buttonView.text = getString(R.string.hint_layout_2)
        binding.includeButtonLayout3.buttonView.text = getString(R.string.hint_layout_3)
        binding.includeButtonLayout4.buttonView.text = getString(R.string.hint_layout_4)
        binding.includeButtonLayout5.buttonView.text = getString(R.string.hint_layout_5)
        binding.includeButtonDefault.buttonView.text = getString(R.string.hint_layout_default)

        binding.includeButtonDefault.buttonView.setPadding(35, 10, 35, 10)

        binding.includeButtonLayout0.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        binding.includeButtonLayout0.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        binding.includeButtonLayout1.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        binding.includeButtonLayout1.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        binding.includeButtonLayout2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        binding.includeButtonLayout2.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        binding.includeButtonLayout3.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        binding.includeButtonLayout3.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        binding.includeButtonLayout4.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        binding.includeButtonLayout4.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        binding.includeButtonLayout5.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        binding.includeButtonLayout5.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        binding.includeButtonDefault.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        binding.includeButtonDefault.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        graphLayoutListener?.getCurrentLayoutFragment()?.let {
            when (it) {
                is DuoFragmentGraph -> {
                    binding.includeButtonLayout0.buttonView.setBackgroundResource(R.drawable.background_green_border)
                }

                is QuadTrendsFragment -> {
                    binding.includeButtonLayout1.buttonView.setBackgroundResource(R.drawable.background_green_border)
                }

                is TrioFragmentGraph -> {
                    binding.includeButtonLayout2.buttonView.setBackgroundResource(R.drawable.background_green_border)
                }

                is DivideQuadFragmentGraph -> {
                    binding.includeButtonLayout3.buttonView.setBackgroundResource(R.drawable.background_green_border)
                }

                is DivideTrioFragmentGraph -> {
                    binding.includeButtonLayout4.buttonView.setBackgroundResource(R.drawable.background_green_border)
                }

                is LoopsFragmentGraph -> {
                    binding.includeButtonLayout5.buttonView.setBackgroundResource(R.drawable.background_green_border)
                }
            }
        }

    }

    // ClickListener on Buttons
    private fun setupClickListener() {

        binding.imageViewCrossGraphics.setOnClickListener {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .remove(this)
                .commitNow()
            closeListener?.handleDialogClose()
        }

        binding.layoutPanelQuadTrends.setOnClickListener {
            prefManager?.setGraphParentType(parentType.QuadTrendsFragment)
            onGraphSelectListener?.onSelectQuadTrendsGraph()
            closeListener?.handleDialogClose()
            closeDialog()
        }

        binding.layoutPanelDividePent.setOnClickListener {
            prefManager?.setGraphParentType(parentType.LoopsFragmentGraph)
            onGraphSelectListener?.onSelectLoopsGraph()
            closeListener?.handleDialogClose()
            closeDialog()
        }

        binding.layoutPanelDivideQuad.setOnClickListener {
            prefManager?.setGraphParentType(parentType.DivideQuadFragmentGraph)
            onGraphSelectListener?.onSelectDivideQuadGraph()
            closeListener?.handleDialogClose()
            closeDialog()
        }

        binding.layoutPanelDuo.setOnClickListener {
            prefManager?.setGraphParentType(parentType.DuoFragmentGraph)
            onGraphSelectListener?.onSelectDuoGraph()
            closeListener?.handleDialogClose()
            closeDialog()
        }

        binding.layoutPanelTrio.setOnClickListener {
            prefManager?.setGraphParentType(parentType.TrioFragmentGraph)
            onGraphSelectListener?.onSelectTrioGraph()
            closeListener?.handleDialogClose()
            closeDialog()
        }

        binding.layoutPanelDivideTrio.setOnClickListener {
            prefManager?.setGraphParentType(parentType.DivideTrioFragmentGraph)
            onGraphSelectListener?.onSelectDivideTrioGraph()
            closeListener?.handleDialogClose()
            closeDialog()
        }

        binding.includeButtonDefault.buttonView.setOnClickListener {
            prefManager?.setGraphParentType(parentType.DuoFragmentGraph)
            onGraphSelectListener?.onSelectDuoGraph()
            closeListener?.handleDialogClose()
            closeDialog()
        }
    }

    private fun closeDialog() {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .remove(this)
            .commitNow()

        closeListener?.handleDialogClose()
    }

    override fun onStart() {
        super.onStart()
        val heightDialog = arguments?.getInt(KEY_HEIGHT)
        val widthDialog = arguments?.getInt(KEY_WIDTH)

        setHeightWidthPercent(heightDialog, widthDialog, true)
    }

}