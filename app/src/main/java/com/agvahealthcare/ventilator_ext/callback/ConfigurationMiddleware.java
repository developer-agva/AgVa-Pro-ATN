package com.agvahealthcare.ventilator_ext.callback;


import com.agvahealthcare.ventilator_ext.utility.utils.ConfigurationArrayList;


public interface ConfigurationMiddleware {
    ConfigurationArrayList modify(ConfigurationArrayList list);
}
