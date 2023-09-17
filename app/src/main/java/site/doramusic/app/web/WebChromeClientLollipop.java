package com.dorachat.dorachat.web;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class WebChromeClientLollipop extends BaseWebChromeClient {

    private ValueCallback<Uri[]> mUploadCallbackLollipop;
    private Activity mActivity;

    public WebChromeClientLollipop(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * 兼容5.0及以上。
     */
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback,
                                     WebChromeClient.FileChooserParams fileChooserParams) {
        mUploadCallbackLollipop = valueCallback;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        mActivity.startActivityForResult(Intent.createChooser(intent, "选择要上传的文件"),
                REQUEST_FILE_PICKER);
        return true;
    }

    @Override
    public void onActivityResult(int resultCode, Intent data) {
        if (mUploadCallbackLollipop == null) {
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    int itemCount = clipData.getItemCount();
                    results = new Uri[itemCount];
                    for (int i = 0; i < itemCount; i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null) {
                    results = new Uri[] { Uri.parse(dataString) };
                }
            }
        }
        mUploadCallbackLollipop.onReceiveValue(results);
        mUploadCallbackLollipop = null;
    }
}