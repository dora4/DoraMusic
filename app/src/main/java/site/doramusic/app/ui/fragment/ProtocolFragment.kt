package site.doramusic.app.ui.fragment

import android.app.DialogFragment
import android.app.FragmentManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView

import site.doramusic.app.R

class ProtocolFragment : DialogFragment(), View.OnClickListener {

    private var callback: Callback? = null
    private var protocolCallback: ProtocolCallback? = null
    private var tvProtocolPrivacyPolicy: TextView? = null
    private var tvProtocolServiceProtocol: TextView? = null
    private var tvProtocolDisagree: TextView? = null
    private var tvProtocolAgree: TextView? = null

    
    @Deprecated("Deprecated in Java")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return inflater.inflate(R.layout.fragment_protocol, container, false)
    }

    @Deprecated("Deprecated in Java")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        tvProtocolAgree =
            view.findViewById<View>(R.id.tv_protocol_agree) as TextView
        tvProtocolDisagree =
            view.findViewById<View>(R.id.tv_protocol_disagree) as TextView
        tvProtocolPrivacyPolicy = view.findViewById<TextView>(R.id.tv_protocol_privacy_policy)!!
        tvProtocolServiceProtocol = view.findViewById(R.id.tv_protocol_service_protocol)
        tvProtocolPrivacyPolicy!!.setOnClickListener(this)
        tvProtocolServiceProtocol!!.setOnClickListener(this)
        tvProtocolAgree!!.setOnClickListener(this)
        tvProtocolDisagree!!.setOnClickListener(this)
    }

    
    @Deprecated("Deprecated in Java")
    override fun onStart() {
        super.onStart()
        isCancelable = false
        val window = dialog!!.window
        window?.setLayout(
            (getLcdWidth(
                context
            ) * 0.9).toInt(),
            (getLcdHeight(
                context
            ) * 0.75).toInt()
        )
    }

    override fun onClick(v: View) {
        if (v.id == R.id.tv_protocol_agree) {
            if (callback != null) {
                dismissDialog()
                callback!!.onAgree()
            }
        } else if (v.id == R.id.tv_protocol_disagree) {
            callback?.onDisagree()
        } else if (v.id == R.id.tv_protocol_privacy_policy) {
            protocolCallback?.onPrivacyPolicy()
        } else if (v.id == R.id.tv_protocol_service_protocol) {
            protocolCallback?.onServiceProtocol()
        }
    }

    /**
     * 为了解决:mainActivity调用onSaveInstanceState以后又调用了show方法,
     * 出现的Can not perform this action after onSaveInstanceState
     * 这个异常(不应该用commit ,而是用commitAllowingStateLoss)
     */
    
    @Deprecated("Deprecated in Java")
    override fun show(
        manager: FragmentManager,
        tag: String?
    ) {
        try {
            super.show(manager, tag)
        } catch (e: Exception) { //do nothing
        }
    }

    /**
     * 注意,不要用super.dismiss(), bug同上show()
     * super.onDismiss就没问题
     */
    private fun dismissDialog() {
        if (activity != null && !activity!!.isFinishing) {
            super.dismissAllowingStateLoss()
        }
    }

    interface Callback {
        fun onAgree()
        fun onDisagree()
    }

    interface ProtocolCallback {
        fun onServiceProtocol()
        fun onPrivacyPolicy()
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun setProtocolCallback(protocolCallback: ProtocolCallback?) {
        this.protocolCallback = protocolCallback
    }

    companion object {

        fun getLcdWidth(context: Context): Int {
            return context.resources
                ?.displayMetrics
                ?.widthPixels ?: 0
        }

        fun getLcdHeight(context: Context): Int {
            return context.resources
                ?.displayMetrics
                ?.heightPixels ?: 0
        }
    }
}