package site.doramusic.app.chat

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import dora.http.DoraHttp.net
import dora.http.DoraHttp.rxResult
import dora.skin.SkinManager
import dora.skin.base.BaseSkinBindingActivity
import dora.util.IntentUtils
import dora.util.RxBus
import dora.util.StatusBarUtils
import dora.util.ViewUtils
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.conf.AppConfig.Companion.EXTRA_ERC20
import site.doramusic.app.conf.AppConfig.Companion.PRODUCT_NAME
import site.doramusic.app.databinding.ActivityChatRoomBinding
import site.doramusic.app.http.SecureRequestBuilder

@Route(path = ARoutePath.ACTIVITY_CHAT_ROOM)
class ChatRoomActivity : BaseSkinBindingActivity<ActivityChatRoomBinding>() {

    private lateinit var erc20: String

    override fun getLayoutId(): Int {
        return R.layout.activity_chat_room
    }

    override fun onSetStatusBar() {
        val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
        StatusBarUtils.setStatusBarColor(this, skinThemeColor)
    }

    override fun onGetExtras(action: String?, bundle: Bundle?, intent: Intent) {
        erc20 = IntentUtils.getStringExtra(intent, EXTRA_ERC20)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 保持聊天界面不熄屏
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        ChatWsManager.connect(AppConfig.URL_WS_CHAT)
    }

    override fun onStop() {
        super.onStop()
        ChatWsManager.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        ChatWsManager.close() // 兜底，不作为主逻辑
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityChatRoomBinding) {
        val adapter = ChannelMsgAdapter(erc20)
        binding.recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true   // 像聊天一样从底部开始
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null
        net {
            val req = ReqChannelMsgList(roomId = PRODUCT_NAME, null, 20)
            val body = SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC)
                ?: return@net
            val data =
                rxResult(ChatService::class) { getChannelMsgList(body.toRequestBody()) }?.data
            adapter.setList(data?.list?.reversed())
        }
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
        addDisposable(RxBus.getInstance()
            .toObservable(ChannelMsgEvent::class.java)
            .subscribe { event ->
                val msg = event.msg
                // 不是当前房间，直接忽略，聊天室这里都是当前房间的😂
                if (msg.roomId != PRODUCT_NAME) return@subscribe
                // 自己发送的不收
                if (msg.senderId == erc20) return@subscribe
                val uiMsg = DoraChannelMsg(
                    msgId = msg.msgId,
                    roomId = msg.roomId,
                    senderId = msg.senderId,
                    senderName = msg.senderName,
                    senderAvatar = msg.senderAvatar,
                    senderRole = msg.senderRole,
                    msgType = msg.msgType,
                    msgContent = msg.msgContent,
                    ts = msg.ts
                )
                runOnUiThread {
                    adapter.addData(uiMsg)
                    binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
                }
            })
        binding.btnSend.setOnClickListener {
            val content = ViewUtils.getText(binding.etInput)
            if (content.isBlank()) return@setOnClickListener
            val req = ReqSendChannelMsg(roomId = PRODUCT_NAME, msgType = 0, msgContent = content)
            val body = SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC)
                ?: return@setOnClickListener
            net {
                val msgId = rxResult(ChatService::class) { sendMsg(body.toRequestBody()) }?.data
                if (msgId != null) {
                    val localMsg = DoraChannelMsg(
                        msgId = System.currentTimeMillis(), // 临时 ID
                        roomId = PRODUCT_NAME,
                        senderId = erc20,
                        senderName = erc20,
                        senderAvatar = "", // 通过UserManager拿，先不考虑
                        senderRole = 0,
                        msgType = 0,
                        msgContent = content,
                        recall = 0,
                        ts = System.currentTimeMillis()
                    )
                    adapter.addData(localMsg)
                    binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
                    binding.etInput.setText("")
                } else {
                    showLongToast("消息未发送")
                }
            }
        }
    }
}