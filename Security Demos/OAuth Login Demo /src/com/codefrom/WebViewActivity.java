package com.codefrom;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebViewActivity extends Activity {

	private WebView webView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);
		
		webView = (WebView) findViewById(R.id.webView1);
		webView.loadUrl("http://www.codefrom.com");
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				Toast.makeText(WebViewActivity.this, "页面加载完成", Toast.LENGTH_SHORT).show();
			}
		});

		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
	}
}
