//package site.doramusic.app.aop
//
//import android.util.Log
//import org.aspectj.lang.ProceedingJoinPoint
//import org.aspectj.lang.annotation.Around
//import org.aspectj.lang.annotation.Aspect
//import org.aspectj.lang.annotation.Pointcut
//import org.aspectj.lang.reflect.MethodSignature
//import org.aspectj.lang.reflect.SourceLocation
//import site.doramusic.app.BuildConfig
//
///**
// * 打印方法执行时间。
// */
//@Aspect
//internal class TimeTraceAspect {
//
//    @Pointcut("execution(@site.doramusic.app.annotation.TimeTrace * *(..))")
//    fun methodAnnotated() {
//    }
//
//    @Around("methodAnnotated()")
//    @Throws(Throwable::class)
//    fun methodTimeTrace(joinPoint: ProceedingJoinPoint) {
//        if (BuildConfig.DEBUG) {
//            val methodSignature: MethodSignature = joinPoint.signature as MethodSignature
//            val methodLocation: SourceLocation = joinPoint.sourceLocation
//            val methodLine = methodLocation.line
//            val className = methodSignature.declaringType.simpleName
//            val methodName = methodSignature.name
//            val startTime = System.currentTimeMillis()
//            joinPoint.proceed()
//            val duration: Long = System.currentTimeMillis() - startTime
//            Log.d("TimeTrace", String.format("ClassName:【%s】,Line:【%s】,Method:【%s】,【%s】耗时:【%dms】", className, methodLine, methodName, duration))
//        } else {
//            joinPoint.proceed()
//        }
//    }
//}