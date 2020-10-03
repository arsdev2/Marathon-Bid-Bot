package com.arsdev.bidapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity{

    private static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36", TAG = "APP";
    private WebView webView;
    private JsonObject object = null;
    private String url = "", API_IP = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sp = getSharedPreferences("logpass", MODE_PRIVATE);
        if(!sp.contains("login") && !sp.contains("pass") && !sp.contains("pin") && !sp.contains("ip")){
            openLoginActivity();
        }
        API_IP = sp.getString("ip", "");
        if(API_IP.equals("")){
            openLoginActivity();
        }
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString(DESKTOP_USER_AGENT);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed(); // Ignore SSL certificate errors
            }
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, url);
                view.evaluateJavascript("(function() { return document.getElementsByClassName(\"login-pass\")[0].innerHTML; })();", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        try {
                            if (s.equals("null")) {
                                Log.d(TAG, "logined");
                                webView.evaluateJavascript("if(document.getElementById(\"punterMessage\").style.display == \"block\" ){\n" +
                                        "\tdocument.getElementById(\"punterMessage\").getElementsByTagName(\"span\")[0].click();\n" +
                                        "}", null);
                                if (object.get("type").getAsString().equals("bid")) {
                                    webView.evaluateJavascript("var input = document.querySelector(\"td.stake\").firstElementChild;\n" +
                                            "input.click();\n" +
                                            "input.focus();\n" +
                                            "input.setAttribute(\"value\", \"\");\n" +
                                            "document.execCommand(\"insertText\", null, \"" + object.get("sum").getAsString() + "\");\n" +
                                            "document.querySelector(\"td.stake\").firstElementChild.onkeyup(4);\n" +
                                            "getBetslip().confirmPlaceBet(false, true);", null);
                                }
                            } else {
                                Log.d(TAG, "don't logined");
                                SharedPreferences preferences = getSharedPreferences("logpass", MODE_PRIVATE);
                                String login = preferences.getString("login", null);
                                String pass = preferences.getString("pass", null);

                                webView.evaluateJavascript("(function() { document.getElementById(\"auth_login\").value = \"" + login + "\";\n" +
                                        "document.getElementsByClassName(\"login-pass\")[0].getElementsByClassName(\"pass\")[0].childNodes[1].value = \"" + pass + "\";\n" +
                                        "document.getElementsByClassName(\"btn-login\")[0].click(); }) ();", null);
                            }
                        }catch (Exception e){
                            Toast.makeText(MainActivity.this, "Error in json object!", Toast.LENGTH_LONG).show();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Error in json object");
                        }
                    }
                });
            }

        });
        requestBid();
        Log.d(TAG, "onCreate");

    }

    private void openLoginActivity(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void requestBid(){
        String data = getIntent().getExtras() != null ? getIntent().getExtras().getString("data") : null;
        if(data != null){
            onResult(data);
        }
    }
    private void onResult(String data){
        object = new JsonParser().parse(data).getAsJsonObject();
        String type = object.get("type").getAsString();
        if(type.equals("bid")) {
            url = object.get("url").getAsString();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl(url);
                }
            });
            Log.d(TAG, "Loading url: " + url);
        }else if(type.equals("refresh")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.reload();
                }
            });
            Log.d("APP", "reload");
        }
    }
}
