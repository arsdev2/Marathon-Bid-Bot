package com.arsdev.bidapplication;


import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static int BLOCKS = 128;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try{
            Log.d("TAG", "RECEIVE");
            SharedPreferences preferences = getSharedPreferences("logpass", MODE_PRIVATE);
            String pass = preferences.getString("pin", "");
            if(pass.equals("")){
                throw new Exception();
            }
            String data = decrypt(remoteMessage.getData().get("data"),  md5(pass));
            Intent intent = new Intent(MyFirebaseMessagingService.this,   MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("data", data);
            startActivity(intent);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Invalid pin code!", Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MyFirebaseMessagingService.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            e.printStackTrace();
        }
        super.onMessageReceived(remoteMessage);
    }

    private static String md5(String st) {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);

        while( md5Hex.length() < 32 ){
            md5Hex = "0" + md5Hex;
        }

        return md5Hex;
    }

    public static String decrypt(String data, String password) throws Exception{
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(data, Base64.DEFAULT);
        byte[] devVal = c.doFinal(decodedValue);
        return new String(devVal);
    }

    public static SecretKeySpec generateKey(String password) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        return new SecretKeySpec(key, "AES");
    }

}