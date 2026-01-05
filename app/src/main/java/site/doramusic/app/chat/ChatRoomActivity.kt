package site.doramusic.app.chat

import android.content.Intent
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import dora.http.DoraHttp.net
import dora.http.DoraHttp.rxResult
import dora.skin.SkinManager
import dora.skin.base.BaseSkinBindingActivity
import dora.util.IntentUtils
import dora.util.StatusBarUtils
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
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

    override fun initData(savedInstanceState: Bundle?, binding: ActivityChatRoomBinding) {
        net {
            val req = ReqSendChannelMsg(roomId = PRODUCT_NAME, msgType = 0, msgContent = "123")
            val body = SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC)
                ?: return@net
            val ok = rxResult(ChatService::class) { sendMsg(body.toRequestBody()) }?.data
            if (ok == true) {
                showLongToast("消息已发送")
            } else {
                showLongToast("消息发送失败")
            }
        }
    }
}