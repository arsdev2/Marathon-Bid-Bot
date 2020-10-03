package com.arsdev.bidapplication;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        Log.d("APP", FirebaseInstanceId.getInstance().getToken());
        super.onTokenRefresh();
    }
}
