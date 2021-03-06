package co.com.exile.exile.chat;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.os.BuildCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;


public class GifEditText extends android.support.v7.widget.AppCompatEditText {

    private OnGifIsSelected onGifIsSelected;

    public GifEditText(Context context) {
        super(context);
    }

    public GifEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GifEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnGifIsSelected(OnGifIsSelected onGifIsSelected) {
        this.onGifIsSelected = onGifIsSelected;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        final InputConnection ic = super.onCreateInputConnection(editorInfo);
        EditorInfoCompat.setContentMimeTypes(editorInfo, new String[]{"image/gif"});

        final InputConnectionCompat.OnCommitContentListener callback =
                new InputConnectionCompat.OnCommitContentListener() {
                    @Override
                    public boolean onCommitContent(InputContentInfoCompat inputContentInfo,
                                                   int flags, Bundle opts) {
                        // read and display inputContentInfo asynchronously
                        if (BuildCompat.isAtLeastNMR1() && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                            try {
                                inputContentInfo.requestPermission();
                            } catch (Exception e) {
                                return false; // return false if failed
                            }
                        }

                        // read and display inputContentInfo asynchronously.
                        // call inputContentInfo.releasePermission() as needed.
                        Log.i("uri", inputContentInfo.getContentUri().getQueryParameter("fileName"));

                        if (onGifIsSelected != null) {
                            onGifIsSelected.onGifIsSelected(inputContentInfo.getContentUri());
                        }
                        inputContentInfo.releasePermission();
                        return true;  // return true if succeeded
                    }
                };
        return InputConnectionCompat.createWrapper(ic, editorInfo, callback);
    }

    interface OnGifIsSelected {
        void onGifIsSelected(Uri contentUri);
    }
}
