package com.omarea.vtools.receiver

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.omarea.shared.FileWrite
import com.omarea.shared.SpfConfig
import com.omarea.shell.KeepShellAsync
import com.omarea.vtools.R


class ReciverBatterychanged(private var service: Service) : BroadcastReceiver() {
    private var chargeDisabled: Boolean = false
    private var keepShellAsync: KeepShellAsync? = null

    private var sharedPreferences: SharedPreferences
    private var globalSharedPreferences: SharedPreferences
    private var listener: SharedPreferences.OnSharedPreferenceChangeListener
    private val myHandler = Handler(Looper.getMainLooper())
    private var qcLimit = 50000

    //显示文本消息
    private fun showMsg(msg: String, longMsg: Boolean) {
        try {
            myHandler.post {
                Toast.makeText(service, msg, if (longMsg) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
            }
        } catch (ex: Exception) {
            Log.e("BatteryService", ex.message)
        }
    }

    var FastChangerBase = "sh " + FileWrite.writePrivateShellFile("custom/battery/fast_charge.sh", "fast_charge.sh", service)
    var ResumeChanger = "sh " + FileWrite.writePrivateShellFile("custom/battery/resume_charge.sh", "resume_charge.sh", service)
    var DisableChanger = "sh " + FileWrite.writePrivateShellFile("custom/battery/disable_charge.sh", "disable_charge.sh", service)

    /*
     "chmod 0777 /sys/class/power_supply/usb/pd_allowed;" +
     "echo 1 > /sys/class/power_supply/usb/pd_allowed;" +
     "chmod 0666 /sys/class/power_supply/main/constant_charge_current_max;" +
     "chmod 0666 /sys/class/qcom-battery/restricted_current;" +
     "chmod 0666 /sys/class/qcom-battery/restricted_charging;" +
     "echo 0 > /sys/class/qcom-battery/restricted_charging;" +
     "echo 0 > /sys/class/power_supply/battery/restricted_charging;" +
     "echo 0 > /sys/class/power_supply/battery/safety_timer_enabled;" +
     "chmod 0666 /sys/class/power_supply/bms/temp_warm;" +
     "echo 500 > /sys/class/power_supply/bms/temp_warm;" +
     "chmod 0666 /sys/class/power_supply/battery/constant_charge_current_max;"
    */

    //快速充电
    private fun fastCharger() {
        try {
            if (!sharedPreferences.getBoolean(SpfConfig.CHARGE_SPF_QC_BOOSTER, false))
                return

            if (FastChangerBase.isNotEmpty()) {
                keepShellAsync!!.doCmd(FastChangerBase)
                FastChangerBase = ""
            }
            keepShellAsync!!.doCmd(computeLeves(qcLimit).toString())
        } catch (ex: Exception) {
            Log.e("ChargeService", ex.stackTrace.toString())
        }
    }

    private fun computeLeves(qcLimit: Int): StringBuilder {
        val arr = StringBuilder()
        if (qcLimit > 2000) {
            var level = 2000
            while (level < qcLimit) {
                arr.append("echo ${level}000 > /sys/class/power_supply/battery/constant_charge_current_max\n")
                arr.append("echo ${level}000 > /sys/class/power_supply/main/constant_charge_current_max\n")
                arr.append("echo ${level}000 > /sys/class/qcom-battery/restricted_current\n")
                level += 300
            }
        }
        arr.append("echo ${qcLimit}000 > /sys/class/power_supply/battery/constant_charge_current_max\n")
        arr.append("echo ${qcLimit}000 > /sys/class/power_supply/main/constant_charge_current_max\n")
        arr.append("echo ${qcLimit}000 > /sys/class/qcom-battery/restricted_current\n")
        return arr;
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        onReceiveAsync(intent, pendingResult)
    }

    private fun onReceiveAsync(intent: Intent, pendingResult: PendingResult) {
        try {
            val action = intent.action
            val onChanger = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
            val currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            var bpLeve = sharedPreferences.getInt(SpfConfig.CHARGE_SPF_BP_LEVEL, 85)
            if (bpLeve <= 30) {
                showMsg("Scene：当前电池保护电量值设为小于30%，会引起一些异常，已自动替换为默认值85%！", true)
                bpLeve = 85
            }
            val bp = sharedPreferences.getBoolean(SpfConfig.CHARGE_SPF_BP, false)

            //BatteryProtection 如果开启充电保护
            if (bp) {
                if (onChanger) {
                    if (currentLevel >= bpLeve) {
                        disableCharge()
                    } else if (currentLevel < bpLeve - 20) {
                        resumeCharge()
                    }
                }
                //电量不足，恢复充电功能
                else if (chargeDisabled && currentLevel != -1 && currentLevel < bpLeve - 20) {
                    //电量低于保护级别20
                    resumeCharge()
                }
            }
            // 如果没有开启充电保护，并且已经禁止充电
            else if (chargeDisabled) {
                resumeCharge()
            }

            // 如果电池电量低
            if (action == Intent.ACTION_BATTERY_LOW) {
                showMsg(service.getString(R.string.battery_low), false)
                resumeCharge()
            }

            // 未进入电池保护状态 并且电量低于85
            if (currentLevel < (bpLeve - 20) && currentLevel < 85) {
                entryFastChanger()
            }
        } catch (ex: Exception) {
            showMsg("充电加速服务：\n" + ex.message, true);
        } finally {
            pendingResult.finish()
        }
    }

    init {
        if (keepShellAsync == null) {
            keepShellAsync = KeepShellAsync(service)
        }
        sharedPreferences = service.getSharedPreferences(SpfConfig.CHARGE_SPF, Context.MODE_PRIVATE)
        listener = SharedPreferences.OnSharedPreferenceChangeListener { spf, key ->
            if (key == SpfConfig.CHARGE_SPF_QC_LIMIT) {
                qcLimit = spf.getInt(SpfConfig.CHARGE_SPF_QC_LIMIT, qcLimit)
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        qcLimit = sharedPreferences.getInt(SpfConfig.CHARGE_SPF_QC_LIMIT, qcLimit)
        globalSharedPreferences = service.getSharedPreferences(SpfConfig.GLOBAL_SPF, Context.MODE_PRIVATE)
    }

    internal fun onDestroy() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this.listener)
        this.resumeCharge()
        keepShellAsync!!.tryExit()
        keepShellAsync = null
    }

    internal fun disableCharge() {
        keepShellAsync!!.doCmd(DisableChanger)
        chargeDisabled = true
    }

    internal fun resumeCharge() {
        keepShellAsync!!.doCmd(ResumeChanger)
        chargeDisabled = false
    }

    internal fun entryFastChanger() {
        if (sharedPreferences.getBoolean(SpfConfig.CHARGE_SPF_QC_BOOSTER, false)) {
            fastCharger()
        }
    }
}
