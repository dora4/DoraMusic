package site.doramusic.app.http

import com.google.gson.annotations.SerializedName

data class DoraGuessingRank(

    /**
     * 用户ID。
     */
    @SerializedName("userId")
    val userId: String,

    /**
     * 昵称。
     */
    @SerializedName("nickname")
    val nickname: String,

    /**
     * 胜率。
     */
    @SerializedName("winRate")
    val winRate: Double,

    /**
     * 盈亏积分。
     */
    @SerializedName("profit")
    val profit: Int,

    /**
     * 投注积分。
     */
    @SerializedName("totalBet")
    val totalBet: Int
)
