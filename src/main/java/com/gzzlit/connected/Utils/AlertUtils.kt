package com.gzzlit.connected.Utils

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import com.gzzlit.connected.CommonDependencies
import com.gzzlit.connected.Entities.ConnectedResult
import com.gzzlit.connected.Entities.DeviceType
import com.gzzlit.connected.Entities.Failure
import com.gzzlit.connected.Presentation.QRCodeScanMaker
import com.gzzlit.connected.R
import kotlinx.android.synthetic.main.dialog_alert_tip.view.*
import java.text.SimpleDateFormat
import java.util.*

internal fun showErrorAlert(dependencies: CommonDependencies, result: ConnectedResult?) {
    if (result is Failure) {
        showAlert(dependencies, title = result.asTitle(), message = result.asDesc())
    }
}

internal fun showBillAlert(dependencies: CommonDependencies, money: Int, time: String) {
    val dateFormatter = SimpleDateFormat()
    dateFormatter.applyPattern("yyyyMMddHHmmss")
    val date = dateFormatter.parse(time) ?: Date()
    dateFormatter.applyPattern("yyyy-MM-dd HH:mm:ss")
    val dateString = dateFormatter.format(date)
    val moneyString = String.format("%.2f", money / 100.0)
    val deviceName = dependencies.makeDevice().info.name.replace("NB", "").replace("4G", "")
    showAlert(dependencies, title = "报告小主！您刚才${if (deviceName.contains("水控")) "洗澡" else deviceName}", message = dateString + "\n\n花了：" + moneyString + "元")
}


fun showRawAlert(activity: Activity, title: String, message: Any? = null, acTitle: List<String> = listOf("确定", "取消"), handler: ((Int) -> Unit)? = null) {
    showAlert(null, activity = activity, title = title, message = message, acTitle = acTitle, handler = handler)
}


fun showAlert(dependencies: CommonDependencies?, activity: Activity? = null, title: String, message: Any? = null, acTitle: List<String> = listOf("确定", "取消"), handler: ((Int) -> Unit)? = null) {
    var desc = "${message ?: ""}"
    if (title == "设备正在忙" || title == "关阀失败建议") {
        desc = if (dependencies?.makeDevice()?.deviceType == DeviceType.bath){
            "如未有人使用，可以手动关闭：\n- 长按水表蓝色按钮5秒；\n- 待嘀一声结束后松开  ；\n- 如未成功请重复一次  。"
        }else{
            "请稍后再使用。"
        }
    }
    log("走对话框 title:$title msg:$message")
    val activity = activity ?: dependencies?.makeActivity()
    hideHUD(activity)
    if (title.isEmpty()&&desc.isEmpty())return
    activity?.runOnUiThread {
        val dialog = AlertDialog.Builder(activity).create()
        val hasNegative = handler != null && acTitle.size == 2 && acTitle.last() != ""
        val layout = LayoutInflater.from(activity).inflate(R.layout.dialog_alert_tip, null)
        layout.tvTitle.text = title
        if (desc != ""){
            layout.tvMessage.visibility = View.VISIBLE
            layout.tvMessage.text = desc
        }
        layout.tvConfirm.text = acTitle.first()
        layout.tvConfirm.setOnClickListener{
            dialog.dismiss()
            handler?.invoke(DialogInterface.BUTTON_POSITIVE)
            if (activity is QRCodeScanMaker){
                activity.restart()
            }
        }
        if (hasNegative) {
            layout.tvCancel.text = acTitle.last()
            layout.tvCancel.visibility = View.VISIBLE
            layout.tvCancel.setOnClickListener {
                dialog.dismiss()
                handler?.invoke(DialogInterface.BUTTON_NEGATIVE)
                if (activity is QRCodeScanMaker) {
                    activity.restart()
                }
            }
        }
        dialog.show()
        val displayWidth = activity.resources.displayMetrics.widthPixels
        // 设置宽度为屏幕的宽度
        val lp = dialog.window?.attributes
        lp?.width = displayWidth * 3 / 4 // 设置宽度
        dialog.window?.attributes = lp
        dialog.window?.setBackgroundDrawable(null)
        dialog.window?.setContentView(layout)
    }
}