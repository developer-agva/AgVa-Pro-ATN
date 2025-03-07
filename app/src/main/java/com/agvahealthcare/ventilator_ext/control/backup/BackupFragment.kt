package com.agvahealthcare.ventilator_ext.control.backup

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.control.basic.ControlParameterAdapter
import com.agvahealthcare.ventilator_ext.control.basic.ControlParameterClickListener
import com.agvahealthcare.ventilator_ext.databinding.FragmentAdvancedBinding
import com.agvahealthcare.ventilator_ext.databinding.FragmentBackupBinding
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel
import com.agvahealthcare.ventilator_ext.utility.GridSpacingItemDecoration
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.github.angads25.toggle.interfaces.OnToggledListener
import java.util.*

class BackupFragment(private var dataList : ArrayList<ControlParameterModel>, private  var controlParameterClickListener: ControlParameterClickListener?, private var onToggledListener: OnToggledListener?) : Fragment() {

    private var preferenceManager : PreferenceManager? = null


    var isactive:Boolean = true

    private var controlParamAdapter : ControlParameterAdapter? = null
    private lateinit var binding: FragmentBackupBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentBackupBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.preferenceManager = PreferenceManager(requireContext())

        setUpAlarmsData()


//        toggleSettings?.setOnToggledListener { v, isOn ->
//            preferenceManager?.setApneaSettingsStatus(isOn)
//            onToggledListener?.onSwitched(v, isOn)
//        }

        /* toggleButton?.setOnClickListener{

             if(isactive)
                 preferenceManager?.setApneaSettingsStatus(true)
             else {
                 preferenceManager?.setApneaSettingsStatus(false)
             }
         }*/

    }

    fun getControlParameter() = controlParamAdapter?.getItems()

    fun getApneaToggleStatus() = preferenceManager?.readApneaSettingsStatus()

    private fun setUpAlarmsData() {
        controlParamAdapter = ControlParameterAdapter(requireContext(), dataList, controlParameterClickListener, Configs.ControlSettingType.BACKUP)
        (binding.recyclerViewBackupParams.getItemAnimator() as SimpleItemAnimator).supportsChangeAnimations = false
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
    fun notifyAdapter() = controlParamAdapter?.notifyDataSetChanged()


    @SuppressLint("NotifyDataSetChanged")
    fun dataList(filterVentParameterTiles: ArrayList<ControlParameterModel>) {
        dataList = filterVentParameterTiles
        controlParamAdapter?.apply{
            addFilterData(dataList)
            notifyDataSetChanged()
        }
    }
}