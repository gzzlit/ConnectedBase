package com.gzzlit.connected.Command

import com.gzzlit.connected.Entities.*
import com.gzzlit.connected.Utils.BLEService
import com.gzzlit.connected.Utils.Weak
import com.gzzlit.connected.Utils.log
import java.lang.ref.WeakReference
import java.util.*


const val SEND_TIMEOUT = 3000L

open class CommandReceiver(dependencies: CommandDependencies) {

    private var dependencies = WeakReference(dependencies)

    private val studentId by lazy {this.dependencies.get()?.makeStudentId()}
    private val device by lazy { this.dependencies.get()?.makeDevice()}
    private val timer by lazy { Timer()}

    var command:Command? = null
        private set
    var sendData: String = ""
        private set
    var readData: String = ""

    private var timeoutTask: TimerTask? = null
    private var bleService: BLEService?=null

    var isSending = false
        private set

    internal fun receiveBySend(data: String, command:Command?, completion: ConnectedCompletion) {
        this.command = command
        this.sendData = data
        this.readData = ""
        bleService = dependencies.get()?.makeBLEService()
        //新建，避免上个查找设备未完成时，又添加一个查找，重复completion回调，并让上个命令移除监听，
        val self by Weak(this)
        bleService?.connectCompletion = { result  ->
            self?.connectCompletion(result,data, self?.command?.name,completion);
        }
        dependencies.get()?.progress = "连接中..."
        bleService?.findConnect(device)
    }

    private fun connectCompletion(result:ConnectedResult?,data: String,name: String?, completion: ConnectedCompletion){
        bleService?.connectCompletion = null
        if (result is Success) {
            if (name != null) {
                dependencies.get()?.progress = "${name}中..."
            }
            val self by Weak(this)
            bleService?.sendCompletion = { result  ->
                self?.sendCompletion(result,completion)
            }
            timeoutTask = object:TimerTask(){
                override fun run() {
                    self?.makeResult(Failure("发送失败", "设备返回数据超时"),completion)
                }
            }
            timer.schedule(timeoutTask, SEND_TIMEOUT)
            isSending = true
            bleService?.send(device?.mac,data)
        } else {
            makeResult(result,completion)
        }
    }

    private fun sendCompletion(result:ConnectedResult?,completion: ConnectedCompletion){
        if (result is Success) {
            resovle(result.asTitle().toUpperCase(), completion)
        } else {
            makeResult(result,completion)
        }
    }

    //        bleService.connect(mac: device.mac)
    internal fun resovle(hex: String, completion: ConnectedCompletion) {
        if (readData==""&&!hasPrefixA5(hex)){
            log("异常数据 不是A5开头 -> $hex ")
            return
        }
        readData += hex
        if (changeChar()&&studentId!=null) {
            val backDataResult = getBackData(studentId!!)
            makeResult(backDataResult,completion)
        }
    }


    internal fun makeResult(result: ConnectedResult?, completion: ConnectedCompletion) {
        timeoutTask?.cancel()
        timeoutTask =null
        bleService?.sendCompletion = null
        isSending = false
        command = null
        completion(result)
    }

    open fun hasPrefixA5(hex: String) : Boolean = hex.startsWith("A5")
    open fun changeChar() : Boolean {
        return true
    }

    open fun getBackData(studentId: String) : ConnectedResult = Success(readData)

}