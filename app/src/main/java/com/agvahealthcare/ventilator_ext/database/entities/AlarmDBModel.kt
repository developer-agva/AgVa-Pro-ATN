package com.agvahealthcare.ventilator_ext.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.agvahealthcare.ventilator_ext.database.DateConverter


@Entity(tableName = "alarm_table") // User Entity represents a table within the database.
data class AlarmDBModel(
    @ColumnInfo(name = "key")
    var key: String,

    @ColumnInfo(name = "message")
    var message: String,

    @TypeConverters(DateConverter::class)
    @ColumnInfo(name = "createdAt")
    var createdAt: String,

    //Uhid filter for the events and alarms
    @ColumnInfo(name="Uhid")
    var uhid:String,

//    @TypeConverters(DateConverter::class)
//    @ColumnInfo(name = "EndDatetimeStamp")
//    var endTimeStamp: String,

) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    var id: Int? = null

}