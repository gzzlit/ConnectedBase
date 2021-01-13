package com.gzzlit.connected.Presentation

import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.gzzlit.connected.Command.CommandDependencies
import com.gzzlit.connected.Command.UIPresentedMaker
import com.gzzlit.connected.Entities.ConnectedCompletion
import com.gzzlit.connected.Entities.ConnectedSuccess
import com.gzzlit.connected.R

open class BaseDialogMaker : DialogFragment(), UIPresentedMaker {

    override var completion: ConnectedCompletion? = null
    override var entrySuccess: ConnectedSuccess? = null
    override var dependencies: CommandDependencies? = null

    var data = mutableMapOf<String,Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data = (entrySuccess?.value as? Map<String, Any>)?.toMutableMap() ?:mutableMapOf()
        setStyle(STYLE_NORMAL, R.style.DialogMakerStyle)
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(false)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        completion = null
        entrySuccess = null
        dependencies = null
    }
}