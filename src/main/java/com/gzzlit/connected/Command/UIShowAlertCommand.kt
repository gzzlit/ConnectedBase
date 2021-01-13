package com.gzzlit.connected.Command

import android.content.DialogInterface
import com.gzzlit.connected.Entities.*
import com.gzzlit.connected.Utils.showAlert

open class UIShowAlertCommand(dependencies: CommandDependencies) : Command(dependencies) {
    open val title: String
        get() = ""
    open val message: String?
        get() = null
    open val acTitle: List<String>
        get() = listOf("确定", "取消")

    override fun execute(action: Action, data: ConnectedSuccess?, completion: ConnectedCompletion) {
        showAlert(dependencies, title = title, message = message, acTitle = acTitle) { action  ->
            if (action == DialogInterface.BUTTON_POSITIVE) {
                completion(Success(acTitle.first()))
            } else {
                completion(Failure(acTitle.last()))
            }
        }
    }
}
