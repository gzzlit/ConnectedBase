package com.gzzlit.connected

import android.content.SharedPreferences
import android.support.v4.app.FragmentActivity
import com.gzzlit.connected.Command.Command
import com.gzzlit.connected.Command.CommandDependencies
import com.gzzlit.connected.Command.CommandExecuter
import com.gzzlit.connected.Command.CommandReceiver
import com.gzzlit.connected.Entities.Action
import com.gzzlit.connected.Entities.ConnectedResult
import com.gzzlit.connected.Entities.Device
import com.gzzlit.connected.Presentation.DeviceListActivity
import com.gzzlit.connected.Presentation.QRCodeScanMaker
import com.gzzlit.connected.Process.Process
import com.gzzlit.connected.Process.ProcessDependencies
import com.gzzlit.connected.Process.ProcessExecuter
import com.gzzlit.connected.Utils.*
import org.jetbrains.anko.defaultSharedPreferences
import kotlin.reflect.KClass

//private var  weakActivity:WeakReference<FragmentActivity>?=null
//
//val activity:FragmentActivity? get() = DeviceListActivity.self ?: weakActivity?.get()

interface CommonDependencies {
    var progress: String
    fun makeStudentId() : String
    fun makeUserId() : String
    fun makeDevice() : Device
    fun makeHost() : String
    fun makeProgressHUD() : ProgressHUD

    fun makeShowHUD(text: String?) : ProgressHUD?
    fun makeDelayHUD(text: String?) : ProgressHUD?
    fun makeResultHUD(result: ConnectedResult?, action: Action): ProgressHUD?
    fun makeHideHUD()

    fun makeErrorAlert(result: ConnectedResult?)
    fun makeBillAlert(money: Int, time: String)
    fun makeAlert(title: String, message: Any? = null)

    fun makeActivity():FragmentActivity?

    fun makePrefs():SharedPreferences?
}

lateinit var NetClass:KClass<out Networking>
lateinit var BleClass:KClass<out BLEService>
lateinit var HudClass:KClass<out ProgressHUD>
lateinit var ScanClass:KClass<out QRCodeScanMaker>
var CommandReceiverClass:KClass<out CommandReceiver> = CommandReceiver::class

lateinit var Host:String
lateinit var UserId:(()->(String))

data class Dependencies(val activity: FragmentActivity,
                        val device: Device = Device(mac = "") ){
}

class DIContainer internal constructor(private val dependencies: Dependencies) : CommandDependencies, ProcessDependencies {
//    init {
//        weakActivity = WeakReference(dependencies.activity)
//    }
    private val commandReceiver: CommandReceiver by lazy { CommandReceiverClass.constructors.first().call(this) }
    private val commandExecuter: CommandExecuter by lazy { CommandExecuter(dependencies = this) }
    private val processExecuter: ProcessExecuter by lazy { ProcessExecuter(dependencies = this) }
    private val networking: Networking by lazy { NetClass.constructors.first().call(this)}
    private val bleService: BLEService by lazy { BleClass.constructors.first().call(this)}

    var washMode:WashMode? = null

    var isShowHUD = true

    override var progress: String = ""
        set(newValue) {
            if (isShowHUD) makeShowHUD(newValue)
        }

    override fun makeCommandReceiver(): CommandReceiver? = commandReceiver

    override fun makeNetworking(): Networking = networking

    override fun makeStudentId(): String = UserId().substring(7)

    override fun makeUserId(): String = UserId()

    override fun makeDevice(): Device = dependencies.device

    override fun makeHost(): String  = Host

    override fun makeProgressHUD(): ProgressHUD = HudClass.constructors.first().call(makeActivity())

    override fun makeCommandExecuter(): CommandExecuter = commandExecuter

    override fun makeCommand(commandType: KClass<out Command>): Command = commandType.constructors.first().call(this)

    override fun makeProcess(processType: KClass<out Process>) : Process = processType.constructors.first().call(this)

    override fun makeProcessExecuter(): ProcessExecuter = processExecuter

    override fun makeBLEService(): BLEService = bleService

    override fun makeShowHUD(text: String?): ProgressHUD? = showHUD(this,text)

    override fun makeDelayHUD(text: String?): ProgressHUD? =  showHUD(this,text,1200L)

    override fun makeResultHUD(result: ConnectedResult?, action: Action): ProgressHUD? =  showHUD(this,result,action)

    override fun makeHideHUD()  = hideHUD(makeActivity())

    override fun makeErrorAlert(result: ConnectedResult?) = showErrorAlert(this,result)

    override fun makeBillAlert(money: Int, time: String) = showBillAlert(this,money,time)

    override fun makeAlert(title: String, message: Any?) = showAlert(this,title = title,message = message)

    override fun makeActivity(): FragmentActivity?  = DeviceListActivity.self?.get() ?: (QRCodeScanMaker.self?.get() ?: dependencies.activity)

    override fun makePrefs(): SharedPreferences?  = makeActivity()?.defaultSharedPreferences

    override fun makeWashMode() : WashMode? = washMode

}

