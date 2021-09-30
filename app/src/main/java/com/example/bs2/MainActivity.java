package com.example.bs2;

import static com.example.bs2.playService.mediaPlayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    LocalBroadcastManager lbm;
    Spinner spinner,spinner2;
    CheckBox checkBox;
    SeekBar seekBar;
    TextView textView;
    Button button;
    ImageButton imageButton, imageButton2, imageButton3;
    public static StorageReference mStorageRef;
    public static SharedPreferences sharedpreferences;
    public static SharedPreferences.Editor editor;
    public static ArrayAdapter<String> arrayAdapter,arrayAdapter2;
    public static String text = " ", subject = " ", topic = " ", totalDuration = " ", currentDuration = " ", enable = "no";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.spinner);
        spinner2 = findViewById(R.id.spinner2);
        textView = findViewById(R.id.textview);
        imageButton = findViewById(R.id.imageButton);
        imageButton2 = findViewById(R.id.imageButton2);
        imageButton3 = findViewById(R.id.imageButton3);
        button = findViewById(R.id.button);
        seekBar = findViewById(R.id.seekbar);
        checkBox = findViewById(R.id.checkBox);

        disableButtons();
        enable = "yes";

        sharedpreferences = getSharedPreferences(""+R.string.app_name, MODE_PRIVATE);
        editor = sharedpreferences.edit();

        checkBox.setChecked(sharedpreferences.getBoolean("checkBox",true));

        mStorageRef = FirebaseStorage.getInstance().getReference();

        mStorageRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        arrayAdapter = new ArrayAdapter<String>(MainActivity.this,R.layout.support_simple_spinner_dropdown_item,new ArrayList<>());
                        spinner.setAdapter(arrayAdapter);
                        for (StorageReference prefix : listResult.getPrefixes()) arrayAdapter.add(prefix.getName());
                        spinner.setSelection(sharedpreferences.getInt("spinner",0));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        text=e.getMessage();
                    }
                });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                keepInt("spinner",i);
                subject = spinner.getSelectedItem().toString();

                mStorageRef.child(subject).listAll()
                        .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                            @Override
                            public void onSuccess(ListResult listResult) {
                                arrayAdapter2 = new ArrayAdapter<String>(MainActivity.this,R.layout.support_simple_spinner_dropdown_item,new ArrayList<>());
                                spinner2.setAdapter(arrayAdapter2);

                                for (StorageReference item : listResult.getItems()) arrayAdapter2.add(item.getName());

                                spinner2.setSelection(sharedpreferences.getInt("spinner2",0));
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                text=e.getMessage();
                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                keepInt("spinner2",i);
                topic = spinner2.getSelectedItem().toString();
                if(enable.equals("yes"))
                enableButtons();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, playService.class));
        }else {
            startService(new Intent(this, playService.class));
        }

    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String str = intent.getStringExtra("key");
                switch (str){
                    case "playImg":
                        imageButton2.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                        break;

                    case "pauseImg":
                        imageButton2.setImageResource(R.drawable.ic_baseline_pause_24);
                        break;

                    case "disableBtn":
                        disableButtons();
                        break;

                    case "enablePlay":
                        enableButtons();
                        imageButton2.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                        break;

                    case "changeSpinner2":
                        enable = "no";
                        spinner2.setSelection(arrayAdapter2.getPosition(topic));
                        break;

                    case "EnablePauseMax":
                        enableButtons();
                        imageButton2.setImageResource(R.drawable.ic_baseline_pause_24);
                        int total = mediaPlayer.getDuration();
                        seekBar.setMax(total);
                        totalDuration =""+total % 3600000 / 60000+" : "+total % 3600000 % 60000 / 1000;
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + str);
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(receiver, new IntentFilter("UI"));

        if(arrayAdapter2!=null)
            spinner2.setSelection(arrayAdapter2.getPosition(topic));

        if(text.contains("preparing")) disableButtons();

        if(mediaPlayer!=null){
            if(mediaPlayer.getCurrentPosition()>0){
                int total = mediaPlayer.getDuration();
                seekBar.setMax(total);
                totalDuration =""+total % 3600000 / 60000+" : "+total % 3600000 % 60000 / 1000;
            }
            if(mediaPlayer.isPlaying())
            imageButton2.setImageResource(R.drawable.ic_baseline_pause_24);
        }else {
            imageButton2.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }

        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playService.getInstance().playpause(MainActivity.this);
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playService.getInstance().prev(MainActivity.this);
            }
        });

        imageButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playService.getInstance().next(MainActivity.this);
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(mediaPlayer!=null) {
                    int duration = mediaPlayer.getCurrentPosition();
                    currentDuration = ""+duration % 3600000 / 60000+" : "+duration % 3600000 % 60000 / 1000;
                    seekBar.setProgress(duration);
                }else {
                    currentDuration ="0 : 0";
                    totalDuration ="0 : 0";
                    seekBar.setProgress(0);
                }
                textView.setText(currentDuration+" / "+totalDuration+"\n"+text);
                handler.postDelayed(this, 500);
            }
        }, 0);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) if(mediaPlayer!=null) mediaPlayer.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                keepBool("checkBox",b);
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        lbm.unregisterReceiver(receiver);
    }

    private void keepInt(String key, int value){
        editor.putInt(key,value);
        editor.apply();
    }
    private void keepBool(String key, boolean value){
        editor.putBoolean(key,value);
        editor.apply();
    }

    public void enableButtons() {
        imageButton.setEnabled(true);
        imageButton2.setEnabled(true);
        imageButton3.setEnabled(true);
    }

    public void disableButtons() {
        imageButton.setEnabled(false);
        imageButton2.setEnabled(false);
        imageButton3.setEnabled(false);
    }
}




/*

        disableButtons();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        mStorageRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        arrayAdapter = new ArrayAdapter<String>(MainActivity.this,R.layout.support_simple_spinner_dropdown_item,new ArrayList<>());
                        spinner.setAdapter(arrayAdapter);
                        for (StorageReference prefix : listResult.getPrefixes()) arrayAdapter.add(prefix.getName());
                        spinner.setSelection(sharedpreferences.getInt("spinner",0));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        text=e.getMessage();
                    }
                });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                keepInt("spinner",i);
                subject = spinner.getSelectedItem().toString();

                mStorageRef.child(subject).listAll()
                        .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                            @Override
                            public void onSuccess(ListResult listResult) {
                                arrayAdapter2 = new ArrayAdapter<String>(MainActivity.this,R.layout.support_simple_spinner_dropdown_item,new ArrayList<>());
                                spinner2.setAdapter(arrayAdapter2);

                                for (StorageReference item : listResult.getItems()) arrayAdapter2.add(item.getName());

                                spinner2.setSelection(sharedpreferences.getInt("spinner2",0));
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                text=e.getMessage();
                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                keepInt("spinner2",i);
                topic = spinner2.getSelectedItem().toString();

                if (playDirectly.equals("yes")) {
                    play(MainActivity.this);
                    playDirectly="no";
                } else if(mediaPlayer!=null && mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    imageButton2.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                }
                else if (!text.contains("preparing")) {
                    enableButtons();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, playService.class));
        }else {
            startService(new Intent(this, playService.class));
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(mediaPlayer!=null) {
                    int duration = mediaPlayer.getCurrentPosition();
                    currentDuration = ""+duration % 3600000 / 60000+" : "+duration % 3600000 % 60000 / 1000;
                    if(mediaPlayer.isPlaying()) seekBar.setProgress(duration);
                }else {
                    currentDuration ="0 : 0";
                    totalDuration ="0 : 0";
                    seekBar.setProgress(0);
                }
                textView.setText(currentDuration+" / "+totalDuration+"\n"+text);
                handler.postDelayed(this, 500);
            }
        }, 0);

        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(changeImage) {
                    imageButton2.setImageResource(Image);
                    changeImage=false;
                }if(enableButtons) {
                    enableButtons();
                    enableButtons=false;
                }if(disableButtons) {
                    disableButtons();
                    disableButtons=false;
                }if(seekBarMax) {
                    seekBar.setMax(mediaPlayer.getDuration());
                    seekBarMax=false;
                }if(spinnerItem) {
                    spinner2.setSelection(spinnerItemPosition);
                    spinnerItem=false;
                }

                handler2.postDelayed(this, 500);
            }
        }, 0);

        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playpause(MainActivity.this);
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prev(MainActivity.this,"yes");
            }
        });

        imageButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next(MainActivity.this,"yes");
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) if(mediaPlayer!=null) mediaPlayer.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }


    private void keepInt(String key, int value){
        editor.putInt(key,value);
        editor.apply();
    }

    public void enableButtons() {
        imageButton.setEnabled(true);
        imageButton2.setEnabled(true);
        imageButton3.setEnabled(true);
    }

    public void disableButtons() {
        imageButton.setEnabled(false);
        imageButton2.setEnabled(false);
        imageButton3.setEnabled(false);
    }

    void playpause(Context context){
        if (mediaPlayer != null && subject.equals(playingSubject) && topic.equals(playingTopic)) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                changeImage=true;
                Image=R.drawable.ic_baseline_play_arrow_24;
                showNotification(context,true, R.drawable.ic_baseline_play_arrow_24);

            } else {
                mediaPlayer.start();
                changeImage=true;
                Image=R.drawable.ic_baseline_pause_24;
                showNotification(context, true, R.drawable.ic_baseline_pause_24);
            }

        } else play(context);
    }

    void play(Context context) {
        changeImage=true;
        Image=R.drawable.ic_baseline_play_arrow_24;
        disableButtons=true;
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
                        enableButtons=true;
                        changeImage=true;
                        Image=R.drawable.ic_baseline_pause_24;
                        playingSubject = subject;
                        playingTopic = topic;
                        int total = mediaPlayer.getDuration();
                        seekBarMax=true;
                        totalDuration =""+total % 3600000 / 60000+" : "+total % 3600000 % 60000 / 1000;
                        text=topic;
                        showNotification(context, true, R.drawable.ic_baseline_pause_24);

                });
                mediaPlayer.setOnCompletionListener(mediaPlayer -> {
                    changeImage=true;
                    Image=R.drawable.ic_baseline_play_arrow_24;
                    showNotification(context, true, R.drawable.ic_baseline_play_arrow_24);

                });
                mediaPlayer.setOnErrorListener((mediaPlayer, i, i1) -> {
                        enableButtons=true;
                        changeImage=true;
                        Image=R.drawable.ic_baseline_play_arrow_24;
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
                enableButtons=true;
                changeImage=true;
                Image=R.drawable.ic_baseline_play_arrow_24;
                showNotification(context, true, R.drawable.ic_baseline_play_arrow_24);

        });
    }

    void prev(Context context, String value) {
        int prev = sharedpreferences.getInt("spinner2", 0) - 1;
        if (prev > -1){
            spinnerItem=true;
            spinnerItemPosition=prev;
            playDirectly=value;
            if(value.equals("no")){
                topic=arrayAdapter2.getItem(prev);
                play(context);
            }
        }
    }

    void next(Context context, String value) {
        int next = sharedpreferences.getInt("spinner2", 0) + 1;
        if (next < arrayAdapter2.getCount()){
            spinnerItem=true;
            spinnerItemPosition=next;
            playDirectly=value;
            if(value.equals("no")){
                topic=arrayAdapter2.getItem(next);
                play(context);
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    void showNotification(Context context, boolean showButtons, int playPause) {
        notificationIntent = new Intent(context, MainActivity.class);
        contentIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (showButtons) {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if(mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
*/
