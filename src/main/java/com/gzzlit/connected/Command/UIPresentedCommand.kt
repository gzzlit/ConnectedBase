package com.gzzlit.connected.Command

import android.content.Intent
import com.gzzlit.connected.Entities.*
import com.gzzlit.connected.Presentation.*
import com.gzzlit.connected.ScanClass

//
//  UIConfirmCommand.swift
//  ConnectedSDK
//
//  Created by a on 2020/10/22.
//
interface UIPresentedMaker {
    var completion: ConnectedCompletion?
    var entrySuccess: ConnectedSuccess?
    var dependencies: CommandDependencies?
}

abstract class UIPresentedCommand<T:UIPresentedMaker>(dependencies: CommandDependencies) : Command(dependencies) {
    override fun execute(action: Action, data: ConnectedSuccess?, completion: ConnectedCompletion) {
        dependencies.makeHideHUD()
        this.show(){
            it.entrySuccess = data
            it.completion = completion
            it.dependencies = dependencies
        }
    }
    abstract fun show(closure: (T) -> Unit)
}

class UIQRCodeScanCommand(dependencies: CommandDependencies) : UIPresentedCommand<QRCodeScanMaker>(dependencies) {
    override fun show(closure: (QRCodeScanMaker) -> Unit) {
        QRCodeScanMaker.closure = closure
        val intent = Intent(dependencies.makeActivity(), ScanClass.java)
        dependencies.makeActivity()?.startActivity(intent)
    }
}

class UIDeviceListCommand(dependencies: CommandDependencies) : UIPresentedCommand<DeviceListMaker>(dependencies) {
    override fun show(closure: (DeviceListMaker) -> Unit) {
        DeviceListActivity.closure = closure
        val intent = Intent(dependencies.makeActivity(),DeviceListActivity::class.java)
        dependencies.makeActivity()?.startActivity(intent)
    }

}