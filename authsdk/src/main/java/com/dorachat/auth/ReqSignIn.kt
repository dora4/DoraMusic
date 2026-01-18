package com.dorachat.auth

data class ReqSignIn(
                     // 用户ID
                     val erc20: String,
                     // 认证短语
                     val authWord: String,
                     // 非Dora Chat本应用要传分区ID
                     val partitionId: String? = null) : BaseReq() {

    init {
        payload = sort()
    }
}