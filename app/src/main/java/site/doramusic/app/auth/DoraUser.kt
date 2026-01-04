package site.doramusic.app.auth

data class DoraUser(
    val erc20: String,
    val latestSignIn: Long,
    val accessToken: String,
    val refreshToken: String
) {

    constructor(erc20: String, latestSignIn: Long) : this(
        erc20 = erc20,
        latestSignIn = latestSignIn,
        accessToken = "",
        refreshToken = ""
    )
}
