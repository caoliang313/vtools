package com.omarea.vtools.dialogs

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.util.Base64
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.omarea.shared.CommonCmds
import com.omarea.shared.MagiskExtend
import com.omarea.shell.KeepShellPublic
import com.omarea.shell.Props
import com.omarea.shell.RootFile
import com.omarea.vtools.R


/**
 * Created by Hello on 2017/12/03.
 */

class DialogAddinModifydevice(var context: Context) {

    val BACKUP_SUCCESS = "persist.vtools.device.backuped"
    val BACKUP_BRAND = "persist.vtools.brand"
    val BACKUP_MODEL = "persist.vtools.model"
    val BACKUP_PRODUCT = "persist.vtools.product"
    val BACKUP_DEVICE = "persist.vtools.device"
    val BACKUP_MANUFACTURER = "persist.vtools.manufacturer"

    private fun getBackupProp(prop: String, default: String): String {
        val value = Props.getProp(prop)
        if (value == "null" || value == "") {
            return default
        }

        return value
    }

    fun modifyDeviceInfo() {
        //SM-N9500@samsung@samsung@dream2qltezc@dream2qltechn

        val layoutInflater = LayoutInflater.from(context)
        val dialog = layoutInflater.inflate(R.layout.dialog_addin_device, null)
        editModel = dialog.findViewById(R.id.dialog_addin_model) as EditText
        editBrand = dialog.findViewById(R.id.dialog_addin_brand) as EditText
        editProductName = dialog.findViewById(R.id.dialog_addin_name) as EditText
        editDevice = dialog.findViewById(R.id.dialog_addin_device) as EditText
        editManufacturer = dialog.findViewById(R.id.dialog_addin_manufacturer) as EditText

        (dialog.findViewById(R.id.dialog_addin_default) as Button).setOnClickListener {
            setDefault()
        }
        (dialog.findViewById(R.id.dialog_addin_x20) as Button).setOnClickListener {
            setX20()
        }
        (dialog.findViewById(R.id.dialog_addin_r11) as Button).setOnClickListener {
            setR11Plus()
        }
        (dialog.findViewById(R.id.dialog_addin_mix3) as Button).setOnClickListener {
            setMIX3()
        }
        (dialog.findViewById(R.id.dialog_addin_mrs) as Button).setOnClickListener {
            setMateRS()
        }
        (dialog.findViewById(R.id.dialog_addin_s9plus) as Button).setOnClickListener {
            setS9Plus()
        }
        (dialog.findViewById(R.id.dialog_addin_r15) as Button).setOnClickListener {
            setR15Plus()
        }
        (dialog.findViewById(R.id.dialog_addin_8848) as Button).setOnClickListener {
            set8848()
        }
        val dialogInstance = AlertDialog.Builder(context)
                //.setTitle("机型信息修改")
                .setView(dialog).setNegativeButton("保存重启", { _, _ ->
                    val model = editModel.text.trim()
                    val brand = editBrand.text.trim()
                    val product = editProductName.text.trim()
                    val device = editDevice.text.trim()
                    val manufacturer = editManufacturer.text.trim()
                    if (model.isNotEmpty() || brand.isNotEmpty() || product.isNotEmpty() || device.isNotEmpty() || manufacturer.isNotEmpty()) {
                        backupDefault()
                        if (MagiskExtend.moduleInstalled()) {
                            if (brand.isNotEmpty())
                                MagiskExtend.setSystemProp("ro.product.brand", brand.toString())
                            if (product.isNotEmpty())
                                MagiskExtend.setSystemProp("ro.product.name", product.toString())
                            if (model.isNotEmpty())
                                MagiskExtend.setSystemProp("ro.product.model", model.toString())
                            if (manufacturer.isNotEmpty())
                                MagiskExtend.setSystemProp("ro.product.manufacturer", manufacturer.toString())
                            if (device.isNotEmpty())
                                MagiskExtend.setSystemProp("ro.product.device", device.toString())
                            // 小米 - 改model参数以后device_features要处理下
                            if (RootFile.fileExists("/system/etc/device_features/${android.os.Build.PRODUCT}.xml")) {
                                if (model != android.os.Build.PRODUCT) {
                                    MagiskExtend.replaceSystemFile("/system/etc/device_features/${product}.xml", "/system/etc/device_features/${android.os.Build.PRODUCT}.xml")
                                }
                            }
                            Toast.makeText(context, "已通过Magisk更改参数，请重启手机~", Toast.LENGTH_SHORT).show()
                        } else {
                            val sb = StringBuilder()
                            sb.append(CommonCmds.MountSystemRW)
                            sb.append("cp /system/build.prop /data/build.prop;chmod 0755 /data/build.prop;")

                            if (brand.isNotEmpty())
                                sb.append("busybox sed -i 's/^ro.product.brand=.*/ro.product.brand=$brand/' /data/build.prop;")
                            if (product.isNotEmpty())
                                sb.append("busybox sed -i 's/^ro.product.name=.*/ro.product.name=$product/' /data/build.prop;")
                            if (model.isNotEmpty())
                                sb.append("busybox sed -i 's/^ro.product.model=.*/ro.product.model=$model/' /data/build.prop;")
                            if (manufacturer.isNotEmpty())
                                sb.append("busybox sed -i 's/^ro.product.manufacturer=.*/ro.product.manufacturer=$manufacturer/' /data/build.prop;")
                            if (device.isNotEmpty())
                                sb.append("busybox sed -i 's/^ro.product.device=.*/ro.product.device=$device/' /data/build.prop;")

                            sb.append("cp /system/build.prop /system/build.bak.prop\n")
                            sb.append("cp /data/build.prop /system/build.prop\n")
                            sb.append("rm /data/build.prop\n")
                            sb.append("chmod 0755 /system/build.prop\n")

                            // 小米 - 改model参数以后device_features要处理下
                            if (RootFile.fileExists("/system/etc/device_features/${android.os.Build.PRODUCT}.xml")) {
                                if (model != android.os.Build.PRODUCT) {
                                    KeepShellPublic.doCmdSync("cp \"/system/etc/device_features/${android.os.Build.PRODUCT}.xml\" \"/system/etc/device_features/${product}.xml\"")
                                }
                            }

                            sb.append("sync\n")
                            sb.append("reboot\n")

                            KeepShellPublic.doCmdSync(sb.toString())
                        }
                    } else {
                        Toast.makeText(context, "什么也没有修改！", Toast.LENGTH_SHORT).show()
                    }
                }).setPositiveButton("使用帮助", DialogInterface.OnClickListener { dialog, which ->
                    AlertDialog.Builder(context).setMessage(R.string.dialog_addin_device_desc).setNegativeButton(R.string.btn_confirm, { _, _ -> }).create().show()
                }).create()

        dialogInstance.window!!.setWindowAnimations(R.style.windowAnim)
        dialogInstance.show()
        loadCurrent()

        try {
            val cm = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val data = cm.getPrimaryClip()
            val item = data.getItemAt(0)
            val content = item.getText()
            if (content.isNotEmpty()) {
                val copyData = String(Base64.decode(content.toString().trim(), Base64.DEFAULT))
                if (Regex("^.*@.*@.*@.*@.*\$").matches(copyData)) {
                    val deviceInfos = copyData.split("@")
                    AlertDialog.Builder(context)
                            .setTitle("可用的模板")
                            .setMessage("检测到已复制的机型信息：\n\n" + copyData + "\n\n是否立即使用？")
                            .setPositiveButton(R.string.btn_confirm, { _, _ ->
                                editModel.setText(deviceInfos[0])
                                editBrand.setText(deviceInfos[1])
                                editProductName.setText(deviceInfos[3])
                                editDevice.setText(deviceInfos[4])
                                editManufacturer.setText(deviceInfos[2])
                            })
                            .setNegativeButton(R.string.btn_cancel, null)
                            .create().show()
                }
            }
        } catch (ex: Exception) {
        }
    }

    private lateinit var editModel: EditText
    private lateinit var editBrand: EditText
    private lateinit var editProductName: EditText
    private lateinit var editDevice: EditText
    private lateinit var editManufacturer: EditText

    private fun loadCurrent() {
        if (getBackupProp(BACKUP_SUCCESS, "false") != "true") {
            return
        } else {
            editBrand.setText(android.os.Build.BRAND)
            editModel.setText(android.os.Build.MODEL)
            editProductName.setText(android.os.Build.PRODUCT)
            editDevice.setText(android.os.Build.DEVICE)
            editManufacturer.setText(android.os.Build.MANUFACTURER)
        }
    }

    private fun setDefault() {
        if (getBackupProp(BACKUP_SUCCESS, "false") != "true") {
            editBrand.setText(android.os.Build.BRAND)
            editModel.setText(android.os.Build.MODEL)
            editProductName.setText(android.os.Build.PRODUCT)
            editDevice.setText(android.os.Build.DEVICE)
            editManufacturer.setText(android.os.Build.MANUFACTURER)
        } else {
            editBrand.setText(getBackupProp(BACKUP_BRAND, android.os.Build.BRAND))
            editModel.setText(getBackupProp(BACKUP_MODEL, android.os.Build.MODEL))
            editProductName.setText(getBackupProp(BACKUP_PRODUCT, android.os.Build.PRODUCT))
            editDevice.setText(getBackupProp(BACKUP_DEVICE, android.os.Build.DEVICE))
            editManufacturer.setText(getBackupProp(BACKUP_MANUFACTURER, android.os.Build.MANUFACTURER))
        }
    }

    private fun setMIX3() {
        editBrand.setText("Xiaomi")
        editModel.setText("MIX 3")
        editProductName.setText("perseus")
        editDevice.setText("perseus")
        editManufacturer.setText("Xiaomi")
    }

    private fun setX20() {
        editBrand.setText("vivo")
        editModel.setText("vivo X20")
        editProductName.setText("X20")
        editDevice.setText("X20")
        editManufacturer.setText("vivo")
    }

    private fun setR11Plus() {
        editBrand.setText("OPPO")
        editModel.setText("OPPO R11 Plus")
        editProductName.setText("R11 Plus")
        editDevice.setText("R11 Plus")
        editManufacturer.setText("OPPO")
    }

    private fun setIPhoneX() {
        editBrand.setText("iPhone")
        editModel.setText("X")
        editProductName.setText("hydrogen")
        editDevice.setText("hydrogen")
        editManufacturer.setText("iPhone")
    }

    private fun setS9Plus() {
        editBrand.setText("samsung")
        editModel.setText("SM-G9650")
        editProductName.setText("star2qltezc")
        editDevice.setText("star2qltechn")
        editManufacturer.setText("samsung")
    }

    private fun setR15Plus() {
        editBrand.setText("OPPO")
        editModel.setText("PAAM00")
        editProductName.setText("OPPO PAAM00")
        editDevice.setText("PAAM00")
        editManufacturer.setText("OPPO")
    }

    private fun set8848() {
        editBrand.setText("ERENEBEN")
        editModel.setText("EBEN M2")
        editProductName.setText("EBEN M2")
        editDevice.setText("EBEN M2")
        editManufacturer.setText("ERENEBEN")
    }

    private fun setMateRS() {
        editBrand.setText("HUAWEI")
        editModel.setText("NEO-AL00")
        editProductName.setText("NEO-AL00")
        editDevice.setText("NEO-AL00")
        editManufacturer.setText("HUAWEI")
    }

    @SuppressLint("ApplySharedPref")
    private fun backupDefault() {
        if (getBackupProp(BACKUP_SUCCESS, "false") != "true") {
            Props.setPorp(BACKUP_BRAND, android.os.Build.BRAND)
            Props.setPorp(BACKUP_MODEL, android.os.Build.MODEL)
            Props.setPorp(BACKUP_PRODUCT, android.os.Build.PRODUCT)
            Props.setPorp(BACKUP_DEVICE, android.os.Build.DEVICE)
            Props.setPorp(BACKUP_MANUFACTURER, android.os.Build.MANUFACTURER)
            Props.setPorp(BACKUP_SUCCESS, "true")
        }
    }
}
