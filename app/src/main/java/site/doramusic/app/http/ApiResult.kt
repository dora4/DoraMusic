package site.doramusic.app.http

import dora.cache.data.adapter.Result

/**
 * 前后端交互数据标准。
 */
class ApiResult<T> : Result<T> {

    var code: String? = null
    var msg: String? = null
    var data: T? = null

    override fun getRealModel(): T? {
        return data
    }
}