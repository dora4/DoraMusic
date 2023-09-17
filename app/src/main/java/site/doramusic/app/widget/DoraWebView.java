package com.dorachat.dorachat.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;

import dora.util.NetUtils;

public class DoraWebView extends WebView {

    public DoraWebView(Context context) {
        this(context, null);
    }

    public DoraWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoraWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 遵循《Effective Java 第二版》一书中第22条:优先考虑静态成员类。
     */
    public static class DoraWebChromeClient extends WebChromeClient {

        // =========HTML5定位==========================================================
        // 需要先加入权限
        // <uses-permission android:name="android.permission.INTERNET"/>
        // <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
        // <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            super.onGeolocationPermissionsHidePrompt();
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);//注意个函数，第二个参数就是是否同意定位权限，第三个是是否希望内核记住
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        //=========HTML5定位==========================================================


        //=========多窗口的问题==========================================================
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebViewTransport transport = (WebViewTransport) resultMsg.obj;
            transport.setWebView(view);
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                // 网页加载完成
//                popupdialog.dismiss();
//                getSettings().setBlockNetworkImage(false);
            } else {
                // 网页加载中
//                popupdialog.show();
            }
        }
    }

    private void init() {
        WebSettings mWebSettings = getSettings();
        mWebSettings.setSupportZoom(true);
//        mWebSettings.setLoadWithOverviewMode(true);
        mWebSettings.setUseWideViewPort(true);
        mWebSettings.setDefaultTextEncodingName("utf-8");
        mWebSettings.setLoadsImagesAutomatically(true);
        mWebSettings.setAllowFileAccess(true);
        // 开启硬件加速
//        mWebSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
//        mWebSettings.setBlockNetworkImage(true);
        mWebSettings.setTextZoom(100);  //不随系统改变字体

        // 调用JS方法.安卓版本大于17,加上注解 @JavascriptInterface
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setAllowFileAccessFromFileURLs(true);
        mWebSettings.setSupportMultipleWindows(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 缓存数据
        saveData(mWebSettings);
        newWindow(mWebSettings);
        setWebChromeClient(new DoraWebChromeClient());
        setWebViewClient(webViewClient);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(this, true);
        }
    }

    /**
     * 多窗口的问题。
     */
    private void newWindow(WebSettings mWebSettings) {
        // html中的_bank标签就是新建窗口打开，有时会打不开，需要加以下
        // 然后 复写 WebChromeClient的onCreateWindow方法
        mWebSettings.setSupportMultipleWindows(false);
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    }

    /**
     * 网页数据存储。
     */
    private void saveData(WebSettings mWebSettings) {
        // 有时候网页需要自己保存一些关键数据, Android WebView 需要自己设置
        if (NetUtils.checkNetworkAvailable(getContext())) {
            // 根据cache-control决定是否从网络上取数据。
            mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        } else {
            // 没网，则从本地获取，即离线加载
            mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        }
        mWebSettings.setDomStorageEnabled(true);
        mWebSettings.setDatabaseEnabled(true);
    }

    public boolean parseThirdPartyPayScheme(String url) {
        return url.startsWith("weixin://wap/pay?")
                || url.startsWith("alipay://")
                || url.startsWith("mqqapi://")
                || url.startsWith("mqqwpa://im");
    }

    public boolean parsePlatformScheme(String url) {
        return url.contains("platformapi/startapp") || (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
                && (url.contains("platformapi") && url.contains("startapp"));
    }

    WebViewClient webViewClient = new WebViewClient() {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d("onPageStarted:", url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("Url:", url);
            // 如下方案可在非微信等内部WebView的H5页面中调出微信支付
            if (parseThirdPartyPayScheme(url)) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                getContext().startActivity(intent);
                return true;
            } else if (parsePlatformScheme(url)) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    intent.addCategory("android.intent.category.BROWSABLE");
                    intent.setComponent(null);
                    // intent.setSelector(null);
                    getContext().startActivity(intent);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                view.loadUrl(url);
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            // 加载图片资源
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            getSettings().setBlockNetworkImage(false);
            // 判断WebView是否加载了，图片资源
            if (!getSettings().getLoadsImagesAutomatically()) {
                // 设置WebView加载图片资源
                getSettings().setLoadsImagesAutomatically(true);
            }
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            view.loadUrl("file:///android_asset/error_page.html");
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    };
}
