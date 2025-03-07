package com.agvahealthcare.ventilator_ext.standby

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager

import com.agvahealthcare.ventilator_ext.MainActivity
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.control.basic.ControlParameterAdapter
import com.agvahealthcare.ventilator_ext.control.basic.ControlParameterClickListener
import com.agvahealthcare.ventilator_ext.databinding.FragmentBackupBinding
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel
import com.agvahealthcare.ventilator_ext.utility.GridSpacingItemDecoration
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.github.angads25.toggle.interfaces.OnToggledListener

class StandbyBackupFragment(private var dataList : ArrayList<ControlParameterModel>, private  var controlParameterClickListener: ControlParameterClickListener?, private var onToggledListener: OnToggledListener?) : StandbyControlSettingFragment() {

    private var preferenceManager : PreferenceManager? = null
    var isactive:Boolean = true
    private var controlParamAdapter : ControlParameterAdapter? = null
    private lateinit var binding : FragmentBackupBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentBackupBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    // knob highlight logic starts here

    @SuppressLint("NotifyDataSetChanged")
    fun highlightAdapterPosition(highlightedIndex : Int) {
        controlParamAdapter?.selectedIndex = highlightedIndex
        controlParamAdapter?.notifyDataSetChanged()
    }

    fun handleClick(highlightedIndex: Int) {
        if (highlightedIndex == 0) {
            if (VentilatorApp.isFromControlFragment == true) controlParameterClickListener?.onStateChange(
                !(preferenceManager?.readApneaSettingsStatus()!!),
                Configs.ControlSettingType.BACKUP,
                highlightedIndex
            )
            else controlParameterClickListener?.onStateChange(
                !(preferenceManager?.readApneaSettingsStatusTemp()!!),
                Configs.ControlSettingType.BACKUP,
                highlightedIndex
            )
            notifyAdapter()
        } else {
            if (highlightedIndex != -1) controlParameterClickListener?.onClick(highlightedIndex-1, dataList[highlightedIndex-1])
        }
    }

    // knob highlight logic ends here

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.preferenceManager = PreferenceManager(requireContext())



        /* toggleButton?.setOnCheckedChangeListener{_, isChecked ->
             Toast.makeText(requireContext(),if(isChecked)"Button is on" else "Button is off",Toast.LENGTH_SHORT).show()
         }*/

        setControlParameters()

    }

    override fun onResume() {
        super.onResume()
        checkMode()
    }

    private fun checkMode(){
        var mainActivity=MainActivity()
        var code = mainActivity.requestedModeCode
        if(code == 22 || code == 25){
            binding.layoutPaneltrigger.visibility = View.VISIBLE
        }
    }


    //fun getApneaToggleStatus() = toggleButton.isChecked
    fun getApneaToggleStatus() = true
    override fun getControlParameters() = controlParamAdapter?.getItems()

    override fun setControlParameters() {
        controlParamAdapter = ControlParameterAdapter(requireContext(), dataList, controlParameterClickListener, Configs.ControlSettingType.BACKUP)

        binding.recyclerViewBackupParams.apply {
            Log.i("BACKUPCHECK", "Params = ${dataList.map { it.toString() }}")

            layoutManager = object: GridLayoutManager(requireContext(), 6){
                override fun canScrollVertically(): Boolean = false
            }
            addItemDecoration(GridSpacingItemDecoration(6, 90))
            adapter = controlParamAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun notifyAdapter() = controlParamAdapter?.notifyDataSetChanged()


    @SuppressLint("NotifyDataSetChanged")
    override fun dataList(filterVentParameterTiles: ArrayList<ControlParameterModel>) {
        dataList = filterVentParameterTiles
        controlParamAdapter?.apply{
            addFilterData(dataList)
            notifyDataSetChanged()
        }
    }
    override fun notifyItemChangedAdapter(position: Int) = controlParamAdapter?.notifyItemChanged(position)
}