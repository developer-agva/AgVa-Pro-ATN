package com.agvahealthcare.ventilator_ext.control.basic

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.LayoutInflaterCompat
import androidx.recyclerview.widget.RecyclerView
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.IERatio
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.globalModeType
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.isFromControlFragment
import com.agvahealthcare.ventilator_ext.control.backup.BackupFragment
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.utility.ToastFactory
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.*
import kotlin.reflect.KClass


interface ControlParameterClickListener {
    fun onClick(position: Int, model: ControlParameterModel)
    fun onStateChange(isActive: Boolean, type: Configs.ControlSettingType, position: Int)
}

class ControlParameterAdapter(
    val ctx: Context,
    modelList: ArrayList<ControlParameterModel>,
    private val controlParamClickListener: ControlParameterClickListener?,
    private val type: Configs.ControlSettingType
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val LAYOUT_DEFAULT_VIEW = 0
        const val LAYOUT_BACKUP_VIEW = 1
        const val LAYOUT_SMART_VIEW = 2
        const val LAYOUT_ADVANCE_FIOVIEW = 2
    }

    var selectedIndex = -1

    val context = ctx
    val prefManager = PreferenceManager(ctx)
    private var dataList: ArrayList<ControlParameterModel> = arrayListOf()

    init {
        if (type == Configs.ControlSettingType.BACKUP) {
            dataList.add(0, ControlParameterModel.empty())  // for backup settings toggle
        }
        if (type == Configs.ControlSettingType.ADVANCED) {
            if (prefManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {

                globalModeType?.let {
                    if (it == ModeType.TYPE_Pressure) {
                        dataList.add(0, ControlParameterModel.empty())
                    } else if (it != ModeType.TYPE_Pressure) {
                        dataList.add(0, ControlParameterModel.empty())
                    } else {
                        dataList.add(0, ControlParameterModel.empty())
                    }
                } ?: kotlin.run {
                    if (prefManager?.readModeType() == Configs.ModeType.TYPE_Pressure) {
                        dataList.add(0, ControlParameterModel.empty())
                    } else if (prefManager?.readModeType() != ModeType.TYPE_Pressure) {
                        dataList.add(0, ControlParameterModel.empty())
                    } else {
                        dataList.add(0, ControlParameterModel.empty())
                    }
                }
            } else {
                globalModeType?.let {
                    if (it != ModeType.TYPE_HFNC) {
                        dataList.add(0, ControlParameterModel.empty())
                    } else {
                        dataList.add(0, ControlParameterModel.empty())
                    }
                } ?: kotlin.run {
                    if (prefManager?.readModeType() != ModeType.TYPE_HFNC) {
                        dataList.add(0, ControlParameterModel.empty())
                    } else {
                        dataList.add(0, ControlParameterModel.empty())
                    }
                }
            }
        }

        if (type == ControlSettingType.SmartFio2) {
            if (prefManager?.readCurrentUid() == PatientProfile.TYPE_ADULT || prefManager?.readCurrentUid() == PatientProfile.TYPE_PED) {
                globalModeType?.let {
                    if (it != ModeType.TYPE_HFNC) {
                        dataList.add(0, ControlParameterModel.empty())
                    }
                } ?: kotlin.run {
                    if (prefManager?.readModeType() != Configs.ModeType.TYPE_HFNC) {
                        dataList.add(0, ControlParameterModel.empty())
                    }
                }
            }
        }

        if (type == ControlSettingType.VTas) {
            globalModeType?.let {
                if (it != ModeType.TYPE_Volume) {
                    dataList.add(0, ControlParameterModel.empty())
                }
            } ?: kotlin.run {
                if (prefManager?.readModeType() != Configs.ModeType.TYPE_Volume) {
                    dataList.add(0, ControlParameterModel.empty())
                }
            }
        }

        if (type == ControlSettingType.EtCuff) {
            globalModeType?.let {
                if (it != ModeType.TYPE_Volume) {
                    dataList.add(0, ControlParameterModel.empty())
                }
            } ?: kotlin.run {
                if (prefManager?.readModeType() != Configs.ModeType.TYPE_Volume) {
                    dataList.add(0, ControlParameterModel.empty())
                }
            }
        }
        dataList.addAll(modelList)

        for (i in dataList){
            Log.i("tetsingsd",i.toString())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val clazz: Class<out RecyclerView.ViewHolder>
        @LayoutRes var layout: Int = if (viewType == LAYOUT_DEFAULT_VIEW) {
            clazz = VHControlParameterAdapter::class.java
            R.layout.item_control_data
        } else if (viewType == LAYOUT_BACKUP_VIEW) {
            clazz = ToggleVhControlParameterAdapter::class.java
            R.layout.item_control_backup_data
        } else {
            clazz = ToggleVhControlParameterAdapter::class.java
            R.layout.item_control_backup_data
        }

        val view = LayoutInflater.from(ctx).inflate(layout, parent, false)
        return clazz.getConstructor(View::class.java).newInstance(view)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tile = dataList[position]

        if (type == ControlSettingType.EtCuff && tile.isEmpty) {

            if (selectedIndex == position) {
                (holder as ToggleVhControlParameterAdapter).toggleLayout?.setBackgroundResource(R.drawable.background_transparent_border_yellow)
            }else{
                (holder as ToggleVhControlParameterAdapter).toggleLayout?.setBackgroundColor(Color.TRANSPARENT)
            }

            if (position == 0) {

                (holder as ToggleVhControlParameterAdapter).apply {
                    tgText?.text = "Et-Cuff"
                    tgButton?.setOnCheckedChangeListener { _, isChecked ->
                        controlParamClickListener?.onStateChange(isChecked, type, 0)
                    }
                }

                if (VentilatorApp.isFromControlFragment == true) {
                    if (prefManager.readEtCuffStatus() == true) {
                        (holder as ToggleVhControlParameterAdapter).apply {
                            tgButton?.isChecked = true
                        }

                    } else if (prefManager.readEtCuffStatus() == false) {
                        (holder as ToggleVhControlParameterAdapter).apply {
                            tgButton?.isChecked = false
                        }
                    }
                } else {
                    if (prefManager.readEtCuffStatusTemp() == true) {
                        (holder as ToggleVhControlParameterAdapter).apply {
                            tgButton?.isChecked = true
                        }

                    } else if (prefManager.readEtCuffStatusTemp() == false) {
                        (holder as ToggleVhControlParameterAdapter).apply {
                            tgButton?.isChecked = false
                        }
                    }
                }
            }

        }
        else if (type == ControlSettingType.VTas && tile.isEmpty) {

            if (selectedIndex == position) {
                (holder as ToggleVhControlParameterAdapter).toggleLayout?.setBackgroundResource(R.drawable.background_transparent_border_yellow)
            }else{
                (holder as ToggleVhControlParameterAdapter).toggleLayout?.setBackgroundColor(Color.TRANSPARENT)
            }


            if (position == 0) {
                (holder as ToggleVhControlParameterAdapter).apply {
                    tgText?.text = "V-TAS"
                    tgButton?.setOnCheckedChangeListener { _, isChecked ->
                        controlParamClickListener?.onStateChange(isChecked, type, 0)
                    }
                }

                if (VentilatorApp.isFromControlFragment == true) {
                    if (prefManager.readVGVStatus() == true) {
                        (holder as ToggleVhControlParameterAdapter).apply {
                            tgButton?.isChecked = true
                        }

                    } else if (prefManager.readVGVStatus() == false) {
                        (holder as ToggleVhControlParameterAdapter).apply {
                            tgButton?.isChecked = false
                        }
                    }
                } else {
                    if (prefManager.readVGVStatusTemp() == true) {
                        (holder as ToggleVhControlParameterAdapter).apply {
                            tgButton?.isChecked = true
                        }

                    } else if (prefManager.readVGVStatusTemp() == false) {
                        (holder as ToggleVhControlParameterAdapter).apply {
                            tgButton?.isChecked = false
                        }
                    }
                }

            }
        }
        else if (type == ControlSettingType.SmartFio2 && tile.isEmpty) {

            if (selectedIndex == position) {
                (holder as ToggleVhControlParameterAdapter).toggleLayout?.setBackgroundResource(R.drawable.background_transparent_border_yellow)
            }else{
                (holder as ToggleVhControlParameterAdapter).toggleLayout?.setBackgroundColor(Color.TRANSPARENT)
            }


            if (position == 0) {
                (holder as ToggleVhControlParameterAdapter).apply {
                    tgText?.text = "Smart FiO2"

                    tgButton?.setOnCheckedChangeListener { _, isChecked ->
                        Log.i("valueasdw", "1")
                        controlParamClickListener?.onStateChange(isChecked, type, 0)
                    }
                }

                if (VentilatorApp.isFromControlFragment == true) {
                    if (prefManager.readSmartFiO2Status()) {
                        (holder as ToggleVhControlParameterAdapter).apply {
                            tgButton?.isChecked = true
                        }

                    } else if (!prefManager.readSmartFiO2Status()) {
                        (holder as ToggleVhControlParameterAdapter).apply {
                            tgButton?.isChecked = false
                        }
                    }
                } else {
                    if (prefManager.readSmartFiO2StatusTemp()) {
                        (holder as ToggleVhControlParameterAdapter).apply {
                            tgButton?.isChecked = true
                        }

                    } else if (!prefManager.readSmartFiO2StatusTemp()) {
                        (holder as ToggleVhControlParameterAdapter).apply {
                            tgButton?.isChecked = false
                        }
                    }
                }

            }
        }
        else if ((type == ControlSettingType.BACKUP || type == ControlSettingType.ADVANCED) && tile.isEmpty) {

            if (selectedIndex == position) {
                (holder as ToggleVhControlParameterAdapter).toggleLayout?.setBackgroundResource(R.drawable.background_transparent_border_yellow)
            }else{
                (holder as ToggleVhControlParameterAdapter).toggleLayout?.setBackgroundColor(Color.TRANSPARENT)
            }


            if (type == Configs.ControlSettingType.BACKUP) {


                    if (VentilatorApp.isFromControlFragment == true) {
                        if (prefManager.readApneaSettingsStatus() == true) {
                            (holder as ToggleVhControlParameterAdapter).apply {
                                tgButton?.isChecked = true
                            }
                        } else if (prefManager.readApneaSettingsStatus() == false) {
                            (holder as ToggleVhControlParameterAdapter).apply {
                                tgButton?.isChecked = false
                            }
                        }
                    } else {
                        if (prefManager.readApneaSettingsStatusTemp() == true) {
                            (holder as ToggleVhControlParameterAdapter).apply {
                                tgButton?.isChecked = true
                            }
                        } else if (prefManager.readApneaSettingsStatusTemp() == false) {
                            (holder as ToggleVhControlParameterAdapter).apply {
                                tgButton?.isChecked = false
                            }
                        }
                    }


            }
            else {
                if (position == 0) {
                    (holder as ToggleVhControlParameterAdapter).apply {
                        tgText?.text = "INVERSE I:E"
                    }

                    if (VentilatorApp.isFromControlFragment == true) {
                        if (prefManager.readIRVStatus() == true) {
                            (holder as ToggleVhControlParameterAdapter).apply {
                                tgButton?.isChecked = true
                            }
                        } else if (prefManager.readIRVStatus() == false) {
                            (holder as ToggleVhControlParameterAdapter).apply {
                                tgButton?.isChecked = false
                            }
                        }
                    } else {
                        if (prefManager.readIRVStatusTemp() == true) {
                            (holder as ToggleVhControlParameterAdapter).apply {
                                tgButton?.isChecked = true
                            }
                        } else if (prefManager.readIRVStatusTemp() == false) {
                            (holder as ToggleVhControlParameterAdapter).apply {
                                tgButton?.isChecked = false
                            }
                        }
                    }
                }
            }

            (holder as ToggleVhControlParameterAdapter).apply {
                if (position == 0) {
                    tgButton?.setOnCheckedChangeListener { _, isChecked ->
                        prefManager.apply {
                            val isActive = !isChecked
                            Log.i("iERatios", IERatio)

                            if ((VentilatorApp.IERatio.split(":")[1].toFloat() < 1.0f && isActive) && type == ControlSettingType.ADVANCED) {
                                tgButton?.isChecked = true
                                ToastFactory.custom(
                                    context,
                                    "Decrease RR until I:E limits is 1:1"
                                )
                            } else controlParamClickListener?.onStateChange(
                                isChecked,
                                type,
                                position
                            )
                        }
                    }
                }
            }
        }

        // run when tile have data
        else {
            setSelection(holder as VHControlParameterAdapter, tile.isIsselected)


            if (selectedIndex == position) {
                (holder as VHControlParameterAdapter).mainLayoutPanel?.setBackgroundResource(R.drawable.background_transparent_border_yellow)
            }else{
                (holder as VHControlParameterAdapter).mainLayoutPanel?.setBackgroundColor(Color.TRANSPARENT)
            }

            (holder as VHControlParameterAdapter).mainLayoutPanel?.setOnClickListener {

                controlParamClickListener?.onClick(
                    if (type == ControlSettingType.BACKUP || type == ControlSettingType.SmartFio2 || type == ControlSettingType.ADVANCED || type == ControlSettingType.VTas || type == ControlSettingType.EtCuff) {
                        position - 1
                    }
                    else {
                         position
                    },
                    tile
                )
                notifyItemChanged(position)
            }

            if (tile.title == LBL_SLOPE) {

                (holder as VHControlParameterAdapter).circleProgressView?.setCurrentProgress(
                    getPercentage(tile).toDouble()
                )

                (holder as VHControlParameterAdapter).tvValue?.text = tile.title

                if (tile.reading == "0") (holder as VHControlParameterAdapter).tvLabel?.setBackgroundResource(
                    R.drawable.ic_slope_zero
                )
                else if (tile.reading == "1") (holder as VHControlParameterAdapter).tvLabel?.setBackgroundResource(
                    R.drawable.ic_slope_one
                )
                else if (tile.reading == "2") (holder as VHControlParameterAdapter).tvLabel?.setBackgroundResource(
                    R.drawable.ic_slope_two
                )

                (holder as VHControlParameterAdapter).tvUnit?.text = tile.units
            } else {
                (holder as VHControlParameterAdapter).circleProgressView?.setCurrentProgress(
                    getPercentage(tile).toDouble()
                )
                (holder as VHControlParameterAdapter).tvValue?.text = tile.title
                (holder as VHControlParameterAdapter).tvLabel?.setBackgroundResource(Color.TRANSPARENT)
                (holder as VHControlParameterAdapter).tvLabel?.text =
                    Configs.supportPrecision(tile.ventKey, tile.reading)
                (holder as VHControlParameterAdapter).tvUnit?.text = tile.units
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        Log.i("see", "enter in here")
        if ((type == Configs.ControlSettingType.BACKUP || type == Configs.ControlSettingType.ADVANCED || type == Configs.ControlSettingType.SmartFio2 || type == Configs.ControlSettingType.VTas || type == Configs.ControlSettingType.EtCuff) && dataList.get(
                position
            ).isEmpty
        ) return LAYOUT_BACKUP_VIEW
        else return LAYOUT_DEFAULT_VIEW
    }

    //To Do for the change of the control parameter tiles.
    private fun setSelection(
        holder: ControlParameterAdapter.VHControlParameterAdapter,
        isSelected: Boolean
    ) {

        Log.i("value1231", "${holder.tvLabel?.text} , $isSelected")
        if (isSelected) {
            holder.circleProgressView?.background =
                ContextCompat.getDrawable(ctx, R.drawable.progresscircle_with_selection_yellow)
            holder.tvLabel?.setTextColor(Color.BLACK)
        } else {
            holder.circleProgressView?.background =
                ContextCompat.getDrawable(ctx, R.drawable.progresscircle)
            holder.tvLabel?.setTextColor(Color.BLACK)
        }
    }

    fun getItems() = dataList

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun addFilterData(filterList: MutableList<ControlParameterModel>) {
        dataList = filterList as ArrayList<ControlParameterModel>
    }

    private fun getPercentage(value: Double, min: Double, max: Double): Int {
        Log.i(
            "PROGPRECENT_CHECK",
            "Min = ${min} Max = ${max} Value = ${value} Percent = ${(((value - min) / (max - min)) * 100).toInt()}"
        )
        return (((value - min) / (max - min)) * 100).toInt()
    }

    private fun getPercentage(param: ControlParameterModel): Int {
        try {
            return getPercentage(param.reading.toDouble(), param.lowerLimit, param.upperLimit);
        } catch (e: Exception) {
            return 0;
        }
    }

    class ToggleVhControlParameterAdapter(view: View) : RecyclerView.ViewHolder(view) {
        var tgButton: ToggleButton? = null
        var toggleLayout: LinearLayoutCompat? = null
        var tgText: TextView? = null

        init {
            tgButton = view.findViewById(R.id.toggleButton)
            toggleLayout = view.findViewById(R.id.toggleLayout)
            tgText = view.findViewById(R.id.tvBackup)

        }

    }


    class VHControlParameterAdapter(view: View) : RecyclerView.ViewHolder(view) {
        var tvValue: TextView? = null
        var tvUnit: TextView? = null
        var circleProgressView: CircularProgressIndicator? = null
        var tvLabel: TextView? = null
        var mainLayoutPanel: LinearLayoutCompat? = null

        init {

            tvValue = view.findViewById(R.id.textViewValue)
            tvUnit = view.findViewById(R.id.textViewUnit)
            circleProgressView = view.findViewById(R.id.param_progress_bar)
            tvLabel = view.findViewById(R.id.textView)
            mainLayoutPanel = view.findViewById(R.id.mainLayoutPanel)
        }

    }


}

