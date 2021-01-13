package com.gzzlit.connected.Utils

import android.text.TextUtils
import com.gzzlit.connected.BuildConfig
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

var logInfo:String? = null

fun log(item: Any) {
    if (BuildConfig.DEBUG){
        println("| $item")
    }
    if (logInfo != null) {
        logInfo += "| \n| $item\n"
    }
//    if DEBUG
//    println("| ")
//    #else
//    #endif
}



fun Map<String, Any>.asJSON() : String {
    return JSONObject(this).toString()
}

fun jsonToMap(content: String?): Map<String,Any>? {
    if (TextUtils.isEmpty(content)) {
        return null
    }
    val content = content?.trim() ?:""
    try {
        return when (content[0]){
            '{'->{
                val result = mutableMapOf<String,Any>()
                val jsonObject = JSONObject(content)
                val iterator = jsonObject.keys()
                while (iterator.hasNext()) {
                    val key = iterator.next()
                    when (val value = jsonObject[key]){
                        is JSONObject -> result[key] = jsonToMap(value.toString().trim()) ?: emptyMap<String,Any>()
                        is JSONArray -> result[key] = jsonToList(value.toString().trim()) ?: emptyList<Any>()
                        else -> result[key] = value
                    }
                }
                result
            }
            else -> null
        }
    } catch (e: JSONException) {
        return null
    }
}
fun jsonToList(content: String?): List<Any>? {
    if (TextUtils.isEmpty(content)) {
        return null
    }
    val content = content?.trim() ?:""
    try {
        return when (content[0]){
            '[' -> {
                val result = mutableListOf<Any>()
                val jsonArray = JSONArray(content)
                for (i in 0 until jsonArray.length()) {
                    when (val value = jsonArray[i]){
                        is JSONObject -> result.add(jsonToMap(value.toString().trim()) ?: emptyMap<String,Any>())
                        is JSONArray -> result.add(jsonToList(value.toString().trim()) ?: emptyList<Any>())
                        else -> result.add(value)
                    }
                }
                result
            }
            else -> null
        }
    } catch (e: JSONException) {
        return null
    }
}