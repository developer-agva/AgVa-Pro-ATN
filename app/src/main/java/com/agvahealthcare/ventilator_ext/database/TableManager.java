package com.agvahealthcare.ventilator_ext.database;

import android.content.Context;

import java.util.List;

import de.codecrafters.tableview.SortableTableView;

public abstract class TableManager<T>{

        // Used for initializing the params
        public TableManager(Context context, SortableTableView<T> tableView){}

        // Used to populate the data over the table
        public abstract void populateData(List<T> dataList);
}