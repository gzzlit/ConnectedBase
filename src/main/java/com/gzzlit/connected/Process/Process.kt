package com.gzzlit.connected.Process

import com.gzzlit.connected.Command.CommandExecuter
import com.gzzlit.connected.CommonDependencies
import com.gzzlit.connected.Entities.Action
import com.gzzlit.connected.Entities.ConnectedCompletion
import kotlin.reflect.KClass

//
//  OperatedWay.swift
//  ZhiXiang
//
//  Created by a on 2020/9/18.
//  Copyright © 2020 广州智磊互联网科技有限公司. All rights reserved.
//

interface ProcessDependencies: CommonDependencies {
    fun makeProcessExecuter() : ProcessExecuter
    fun makeCommandExecuter() : CommandExecuter
    fun makeProcess(processType: KClass<out Process>) : Process
}

abstract class Process(var dependencies: ProcessDependencies) {
    abstract fun execute(action: Action, completion: ConnectedCompletion)
}