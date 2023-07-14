package site.doramusic.app.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lwh.jackknife.xskin.SkinLoader
import com.lwh.jackknife.xskin.util.PrefsUtils
import dora.arouter.open
import dora.http.retrofit.RetrofitManager
import dora.util.ApkUtils
import dora.util.IoUtils
import dora.util.StatusBarUtils
import dora.util.TextUtils
import dora.widget.DoraLoadingDialog
import dora.widget.DoraToggleButton
import okhttp3.*
import site.doramusic.app.BuildConfig
import site.doramusic.app.MusicApp
import site.doramusic.app.R
import site.doramusic.app.base.BaseSkinActivity
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.databinding.ActivitySettingsBinding
import site.doramusic.app.http.DoraCallback
import site.doramusic.app.http.service.UpdateService
import site.doramusic.app.http.service.UserService
import site.doramusic.app.util.PreferencesManager
import site.doramusic.app.util.UserManager
import java.io.File
import java.io.IOException

@Route(path = ARoutePath.ACTIVITY_SETTINGS)
class SettingsActivity : BaseSkinActivity<ActivitySettingsBinding>(), AppConfig, View.OnClickListener {

    internal lateinit var prefsManager: PreferencesManager
    internal var updateDialog: DoraLoadingDialog? = null

    internal var updateHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                0 //显示对话框
                -> {
                    updateDialog?.show("正在更新")
                    showShortToast("更新过程可能需要耗费几十秒的时间，请耐心等待...")
                }
                1 -> {
                    val savePath = msg.obj as String
                    val saveFile = File(savePath)
                    if (saveFile.exists() && saveFile.isFile && saveFile.name.endsWith(".apk")) {
                        sendEmptyMessage(2)
                        installApk(this@SettingsActivity, saveFile)
                    }
                }
                2 -> updateDialog?.dismissWithAnimation()
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_settings
    }

    override fun onSetStatusBar() {
        super.onSetStatusBar()
        StatusBarUtils.setTransparencyStatusBar(this)
    }
    override fun initData(savedInstanceState: Bundle?) {
        mBinding.statusbarSettings.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtils.getStatusBarHeight()
        )
        mBinding.statusbarSettings.background = ContextCompat.getDrawable(this, SkinLoader.getInstance().getColorRes("skin_theme_color_"+ PrefsUtils(this).suffix))
        mBinding.v = this
        updateDialog = DoraLoadingDialog(this)
        prefsManager = PreferencesManager(this)
        mBinding.tvSettingsVersion.text = getString(R.string.app_version)
        if (UserManager.currentUser != null) {
            mBinding.rlSettingsLogout.visibility = View.VISIBLE
        }
        mBinding.tbSettingsOne.isChecked = prefsManager.getHelloDora()
        mBinding.tbSettingsTwo.isChecked = prefsManager.getColdLaunchAutoPlay()
        mBinding.tbSettingsThree.isChecked = prefsManager.getShakeChangeMusic()
        mBinding.tbSettingsFour.isChecked = prefsManager.getHotFix()
        mBinding.tbSettingsFive.isChecked = prefsManager.getBassBoost()

        mBinding.tbSettingsOne.setOnCheckedChangeListener(object : DoraToggleButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: DoraToggleButton?, isChecked: Boolean) {
                mBinding.tbSettingsOne.isChecked = isChecked
                prefsManager.saveHelloDora(isChecked)
            }
        })
        mBinding.tbSettingsTwo.setOnCheckedChangeListener(object : DoraToggleButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: DoraToggleButton?, isChecked: Boolean) {
                mBinding.tbSettingsTwo.isChecked = isChecked
                prefsManager.saveColdLaunchAutoPlay(isChecked)
            }
        })
        mBinding.tbSettingsThree.setOnCheckedChangeListener(object : DoraToggleButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: DoraToggleButton?, isChecked: Boolean) {
                mBinding.tbSettingsThree.isChecked = isChecked
                prefsManager.saveShakeChangeMusic(isChecked)
            }
        })
        mBinding.tbSettingsFour.setOnCheckedChangeListener(object : DoraToggleButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: DoraToggleButton?, isChecked: Boolean) {
                mBinding.tbSettingsFour.isChecked = isChecked
                prefsManager.saveHotFix(isChecked)
            }
        })
        mBinding.tbSettingsFive.setOnCheckedChangeListener(object : DoraToggleButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: DoraToggleButton?, isChecked: Boolean) {
                mBinding.tbSettingsFive.isChecked = isChecked
                prefsManager.saveBassBoost(isChecked)
                if (isChecked) {
                    MusicApp.instance!!.mediaManager!!.setBassBoost(1000)
                } else {
                    MusicApp.instance!!.mediaManager!!.setBassBoost(1)
                }
                mBinding.tbSettingsFive.isChecked = isChecked
                prefsManager.saveHelloDora(isChecked)
            }
        })
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.rl_settings_check_version -> checkUpdate()
            R.id.rl_settings_logout -> {
                val service = RetrofitManager.getService(UserService::class.java)
                val prefsManager = PreferencesManager(this)
                if (TextUtils.isNotEmpty(prefsManager.getToken())) {
                    val call = service.logout(prefsManager.getToken() ?: "")
                    call.enqueue(object : DoraCallback<Long>() {

                        override fun onSuccess(body: Long) {
                            showShortToast("注销登录")
                            UserManager.update(null)
                            prefsManager.removeToken()
                        }

                        override fun onFailure(code: Int, msg: String) {
                            showShortToast(msg)
                        }
                    })
                    finish()
                } else {
                    showShortToast("注销登录")
                    UserManager.update(null)
                }
            }
            R.id.rl_settings_one -> {
                val isChecked = mBinding.tbSettingsOne.isChecked
                mBinding.tbSettingsOne.isChecked = !isChecked
                prefsManager.saveHelloDora(!isChecked)
            }
            R.id.rl_settings_two -> {
                val isChecked = mBinding.tbSettingsTwo.isChecked
                mBinding.tbSettingsTwo.isChecked = !isChecked
                prefsManager.saveColdLaunchAutoPlay(!isChecked)
            }
            R.id.rl_settings_three -> {
                val isChecked = mBinding.tbSettingsThree.isChecked
                mBinding.tbSettingsThree.isChecked = !isChecked
                prefsManager.saveShakeChangeMusic(!isChecked)
            }
            R.id.rl_settings_four -> {
                val isChecked = mBinding.tbSettingsFour.isChecked
                mBinding.tbSettingsFour.isChecked = !isChecked
                prefsManager.saveHotFix(!isChecked)
            }
            R.id.rl_settings_five -> {
                val isChecked = mBinding.tbSettingsFive.isChecked
                mBinding.tbSettingsFive.isChecked = !isChecked
                prefsManager.saveBassBoost(!isChecked)
                if (isChecked) {
                    MusicApp.instance!!.mediaManager!!.setBassBoost(1000)
                } else {
                    MusicApp.instance!!.mediaManager!!.setBassBoost(1)
                }
            }
            R.id.rl_settings_user_protocol -> {
                open(ARoutePath.ACTIVITY_PROTOCOL) {
                    withString("title", "用户协议")
                }
            }
            R.id.rl_settings_privacy_policy -> {
                open(ARoutePath.ACTIVITY_PROTOCOL) {
                    withString("title", "隐私政策")
                }
            }
        }
    }

    private fun checkUpdate() {
        showShortToast("正在检测更新...")
        val versionCode = ApkUtils.getVersionCode(this)
        val service = RetrofitManager.getService(UpdateService::class.java)
        val call = service.updateApk(versionCode)
        call.enqueue(object : DoraCallback<String>(){
            override fun onSuccess(body: String) {
                XXPermissions.with(this@SettingsActivity)
                        .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                    .request { permissions, allGranted -> downloadApk(body) }
            }

            override fun onFailure(code: Int, msg: String) {
                if (code == 201) {
                    showShortToast("暂无可用更新")
                } else if (code == 202) {
                    showShortToast("已经是最新版本了")
                }  else {
                    showShortToast("无法访问服务器")
                }
            }
        })
    }

    private fun downloadApk(url: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(url)
                .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body
                updateHandler.obtainMessage(0).sendToTarget()
                try {
                    val bytes = responseBody!!.bytes()
                    val savePath = (AppConfig.FOLDER_APK + File.separator
                            + "doramusic-" + ApkUtils.getVersionName(this@SettingsActivity) + ".apk")
                    IoUtils.write(bytes, savePath)
                    updateHandler.obtainMessage(1, savePath).sendToTarget()
                } catch (e: IOException) {
                    updateHandler.obtainMessage(2).sendToTarget()
                }
            }
        })
    }

    fun installApk(context: Context, file: File) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        val uri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val contentUri = FileProvider.getUriForFile(context,
                    BuildConfig.APPLICATION_ID + ".fileprovider", file)
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        } else {
            uri = Uri.fromFile(file)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
        }
        context.startActivity(intent)
    }
}
