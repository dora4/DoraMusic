package com.dorachat.auth

interface ApiCode {
    companion object {
        const val SUCCESS = "000000"
        const val ERROR_SIGN_IN_EXPIRED = "100112"
    }
}