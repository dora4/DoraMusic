package com.dorachat.dorachat.web;

import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.dorachat.dorachat.widget.DoraWebView;

public abstract class BaseWebChromeClient extends DoraWebView.DoraWebChromeClient {

    protected static final int REQUEST_FILE_PICKER = 1;

    /**
     * For Android < 3.0.
     */
    public void openFileChooser(ValueCallback<Uri> valueCallback) {
    }

    /**
     * For Android 3.0+
     */
    public void openFileChooser(ValueCallback valueCallback, String acceptType) {
    }

    /**
     * For Android 4.1.
     */
    public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
    }

    /**
     * For Android 5.0+.
     */
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback,
                                     FileChooserParams fileChooserParams) {
        return true;
    }

    /**
     * 从相册返回数据的回调。
     *
     * @param resultCode 结果码
     * @param data 图片uri
     */
    public abstract void onActivityResult(int resultCode, Intent data);
}