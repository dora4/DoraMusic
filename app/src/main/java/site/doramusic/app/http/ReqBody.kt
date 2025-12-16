package site.doramusic.app.http

data class ReqBody(
    val mode: String = "",
    val key: String = "",
    val data: String = "",
    val sign: String = "",
)