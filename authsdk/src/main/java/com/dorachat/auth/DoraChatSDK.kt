package com.dorachat.auth

import android.content.Context

object DoraChatSDK {

    private var context: Context? = null
    private var config: DoraChatConfig? = null
    private val auth = AuthManager

    fun init(context: Context, config: DoraChatConfig) {
        this.context = context.applicationContext
        this.config = config
        auth.init(context, config.partitionId)
    }

    fun getConfig() : DoraChatConfig? {
        return config
    }
}
