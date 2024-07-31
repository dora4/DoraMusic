package site.doramusic.app.ui.fragment

import android.app.DialogFragment
import android.app.FragmentManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView

import site.doramusic.app.R

class ProtocolFragment : DialogFragment(),

    View.OnClickListener {
    private var callback: Callback? = null
    private var protocolCallback: ProtocolCallback? = null
    private var tvProtocolPrivacyPolicy: TextView? = null
    private var tvProtocolServiceProtocol: TextView? = null
    private var tvProtocolDisagree: TextView? = null
    private var tvProtocolAgree: TextView? = null

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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        tvProtocolAgree =
            view.findViewById<View>(R.id.tv_protocol_agree) as TextView
        tvProtocolDisagree =
            view.findViewById<View>(R.id.tv_protocol_disagree) as TextView
        tvProtocolPrivacyPolicy = view.findViewById(R.id.tv_protocol_privacy_policy) as TextView
        tvProtocolServiceProtocol = view.findViewById(R.id.tv_protocol_service_protocol)
        tvProtocolPrivacyPolicy!!.setOnClickListener(this)
        tvProtocolServiceProtocol!!.setOnClickListener(this)
        tvProtocolAgree!!.setOnClickListener(this)
        tvProtocolDisagree!!.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        isCancelable = false
        val window = dialog!!.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window?.setLayout(
                (getLcdWidth(
                    context
                ) * 0.9).toInt(),
                (getLcdHeight(
                    context
                ) * 0.75).toInt()
            )
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.tv_protocol_agree) {
            if (callback != null) {
                dismissDialog()
                callback!!.onAgree()
            }
        } else if (id == R.id.tv_protocol_disagree) {
            if (callback != null) {
                callback!!.onDisagree()
            }
        } else if (id == R.id.tv_protocol_privacy_policy) {
            if (protocolCallback != null) {
                protocolCallback!!.onPrivacyPolicy()
            }
        } else if (id == R.id.tv_protocol_service_protocol) {
            if (protocolCallback != null) {
                protocolCallback!!.onServiceProtocol()
            }
        }
    }

    /**
     * 为了解决:mainActivity调用onSaveInstanceState以后又调用了show方法,
     * 出现的Can not perform this action after onSaveInstanceState
     * 这个异常(不应该用commit ,而是用commitAllowingStateLoss)
     */
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
    fun dismissDialog() {
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
        const val TAG = "ProtocolFragment"
        fun newInstance(): ProtocolFragment {
            return ProtocolFragment()
        }

        fun getLcdWidth(context: Context?): Int {
            return context?.resources?.displayMetrics?.widthPixels ?: 0
        }

        fun getLcdHeight(context: Context?): Int {
            return context?.resources?.displayMetrics?.heightPixels ?: 0
        }
    }
}