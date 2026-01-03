//package site.doramusic.app.aop
//
//import android.app.Activity
//import com.alibaba.android.arouter.launcher.ARouter
//import com.hjq.permissions.OnPermissionCallback
//import com.hjq.permissions.XXPermissions
//import dora.util.ToastUtils
//import org.aspectj.lang.ProceedingJoinPoint
//import org.aspectj.lang.annotation.Around
//import org.aspectj.lang.annotation.Aspect
//import org.aspectj.lang.annotation.Pointcut
//import site.doramusic.app.annotation.RequirePermission
//import site.doramusic.app.conf.ARoutePath
//import site.doramusic.app.ui.fragment.ProtocolFragment
//
///**
// * Android 6.0以上动态权限申请。
// */
//@Aspect
//class CheckPermissionAspect {
//    @Pointcut("execution(@site.doramusic.app.annotation.RequirePermission * *(..)) && @annotation(permission)")
//    fun checkPermission(permission: RequirePermission) {
//    }
//
//    @Around("checkPermission(permission)")
//    @Throws(Throwable::class)
//    fun aroundJoinPoint(joinPoint: ProceedingJoinPoint, permission: RequirePermission) {
//        val activity = joinPoint.target as Activity
//        if (XXPermissions.isGranted(activity, *permission.value)) {
//            joinPoint.proceed() // 获得权限，执行原方法
//        } else {
//            // 显示隐私权限
//            val dialog: ProtocolFragment = ProtocolFragment.newInstance()
//            dialog.setCallback(object : ProtocolFragment.Callback {
//                override fun onAgree() {
//                    XXPermissions.with(activity)
//                        .permission(*permission.value)
//                        .request(object : OnPermissionCallback {
//                            override fun onGranted(
//                                permissions: MutableList<String>,
//                                allGranted: Boolean
//                            ) {
//                                try {
//                                    joinPoint.proceed() //获得权限，执行原方法
//                                } catch (throwable: Throwable) {
//                                    throwable.printStackTrace()
//                                }
//                            }
//
//                            override fun onDenied(
//                                permissions: MutableList<String>,
//                                doNotAskAgain: Boolean
//                            ) {
//                                if (XXPermissions.isPermanentDenied(activity, permissions)) { // 如果是被永久拒绝就跳转到应用权限系统设置页面
//                                    ToastUtils.showLong(activity, "请在设置中手动授权")
//                                    XXPermissions.startPermissionActivity(activity, permissions)
//                                }
//                            }
//
//                        })
//                    dialog.dismissDialog()
//                }
//
//                override fun onDisagree() {
//                    dialog.dismissDialog()
//                }
//
//            })
//            dialog.setProtocolCallback(object : ProtocolFragment.ProtocolCallback {
//
//                override fun onServiceProtocol() {
//                    ARouter.getInstance().build(ARoutePath.ACTIVITY_PROTOCOL).withString("title", "用户协议").navigation()
//                }
//
//                override fun onPrivacyPolicy() {
//                    ARouter.getInstance().build(ARoutePath.ACTIVITY_PROTOCOL).withString("title", "隐私政策").navigation()
//                }
//            })
//            dialog.show(activity.fragmentManager, "protocol_dialog")
//        }
//    }
//}package site.doramusic.app.aop
//
//import android.app.Activity
//import com.alibaba.android.arouter.launcher.ARouter
//import com.hjq.permissions.OnPermissionCallback
//import com.hjq.permissions.XXPermissions
//import dora.util.ToastUtils
//import org.aspectj.lang.ProceedingJoinPoint
//import org.aspectj.lang.annotation.Around
//import org.aspectj.lang.annotation.Aspect
//import org.aspectj.lang.annotation.Pointcut
//import site.doramusic.app.annotation.RequirePermission
//import site.doramusic.app.conf.ARoutePath
//import site.doramusic.app.ui.fragment.ProtocolFragment
//
///**
// * Android 6.0以上动态权限申请。
// */
//@Aspect
//class CheckPermissionAspect {
//    @Pointcut("execution(@site.doramusic.app.annotation.RequirePermission * *(..)) && @annotation(permission)")
//    fun checkPermission(permission: RequirePermission) {
//    }
//
//    @Around("checkPermission(permission)")
//    @Throws(Throwable::class)
//    fun aroundJoinPoint(joinPoint: ProceedingJoinPoint, permission: RequirePermission) {
//        val activity = joinPoint.target as Activity
//        if (XXPermissions.isGranted(activity, *permission.value)) {
//            joinPoint.proceed() // 获得权限，执行原方法
//        } else {
//            // 显示隐私权限
//            val dialog: ProtocolFragment = ProtocolFragment.newInstance()
//            dialog.setCallback(object : ProtocolFragment.Callback {
//                override fun onAgree() {
//                    XXPermissions.with(activity)
//                        .permission(*permission.value)
//                        .request(object : OnPermissionCallback {
//                            override fun onGranted(
//                                permissions: MutableList<String>,
//                                allGranted: Boolean
//                            ) {
//                                try {
//                                    joinPoint.proceed() //获得权限，执行原方法
//                                } catch (throwable: Throwable) {
//                                    throwable.printStackTrace()
//                                }
//                            }
//
//                            override fun onDenied(
//                                permissions: MutableList<String>,
//                                doNotAskAgain: Boolean
//                            ) {
//                                if (XXPermissions.isPermanentDenied(activity, permissions)) { // 如果是被永久拒绝就跳转到应用权限系统设置页面
//                                    ToastUtils.showLong(activity, "请在设置中手动授权")
//                                    XXPermissions.startPermissionActivity(activity, permissions)
//                                }
//                            }
//
//                        })
//                    dialog.dismissDialog()
//                }
//
//                override fun onDisagree() {
//                    dialog.dismissDialog()
//                }
//
//            })
//            dialog.setProtocolCallback(object : ProtocolFragment.ProtocolCallback {
//
//                override fun onServiceProtocol() {
//                    ARouter.getInstance().build(ARoutePath.ACTIVITY_PROTOCOL).withString("title", "用户协议").navigation()
//                }
//
//                override fun onPrivacyPolicy() {
//                    ARouter.getInstance().build(ARoutePath.ACTIVITY_PROTOCOL).withString("title", "隐私政策").navigation()
//                }
//            })
//            dialog.show(activity.fragmentManager, "protocol_dialog")
//        }
//    }
//}