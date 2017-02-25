package com.codefrom;

import com.codefrom.utils.CommonVars;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.content.LocalBroadcastManager;

public class MainActivity extends Activity {

    private TextView username;
    private Button loginbtn, openWebView;

    private BroadcastReceiver loginBroadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;

    boolean isLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = (TextView) findViewById(R.id.username);
        loginbtn = (Button) findViewById(R.id.loginbtn);
        openWebView = (Button) findViewById(R.id.openWebView);

        loginbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isLogin) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    CookieManager cookies = CookieManager.getInstance();
                    String fbdata = cookies.getCookie("www.weibo.com");
                    String TAG = "FB Cookie: ";
                    Log.d(TAG, "Captured:" + fbdata);
                } else {
                    username.setText("Name");
                    loginbtn.setText("Log In");
                    isLogin = false;
                    //CookieManager cookieManager = CookieManager.getInstance();
                    //cookieManager.removeAllCookie();
                    //CookieSyncManager.getInstance().sync();
                    CookieManager cookies = CookieManager.getInstance();
                    String fbdata = cookies.getCookie("www.weibo.com");
                    String TAG = "FB Cookie: ";
                    Log.d(TAG, "Captured:" + fbdata);
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        openWebView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                startActivity(intent);
            }
        });

        localBroadcastManager = LocalBroadcastManager.getInstance(MainActivity.this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CommonVars.LOGIN_BROADCAST);
        loginBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();

                boolean result = bundle.getBoolean("result");
                String user = bundle.getString("username");
                if (result) {
                    username.setText(user);
                    Toast.makeText(MainActivity.this, "Page load successfully.", Toast.LENGTH_SHORT).show();
                    loginbtn.setText("Log Out");
                    isLogin = true;
                } else {
                    Toast.makeText(MainActivity.this, "Page load failed.", Toast.LENGTH_SHORT).show();
                    username.setText("Not Log-In");
                    loginbtn.setText("Log In");
                    isLogin = false;
                }
            }
        };
        localBroadcastManager.registerReceiver(loginBroadcastReceiver, intentFilter);
    }
}
