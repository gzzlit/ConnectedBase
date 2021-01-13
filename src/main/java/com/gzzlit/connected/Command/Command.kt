package com.gzzlit.connected.Command

import android.os.Handler
import com.gzzlit.connected.CommonDependencies
import com.gzzlit.connected.Entities.*
import com.gzzlit.connected.Utils.BLEService
import com.gzzlit.connected.Utils.Networking
import com.gzzlit.connected.Utils.log
import com.gzzlit.connected.WashMode
import kotlin.reflect.KClass

//  Command.swift
//  Pods
//
//  Created by a on 2020/9/21.


interface CommandDependencies: CommonDependencies {
    fun makeCommandReceiver() : CommandReceiver?
    fun makeCommand(commandType: KClass<out Command>) : Command
    fun makeNetworking() : Networking
    fun makeBLEService() : BLEService
    fun makeWashMode() : WashMode?

}

open class Command constructor(var dependencies: CommandDependencies) {

    var action: Action = Action.none
        private set

    open val isConcurrent: Boolean get() = false
    open val cmd: String? get() = null
    open val parm: String? get() = null
    var data: String? = null
    open val name: String? get() = null
    var isCancelled: Boolean = false
    val userId by lazy {dependencies.makeUserId()}
    val studentId by lazy {dependencies.makeStudentId()}
    val device by lazy {dependencies.makeDevice()}
    val prefs by lazy {dependencies.makePrefs()}

    open fun execute(action: Action = Action.none, data: ConnectedSuccess? = null, completion: ConnectedCompletion) {

        if (isCancelled) {
            log("取消命令 ${cmd ?: ""} ${parm ?: ""}")
            return
        }
        if (data != null && data.asTitle() == "采集成功") {
            completion(data)
            return
        }

        if (data != null && data.value is Device) {
            device.mac = data.value.mac
        }
        this.data = data?.asTitle()
        val cmd = cmd
        if (cmd != null) {
            log("执行命令 ${cmd} ${parm ?: ""}")
            val data = getData(cmd, parm ?: "")
            if (data != null) {
                send(data,completion)
            } else {
                completion(Failure("获取${cmd}命令失败"))
            }
        }
    }

    open fun getData(cmd: String, parm: String) : String? =
            null

    private fun send(data: String,completion: ConnectedCompletion,count:Int = 0){
        val receiver = dependencies.makeCommandReceiver()
        if (receiver?.isSending == false||count>2) {
            receiver?.receiveBySend(data, this, completion)
        }else{
            Handler().postDelayed({
                send(data,completion,count+1)
            },1000)
        }
    }

    internal fun cancel() {
        isCancelled = true
    }
}

