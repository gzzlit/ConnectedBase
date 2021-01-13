package com.gzzlit.connected

import android.support.v4.app.FragmentActivity
import com.gzzlit.connected.Command.*
import com.gzzlit.connected.Entities.*
import com.gzzlit.connected.Process.*
import com.gzzlit.connected.Utils.Weak
import com.gzzlit.connected.Utils.log
import com.gzzlit.connected.Utils.logInfo
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

//  OperatedManager.swift
//  ZhiXiang
//
//  Created by a on 2020/9/18.
//  Copyright © 2020 广州智磊互联网科技有限公司. All rights reserved.
//
enum class SwitchMode (val rawValue: Int) {
    优先(0), 联网(1), 蓝牙(2), 离线(3), 日志(4);

    companion object {
        operator fun invoke(rawValue: Int): SwitchMode? = SwitchMode.values().firstOrNull { it.rawValue == rawValue }
    }
}

enum class WashMode (val rawValue: Int) {
    无(0), 脱水(1), 快洗(2), 标准洗(3), 大件洗(4);

    companion object {
        operator fun invoke(rawValue: Int) = WashMode.values().firstOrNull { it.rawValue == rawValue }
    }
}

typealias ObjCompletion = (Map<String, Any>?) -> Unit

fun initManager(activity: FragmentActivity, device: Device = Device()) : ConnectedManager = ConnectedManager(Dependencies(activity, device))

open class ConnectedManager(dependencies: Dependencies) {

    var washMode: Int = 0
        set(newValue) {
            field = newValue
            if (newValue != 0) {
                this.diContainer.washMode = WashMode(rawValue = newValue)
            }
        }

    var isLog: Boolean = false
        set(newValue) {
            field = newValue
            if (newValue) {
                logInfo = ""
            }
        }
    val getLog: String?
        get() = logInfo

    val diContainer: DIContainer = DIContainer(dependencies = dependencies)

    companion object{
        @JvmStatic
        fun get(activity: FragmentActivity) = initManager(activity)
        @JvmStatic
        fun get(activity: FragmentActivity, device: Device) = initManager(activity, device)
    }

    init {
        logInfo = null
    }

    fun clear(){
        diContainer.makeBLEService().clear()
    }

    fun hadDevice(device: Device) : Boolean = device.mac == diContainer.makeDevice().mac

    fun execute(action: Action = Action.none, isShowHUD: Boolean = true, processTypes: List<KClass<out Process>>, completion: ConnectedCompletion) {
        val self by Weak(this)
        diContainer.isShowHUD = isShowHUD
        log("${action.name}开始 --------------------------------------------------------------------------")
        diContainer.makeProcessExecuter().execute(action = action, processTypes = processTypes) { result  ->
            log("${action.name}结束 ${result}")
            if (isShowHUD) {
                self?.diContainer?.makeResultHUD(result,action)
            }
            completion(result)
        }
    }

    fun execute(action: Action = Action.none, isShowHUD: Boolean = true, data: ConnectedSuccess? = null, commandTypes: List<KClass<out Command>>, completion: ConnectedCompletion) {
        val self by Weak(this)
        diContainer.isShowHUD = isShowHUD
        log("${action.name}开始 --------------------------------------------------------------------------")
        diContainer.makeCommandExecuter().execute(action = action, data = data, commandTypes = commandTypes) { result  ->
            log("${action.name}结束 ${result}")
            if (isShowHUD) {
                self?.diContainer?.makeResultHUD(result,action)
            }
            completion(result)
        }
    }

    fun makeResult(result: ConnectedResult?, completion: ConnectedCompletion) {
        diContainer.makeErrorAlert(result = result)
        completion(result)
    }

    fun objResult(result: ConnectedResult?, completion: ObjCompletion) {
        diContainer.makeErrorAlert(result)
        completion(result?.get()?.value as? Map<String, Any>)
    }
}
