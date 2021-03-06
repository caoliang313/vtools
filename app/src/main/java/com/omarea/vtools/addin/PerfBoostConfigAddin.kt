package com.omarea.vtools.addin

import android.content.Context
import android.os.Build
import android.widget.Toast
import com.omarea.shared.CommonCmds
import com.omarea.shared.FileWrite
import com.omarea.shared.MagiskExtend
import com.omarea.shell.Platform

/**
 * Created by Hello on 2018/03/22.
 */

class PerfBoostConfigAddin(private var context: Context) : AddinBase(context) {
    fun install() {
        val soc = Platform().getCPUName()
        if (soc == "msm8998") {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Toast.makeText(context, "只支持Android 8.0以后的系统！", Toast.LENGTH_SHORT).show()
                return
            }
            msm8898()
        } else if (soc == "sdm845") {
            sdm845()
        } else if (soc == "sdm630") {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Toast.makeText(context, "只支持Android 8.0以后的系统！", Toast.LENGTH_SHORT).show()
                return
            }
            sdm630()
        } else if (soc == "sdm660") {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Toast.makeText(context, "只支持Android 8.0以后的系统！", Toast.LENGTH_SHORT).show()
                return
            }
            sdm660()
        } else {
            Toast.makeText(context, "暂未适配这个处理器，暂时只支持 骁龙835、845、660AIE、630！", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sdm845() {
        val path = FileWrite.writePrivateFile(context.assets, "addin/perfboostsconfig_sdm845.xml", "perfboostsconfig_sdm845.xml", context)
        copyFile(path)
    }

    private fun msm8898() {
        val path = FileWrite.writePrivateFile(context.assets, "addin/perfboostsconfig_msm8998.xml", "perfboostsconfig_msm8998.xml", context)
        copyFile(path)
    }

    // 配置文件和660AIE的写在一起了
    private fun sdm630() {
        val path = FileWrite.writePrivateFile(context.assets, "addin/perfboostsconfig_sdm660.xml", "perfboostsconfig_sdm660.xml", context)
        copyFile(path)
    }

    private fun sdm660() {
        val path = FileWrite.writePrivateFile(context.assets, "addin/perfboostsconfig_sdm660.xml", "perfboostsconfig_sdm660.xml", context)
        copyFile(path)
    }

    private fun copyFile(path: String?) {
        if (path != null) {
            val installPath = "/system/vendor/etc/perf/perfboostsconfig.xml"
            if (MagiskExtend.moduleInstalled()) {
                MagiskExtend.replaceSystemFile(installPath, path)

                Toast.makeText(context, "已通过Magisk更改参数，请重启手机~", Toast.LENGTH_SHORT).show()
            } else {
                command = StringBuilder()
                        .append(CommonCmds.MountSystemRW)
                        .append(CommonCmds.MountVendorRW)
                        .append("if [[ ! -e $installPath.bak ]]; then cp $installPath $installPath.bak; fi;\n")
                        .append("rm -f $installPath\n")
                        .append("cp $path $installPath\n")
                        .append("chmod 755 $installPath\n")
                        .append("verify=`busybox md5sum $path | cut -f1 -d ' '`\n")
                        .append("md5=`busybox md5sum $installPath | cut -f1 -d ' '`\n")
                        .append("if [[ \$md5 = \$verify ]]; then exit 0; else exit 1; fi;\n")
                        .toString()

                super.run()
            }
        } else {
            Toast.makeText(context, "配置文件提取失败！", Toast.LENGTH_SHORT).show()
        }
    }
}
