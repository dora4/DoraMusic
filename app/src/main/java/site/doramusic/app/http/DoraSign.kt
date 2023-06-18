package site.doramusic.app.http

import java.io.Serializable

class DoraSign : Serializable {
    var userId: Long = 0
    var lastSignTime: String? = null
    var signNum = 0
    var dob = 0
    var score = 0

    companion object {
        private const val serialVersionUID = 1L
    }
}