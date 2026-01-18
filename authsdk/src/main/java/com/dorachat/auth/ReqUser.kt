package com.dorachat.auth

data class ReqUser(val erc20: String) : BaseReq() {

    init {
        payload = sort()
    }
}