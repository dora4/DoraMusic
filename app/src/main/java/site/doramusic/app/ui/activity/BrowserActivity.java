package site.doramusic.app.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.just.agentweb.AgentWeb;
import com.just.agentweb.WebIndicator;

import dora.BaseActivity;
import dora.util.IntentUtils;
import dora.util.StatusBarUtils;
import site.doramusic.app.R;
import site.doramusic.app.databinding.ActivityBrowserBinding;

public class BrowserActivity extends BaseActivity<ActivityBrowserBinding> {

    private String title = "扫码结果";
    private String url;
    private AgentWeb agentWeb;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_browser;
    }

    @Override
    protected void onGetExtras(@Nullable String action, @Nullable Bundle bundle, @NonNull Intent intent) {
        if (IntentUtils.hasExtra(intent, "title")) {
            title = IntentUtils.getStringExtra(intent, "title");
        }
        if (IntentUtils.hasExtra(intent, "url")) {
            url = IntentUtils.getStringExtra(intent, "url");
        }
    }

    @Override
    protected void onSetStatusBar() {
        StatusBarUtils.setStatusBarColorRes(this, R.color.colorPrimaryDark);
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        mBinding.titlebar.setTitle(title);
        WebIndicator webIndicator = new WebIndicator(this);
        webIndicator.setColor(getResources().getColor(R.color.colorPrimary));
        agentWeb = AgentWeb.with(this)
                .setAgentWebParent(mBinding.rlBrowserWebPage,
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)
                )
                .setCustomIndicator(webIndicator)
                .createAgentWeb()
                .ready()
                .go(url);
        agentWeb.getJsInterfaceHolder().addJavaObject("android", new AndroidInterface(agentWeb, this, new WebJsInterfaceCallback() {
            @Override
            public void nativeApi(String params) {

            }
        }));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    public interface WebJsInterfaceCallback {

        void nativeApi(String params);

    }

    public class AndroidInterface {
        private AgentWeb agent;
        private Context context;
        private WebJsInterfaceCallback interfaceCallback;
        private String TAG = "AndroidInterfaceWeb";

        public AndroidInterface(AgentWeb agent, Context context, WebJsInterfaceCallback interfaceCallback) {
            this.agent = agent;
            this.context = context;
            this.interfaceCallback = interfaceCallback;
        }

        //定义h5要调用的本地方法
        @JavascriptInterface
        public void Android_BuyVip(String mtdName, String params) {
            switch (mtdName) {
                case "nativeApi":
                    if (interfaceCallback != null) {
                        interfaceCallback.nativeApi(params);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return agentWeb.handleKeyEvent(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        agentWeb.destroy();
    }
}
