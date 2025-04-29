package com.example.weather.ui;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weather.BuildConfig;
import com.example.weather.R;

import java.lang.reflect.Field;

public class SetSchActivity extends AppCompatActivity {

    private LinearLayout mywebLayout;
    private WebView web = null;

    private Button backButton;
    private Button downloadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_sch);
        backButton = findViewById(R.id.back_button);
        downloadBtn = findViewById(R.id.setting);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                injectJavaScript();
                // 弹出询问框
                AlertDialog.Builder builder = new AlertDialog.Builder(SetSchActivity.this);
                builder.setMessage("是否覆盖当前课表？")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                // 创建并显示AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        initView();
        initData();
    }


    private void initView() {
        mywebLayout = findViewById(R.id.LinerLayout);
    }

    @Override
    public void onDestroy() {
        if (web != null) {
            web.clearHistory();
            web.clearCache(true);
            mywebLayout.removeView(web);
            web.removeAllViews();
            web.destroy();
            web = null;
        }
        releaseAllWebViewCallback();
        super.onDestroy();
    }

    private void initData() {
        setWebView();
    }

    private void injectJavaScript() {
        String javascriptCode = "javascript:(function(u,s){s=document.body.appendChild(document.createElement('script'));s.src=u+'?ts='+Date.now();s.charset='UTF-8'}('https://the-red-hat-was-uncovered.gitee.io/supwisdom-course-table/dist.js'))";

        web.loadUrl(javascriptCode);
    }

    private void setWebView() {
        if(web == null) {
            web = new WebView(this);
            web.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            mywebLayout.addView(web);
        }
        // WebView也就是我们熟悉的“网络视图”，能加载并显示网页，可以将其视为一个浏览器。主要用于展示网络请求后的内容，就是将网络地址请求的内容展示在里面。
        String url = "http://219.216.96.4/eams/homeExt.action";
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);    // 允许执行 JavaScript 脚本
        settings.setDomStorageEnabled(true);    // 支持网页使用 localStorage 或 sessionStorage，适用于需要登录的页面。
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(true);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setTextZoom(100);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setCacheMode(settings.LOAD_NO_CACHE); // 不使用缓存

        // webview加载网络，是需要联网的，这是时候就需要一个联网的权限： <uses-permission android:name="android.permission.INTERNET"/>
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            settings.setMixedContentMode(settings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        if (web.canGoBack()) {
            web.goBack();
        }
        if (web.canGoForward()) {
            web.goForward();
        }
// 缓存相关
        web.clearCache(true); // 清除缓存
        web.clearHistory(); // 清除历史
        web.clearFormData(); // 清除表单数据
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);// 设置缓存模式
        settings.setUseWideViewPort(true); // 将图片调整到适合 WebView 的大小
        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
// 缩放操作
        settings.setSupportZoom(true); // 支持缩放，默认为 true
        settings.setBuiltInZoomControls(true); // 设置内置的缩放控件，若为 false，则该 WebView 不可缩放
        settings.setDisplayZoomControls(false); // 隐藏原生的缩放控件

        web.setInitialScale(100);
        web.loadUrl(url);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            class JsObject{

                @JavascriptInterface
                public void funAndroid() {
                    Toast.makeText(getApplicationContext(), "调用android本地方法funAndroid!", Toast.LENGTH_LONG).show();
                }

            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                web.addJavascriptInterface(new JsObject(), "android");
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }
        });
    }

    public void releaseAllWebViewCallback() {
        if (android.os.Build.VERSION.SDK_INT < 16) {
            try {
                Field field = WebView.class.getDeclaredField("mWebViewCore");
                field = field.getType().getDeclaredField("mBrowserFrame");
                field = field.getType().getDeclaredField("sConfigCallback");
                field.setAccessible(true);
                field.set(null, null);
            } catch (NoSuchFieldException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } catch (IllegalAccessException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                Field sConfigCallback = Class.forName("android.webkit.BrowserFrame").getDeclaredField("sConfigCallback");
                if (sConfigCallback != null) {
                    sConfigCallback.setAccessible(true);
                    sConfigCallback.set(null, null);
                }
            } catch (NoSuchFieldException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } catch (IllegalAccessException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }
}
