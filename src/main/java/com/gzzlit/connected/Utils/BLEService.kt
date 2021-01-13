package com.gzzlit.connected.Utils

import com.gzzlit.connected.CommonDependencies
import com.gzzlit.connected.Entities.*
import java.lang.ref.WeakReference

//
//  Bluetooth.swift
//  ConnectedSDK
//
//  Created by a on 2020/10/12.
//

abstract class BLEService(dependencies: CommonDependencies) {

    var dependencies = WeakReference(dependencies)

    var connectCompletion: ConnectedCompletion? = null
    var sendCompletion: ConnectedCompletion? = null
    var notification: (()->Unit)? = null

    open val resKey: String get() = ""

    var deviceList = mutableListOf<Device> ()

    abstract fun clear()

    abstract fun startScan()

    abstract fun stopScan()

    abstract fun find(mac: String? = null , sn:String?=null, completion: ((Device?) -> Unit)? = null )

    abstract fun isConnected(mac: String):Boolean

    abstract fun connect(mac: String,count:Int)

    abstract fun send(mac:String?, data: String)

    abstract fun disconnect()

//    deinit {
//        log("结束监听")
//        removeObserver()
//    }
    fun connectResult(result: ConnectedResult) {
        if (connectCompletion != null) {
            log("连接结果 " + result.asTitle())
            connectCompletion?.invoke(result)
        }
    }

    fun sendResult(result: ConnectedResult) {
        if (sendCompletion != null) {
            log("接受数据 " + result.asTitle().toUpperCase())
            sendCompletion?.invoke(result)
        }
    }

    fun findConnect(device: Device?) {
        if (device!=null&&device.mac.isEmpty()){
            val self by Weak(this)
            find(sn=device.sn){
                device.mac = it?.mac ?: ""
                self?.connect(it)
            }
        }else{
            connect(device)
        }
    }

    private fun connect(device: Device?){
        if (device != null){
            if (isConnected(device.mac)) {
                log("已经连接 " + device.mac)
                connectCompletion?.invoke(Success("已经连接"))
            } else {
                log("开始连接 " + device.mac)
                connect(device.mac,0)
            }
        }else{
            connectCompletion?.invoke(Failure("连接失败","找不到设备"))
        }

    }
}
