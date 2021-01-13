package com.gzzlit.connected.Entities

import com.gzzlit.connected.Utils.log
import java.lang.Exception

//
//  ConnectedError.swift
//  ConnectedSDK
//
//  Created by a on 2020/10/9.
//
fun main() {
//    val s = Failure(NetError.接口异常)
//    print(s.asAlarm())
}
typealias ConnectedResult = Result<Any>
typealias ConnectedSuccess = Success<Any>
typealias ConnectedError = Failure<Any>
typealias ConnectedCompletion = (ConnectedResult?) -> Unit

val ConnectedResult.isSuccess get() = this is Success

open class Result<T : Any>(open val value:T? = null, private val title:Any?=null, private val desc:Any?=null){
    fun asTitle(): String {
        return when(val value = value ?: title){
            is String -> value
            is Array<*> -> value.first() as? String ?: ""
            is Map<*, *> -> value["data"] as? String ?: ""
            else -> "$value"
        }
    }

    fun asDesc(): String {
        return "$desc"
    }

    fun get():Success<T>? {
        return if (this is Success){
            this
        }else{
            null
        }
    }

}

class Success<T:Any>(override val value:T, val desc:Any=""):Result<T>(value = value,desc = desc){
    override fun toString(): String {
        return javaClass.simpleName+"(value=$value, desc=$desc)"
    }
    fun asMap(): Map<String,Any> {
        return value as? Map<String, Any> ?: mapOf()
    }
    val successValue by lazy {ConnectedSuccess(value)}
}

class Failure<T:Any>(val title:Any, val desc:Any=""): Result<T>(title = title,desc = desc){
    override fun toString(): String {
        return javaClass.simpleName+"(title=$title, desc=$desc)"
    }
    val failurError by lazy {ConnectedError(title,desc)}
}

val String.toInt16: Int get() = try { this.toInt(16) }catch(e:Exception){log(e);0}

fun String.substr(loc:Int,len:Int): String  = try { this.substring(loc,loc+len) }catch(e:Exception){log(e);""}

val Map<String,Any>.val_: String get() = str("val")

fun Map<String,Any>.str(key: String) : String = "${this[key] ?: ""}"

fun Map<String,Any>.float(key: String) : Float = this[key] as? Float ?: Float.NaN

fun Map<String,Any>.dic(key: String) : Map<String, Any> = this[key] as? Map<String, Any> ?: mapOf()

val Map<String,Any>.data : String get() = this["data"] as? String ?: ""

enum class NetError {
    数据异常,
    接口异常,
    请求失败
}
enum class MsgError {
    选择设备
}