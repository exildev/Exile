package co.com.exile.exile.chat;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.liuguangqiang.ipicker.IPicker;

import co.com.exile.exile.R;
import pl.droidsonroids.gif.GifImageView;

public class ChatActivity extends AppCompatActivity implements GifEditText.OnGifIsSelected {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

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
}
