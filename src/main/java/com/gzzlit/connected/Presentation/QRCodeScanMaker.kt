package com.gzzlit.connected.Presentation

import android.support.v4.app.FragmentActivity
import com.gzzlit.connected.Command.UIPresentedMaker
import java.lang.ref.WeakReference

interface QRCodeScanMaker:UIPresentedMaker {
    fun restart()
    companion object{
        var closure: ((QRCodeScanMaker) -> Unit)? = null
        var self: WeakReference<FragmentActivity>? = null
        var tips: String? = null
    }
}