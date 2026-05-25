package site.doramusic.app.http

import com.google.gson.annotations.SerializedName

data class DoraGuessingReward(

    /**
     * 竞猜ID。
     */
    @SerializedName("guessingId")
    val guessingId: Long,

    /**
     * 竞猜标题。
     */
    @SerializedName("title")
    val title: String,

    /**
     * 总投注积分。
     */
    @SerializedName("totalScore")
    val totalScore: Long,

    /**
     * 总奖励积分。
     */
    @SerializedName("totalRewardScore")
    val totalRewardScore: Long,

    /**
     * 猜对额外赠送积分（不计入排行榜）。
     */
    @SerializedName("winBonusScore")
    val winBonusScore: Long,

    /**
     * 是否已开奖。
     */
    @SerializedName("opened")
    val opened: Boolean,

    /**
     * 是否中奖。
     */
    @SerializedName("win")
    val win: Boolean,

    /**
     * 是否已领取。
     */
    @SerializedName("claimed")
    var claimed: Boolean,

    /**
     * 赔率。
     */
    @SerializedName("odds")
    val odds: Double
)
