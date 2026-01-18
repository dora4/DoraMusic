package com.dorachat.auth

data class ReqUserInfo(val erc20: String,
                       val nickname: String? = null,
                       val gender: Int? = null,
                       val introduce: String? = null) : BaseReq() {

    init {
        payload = sort()
    }
}