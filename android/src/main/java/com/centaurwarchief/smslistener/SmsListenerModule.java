package com.centaurwarchief.smslistener;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

public class SmsListenerModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private BroadcastReceiver mReceiver;
    private boolean isReceiverRegistered = false;
    private ReactApplicationContext context;

    public SmsListenerModule(ReactApplicationContext context) {
        super(context);

        this.context = context;
        mReceiver = new SmsReceiver(context);
        getReactApplicationContext().addLifecycleEventListener(this);
        registerReceiverIfNecessary(mReceiver);
    }

    private void registerReceiverIfNecessary(BroadcastReceiver receiver) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && getCurrentActivity() != null) {
            this.requestPermissions();
            getCurrentActivity().registerReceiver(receiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
            isReceiverRegistered = true;
            return;
        }

        if (getCurrentActivity() != null) {
            getCurrentActivity().registerReceiver(receiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
            isReceiverRegistered = true;
        }
    }

    private void unregisterReceiver(BroadcastReceiver receiver) {
        if (isReceiverRegistered && getCurrentActivity() != null) {
            getCurrentActivity().unregisterReceiver(receiver);
            isReceiverRegistered = false;
        }
    }

    private void requestPermissions() {
        String[] permissions = new String[]{Manifest.permission.RECEIVE_SMS};
        Log.d(SmsListenerPackage.TAG, "Trying to receive permissions");
        ActivityCompat.requestPermissions(getCurrentActivity(), permissions, 1);
    }

    @Override
    public void onHostResume() {
        registerReceiverIfNecessary(mReceiver);
    }

    @Override
    public void onHostPause() {
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onHostDestroy() {
        unregisterReceiver(mReceiver);
    }

    @Override
    public String getName() {
        return "SmsListenerPackage";
    }
}
