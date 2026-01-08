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
    fun sendMsg(@Body body: RequestBody): Observable<ApiResult<Boolean>>

    /**
     * 拉取频道消息列表。
     * 支持cursor+limit（向上翻历史）
     *
     * @see ReqChannelMsgList
     */
    @POST("chat/channel/msg/list")
    fun getChannelMsgList(@Body body: RequestBody): Observable<ApiResult<DoraChannelMsgList>>
}