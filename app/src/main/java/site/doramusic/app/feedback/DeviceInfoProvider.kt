package site.doramusic.app.feedback

import android.os.Build
import dora.util.ApkUtils

object DeviceInfoProvider {

    data class DeviceInfo(
        val brand: String,
        val model: String,
        val manufacturer: String,
        val os: String,
        val osVersion: String,
        val sdkInt: Int,
        val appVersion: String
    )

    fun collect(): DeviceInfo {
        return DeviceInfo(
            brand = Build.BRAND ?: "",
            model = Build.MODEL ?: "",
            manufacturer = Build.MANUFACTURER ?: "",
            os = "Android",
            osVersion = Build.VERSION.RELEASE ?: "",
            sdkInt = Build.VERSION.SDK_INT,
            appVersion = ApkUtils.getVersionName()
        )
    }
}
