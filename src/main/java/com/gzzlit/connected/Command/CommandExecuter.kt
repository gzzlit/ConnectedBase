package com.gzzlit.connected.Command

import com.gzzlit.connected.Entities.*
import com.gzzlit.connected.Utils.Weak
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

//
//  CommandExecuter.swift
//  Pods
//
//  Created by a on 2020/9/21.
//

class CommandExecuter(dependencies: CommandDependencies) {

    private var dependencies = WeakReference(dependencies)

    private var commandTypes: List<KClass<out Command>> = listOf()
    internal var action: Action = Action.none
    private var commands = mutableListOf<Command?>()

    fun execute(action: Action = Action.none, data: ConnectedSuccess?=null, commandTypes: List<KClass<out Command>>, completion: ConnectedCompletion) {
        this.action = action
        this.commandTypes = commandTypes
        this.executeCommand(0, data,completion)
    }

    private fun executeCommand(index: Int, lastResult: ConnectedResult?, completion: ConnectedCompletion) : Command? {
        if (index < commandTypes.size) {
            val commandType = commandTypes[index]
            val command = dependencies.get()?.makeCommand(commandType)
            val self by Weak(this)
            if (command?.isConcurrent == true && index == commandTypes.size - 2) {
                //只有倒数第二个命令才能与最后一个命令并发执行
                commands.add(command)
                command.execute(action, lastResult?.get()) { result  ->
                    if (result is Success) {
                        self?.completionHandler(result,completion)
                    }
                }
                commands.add(executeCommand(index + 1, lastResult,completion))
            } else {
                command?.execute(action,lastResult?.get()) { result  ->
                    if (result is Success) {
                        self?.executeCommand(index + 1, result,completion)
                    } else {
                        self?.completionHandler(result,completion)
                    }
                }
            }
            return command
        } else if (lastResult != null) {
            completionHandler(lastResult,completion)
        } else {
            completionHandler(Failure("失败"),completion)
        }
        return null
    }

    internal fun completionHandler(result: ConnectedResult?, completion: ConnectedCompletion) {
        commands.forEach { command  ->
            command?.cancel()
        }
        commands.clear()
        //      commandTypes.removeAll()
        completion.invoke(result)
    }

}