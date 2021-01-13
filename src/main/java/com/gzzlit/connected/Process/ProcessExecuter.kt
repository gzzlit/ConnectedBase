package com.gzzlit.connected.Process

import com.gzzlit.connected.Entities.*
import com.gzzlit.connected.Utils.Weak
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

//
//  ProcessExecuter.swift
//  ConnectedSDK
//
//  Created by a on 2020/10/14.
//

class ProcessExecuter(dependencies: ProcessDependencies) {
    private var processTypes: List<KClass<out Process>> = listOf()
    internal var completion: ConnectedCompletion? = null
    internal var action: Action = Action.none
    internal var dependencies = WeakReference(dependencies)

    internal fun execute(action: Action, processTypes: List<KClass<out Process>>, completion: ConnectedCompletion) {
        this.completion = completion
        this.processTypes = processTypes
        this.action = action
        this.executeProcess(0,  null)
    }

    private fun executeProcess(index: Int, lastResult: ConnectedResult?) {
        if (index < processTypes.size) {
            val processType = processTypes[index]
            if (processType.simpleName == "OfflineProcess" && lastResult != null) {
                if (lastResult.asTitle() != "${NetError.接口异常}") {
                    //服务异常才走离线步骤
                    completion?.invoke(lastResult)
                    return
                }
            }
            val self by Weak(this)
            dependencies.get()
                    ?.makeProcess(processTypes[index])
                    ?.execute(action) { result ->
                        if (result is Success || result?.asTitle() == "${NetError.请求失败}") {
                            self?.processTypes = listOf()
                            self?.completion?.invoke(result)
                        } else
                            self?.executeProcess(index + 1, result)

                    }
        } else if (lastResult != null) {
            processTypes = listOf()
            completion?.invoke(lastResult)
        } else {
            processTypes = listOf()
            completion?.invoke(Failure("失败"))
        }
    }
}
