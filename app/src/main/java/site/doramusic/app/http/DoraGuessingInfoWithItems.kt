package site.doramusic.app.http

import com.google.gson.annotations.SerializedName

data class DoraGuessingInfoWithItems(

    @SerializedName("id")
    val id: Long,

    @SerializedName("title")
    val title: String,

    /**
     * 封盘时间（秒级时间戳）。
     */
    @SerializedName("closeTime")
    val closeTime: Long,

    /**
     * 竞猜状态。
     * 0 投注中
     * 1 已封盘
     * 2 已结束
     * 3 已取消
     */
    @SerializedName("status")
    val status: Int,

    /**
     * 剩余秒数。
     */
    @SerializedName("remainSeconds")
    val remainSeconds: Long,

    /**
     * 竞猜选项。
     */
    @SerializedName("items")
    val items: List<DoraGuessingItem>
)