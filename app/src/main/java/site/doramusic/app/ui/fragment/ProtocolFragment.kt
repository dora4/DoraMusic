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
    private var mCallback: Callback? = null
    private var mProtocolCallback: ProtocolCallback? = null
    private var tv_protocol_privacy_policy: TextView? = null
    private var tv_protocol_service_protocol: TextView? = null
    private var tv_protocol_disagree: TextView? = null
    private var tv_protocol_agree: TextView? = null

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
        tv_protocol_agree =
            view.findViewById<View>(R.id.tv_protocol_agree) as TextView
        tv_protocol_disagree =
            view.findViewById<View>(R.id.tv_protocol_disagree) as TextView
        tv_protocol_privacy_policy = view.findViewById(R.id.tv_protocol_privacy_policy) as TextView
        tv_protocol_service_protocol = view.findViewById(R.id.tv_protocol_service_protocol)
        tv_protocol_privacy_policy!!.setOnClickListener(this)
        tv_protocol_service_protocol!!.setOnClickListener(this)
        tv_protocol_agree!!.setOnClickListener(this)
        tv_protocol_disagree!!.setOnClickListener(this)
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
            if (mCallback != null) {
                dismissDialog()
                mCallback!!.onAgree()
            }
        } else if (id == R.id.tv_protocol_disagree) {
            if (mCallback != null) {
                mCallback!!.onDisagree()
            }
        } else if (id == R.id.tv_protocol_privacy_policy) {
            if (mProtocolCallback != null) {
                mProtocolCallback!!.onPrivacyPolicy()
            }
        } else if (id == R.id.tv_protocol_service_protocol) {
            if (mProtocolCallback != null) {
                mProtocolCallback!!.onServiceProtocol()
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
        mCallback = callback
    }

    fun setProtocolCallback(protocolCallback: ProtocolCallback?) {
        mProtocolCallback = protocolCallback
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