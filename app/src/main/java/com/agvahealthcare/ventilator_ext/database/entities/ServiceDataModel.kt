package com.agvahealthcare.ventilator_ext.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.agvahealthcare.ventilator_ext.database.DateConverter
import org.json.JSONArray


@Entity(tableName = "service_table") // User Entity represents a table within the database.
data class ServiceDataModel(

    @ColumnInfo(name = "desc")
    var desc: String,

    @ColumnInfo(name = "name")
    var name :String,

    @ColumnInfo(name = "department")
    var deparment :String,

    @ColumnInfo(name = "ward_no")
    var ward_no :String,

    @ColumnInfo(name = "hospital_name")
    var hospital_name :String,

    @ColumnInfo(name = "email_id")
    var email_id :String,

    @ColumnInfo(name = "contactNo")
    var contactNo :String,

    @TypeConverters(DateConverter::class)
    var date: String,

    ) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    var id: Int? = null

}