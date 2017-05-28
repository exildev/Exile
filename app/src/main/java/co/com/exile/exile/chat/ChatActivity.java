package co.com.exile.exile.chat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.liuguangqiang.ipicker.IPicker;

import java.io.File;
import java.io.IOException;

import co.com.exile.exile.R;
import pl.droidsonroids.gif.GifImageView;

public class ChatActivity extends AppCompatActivity implements GifEditText.OnGifIsSelected {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String LOG_TAG = "Record";
    private static String mFileName = null;

    // Requesting permission to RECORD_AUDIO
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        checkBeforeStart();
                        break;
                    case MotionEvent.ACTION_UP:
                        stopRecording();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        stopRecording();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        final ImageView takePicture = (ImageView) findViewById(R.id.take_picture);


        GifEditText editText = (GifEditText) findViewById(R.id.message_input);
        editText.setOnGifIsSelected(this);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    fab.setImageResource(R.drawable.ic_send_24dp);
                    takePicture.setVisibility(View.GONE);
                } else {
                    fab.setImageResource(R.drawable.ic_mic_24dp);
                    takePicture.setVisibility(View.VISIBLE);
                }
            }
        });

        File cacheDir = getExternalCacheDir();
        mFileName = cacheDir != null ? cacheDir.getAbsolutePath() : "";
        mFileName += "/audiorecordtest.3gp";
    }

    public void back(View view) {
        finish();
    }

    public void openPicker(View view) {
        IPicker.setLimit(1);
        IPicker.open(this);
    }

    @Override
    public void onGifIsSelected(Uri contentUri) {
        LinearLayout container = (LinearLayout) findViewById(R.id.messages_container);

        LayoutInflater inflater = LayoutInflater.from(this);
        View message = inflater.inflate(R.layout.chat_message_me_gif, container, false);

        final GifImageView gif = (GifImageView) ((ViewGroup) ((ViewGroup) message).getChildAt(0)).getChildAt(0);

        gif.setImageURI(contentUri);

        container.addView(message);

        ((NestedScrollView) container.getParent()).fullScroll(View.FOCUS_DOWN);
    }


    private void checkBeforeStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                startRecording();
            }
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        playSound();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        startPlaying();
    }


    private void startPlaying() {
        mPlayer = new MediaPlayer();

        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void playSound() {
        MediaPlayer player = MediaPlayer.create(this, R.raw.audio_record_ready);
        player.start();
    }
}
