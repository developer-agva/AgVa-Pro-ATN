package com.agvahealthcare.ventilator_ext.database;

/**
 * Created by MOHIT MALHOTRA on 14-11-2018.
 */

public interface DatabaseUtils {

    // Database Version
    int DATABASE_VERSION = 2;

    // Database Name
    String DATABASE_NAME = "agva_pro_db";

    // Tables Name
    String TABLE_VENT_DATA_STORE = "tbl_data_store";
    String TABLE_VENT_ERROR_STORE = "tbl_error_store";


    // Create table queries
    String CREATE_TBL_DATA_STORE = "CREATE TABLE " + TABLE_VENT_DATA_STORE
            + "("
            + DataStoreTable.ID  + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DataStoreTable.TIME + " TEXT NOT NULL,"
            + DataStoreTable.PIP + " REAL NOT NULL,"
            + DataStoreTable.VINSP + " REAL NOT NULL,"
            + DataStoreTable.RR + " REAL NOT NULL,"
            + DataStoreTable.FIO2 + " REAL NOT NULL,"
            + DataStoreTable.MVE + " REAL NOT NULL,"
            + DataStoreTable.VTE + " REAL NOT NULL,"
            + DataStoreTable.PEEP + " REAL NOT NULL,"
            + DataStoreTable.LEAK + " REAL NOT NULL,"
            + DataStoreTable.IE_RATIO + " REAL NOT NULL,"
            + DataStoreTable.TINSP + " REAL NOT NULL,"
            + DataStoreTable.TEXP + " REAL NOT NULL,"
            + DataStoreTable.MVI + " REAL NOT NULL,"
            + DataStoreTable.TRIGGER + " REAL NOT NULL,"
            //database change to be made to add the ventmode in the database
            + DataStoreTable.MODE + " TEXT NOT NULL,"
            + DataStoreTable.MEANAIRWAYPRESS + " REAL NOT NULL,"
            + DataStoreTable.Spo2 + " REAL NOT NULL,"
            + DataStoreTable.pr + " REAL NOT NULL"
            + ");";

    String CREATE_TBL_ERROR_STORE = "CREATE TABLE " + TABLE_VENT_ERROR_STORE
            + "("
            + ErrorStoreTable.ID  + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ErrorStoreTable.TIME + " TEXT NOT NULL,"
            + ErrorStoreTable.ERROR_CODE + " TEXT NOT NULL"
            + ");";


    interface DataStoreTable{
        String ID = "id";
        String TIME = "log_time";
        String PIP = "pip";
        String VINSP = "vinsp";
        String RR = "rr";
        String FIO2 = "fio";
        String MVE = "mve";
        String VTE = "vte";
        String PEEP = "peep";
        String LEAK = "leak";
        String IE_RATIO = "ie";
        String TINSP="tinsp";
        String TEXP="texp";
        String MVI="mvi";
        String TRIGGER="trigger";
        String MEANAIRWAYPRESS="meanAirwayPressure";
        String MODE="mode";
        String Spo2="spo2";
        String pr="pr";

        String[] COLUMNS = { ID, TIME, PIP, VINSP, RR, FIO2, MVE, VTE, PEEP, LEAK, IE_RATIO, TINSP, TEXP, MVI, TRIGGER, MEANAIRWAYPRESS ,MODE,Spo2,pr};
    }

    interface ErrorStoreTable{
        String ID = "id";
        String TIME = "error_time";
        String ERROR_CODE = "error_code";

        String[] COLUMNS = { ID, ERROR_CODE, TIME };
    }
}
