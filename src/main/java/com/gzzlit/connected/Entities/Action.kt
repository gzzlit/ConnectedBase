package com.gzzlit.connected.Entities

//
//  Action.swift
//  ConnectedSDK
//
//  Created by a on 2020/9/30.
//
fun main() {
    print(Action.new("采集自己").name)
}
sealed class Action {
    object none : Action()
    object opening : Action()
    object closing : Action()
    data class collect(val type: CollectType = CollectType.all, val isAlert: Boolean = false) : Action()
    data class card(val type: CardAlter) : Action()
    data class pick(val type: PickType = PickType.none) : Action()

    val name: String
        get() {
            return when (this) {
                opening -> "开阀"
                closing -> "关阀"
                is collect -> if (type==CollectType.myself) "采集自己 " else if (type==CollectType.others) "采集别人 " else "采集"
                is card -> if (type==CardAlter.bind) "绑卡" else "解绑"
                is pick -> when (type){
                    PickType.register -> "注册"
                    PickType.settings -> "设置"
                    else  -> "获取"
                }
                else -> ""
            }
        }
    companion object{
        fun new(name: String) : Action {
            return when (name) {
                "开阀" -> opening
                "关阀" -> closing
                "采集自己" -> collect(CollectType.myself, isAlert = true)
                "采集别人" -> collect(CollectType.others, isAlert = true)
                else -> none
            }
        }
    }
}
enum class CollectType {
    all,
    myself,
    others
}
enum class CardAlter {
    bind,
    unbind
}
enum class PickType {
    none,
    register,
    settings;

    companion object{
        fun new(name: String) : PickType {
            return when (name) {
                "setting" -> settings
                "register" -> register
                else -> none
            }
        }
    }

}
