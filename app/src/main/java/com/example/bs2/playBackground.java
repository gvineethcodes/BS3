package com.example.bs2;

import static com.example.bs2.MainActivity.setAlarming;
import static com.example.bs2.MainActivity.sharedpreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class playBackground extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case "alarm":
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(new Intent(context, playService.class).setAction("play"));
                        }else {
                            context.startService(new Intent(context, playService.class).setAction("play"));
                        }
                        setAlarming(context);
                        break;

                    case "android.intent.action.BOOT_COMPLETED":
                        if (sharedpreferences.getBoolean("checkBox",true))
                            setAlarming(context);
                        break;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
