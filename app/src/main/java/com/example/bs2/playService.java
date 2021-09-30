package com.example.bs2;

import static com.example.bs2.MainActivity.arrayAdapter2;
import static com.example.bs2.MainActivity.subject;
import static com.example.bs2.MainActivity.topic;
import static com.example.bs2.MainActivity.text;
import static com.example.bs2.MainActivity.mStorageRef;
import static com.example.bs2.MainActivity.sharedpreferences;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;


public class playService extends Service {
    public static playService playServiceInstance;
    public static MediaPlayer mediaPlayer = null;
    MediaSessionCompat mediaSessionCompat;
    NotificationManager notificationManager;
    public static String playingSubject = " ", playingTopic = " ";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1", "n", NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, "1")
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }
        playServiceInstance=this;
        mediaSessionCompat = new MediaSessionCompat(this, "My_Media_tag");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         super.onStartCommand(intent, flags, startId);

        try {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case "playPause":
                        playpause(this);
                        break;
                    case "prev":
                        prev(this);
                        break;
                    case "next":
                        next(this);
                        break;
                    case "alarm":
//                        context.sendBroadcast(new Intent(context, playBackground.class).setAction("playPause"));
//                        setAlarming(context);
                        break;

                    case "android.intent.action.BOOT_COMPLETED":
//                        if (sharedpreferences.getBoolean("onOff",true))
//                            setAlarming(context);
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

         return START_STICKY;
    }


    public void playpause(Context context){
        if (mediaPlayer != null && subject.equals(playingSubject) && topic.equals(playingTopic)) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                Intent Bintent = new Intent("UI");
                Bintent.putExtra("key", "playImg");
                LocalBroadcastManager.getInstance(this).sendBroadcast(Bintent);
                showNotification(context,true, R.drawable.ic_baseline_play_arrow_24);

            } else {
                mediaPlayer.start();
                Intent Bintent = new Intent("UI");
                Bintent.putExtra("key", "pauseImg");
                LocalBroadcastManager.getInstance(this).sendBroadcast(Bintent);
                showNotification(context, true, R.drawable.ic_baseline_pause_24);
            }

        } else play(context);
    }

    public void play(Context context) {
        Intent Bintent = new Intent("UI");
        Bintent.putExtra("key", "disableBtn");
        LocalBroadcastManager.getInstance(this).sendBroadcast(Bintent);
        text = "prepaing "+topic;
        showNotification(context, false, R.drawable.ic_baseline_play_arrow_24);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mStorageRef.child(subject)
                .child(topic)
                .getDownloadUrl().addOnSuccessListener(uri -> {
            try {
                mediaPlayer = new MediaPlayer();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mediaPlayer.setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build()
                    );
                }
                mediaPlayer.setDataSource(String.valueOf(uri));
                mediaPlayer.prepareAsync();

                mediaPlayer.setOnPreparedListener(mediaPlayer -> {
                    mediaPlayer.start();
                    playingSubject = subject;
                    playingTopic = topic;
                    Bintent.putExtra("key", "EnablePauseMax");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(Bintent);
                    text=topic;
                    showNotification(context, true, R.drawable.ic_baseline_pause_24);

                });
                mediaPlayer.setOnCompletionListener(mediaPlayer -> {
                    Bintent.putExtra("key", "playImg");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(Bintent);
                    showNotification(context, true, R.drawable.ic_baseline_play_arrow_24);

                });
                mediaPlayer.setOnErrorListener((mediaPlayer, i, i1) -> {
                    Bintent.putExtra("key", "enablePlay");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(Bintent);
                    text="Try again "+topic;
                    showNotification(context, true, R.drawable.ic_baseline_play_arrow_24);

                    return false;
                });

            } catch (IOException e) {
                text=e.getMessage();
            }
        });
        mStorageRef.child(subject)
                .child(topic)
                .getDownloadUrl().addOnFailureListener(e -> {
            text=e.getMessage();
            Bintent.putExtra("key", "enablePlay");
            LocalBroadcastManager.getInstance(this).sendBroadcast(Bintent);
            showNotification(context, true, R.drawable.ic_baseline_play_arrow_24);

        });
    }

    public void prev(Context context) {
        int prev = sharedpreferences.getInt("spinner2", 0) - 1;
        if (prev > -1){
            topic=arrayAdapter2.getItem(prev);
            Intent Bintent = new Intent("UI");
            Bintent.putExtra("key", "changeSpinner2");
            LocalBroadcastManager.getInstance(this).sendBroadcast(Bintent);
            play(context);
        }
    }

    public void next(Context context){
        int next = sharedpreferences.getInt("spinner2", 0) + 1;
        if (next < arrayAdapter2.getCount()){
            topic=arrayAdapter2.getItem(next);
            Intent Bintent = new Intent("UI");
            Bintent.putExtra("key", "changeSpinner2");
            LocalBroadcastManager.getInstance(context).sendBroadcast(Bintent);
            play(context);
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public void showNotification(Context context, boolean showButtons, int playPause) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (showButtons) {
            Intent playI,prevI,nextI;
            PendingIntent playPI,prevPI,nextPI;
            playI = new Intent(context, playService.class).setAction("playPause");
            prevI = new Intent(context, playService.class).setAction("prev");
            nextI = new Intent(context, playService.class).setAction("next");

            playPI = PendingIntent.getService(context, 2, playI, PendingIntent.FLAG_UPDATE_CURRENT);
            prevPI = PendingIntent.getService(context, 3, prevI, PendingIntent.FLAG_UPDATE_CURRENT);
            nextPI = PendingIntent.getService(context, 4, nextI, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(context, "1")
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_foreground))
                    .setContentTitle(subject)
                    .setContentText(text)
                    .setContentIntent(contentIntent)
                    .addAction(R.drawable.ic_baseline_skip_previous_24, "prev", prevPI)
                    .addAction(playPause, "play", playPI)
                    .addAction(R.drawable.ic_baseline_skip_next_24, "next", nextPI)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOnlyAlertOnce(true)
                    .build();

            notificationManager.notify(1, notification);

        } else {

            Notification notification = new NotificationCompat.Builder(context, "1")
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setContentTitle(subject)
                    .setContentText(text)
                    .setContentIntent(contentIntent)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOnlyAlertOnce(true)
                    .build();

            notificationManager.notify(1, notification);
        }

    }

    public static playService getInstance() {
        return playServiceInstance;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if(mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }else {
            notificationManager.cancel(1);
        }
        stopSelf();

    }

}
