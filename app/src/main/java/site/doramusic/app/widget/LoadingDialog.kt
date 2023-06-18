package site.doramusic.app.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import android.widget.TextView
import site.doramusic.app.R

class LoadingDialog constructor(ctx: Context) : Dialog(ctx, R.style.AppTheme_LoadingDialog) {

    private var dialogView: View? = null
    private val modalInAnim: AnimationSet
    private val modalOutAnim: AnimationSet
    private val overlayOutAnim: Animation
    private var tv_loading_title: TextView? = null
    var titleText: String? = null
        set
    private var closeFromCancel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_loading_dialog)
        dialogView = window!!.decorView.findViewById(android.R.id.content)
        tv_loading_title = findViewById(R.id.tv_loading_title)
        setTitleText(titleText)
    }

    private fun setTitleText(text: String?): LoadingDialog {
        titleText = text
        if (tv_loading_title != null && titleText != null) {
            tv_loading_title!!.text = titleText
        }
        return this
    }

    override fun onStart() {
        dialogView!!.startAnimation(modalInAnim)
    }

    /**
     * The real Dialog.cancel() will be invoked async-ly after the animation finishes.
     */
    override fun cancel() {
        dismissWithAnimation(true)
    }

    /**
     * The real Dialog.dismiss() will be invoked async-ly after the animation finishes.
     */
    fun dismissWithAnimation() {
        dismissWithAnimation(false)
    }

    private fun dismissWithAnimation(fromCancel: Boolean) {
        closeFromCancel = fromCancel
        dialogView!!.startAnimation(modalOutAnim)
    }

    init {
        setCancelable(true)
        setCanceledOnTouchOutside(false)
        modalInAnim = AnimationUtils.loadAnimation(ctx, R.anim.anim_modal_in) as AnimationSet
        modalOutAnim = AnimationUtils.loadAnimation(ctx, R.anim.anim_modal_out) as AnimationSet
        modalOutAnim.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                dialogView!!.visibility = View.INVISIBLE
                dialogView!!.post {
                    if (closeFromCancel) {
                        super@LoadingDialog.cancel()
                    } else {
                        super@LoadingDialog.dismiss()
                    }
                }
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        overlayOutAnim = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                val wlp = window!!.attributes
                wlp.alpha = 1 - interpolatedTime
                window!!.attributes = wlp
            }
        }
        overlayOutAnim.setDuration(120)
    }
}