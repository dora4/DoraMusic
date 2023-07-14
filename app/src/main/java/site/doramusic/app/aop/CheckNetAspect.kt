/*
 * Copyright (C) 2019 The JackKnife Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package site.doramusic.app.aop

import android.app.Activity
import dora.util.NetUtils
import dora.widget.DoraAlertDialog
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import site.doramusic.app.R

/**
 * 如果用户使用移动网络下载则拦截。
 */
@Aspect
class CheckNetAspect {

    @Around("execution(@site.doramusic.app.annotation.CheckNet * *(..))")
    @Throws(Throwable::class)
    fun aroundJoinPoint(joinPoint: ProceedingJoinPoint) {
        val activity = joinPoint.target as Activity
        if (NetUtils.isMobileConnected(activity)) {
            DoraAlertDialog(activity)
                .show("当前处于移动网络，下载将消耗较多流量，是否继续下载？") {
                themeColorResId(R.color.colorPrimary)
                positiveListener {
                    joinPoint.proceed()
                }
            }
        } else {
            joinPoint.proceed()
        }
    }
}