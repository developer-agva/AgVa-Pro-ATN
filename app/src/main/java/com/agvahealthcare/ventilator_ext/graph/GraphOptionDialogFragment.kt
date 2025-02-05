package com.agvahealthcare.ventilator_ext.graph

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.graph.graphics.GraphicsFragment
import com.agvahealthcare.ventilator_ext.graph.loops.LoopsFragment
import com.agvahealthcare.ventilator_ext.graph.trends.TrendsFragment
import com.agvahealthcare.ventilator_ext.graph.waveforms.WaveformsFragment
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_graph_option_dialog.*

class GraphOptionDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "GraphDialog"

        fun newInstance(): GraphOptionDialogFragment {
            return GraphOptionDialogFragment()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_graph_option_dialog, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.CustomDialog);

        setupTrends()
        setupClickListener()
    }

    // ClickListener on Buttons
    private fun setupClickListener() {

        includeButtonTrends.buttonView.text = getString(R.string.hint_trends)
        includeButtonLoops.buttonView.text = getString(R.string.hint_loops)
        includeButtonGraphics.buttonView.text = getString(R.string.hint_graphics)
        includeButtonWaveform.buttonView.text = getString(R.string.hint_waveforms)


        imageViewCross.setOnClickListener {
            dismiss()
        }

        includeButtonTrends.buttonView.setOnClickListener {
            setupTrends()
        }

        includeButtonLoops.buttonView.setOnClickListener {
            val fragment = LoopsFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.graph_option_nav_container, fragment, fragment::class.java.javaClass.simpleName)
                .commit()

            includeButtonTrends.buttonView.setBackgroundResource(R.drawable.background_light_grey)
            includeButtonLoops.buttonView.setBackgroundResource(R.drawable.background_light_grey_border)
            includeButtonGraphics.buttonView.setBackgroundResource(R.drawable.background_light_grey)
            includeButtonWaveform.buttonView.setBackgroundResource(R.drawable.background_light_grey)

        }

        includeButtonGraphics.buttonView.setOnClickListener {

            val fragment = GraphicsFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.graph_option_nav_container, fragment, fragment::class.java.javaClass.simpleName)
                .commit()

            includeButtonTrends.buttonView.setBackgroundResource(R.drawable.background_light_grey)
            includeButtonLoops.buttonView.setBackgroundResource(R.drawable.background_light_grey)
            includeButtonGraphics.buttonView.setBackgroundResource(R.drawable.background_light_grey_border)
            includeButtonWaveform.buttonView.setBackgroundResource(R.drawable.background_light_grey)

        }

        includeButtonWaveform.buttonView.setOnClickListener {
            val fragment = WaveformsFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.graph_option_nav_container, fragment, fragment::class.java.javaClass.simpleName)
                .commit()

            includeButtonTrends.buttonView.setBackgroundResource(R.drawable.background_light_grey)
            includeButtonLoops.buttonView.setBackgroundResource(R.drawable.background_light_grey)
            includeButtonGraphics.buttonView.setBackgroundResource(R.drawable.background_light_grey)
            includeButtonWaveform.buttonView.setBackgroundResource(R.drawable.background_light_grey_border)
        }
    }

    //By Default Fragment
    private fun setupTrends() {
        val fragment = TrendsFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.graph_option_nav_container, fragment, fragment::class.java.javaClass.simpleName)
            .commit()

        includeButtonTrends.buttonView.setBackgroundResource(R.drawable.background_light_grey_border)
        includeButtonLoops.buttonView.setBackgroundResource(R.drawable.background_light_grey)
        includeButtonGraphics.buttonView.setBackgroundResource(R.drawable.background_light_grey)
        includeButtonWaveform.buttonView.setBackgroundResource(R.drawable.background_light_grey)
    }

    override fun onStart() {
        super.onStart()
//        setHeightWidthPercent(KEY_HEIGHT, KEY_WIDTH)

    }

}