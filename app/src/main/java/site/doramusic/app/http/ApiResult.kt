package site.doramusic.app.http

import dora.cache.data.adapter.Result

class ApiResult<T> : Result<T> {

    var errorCode: String? = null
    var errorDetail: String? = null
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