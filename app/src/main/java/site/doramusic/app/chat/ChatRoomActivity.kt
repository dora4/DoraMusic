package site.doramusic.app.chat

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildLongClickListener
import com.dorachat.auth.ApiCode
import dora.http.DoraHttp.net
import dora.http.DoraHttp.rxResult
import dora.util.DensityUtils
import dora.util.IntentUtils
import dora.util.RxBus
import dora.util.StatusBarUtils
import dora.util.ViewUtils
import dora.widget.DoraPopupWindow
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.conf.AppConfig.Companion.EXTRA_ERC20
import site.doramusic.app.conf.AppConfig.Companion.PRODUCT_NAME
import site.doramusic.app.databinding.ActivityChatRoomBinding
import site.doramusic.app.http.SecureRequestBuilder
import site.doramusic.app.ui.activity.BaseSkinActivity
import site.doramusic.app.util.ThemeSelector

@Route(path = ARoutePath.ACTIVITY_CHAT_ROOM)
class ChatRoomActivity : BaseSkinActivity<ActivityChatRoomBinding>() {

    private lateinit var erc20: String
    private lateinit var adapter: ChannelMsgAdapter
    private var loadingHistory = false
    private var noMoreHistory = false
    private var lastMsgSeq: Long? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_chat_room
    }

    override fun onSetStatusBar() {
        val skinThemeColor = ThemeSelector.getThemeColor(this)
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

    private fun handleRecallEvent(chatMsg: DoraChannelMsg) {
        val recalledMsgId = chatMsg.msgContent.toLongOrNull() ?: return
        runOnUiThread {
            val list = adapter.data
            for (i in list.indices) {
                val target = list[i]
                if (target.msgId == recalledMsgId) {
                    target.recall = 1
                    target.msgContent = "[该消息已被撤回]"
                    adapter.setData(i, target)
                    break
                }
            }
        }
    }

    override fun isAutoDispose(): Boolean {
        return true
    }

    /**
     * 首次进入：拉最新一页。
     */
    private fun loadLatest() {
        loadingHistory = true
        net {
            val req = ReqChannelMsgList(roomId = PRODUCT_NAME, cursor = null, limit = 20)
            val body = SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC)
                ?: return@net

            val data = rxResult(ChatService::class) {
                getChannelMsgList(body.toRequestBody())
            }?.data

            val list = data?.list ?: emptyList()
            if (list.isNotEmpty()) {
                val uiList = list.reversed()
                lastMsgSeq = uiList.first().msgSeq // 记录最早一条
                adapter.setList(uiList)
                mBinding.recyclerView.scrollToPosition(adapter.itemCount - 1)
            } else {
                noMoreHistory = true
            }
            loadingHistory = false
        }
    }

    /**
     * 拉历史消息。
     */
    private fun loadHistory() {
        if (lastMsgSeq == null) return
        loadingHistory = true
        net {
            val req = ReqChannelMsgList(roomId = PRODUCT_NAME, cursor = lastMsgSeq, limit = 20)
            val body = SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC)
                ?: return@net
            val data = rxResult(ChatService::class) {
                getChannelMsgList(body.toRequestBody())
            }?.data
            val list = data?.list ?: emptyList()
            if (list.isEmpty()) {
                noMoreHistory = true
            } else {
                val uiList = list.reversed()
                runOnUiThread {
                    val oldFirstPos =
                        (mBinding.recyclerView.layoutManager as LinearLayoutManager)
                            .findFirstVisibleItemPosition()

                    adapter.addData(0, uiList)
                    lastMsgSeq = uiList.first().msgSeq

                    // 保持视觉位置不跳
                    mBinding.recyclerView.scrollToPosition(oldFirstPos + uiList.size)
                }
            }
            loadingHistory = false
        }
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityChatRoomBinding) {
        ThemeSelector.applyViewTheme(binding.titlebar)
        adapter = ChannelMsgAdapter(erc20)
        binding.recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true   // 像聊天一样从底部开始
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lm = recyclerView.layoutManager as LinearLayoutManager
                if (lm.findFirstVisibleItemPosition() == 0 &&
                    !loadingHistory &&
                    !noMoreHistory
                ) {
                    loadHistory()
                }
            }
        })
        loadLatest()


        adapter.addChildLongClickViewIds(R.id.rl_left_content, R.id.rl_right_content)
        adapter.setOnItemChildLongClickListener(object : OnItemChildLongClickListener {
            override fun onItemChildLongClick(
                adapter: BaseQuickAdapter<*, *>,
                view: View,
                position: Int
            ): Boolean {
                val popup = DoraPopupWindow.create(this@ChatRoomActivity)
                    .contentView(R.layout.layout_msg_op)
                    .cornerRadius(12f)
                    .backgroundColor(Color.WHITE)
                    .build()
                popup.show(view, DensityUtils.DP60, -DensityUtils.DP60)
                val tvMsgRecall = popup.contentView.findViewById<TextView>(R.id.tv_msg_recall)
                val tvMsgDelete = popup.contentView.findViewById<TextView>(R.id.tv_msg_delete)
                tvMsgRecall.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        popup.dismiss()
                        net {
                            val msg = adapter.getItem(position) as DoraChannelMsg
                            val req = ReqRecallChannelMsg(roomId = PRODUCT_NAME, msgId = msg.msgId)
                            val body = SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC)
                                ?: return@net
                            val resp = rxResult(ChatService::class) { recallMsg(body.toRequestBody()) }
                            if (resp != null) {
                                if (resp.code != ApiCode.SUCCESS) {
                                    showLongToast(resp.msg)
                                }
                            }
                        }
                    }
                })
                tvMsgDelete.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        popup.dismiss()
                        net {
                            val msg = adapter.getItem(position) as DoraChannelMsg
                            val req = ReqDeleteChannelMsg(roomId = PRODUCT_NAME, msgId = msg.msgId)
                            val body = SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC)
                                ?: return@net
                            val resp = rxResult(ChatService::class) { deleteMsg(body.toRequestBody()) }
                            if (resp != null) {
                                if (resp.code != ApiCode.SUCCESS) {
                                    showLongToast(resp.msg)
                                } else {
                                    val ok = resp.data as Boolean
                                    if (ok) {
                                        runOnUiThread {
                                            adapter.removeAt(position)
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
                return true
            }
        })
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
        addDisposable(RxBus.getInstance()
            .toObservable(ChannelMsgEvent::class.java)
            .subscribe { event ->
                val msg = event.msg
                // 不是当前房间，直接忽略，聊天室这里都是当前房间的😂
                if (msg.roomId != PRODUCT_NAME) return@subscribe
                // 撤回事件
                if (msg.msgType == 100) {
                    handleRecallEvent(msg)
                    return@subscribe
                }
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
                        msgId = msgId,
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