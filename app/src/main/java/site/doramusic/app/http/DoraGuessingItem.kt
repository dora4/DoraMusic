package site.doramusic.app.http

import com.google.gson.annotations.SerializedName

data class DoraGuessingItem(

    @SerializedName("id")
    val id: Long,

    /**
     * 归属竞猜ID。
     */
    @SerializedName("guessingId")
    val guessingId: Long,

    /**
     * 选项名称。
     */
    @SerializedName("itemDesc")
    val itemDesc: String,

    /**
     * 当前投注总积分。
     */
    @SerializedName("totalScore")
    val totalScore: Long,

    /**
     * 赔率。
     */
    @SerializedName("odds")
    val odds: Double? = null,

    /**
     * 是否正确答案。
     */
    @SerializedName("isWin")
    val isWin: Boolean? = null,

    /**
     * 当前用户是否投注。
     */
    @SerializedName("isBet")
    val isBet: Boolean? = null,

    /**
     * 当前用户是否猜中。
     */
    @SerializedName("isHit")
    val isHit: Boolean? = null
)