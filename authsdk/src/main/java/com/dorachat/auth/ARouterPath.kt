package com.dorachat.auth

interface ARouterPath {

    companion object {
        const val GROUP_AUTH_SDK: String = "AuthSdk"
        const val ACTIVITY_SIGN_IN: String = "/$GROUP_AUTH_SDK/SignInActivity"
    }
}