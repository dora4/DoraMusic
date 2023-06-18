package site.doramusic.app.aop

import com.lwh.jackknife.util.Logger
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect

/**
 * 能过注解@SingleClick aop切片的方式在编译期间织入源代码中，防止二次点击。
 */
@Aspect
class SingleClickAspect {

    private var lastClickTime: Long = 0

    @Around("execution(@site.doramusic.app.annotation.SingleClick * *(..))")
    @Throws(Throwable::class)
    fun aroundPointMethod(joinPoint: ProceedingJoinPoint) {
        if (isFastClick) {
            Logger.debug("您的手速太赞了")
            return
        }
        joinPoint.proceed()
    }

    private val isFastClick: Boolean
        get() {
            val currentClickTime = System.currentTimeMillis()
            val flag = currentClickTime - lastClickTime < MIN_DELAY_TIME
            lastClickTime = currentClickTime
            return flag
        }

    companion object {
        private const val MIN_DELAY_TIME = 500
    }
}