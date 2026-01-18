package com.dorachat.auth

import com.google.gson.Gson
import dora.util.GlobalContext
import dora.util.LanguageUtils
import java.lang.reflect.Modifier
import java.util.Locale

abstract class BaseReq {

    /**
     * 根据不同的语种返回本地化的内容。
     */
    var lang: String = ""

    /**
     * 数据载体。
     */
    var payload: String = ""

    /**
     * 防抓包伪造签名重复请求，签名过期，拒绝请求。
     */
    var timestamp: String = ""

    init {
        lang = LanguageUtils.getLangTag(GlobalContext.get()).ifEmpty { Locale.getDefault().language }
        timestamp = (System.currentTimeMillis() / 1000).toString()
    }

    /**
     * 对数据进行排序，保证唯一性，返回排序后的JSON字符串。
     */
    fun sort(): String {
        val map = sortedMapOf<String, Any?>()
        var clazz: Class<*>? = this.javaClass
        while (clazz != null && clazz != BaseReq::class.java) {
            clazz.declaredFields
                .filter { field ->
                    !field.isSynthetic &&
                            !Modifier.isStatic(field.modifiers)
                }
                .forEach { field ->
                    field.isAccessible = true
                    map[field.name] = field[this]
                }
            clazz = clazz.superclass
        }
        // 父类字段（显式加入，避免遗漏）
        map["lang"] = lang
        map["payload"] = payload
        map["timestamp"] = timestamp
        return Gson().toJson(map)
    }
}
