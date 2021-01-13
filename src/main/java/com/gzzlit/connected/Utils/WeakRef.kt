package com.gzzlit.connected.Utils

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty


class Weak<T : Any>(obj: T?) {
    var weakReference:WeakReference<T?>

    init {
        weakReference = WeakReference(obj)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return weakReference.get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        weakReference = WeakReference(value)
    }
}