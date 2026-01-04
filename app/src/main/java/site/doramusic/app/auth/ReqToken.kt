package site.doramusic.app.auth

import site.doramusic.app.http.BaseReq

data class ReqToken(val token: String) : BaseReq() {

    init {
        payload = sort()
    }
}