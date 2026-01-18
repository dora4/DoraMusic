package com.dorachat.auth

data class ReqToken(val token: String) : BaseReq() {

    init {
        payload = sort()
    }
}