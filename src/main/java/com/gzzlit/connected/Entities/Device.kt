package com.gzzlit.connected.Entities

import java.io.Serializable

//
//  ConnectedDevice.swift
//  ConnectedSDK
//
//  Created by a on 2020/9/28.
//



enum class DeviceType (val rawValue: Int) {
    none(0), bath(1), drink(2), wind(3), wash(4);

    companion object {
        operator fun invoke(rawValue: Int) = DeviceType.values().firstOrNull { it.rawValue == rawValue }
    }
}

open class Device(var sn: String = "", var nbid: String = "", var mac: String="", var type: String = "", var version: String = "", var rawName: String = "", var rssi:Int = 0, var ble:Any? = null):Serializable {
    companion object

    val deviceType get() = DeviceType(try { type.toInt() } catch (e: Exception) { 0 })

    data class Info(val flag: String, val name: String,val path: String)
    val info: Info
        get() {
            return when (deviceType) {
                DeviceType.bath -> when (version) {
                    "06", "08" -> Info("bath", "4G水控","cat1")
                    else -> Info("bath", "NB水控","nb")
                }
                DeviceType.drink -> Info("drink", "NB饮水","nb")
                DeviceType.wind -> Info("wind", "NB吹风","nb")
                DeviceType.wash -> when (version) {
                    "03" -> Info("wash", "4G洗衣","cat1")
                    else -> Info("wash", "NB洗衣","nb")
                }
                else -> Info("", "","")
            }

        }
}