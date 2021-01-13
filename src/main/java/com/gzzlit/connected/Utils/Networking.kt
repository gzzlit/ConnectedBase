package com.gzzlit.connected.Utils

import com.gzzlit.connected.CommonDependencies
import com.gzzlit.connected.Entities.*
import java.lang.Exception
import java.lang.ref.WeakReference
import java.net.URL

//
//  ConnectedNetworking.swift
//  ConnectedSDK
//
//  Created by a on 2020/9/28.
//
typealias NetworkingCompletion = (Result< Map<String, Any>>) -> Unit

abstract class Networking(dependencies: CommonDependencies) {

    var dependencies = WeakReference(dependencies)

    abstract fun get(url: URL, completion: (Any?, Exception?) -> Unit)
    abstract fun post(url: URL, params: Map<String, Any>, completion: (Any?, Exception?) -> Unit)
    abstract fun post(url: URL, params: List<Any>, header: Map<String, String>,  completion: (Any?, Exception?) -> Unit)
    fun get(path: String, completion: NetworkingCompletion){
        val url = pathAsURL(path)
        log("请求接口 $url")
        if (url != null) {
            val self by Weak(this)
            get(url = url) { value, error  ->
                self?.handleRes(value = value, error = error, path = path, completion = completion)
            }
        }
    }
    fun post(path: String, params: Map<String, Any>, completion: NetworkingCompletion){
        val url = pathAsURL(path)
        log("请求接口 $url")
        log("请求参数 $params")
        if (url != null) {
            val self by Weak(this)
            post(url = url, params = params) { value, error  ->
                self?.handleRes(value = value, error = error, path = path, completion = completion)
            }
        }
    }
    fun post(path: String, params: List<Any>, header: Map<String, String>, completion: NetworkingCompletion){
        val url = pathAsURL(path)
        log("请求接口 $url")
        log("请求参数 $params")
        if (url != null) {
            val self by Weak(this)
            post(url = url, params = params,header = header) { value, error  ->
                self?.handleRes(value = value, error = error, path = path, completion = completion)
            }
        }
    }
    private fun pathAsURL(path: String): URL? {
        return dependencies.get()?.let {
            if (path.startsWith("devicenb")||path.startsWith("machineinfo")) {
                return URL("https://${String.format(it.makeHost(), "")}/api/" + path)
            }
            return URL("https://${String.format(it.makeHost(), it.makeDevice().info.flag)}/api/" + path)
        }
    }

    private fun handleRes(value: Any?, error: Exception?, path: String, completion: NetworkingCompletion){
        val value = value as? Map<String, Any>
        if (value != null) {
            log("请求结果 ${value}")
            val status = value["status"] as? Boolean
            if (status != null) {
                if (status) {
                    val data = value["data"] as? Map<String, Any> ?: value
                    completion(Success(data))
                } else {
                    val msg = value["errormsg"] as? String ?: "缺少字段errormsg"
                    completion(Failure(NetError.请求失败, msg + " -> " + path.lastPath))
                }
            } else if (value["Status"] as? Int != null) {
                //Status:0：成功 1：失败
                if (value["Status"] as? Int == 0) {
                    completion(Success(value))
                } else {
                    val msg = value["Message"] as? String ?: "缺少字段Message"
                    completion(Failure(NetError.请求失败, msg + " -> " + path.lastPath))
                }
            }else if (value["Authorization"] != null){
                completion(Success(value))
            }else if ((value["Result"] as? Int ?: 0) > 0){
                completion(Success(value))
            }else{
                val msg = "${value["Message"] ?: ""}${value["ExceptionMessage"] ?: ""}"
                completion(Failure(NetError.数据异常, "${if (msg.isEmpty()) value.asJSON() else msg} -> ${path.lastPath}"))
            }
        } else {
            val value = jsonToMap(error?.localizedMessage)
            val msg =  "${value?.get("Message") ?: error?.localizedMessage}${value?.get("ExceptionMessage") ?: ""}"
            completion(Failure(NetError.接口异常, "$msg -> ${path.lastPath}"))
        }
    }
}
internal val String.lastPath: String get() = split("/").last()
