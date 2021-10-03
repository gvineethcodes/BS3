package com.example.bs2;

import static com.example.bs2.MainActivity.setAlarming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

public class playBackground extends BroadcastReceiver {
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            sharedpreferences = context.getSharedPreferences(""+R.string.app_name, Context.MODE_PRIVATE);
            editor = sharedpreferences.edit();
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case "alarm":
                        setAlarming(context);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(new Intent(context, playService.class).setAction("play"));
                        }else {
                            context.startService(new Intent(context, playService.class).setAction("play"));
                        }

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
