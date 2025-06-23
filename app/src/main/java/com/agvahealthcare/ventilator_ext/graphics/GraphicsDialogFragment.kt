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
import com.agvahealthcare.ventilator_ext.graph.divide_trends.QuadTrendsFragment
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.setHeightWidthPercent
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_AND
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_MINUS
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_PLUS
import kotlinx.android.synthetic.main.content_button_center_layout.view.buttonView
import kotlinx.android.synthetic.main.fragment_graphics_dialog.focusLayoutGraphics
import kotlinx.android.synthetic.main.fragment_graphics_dialog.imageViewCrossGraphics
import kotlinx.android.synthetic.main.fragment_graphics_dialog.includeButtonDefault
import kotlinx.android.synthetic.main.fragment_graphics_dialog.includeButtonLayout0
import kotlinx.android.synthetic.main.fragment_graphics_dialog.includeButtonLayout1
import kotlinx.android.synthetic.main.fragment_graphics_dialog.includeButtonLayout2
import kotlinx.android.synthetic.main.fragment_graphics_dialog.includeButtonLayout3
import kotlinx.android.synthetic.main.fragment_graphics_dialog.includeButtonLayout4
import kotlinx.android.synthetic.main.fragment_graphics_dialog.includeButtonLayout5
import kotlinx.android.synthetic.main.fragment_graphics_dialog.layoutPanelDividePent
import kotlinx.android.synthetic.main.fragment_graphics_dialog.layoutPanelDivideQuad
import kotlinx.android.synthetic.main.fragment_graphics_dialog.layoutPanelDivideTrio
import kotlinx.android.synthetic.main.fragment_graphics_dialog.layoutPanelDuo
import kotlinx.android.synthetic.main.fragment_graphics_dialog.layoutPanelQuadTrends
import kotlinx.android.synthetic.main.fragment_graphics_dialog.layoutPanelTrio
import kotlinx.android.synthetic.main.fragment_graphics_dialog.mainViewPanelGraphics
import kotlinx.android.synthetic.main.hold_buttons_layout.buttonView


class GraphicsDialogFragment(
    private val onGraphSelectListener: OnGraphSelectListener?,
    val closeListener: OnDismissDialogListener?,
    val graphLayoutListener: GraphLayoutListener?
) : DialogFragment() {
    
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
                if (highlightedIndex < 7) highlightedIndex++
                else highlightedIndex = 1

                getViewForFocus()?.let { changeConstraintsOfFocusLayout(it.second) }
            }

            PREFIX_MINUS -> {
                if (highlightedIndex > 1) highlightedIndex--
                else highlightedIndex = 7

                getViewForFocus()?.let { changeConstraintsOfFocusLayout(it.second) }
            }

            PREFIX_AND -> {

                if (highlightedIndex != 7) {
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
            constraintSet.clone(mainViewPanelGraphics)
            constraintSet.clear(focusLayoutGraphics.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutGraphics.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutGraphics.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutGraphics.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelGraphics)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainViewPanelGraphics)
        constraintSet.connect(
            focusLayoutGraphics.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutGraphics.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutGraphics.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutGraphics.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainViewPanelGraphics)
    }

    private fun getViewForFocus(): Pair<View,View>? {

        return when (highlightedIndex) {
            1 -> Pair(imageViewCrossGraphics,imageViewCrossGraphics)
            2 -> Pair(layoutPanelQuadTrends,layoutPanelQuadTrends)
            3 -> Pair(layoutPanelDuo,layoutPanelDuo)
            3 -> Pair(layoutPanelTrio,layoutPanelTrio)
            4 -> Pair(layoutPanelDivideQuad,layoutPanelDivideQuad)
            5 -> Pair(layoutPanelDivideTrio,layoutPanelDivideTrio)
            6 -> Pair(layoutPanelDividePent,layoutPanelDividePent)
            7 -> Pair(includeButtonDefault.buttonView,includeButtonDefault)

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
        val view = inflater.inflate(R.layout.fragment_graphics_dialog, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
        prefManager = PreferenceManager(requireContext())

        setupClickListener()
        initView()
    }

    private fun initView() {
        includeButtonLayout0.buttonView.text = getString(R.string.hint_layout_1)
        includeButtonLayout1.buttonView.text = getString(R.string.hint_layout_default)
        includeButtonLayout2.buttonView.text = getString(R.string.hint_layout_2)
        includeButtonLayout3.buttonView.text = getString(R.string.hint_layout_3)
        includeButtonLayout4.buttonView.text = getString(R.string.hint_layout_4)
        includeButtonLayout5.buttonView.text = getString(R.string.hint_layout_5)
        includeButtonDefault.buttonView.text = getString(R.string.hint_layout_default)

        includeButtonDefault.buttonView.setPadding(35, 10, 35, 10)

        includeButtonLayout0.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonLayout0.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        includeButtonLayout1.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonLayout1.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonLayout2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonLayout2.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonLayout3.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonLayout3.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonLayout4.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonLayout4.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonLayout5.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonLayout5.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonDefault.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonDefault.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        graphLayoutListener?.getCurrentLayoutFragment()?.let {
            when (it) {
                is DuoFragmentGraph -> {
                    includeButtonLayout0.buttonView.setBackgroundResource(R.drawable.background_green_border)
                }

                is QuadTrendsFragment -> {
                    includeButtonLayout1.buttonView.setBackgroundResource(R.drawable.background_green_border)
                }

                is TrioFragmentGraph -> {
                    includeButtonLayout2.buttonView.setBackgroundResource(R.drawable.background_green_border)
                }

                is DivideQuadFragmentGraph -> {
                    includeButtonLayout3.buttonView.setBackgroundResource(R.drawable.background_green_border)
                }

                is DivideTrioFragmentGraph -> {
                    includeButtonLayout4.buttonView.setBackgroundResource(R.drawable.background_green_border)
                }

                is LoopsFragmentGraph -> {
                    includeButtonLayout5.buttonView.setBackgroundResource(R.drawable.background_green_border)
                }
            }
        }

    }

    // ClickListener on Buttons
    private fun setupClickListener() {

        imageViewCrossGraphics.setOnClickListener {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .remove(this)
                .commitNow()
            closeListener?.handleDialogClose()
        }

        layoutPanelQuadTrends.setOnClickListener {
            prefManager?.setGraphParentType(parentType.QuadTrendsFragment)
            onGraphSelectListener?.onSelectQuadTrendsGraph()
            closeListener?.handleDialogClose()
            closeDialog()
        }

        layoutPanelDividePent.setOnClickListener {
            prefManager?.setGraphParentType(parentType.LoopsFragmentGraph)
            onGraphSelectListener?.onSelectLoopsGraph()
            closeListener?.handleDialogClose()
            closeDialog()
        }

        layoutPanelDivideQuad.setOnClickListener {
            prefManager?.setGraphParentType(parentType.DivideQuadFragmentGraph)
            onGraphSelectListener?.onSelectDivideQuadGraph()
            closeListener?.handleDialogClose()
            closeDialog()
        }

        layoutPanelDuo.setOnClickListener {
            prefManager?.setGraphParentType(parentType.DuoFragmentGraph)
            onGraphSelectListener?.onSelectDuoGraph()
            closeListener?.handleDialogClose()
            closeDialog()
        }

        layoutPanelTrio.setOnClickListener {
            prefManager?.setGraphParentType(parentType.TrioFragmentGraph)
            onGraphSelectListener?.onSelectTrioGraph()
            closeListener?.handleDialogClose()
            closeDialog()
        }

        layoutPanelDivideTrio.setOnClickListener {
            prefManager?.setGraphParentType(parentType.DivideTrioFragmentGraph)
            onGraphSelectListener?.onSelectDivideTrioGraph()
            closeListener?.handleDialogClose()
            closeDialog()
        }

        includeButtonDefault.buttonView.setOnClickListener {
            prefManager?.setGraphParentType(parentType.QuadTrendsFragment)
            onGraphSelectListener?.onSelectQuadTrendsGraph()
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