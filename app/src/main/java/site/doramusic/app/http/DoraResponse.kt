package site.doramusic.app.http

import java.io.Serializable

/**
 * 前后端交互数据标准。
 */
class DoraResponse<T> : Serializable {

    var ok: Boolean = false

    /**
     * 返回代码。
     */
    var code: Int = 200

    /**
     * 失败消息。
     */
    var msg: String = ""

    /**
     * 结果对象。
     */
    var result: T? = null

    companion object {
        private const val serialVersionUID = 1L
    }
}
