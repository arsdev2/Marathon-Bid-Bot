package com.arsdev.bidapplication;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends Activity{
    EditText loginEditText, passEditText, pin, ip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        setContentView(R.layout.login_layout);
        SharedPreferences sp = getSharedPreferences("logpass", MODE_PRIVATE);
        loginEditText = (EditText) findViewById(R.id.loginEditText);
        passEditText = (EditText) findViewById(R.id.passEditText);
        pin = (EditText) findViewById(R.id.pin);
        ip = (EditText) findViewById(R.id.ipEditText);
        loginEditText.setText(sp.getString("login", ""));
        passEditText.setText(sp.getString("pass", ""));
        pin.setText(sp.getString("pin", ""));
        ip.setText(sp.getString("ip", ""));
    }

    public void login(View view) {
        SharedPreferences sp = getSharedPreferences("logpass", MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        editor.putString("login", loginEditText.getText().toString());
        editor.putString("pass", passEditText.getText().toString());
        editor.putString("pin", pin.getText().toString());
        editor.putString("ip", ip.getText().toString());
        editor.apply();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
