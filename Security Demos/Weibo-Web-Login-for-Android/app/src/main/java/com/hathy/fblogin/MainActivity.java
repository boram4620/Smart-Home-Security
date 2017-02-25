package com.hathy.fblogin;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Bitmap;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.ViewGroup.LayoutParams;

import android.content.Context;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.net.*;
import java.util.*;

public class MainActivity extends Activity {

    protected WebView mainWebView;
    // private ProgressBar mProgress;
    private Context mContext;
    private WebView mWebviewPop;
    private FrameLayout mContainer;
    private ProgressBar progress;

    private String url = "http://www.weibo.com";
    private String target_url_prefix = "www.weibo.com";

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    // private GoogleApiClient client;
/*    public void onBackPressed() {

        if (mainWebView.isFocused() && mainWebView.canGoBack()) {
            mainWebView.goBack();
        } else {
            super.onBackPressed();
            finish();
        }
    }
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*

        // Get cookie manager for WebView
        // This must occur before setContentView() instantiates your WebView
        android.webkit.CookieSyncManager webCookieSync =
                CookieSyncManager.createInstance(this);
        android.webkit.CookieManager webCookieManager =
                android.webkit.CookieManager.getInstance();
        webCookieManager.setAcceptCookie(true);

        // Get cookie manager for HttpURLConnection
        java.net.CookieStore rawCookieStore = ((java.net.CookieManager)
                CookieHandler.getDefault()).getCookieStore();

        // Construct URI
        java.net.URI baseUri = null;
        try {
            baseUri = new URI("http://www.weibo.com");
        } catch (URISyntaxException e) {
            // Handle invalid URI
            e.printStackTrace();
        }

        // Copy cookies from HttpURLConnection to WebView
        List<HttpCookie> cookies = rawCookieStore.get(baseUri);
        String url = baseUri.toString();
        for (HttpCookie cookie : cookies) {
            String setCookie = new StringBuilder(cookie.toString())
                    .append("; domain=").append(cookie.getDomain())
                    .append("; path=").append(cookie.getPath())
                    .toString();
            webCookieManager.setCookie(url, setCookie);
        }

        */

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mContext = this.getApplicationContext();

        // Get main webview
        mainWebView = (WebView) findViewById(R.id.wv_main);

        progress = (ProgressBar) findViewById(R.id.progressBar);
        progress.setMax(100);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mainWebView.setWebContentsDebuggingEnabled(true);
        }

        mainWebView.getSettings().setUserAgentString("MyLogin");

        // Cookie manager for the webview
        android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        String cookies = android.webkit.CookieManager.getInstance().getCookie(url);
        String TAG = "Initialization: ";
        Log.d(TAG, "1st Cookies:" + cookies);

        // Get outer container
        mContainer = (FrameLayout) findViewById(R.id.webview_frame);

        //LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        //mContainer.addView(mainWebView, params);

        // Settings
        WebSettings webSettings = mainWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        //webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);

        mainWebView.setWebViewClient(new MyCustomWebViewClient());
        //mainWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        mainWebView.setWebChromeClient(new MyCustomChromeClient());
        mainWebView.loadUrl(url);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        mContext = this.getApplicationContext();
    }

/*    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.hathy.fblogin/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.hathy.fblogin/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
*/

// @Override
// public boolean onCreateOptionsMenu(Menu menu) {
// // Inflate the menu; this adds items to the action bar if it is present.
// getMenuInflater().inflate(R.menu.example_main, menu);
// return true;
// }

    private class MyCustomWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            progress.setProgress(0);
            progress.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @SuppressLint("LongLogTag")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String host = Uri.parse(url).getHost();
            Log.d("shouldOverrideUrlLoading", host);
            //Toast.makeText(MainActivity.this, host,
            //Toast.LENGTH_SHORT).show();
            if (host.equals(target_url_prefix)) {
                // This is my web site, so do not override; let my WebView load
                // the page
                if (mWebviewPop != null) {
                    mWebviewPop.setVisibility(View.GONE);
                    mContainer.removeView(mWebviewPop);
                    mWebviewPop = null;
                }
                return false;
            }

            if (host.contains("www.weibo.com") || host.contains("m.weibo.com")) {
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch
            // another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
            //return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progress.setVisibility(View.GONE);
            super.onPageFinished(view, url);
            String cookies = android.webkit.CookieManager.getInstance().getCookie(url);
            String TAG = "Page Loaded: ";
            Log.d(TAG, "Get Cookies:" + cookies);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            Log.d("onReceivedSslError", "onReceivedSslError");
            // super.onReceivedSslError(view, handler, error);
        }
    }

    public void setValue(int progress) {
        this.progress.setProgress(progress);
    }

    public void showAlert(Context context, String title, String text) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set title
        alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder.setMessage(text).setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        finish();
                    }
                }).create().show();

    }

    private class MyCustomChromeClient extends WebChromeClient {

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            mWebviewPop = new WebView(mContext);
            mWebviewPop.setVerticalScrollBarEnabled(false);
            mWebviewPop.setHorizontalScrollBarEnabled(false);
            mWebviewPop.setWebViewClient(new MyCustomWebViewClient());
            mWebviewPop.getSettings().setJavaScriptEnabled(true);
            mWebviewPop.getSettings().setSavePassword(false);
            mWebviewPop.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mContainer.addView(mWebviewPop);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebviewPop);
            resultMsg.sendToTarget();

            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            // TODO Auto-generated method stub
            super.onProgressChanged(view, newProgress);
            MainActivity.this.setValue(newProgress);
        }

        @Override
        public void onCloseWindow(WebView window) {
            Log.d("onCloseWindow", "called");
        }

    }
}