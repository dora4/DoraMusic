# 保留所有注解
-keep @interface * { *; }
# 确保注解的元数据不会被移除
-keepattributes *Annotation*

-dontwarn com.alipay.sdk.app.H5PayCallback
-dontwarn com.alipay.sdk.app.PayTask
-dontwarn com.download.library.DownloadImpl
-dontwarn com.download.library.DownloadListenerAdapter
-dontwarn com.download.library.DownloadTask
-dontwarn com.download.library.ResourceRequest
-dontwarn javax.lang.model.element.Element
-dontwarn javax.lang.model.element.Modifier

# ARouter
-keep class com.alibaba.android.arouter.** { *; }
-keep class * implements com.alibaba.android.arouter.facade.template.ISyringe { *; }
-keep class * implements com.alibaba.android.arouter.facade.template.IRouteGroup { *; }
-keep class * implements com.alibaba.android.arouter.facade.template.IProvider { *; }
-keepclasseswithmembernames class * {
    @Autowired <fields>;
}

# 保留 GlobalConfig 接口的所有实现类
-keep class * implements dora.lifecycle.config.GlobalConfig { *; }
# 保留泛型参数，确保 getActualTypeArguments() 能正确解析
-keepattributes Signature

-keep class * implements dora.db.table.OrmTable { *; }
-keep class * implements dora.db.converter.PropertyConverter { *; }
