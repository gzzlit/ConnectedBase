package com.gzzlit.connected.Utils

import android.app.Activity
import android.content.Context
import android.os.Handler
import com.gzzlit.connected.CommonDependencies
import com.gzzlit.connected.Entities.Action
import com.gzzlit.connected.Entities.ConnectedResult
import com.gzzlit.connected.Entities.Failure


//
//  ConnectedSDK
//  Created by a on 2020/10/27.
//

interface ProgressHUD {
    var context:Context
    fun updateLabel(label: String)
    fun dismiss()
    fun show(label: String): ProgressHUD
    var isViewGo: Boolean
}

var huds:MutableMap<Context?, ProgressHUD?> = mutableMapOf()


fun showHUD(dependencies: CommonDependencies, label: String?, millisecond: Long = 0):ProgressHUD?{
    if (label == null || label.isEmpty()) {
        return null
    }
    val activity = dependencies.makeActivity()
    activity?.runOnUiThread {
        var hud = huds[activity]
        val label = "  $label  "
        if (hud != null&&!hud.isViewGo) {
            hud.updateLabel(label)
        } else {
            hud = dependencies.makeProgressHUD().show(label)
            huds[activity] = hud
        }
        if (millisecond>0) {
            hideHUD(activity,hud,millisecond)
        }
    }
    return huds[activity]
}

fun showHUD(dependencies: CommonDependencies, result: ConnectedResult?, action: Action):ProgressHUD? {
    return if (result is Failure) {
        showHUD(dependencies, result.asTitle(), 1200L)
    } else if (result != null){
        var text = result.get()?.value as? String
        text = if (text==null || text.startsWith("http")) "${action.name}成功" else text
        showHUD(dependencies, text, 1200L)
    }else null
}

fun hideHUD(activity: Activity?){
    activity?.runOnUiThread {
        huds.values.forEach {
            it?.isViewGo = true
            try {
                it?.dismiss()
            } catch (e: Exception) {
            }
        }
        huds.clear()
    }
}

fun hideHUD(activity: Activity?,hud:ProgressHUD?, millisecond: Long = 0) {
    activity?.runOnUiThread {
        hud?.isViewGo = true
        val handler = Handler()
        handler.postDelayed({
            try {
                hud?.dismiss()
            } catch (e: Exception) {
            }
        }, millisecond)
    }
}

