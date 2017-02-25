package com.codefrom;

/**
 * Created by Justin on 7/19/16.
 */

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static String CLIENT_ID = "227791440613076";
    //Use your own client id
    private static String CLIENT_SECRET = "12b26b5772015e4fac7bd12afabaf660";
    //Use your own client secret
    private static String REDIRECT_URI = "http://www.facebook.com/connect/login_success.html";
    private static String GRANT_TYPE = "authorization_code";
    private static String TOKEN_URL = "http://m.facebook.com";
    private static String OAUTH_URL = "https://www.facebook.com/dialog/oauth?";
    //private static String OAUTH_SCOPE = "https://www.facebook.com";
    //Change the Scope as you need
    WebView web;
    Button auth;
    SharedPreferences pref;
    TextView Access;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        Access = (TextView) findViewById(R.id.Access);
        auth = (Button) findViewById(R.id.auth);
        auth.setOnClickListener(new View.OnClickListener() {
            Dialog auth_dialog;

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                auth_dialog = new Dialog(MainActivity.this);
                auth_dialog.setContentView(R.layout.auth_dialog);
                web = (WebView) auth_dialog.findViewById(R.id.webv);
                web.getSettings().setJavaScriptEnabled(true);
                //web.loadUrl("https://m.facebook.com/login.php?skip_api_login=1&api_key=227791440613076&signed_next=1&next=https%3A%2F%2Fm.facebook.com%2Fv2.2%2Fdialog%2Foauth%3Fredirect_uri%3Dfbconnect%253A%252F%252Fsuccess%26display%3Dtouch%26scope%3Demail%26response_type%3Dtoken%26default_audience%3Dfriends%26return_scopes%3Dtrue%26client_id%3D227791440613076%26ret%3Dlogin%26logger_id%3D7bad185b-8c9e-4c18-bbaf-4e424f75ee21&cancel_url=fbconnect%3A%2F%2Fsuccess%3Ferror%3Daccess_denied%26error_code%3D200%26error_description%3DPermissions%2Berror%26error_reason%3Duser_denied%26e2e%3D%257B%2522init%2522%253A1468775239718%257D&display=touch&locale=en_US&logger_id=7bad185b-8c9e-4c18-bbaf-4e424f75ee21&_rdr%20HTTP/1.1");
                web.loadUrl(OAUTH_URL + "client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&response_type=token,granted_scopes" + "&display=popup" + "&scope=publish_actions,user_photos");
                web.setWebViewClient(new WebViewClient() {

                    boolean authComplete = false;
                    Intent resultIntent = new Intent();

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);

                    }

                    String authCode;

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        String cookies = android.webkit.CookieManager.getInstance().getCookie(url);
                        String TAG = "Page Loaded: ";
                        Log.d(TAG, "Get Cookies:" + cookies);

                        if (url.contains("?code=") && authComplete != true) {
                            Uri uri = Uri.parse(url);
                            authCode = uri.getQueryParameter("code");
                            Log.i("", "CODE : " + authCode);
                            authComplete = true;
                            resultIntent.putExtra("code", authCode);
                            MainActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                            setResult(Activity.RESULT_CANCELED, resultIntent);

                            SharedPreferences.Editor edit = pref.edit();
                            edit.putString("Code", authCode);
                            edit.commit();
                            auth_dialog.dismiss();
                            new TokenGet().execute();
                            Toast.makeText(getApplicationContext(), "Authorization Token is: " + authCode, Toast.LENGTH_SHORT).show();
                        } else if (url.contains("error=access_denied")) {
                            Log.i("", "ACCESS_DENIED_HERE");
                            resultIntent.putExtra("code", authCode);
                            authComplete = true;
                            setResult(Activity.RESULT_CANCELED, resultIntent);
                            Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();

                            auth_dialog.dismiss();
                        }
                    }
                });
                auth_dialog.show();
                auth_dialog.setTitle("Facebook Login");
                auth_dialog.setCancelable(true);
            }
        });
    }

    private class TokenGet extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;
        String Code;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Contacting Facebook ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            Code = pref.getString("Code", "");
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            GetAccessToken jParser = new GetAccessToken();
            JSONObject json = jParser.gettoken(TOKEN_URL, Code, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI, GRANT_TYPE);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            pDialog.dismiss();
            if (json != null) {

                try {

                    String tok = json.getString("access_token");
                    String expire = json.getString("expires_in");
                    String refresh = json.getString("refresh_token");

                    Log.d("Token Access", tok);
                    Log.d("Expire", expire);
                    Log.d("Refresh", refresh);
                    auth.setText("Authenticated");
                    Access.setText("Access Token:" + tok + "nExpires:" + expire + "nRefresh Token:" + refresh);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            }
        }
    }

}
