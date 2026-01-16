package site.doramusic.app.chat

import dora.http.retrofit.ApiService
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import site.doramusic.app.http.ApiResult

interface ChatService : ApiService {

    /**
     * 加入聊天室。
     *
     * @see ReqJoinChannel
     */
    @POST("chat/channel/join")
    fun joinChannel(@Body body: RequestBody): Observable<ApiResult<Boolean>>

    /**
     * 离开聊天室。
     *
     * @see ReqLeaveChannel
     */
    @POST("chat/channel/leave")
    fun leaveChannel(@Body body: RequestBody): Observable<ApiResult<Boolean>>

    /**
     * 发送消息。
     *
     * @see ReqSendChannelMsg
     */
    @POST("chat/channel/send")
    fun sendMsg(@Body body: RequestBody): Observable<ApiResult<Long>>

    /**
     * 撤回自己发送的消息。
     *
     * @see ReqRecallChannelMsg
     */
    @POST("chat/channel/recall")
    fun recallMsg(@Body body: RequestBody): Observable<ApiResult<Boolean>>

    /**
     * 删除消息（仅自己不可见），可删除自己发送的消息，也可删除别人的骚扰信息。
     *
     * @see ReqDeleteChannelMsg
     */
    @POST("chat/channel/msg/delete")
    fun deleteMsg(@Body body: RequestBody): Observable<ApiResult<Boolean>>

    /**
     * 拉取频道消息列表。
     * 支持cursor+limit（向上翻历史）
     *
     * @see ReqChannelMsgList
     */
    @POST("chat/channel/msg/list")
    fun getChannelMsgList(@Body body: RequestBody): Observable<ApiResult<DoraChannelMsgList>>
}