package com.agvahealthcare.ventilator_ext.database

import androidx.room.TypeConverter
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import java.text.ParseException
import java.util.*

class DateConverter {

    @TypeConverter
    @Throws(ParseException::class)
    fun fromTimestamp(value: String): Date?  {
        return AppUtils.dateTimeFormatter.parse(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): String {
        return AppUtils.dateTimeFormatter.format(date)
    }
}