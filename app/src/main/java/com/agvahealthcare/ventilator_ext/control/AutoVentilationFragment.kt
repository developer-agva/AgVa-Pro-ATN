package com.agvahealthcare.ventilator_ext.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.agvahealthcare.ventilator_ext.R

class AutoVentilationFragment : DialogFragment(){
    companion object {
        const val TAG = "AutoVentilation"
        private const val KEY_HEIGHT = "KEY_HEIGHT"
        private const val KEY_WIDTH = "KEY_WIDTH"
        private const val KEY_STATUS = "KEY_STATUS"

        fun newInstance(
            height: Int?,
            width: Int?,
            status: Boolean?,
        ) : AutoVentilationFragment {
            val args = Bundle()
            height?.let { args.putInt(AutoVentilationFragment.KEY_HEIGHT, it) }
            width?.let { args.putInt(AutoVentilationFragment.KEY_WIDTH, it) }
            status?.let { args.putBoolean(AutoVentilationFragment.KEY_STATUS, it) }

            val fragment = AutoVentilationFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_autovent, container, false)    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}


