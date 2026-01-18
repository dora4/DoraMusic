package com.dorachat.auth

import dora.cache.data.adapter.Result

class ApiResult<T> : Result<T> {

    var code: String? = null
    var msg: String? = null
    var data: T? = null
        private set
    val timestamp = System.currentTimeMillis()

    fun setData(data: T) {
        this.data = data
    }

    override fun getRealModel(): T? {
        return data
    }
}