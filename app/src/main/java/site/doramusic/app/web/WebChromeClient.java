package com.dorachat.dorachat.web;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.ValueCallback;

public class WebChromeClient extends BaseWebChromeClient {

    private ValueCallback<Uri> mUploadMessage;
    private Activity mActivity;

    public WebChromeClient(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooser(mUploadMessage, "", "");
    }

    @Override
    public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
        openFileChooser(mUploadMessage, acceptType, "");
    }

    @Override
    public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
        mUploadMessage = valueCallback;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        mActivity.startActivityForResult(Intent.createChooser(intent, "选择要上传的文件"),
                REQUEST_FILE_PICKER);
    }

    @Override
    public void onActivityResult(int resultCode, Intent data) {
        if (mUploadMessage == null) {
            return;
        }
        Uri result = data == null || resultCode != Activity.RESULT_OK ? null : data.getData();
        if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }
}