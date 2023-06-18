package site.doramusic.app.http

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class DoraCallback<T> : Callback<DoraResponse<T>> {

    override fun onResponse(call: Call<DoraResponse<T>>, response: Response<DoraResponse<T>>) {
        if (response.code() == 200) {
            val body = response.body()
            if (body!!.ok) {
                val result = body!!.result
                if (result != null) {
                    onSuccess(result)
                } else {
                    onFailure(-2, "服务端数据返回错误")
                }
            } else {
                onFailure(body!!.code, body!!.msg)
            }
        } else {
            onFailure(response.code(), response.message())
        }
    }

    override fun onFailure(call: Call<DoraResponse<T>>, t: Throwable) {
        onFailure(-1, t.message!!)
    }

    abstract fun onSuccess(body: T)
    abstract fun onFailure(code: Int, msg: String)
}