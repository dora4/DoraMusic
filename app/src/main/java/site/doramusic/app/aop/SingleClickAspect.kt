//package site.doramusic.app.aop
//
//import dora.util.LogUtils
//import org.aspectj.lang.ProceedingJoinPoint
//import org.aspectj.lang.annotation.Around
//import org.aspectj.lang.annotation.Aspect
//import org.aspectj.lang.annotation.Pointcut
//
///**
// * 能过注解@SingleClick aop切片的方式在编译期间织入源代码中，防止二次点击。
// */
//@Aspect
//class SingleClickAspect {
//
//    private var lastClickTime: Long = 0
//
//    @Pointcut("execution(@site.doramusic.app.annotation.SingleClick * *(..))")
//    fun singleClick() {
//    }
//
//    @Around("singleClick()")
//    @Throws(Throwable::class)
//    fun aroundPointMethod(joinPoint: ProceedingJoinPoint) {
//        if (isFastClick) {
//            LogUtils.e("您的手速太赞了")
//            return
//        }
//        joinPoint.proceed()
//    }
//
//    private val isFastClick: Boolean
//        get() {
//            val currentClickTime = System.currentTimeMillis()
//            val flag = currentClickTime - lastClickTime < MIN_DELAY_TIME
//            lastClickTime = currentClickTime
//            return flag
//        }
//
//    companion object {
//        private const val MIN_DELAY_TIME = 500
//    }
//}