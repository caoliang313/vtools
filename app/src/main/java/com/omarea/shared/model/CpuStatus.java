package com.omarea.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Hello on 2018/08/04.
 */

public class CpuStatus implements Serializable {
    public String cluster_little_min_freq = "";
    public String cluster_little_max_freq = "";
    public String cluster_little_governor = "";
    public HashMap<String, String> cluster_little_governor_params = null;

    public String cluster_big_min_freq = "";
    public String cluster_big_max_freq = "";
    public String cluster_big_governor = "";
    public HashMap<String, String> cluster_big_governor_params = null;

    public String coreControl = "";
    public String vdd = "";
    public String msmThermal = "";
    public String boost = "";
    public String boostFreq = "";
    public String boostTime = "";
    public ArrayList<Boolean> coreOnline = null;

    public int exynosHmpUP = 0;
    public int exynosHmpDown = 0;
    public boolean exynosHmpBooster = false;
    public boolean exynosHotplug = false;

    public String adrenoMinFreq = "";
    public String adrenoMaxFreq = "";
    public String adrenoMinPL = "";
    public String adrenoMaxPL = "";
    public String adrenoDefaultPL = "";
    public String adrenoGovernor = "";

    public String cpusetBackground = "";
    public String cpusetSysBackground = "";
    public String cpusetForeground = "";
    public String cpusetTopApp = "";
    public String cpusetForegroundBoost = "";
}
