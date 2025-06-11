package com.agvahealthcare.ventilator_ext.utility.utils

import android.net.Uri
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager

class AlarmConfiguration {

    private val preferenceManager:PreferenceManager? = null
    companion object{

        private val cycleCheckedAcks = listOf<String>(
            Configs.ACK_CODE_61,
            Configs.ACK_CODE_80,
            Configs.ACK_CODE_81,
            Configs.ACK_CODE_82,
            Configs.ACK_CODE_389,
            Configs.ACK_CODE_61,
            Configs.ACK_CODE_80,
            Configs.ACK_CODE_81,
            Configs.ACK_CODE_82,
            Configs.ACK_CODE_389,
            Configs.ACK_CODE_408,
            Configs.ACK_CODE_409,
            Configs.ACK_CODE_420,
            Configs.ACK_CODE_640,
            Configs.ACK_CODE_641,
            Configs.ACK_CODE_642,
            Configs.ACK_CODE_705,
            Configs.ACK_CODE_706,
            Configs.ACK_CODE_722,
            Configs.ACK_CODE_723,
            Configs.ACK_CODE_740,
            Configs.ACK_CODE_741,
            Configs.ACK_CODE_742,
            Configs.ACK_CODE_743,
            Configs.ACK_CODE_745,
            Configs.ACK_CODE_769,
            Configs.ACK_CODE_780,
            Configs.ACK_CODE_781,
            Configs.ACK_CODE_782,
            Configs.ACK_CODE_783,
            Configs.ACK_CODE_784,
            Configs.ACK_CODE_785
        )

        /*  @Deprecated("Deprecated medium level alarm list")
          var mediumLevelAck = listOf(
              Configs.ACK_CODE_12,
              Configs.ACK_CODE_13,
              Configs.ACK_CODE_16,
              Configs.ACK_CODE_17,
              Configs.ACK_CODE_18,
              Configs.ACK_CODE_19,
              Configs.ACK_CODE_30
          )

          @Deprecated("Deprecated low level alarm list")
          private var lowLevelAck = listOf(

              Configs.ACK_CODE_10,
              Configs.ACK_CODE_11,
              Configs.ACK_CODE_14,
              Configs.ACK_CODE_15,
              Configs.ACK_CODE_35,
              Configs.ACK_CODE_50,
              Configs.ACK_CODE_62,
              Configs.ACK_CODE_68,
              Configs.ACK_CODE_80,
          )

          @Deprecated("Deprecated high level alarm list")
          var highLevelAck = listOf<String>()

          @Deprecated("Deprecated critical level alarm list")
          var criticalLevelAck = listOf(
              Configs.ACK_CODE_0,
              Configs.ACK_CODE_5,
          )*/

        private val controlLimitAlarms= listOf(
            Configs.ALARM_PIP,
            Configs.ALARM_VTE,
            Configs.ALARM_RAW_VOLUME,
            Configs.ALARM_RR,
            Configs.ALARM_PEEP,
            Configs.ALARM_MVE,
            Configs.LBL_FIO2,
            Configs.LBL_SPO2,
            Configs.ALARM_TRIGGER,
            Configs.ALARM_RESPIRATORY_PHASE,
            Configs.ALARM_TITOT,
            Configs.ALARM_MAX_FIO2,
            Configs.ALARM_MAX_FIO2_HR_HIGH,
            Configs.POWER_BUTTON,
            Configs.LBL_AVERAGE_LEAK

        )

        private val lowLimitAlarms = listOf(
            Configs.LBL_AVERAGE_LEAK
        )


        private fun isAckValid(ack: String) = ack.startsWith(Configs.PREFIX_ACK)
                && ack.replace(Configs.PREFIX_ACK, "").toIntOrNull()?.let { it in 0..6000 } == true

        @JvmStatic
        fun getColor(alarm: String): Int{
            if(alarm.startsWith(Configs.PREFIX_ACK)) {
                alarm.replace(Configs.PREFIX_ACK, "").toIntOrNull()?.let {
                    return when (it) {
                        in 0..320 -> {
                            R.color.preCalib_amber
                        }
                        in 320..640 -> {
                            R.color.preCalib_amber
                        }
                        in 640..960 -> {
                            R.color.ack_red
                        }
                        else -> R.color.ack_red
                    }

                }

            } else{
                if(alarm in controlLimitAlarms) return  R.color.black
            }

            return R.color.black
        }


        @JvmStatic
        fun getPriority(alarm:String): Configs.AlarmType{

            if(alarm.startsWith(Configs.PREFIX_ACK)) {
                alarm.replace(Configs.PREFIX_ACK, "").toIntOrNull()?.let {
                    return when (it) {
                        786 -> Configs.AlarmType.ALARM_CRITICAL_LEVEL
                        821 -> Configs.AlarmType.ALARM_CRITICAL_LEVEL
                        in 0..320 -> {
                            Configs.AlarmType.ALARM_LOW_LEVEL
                        }
                        in 320..640 -> {
                            Configs.AlarmType.ALARM_MEDIUM_LEVEL
                        }
                        in 640..960  -> {
                            Configs.AlarmType.ALARM_HIGH_LEVEL
                        }

                        else -> Configs.AlarmType.ALARM_NO_LEVEL
                    }
                    /*      if(it.equals(786)) {
                              Configs.AlarmType.ALARM_CRITICAL_LEVEL
                      }*/
                }

            }else if(alarm in lowLimitAlarms){
                return Configs.AlarmType.ALARM_LOW_LEVEL
            }
            else if(alarm == Configs.ALARM_VENTILATOR_FAILURE) return Configs.AlarmType.ALARM_HIGH_LEVEL
            else if(alarm == Configs.ALARM_KNOB_FAILURE) return Configs.AlarmType.ALARM_LOW_LEVEL

            else if (alarm == Configs.ALARM_FIO2_LEAK) return Configs.AlarmType.ALARM_MEDIUM_LEVEL
            else if (alarm == Configs.ALARM_AUTO_PEEP) return Configs.AlarmType.ALARM_LOW_LEVEL
            else {
                if(alarm in controlLimitAlarms) return Configs.AlarmType.ALARM_MEDIUM_LEVEL
            }
//            else if(PreferenceManager(ApplicationProvider.getApplicationContext()).readSelectedOptions() == Configs.SELECTED_OPTIONS.NON_INVASIVE_NAME){
//                if(alarm == Configs.ALARM_RESPIRATORY_RATE) return Configs.AlarmType.ALARM_MEDIUM_LEVEL
//                if(alarm == Configs.ALARM_TIDAL_VOLUME) return Configs.AlarmType.ALARM_MEDIUM_LEVEL
//                if(alarm == Configs.ALARM_INSPIRATORY_PRESSURE) return Configs.AlarmType.ALARM_MEDIUM_LEVEL
//                if(alarm == Configs.ALARM_EXPIRATORY_PRESSURE) return Configs.AlarmType.ALARM_MEDIUM_LEVEL
//            }


            return Configs.AlarmType.ALARM_NO_LEVEL
        }


        @JvmStatic
        fun getAckType(ack: String): Configs.AckType {
            return ack.takeIf { isAckValid(it) }?.replace(Configs.PREFIX_ACK, "")?.toIntOrNull()?.let {
                return if (it < 5000){
                    val tensDigit = (it %  100) / 10
                    if(tensDigit % 2 == 0)  Configs.AckType.ACK
                    else Configs.AckType.NACK
                } else Configs.AckType.OP_ACK
            } ?: Configs.AckType.INVALID_ACK
        }

        @JvmStatic
        fun getAckFor(ack: String): String?{
            val mainAck=ack.substring(0,ack.length-2)+ack[ack.length-2].minus(1)+ack[ack.length-1]
            return mainAck
        }

        @JvmStatic
        fun getNackFor(ack: String): String?{
            return ack.replace(Configs.PREFIX_ACK, "").toIntOrNull()?.let {
                Configs.PREFIX_ACK + (it + 10)
            }
        }



        @JvmStatic
        fun getAlarmUri(priority: Configs.AlarmType): Uri? {
            return when (priority) {

                Configs.AlarmType.ALARM_NO_LEVEL -> null
                Configs.AlarmType.ALARM_LOW_LEVEL -> Configs.URI_ALARM_LOW_LEVEL
                Configs.AlarmType.ALARM_MEDIUM_LEVEL -> Configs.URI_ALARM_MEDIUM_LEVEL
                Configs.AlarmType.ALARM_HIGH_LEVEL -> Configs.URI_ALARM_HIGH_LEVEL
                Configs.AlarmType.ALARM_CRITICAL_LEVEL -> Configs.URI_ALARM_CRITICAL_LEVEL

//                else -> Configs.URI_ALARM_BATTERY_LOW

            }
        }


        @JvmStatic
        fun isCycleCheckRequired(ack: String) = cycleCheckedAcks.contains(ack)
    }
}
