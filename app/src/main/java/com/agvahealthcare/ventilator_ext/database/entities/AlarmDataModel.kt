package com.agvahealthcare.ventilator_ext.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.agvahealthcare.ventilator_ext.database.DateConverter


@Entity(tableName = "alarm_table") // User Entity represents a table within the database.
data class AlarmDataModel(

    @ColumnInfo(name = "message")
    var message: String,

    @ColumnInfo(name = "ackcode", defaultValue = "0")
    var ackcode: String,

    @TypeConverters(DateConverter::class)
    @ColumnInfo(name = "StartDatetimeStamp")
    var startTimeStamp: String,

    @TypeConverters(DateConverter::class)
    @ColumnInfo(name = "EndDatetimeStamp")
    var endTimeStamp: String,

) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    var id: Int? = null

}