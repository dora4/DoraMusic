package site.doramusic.app.http

import java.io.Serializable

class DoraUser : Serializable {

    /**
     * 用户id，唯一。
     */
    var id: Long? = null
    /**
     * 用户名。
     */
    var username: String? = null
    /**
     * 用户密码。
     */
    var password: String? = null
    /**
     * 手机号。
     */
    var phone: String? = null
    /**
     * 朵币。
     */
    var dob = 0
    /**
     * 积分。
     */
    var score = 0
    /**
     * VIP等级，非0代表是VIP。
     */
    var vip = 0
    /**
     * 返回Token给客户端。
     */
    var token: String? = null

    companion object {
        private const val serialVersionUID = 1L
    }
}