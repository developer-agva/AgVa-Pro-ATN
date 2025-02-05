package com.agvahealthcare.ventilator_ext.utility.utils;

import androidx.annotation.NonNull;

import java.util.ArrayList;


/**
 * Created by MOHIT MALHOTRA on 26-09-2018.
 */

public class ConfigurationArrayList extends ArrayList<String> {

//    public static final int MAX_MODULE_SIZE = isFio2SettingAvailable ? 5 : 4;


//    public static ArrayList<ConfigurationArrayList> bisectConfigurationIntoList(ConfigurationArrayList configs){
//        ArrayList<ConfigurationArrayList> list = new ArrayList<>();
//
//        // prepare vancant lists
//        int listCount = configs.size() / ConfigurationArrayList.MAX_MODULE_SIZE;
//        listCount += ((configs.size() % ConfigurationArrayList.MAX_MODULE_SIZE) > 0) ? 1 : 0;
//
//        for(int i =0; i<=listCount; i++){
//            list.add(new ConfigurationArrayList());
//        }
//
//        for(int i=0; i<configs.size(); i++){
//            list.get(i / ConfigurationArrayList.MAX_MODULE_SIZE).add(configs.get(i));
//        }
//
//        return list;
//    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for(String s : this){
            sb.append(s);
            if(count < this.size()-1) sb.append(",");
            count++;
        }
        return sb.toString();
    }
}
