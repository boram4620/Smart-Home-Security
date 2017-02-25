package com.codefrom;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.webkit.CookieManager;
import android.util.Log;

public class WebViewActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = (WebView) findViewById(R.id.webView1);
        webView.loadUrl("http://www.weibo.com");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Toast.makeText(WebViewActivity.this, "Page load successfully.", Toast.LENGTH_SHORT).show();
                String cookies = CookieManager.getInstance().getCookie(url);
                String TAG = "FB testing: ";
                Log.d(TAG, "All the cookies in a string:" + cookies);
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
    }
}
