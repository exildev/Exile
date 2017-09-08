package co.com.exile.exile.task;


import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import co.com.exile.exile.R;
import co.com.exile.exile.network.VolleySingleton;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TodayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TodayFragment extends Fragment implements SubTaskListAdapter.onSubTaskCheckedChangeListener, TaskListAdapter.OnRecordVoice, MultimediaListAdapter.onMultimediaClickListener {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;
    TaskListAdapter mAdapter;
    SwipeRefreshLayout mSwipe;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private MediaPlayer mPlayer = null;
    private MediaRecorder mRecorder = null;


    public TodayFragment() {
        // Required empty public constructor
    }

    public static TodayFragment newInstance() {
        return new TodayFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_today, container, false);

        RecyclerView reportList = view.findViewById(R.id.task_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        reportList.setLayoutManager(layoutManager);
        reportList.setHasFixedSize(true);
        mAdapter = new TaskListAdapter()
                .setmCheckedChangeListener(this)
                .setmOnRecordVoice(this)
                .setMultimediaClickListener(this);
        reportList.setAdapter(mAdapter);

        mSwipe = view.findViewById(R.id.swipe);

        mSwipe.setRefreshing(true);
        loadData();

        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File dir = getActivity().getExternalCacheDir();
        assert dir != null;
        mFileName = dir.getAbsolutePath();
        mFileName += "/audiorecord.aac";
        mPlayer = new MediaPlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void loadData() {

        String serviceUrl = getString(R.string.task_url);

        String url = getString(R.string.url, serviceUrl);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("response", response.toString());
                try {
                    JSONArray object_list = response.getJSONArray("object_list");
                    mAdapter.setTasks(object_list);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSwipe.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("response", error.toString());
                mSwipe.setRefreshing(false);
            }
        });
        VolleySingleton.getInstance(this.getContext()).addToRequestQueue(request);
    }

    private void completeSubTask(final JSONObject subTask) throws JSONException {

        final int id = subTask.getInt("id");

        String serviceUrl = getString(R.string.subtask_complete);

        String url = getString(R.string.url, serviceUrl);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mSwipe.setRefreshing(false);
                        try {
                            Log.i("reponse", response.getInt("id") + " " + response.getInt("subtarea_id"));
                            subTask.put("completado", response.getInt("id"));
                            mAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mSwipe.setRefreshing(false);
                        View view = getView();
                        if (view != null) {
                            Snackbar.make(view, "Hubo un error al enviar la solicitud", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }) {
            @Override
            public byte[] getBody() {
                JSONObject body = new JSONObject();
                try {
                    body.put("subtarea", id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return body.toString().getBytes();
            }
        };
        VolleySingleton.getInstance(this.getContext()).addToRequestQueue(request);
        mSwipe.setRefreshing(true);
    }

    private void uncompleteSubTask(final JSONObject subTask) throws JSONException {
        final int id = subTask.getInt("completado");

        String serviceUrl = getString(R.string.subtask_uncomplete, id);

        String url = getString(R.string.url, serviceUrl);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mSwipe.setRefreshing(false);
                        try {
                            subTask.put("completado", JSONObject.NULL);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mSwipe.setRefreshing(false);
                        View view = getView();
                        if (view != null) {
                            Snackbar.make(view, "Hubo un error al enviar la solicitud", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject body = new JSONObject();
                try {
                    body.put("subtarea", id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return body.toString().getBytes();
            }
        };
        VolleySingleton.getInstance(this.getContext()).addToRequestQueue(request);
        mSwipe.setRefreshing(true);
    }

    private void uploadAudioVoice(JSONObject task) throws FileNotFoundException, MalformedURLException, JSONException {

        String serviceUrl = getString(R.string.multimedia_add);
        String url = getString(R.string.url, serviceUrl);
        UploadNotificationConfig notificationConfig = new UploadNotificationConfig()
                .setTitle("Subiendo archivo")
                .setInProgressMessage("Subiendo archivo a [[UPLOAD_RATE]] ([[PROGRESS]])")
                .setErrorMessage("Hubo un error al subir el archivo")
                .setCompletedMessage("Subida completada exitosamente en [[ELAPSED_TIME]]")
                .setAutoClearOnSuccess(true);

        new MultipartUploadRequest(this.getContext(), url)
                .addFileToUpload(mFileName, "archivo")
                .setNotificationConfig(notificationConfig)
                .setMaxRetries(1)
                .addParameter("tarea", task.getInt("id") + "")
                .addParameter("tipo", getString(R.string.param_multimedia_audio))
                .setDelegate(new UploadStatusDelegate() {
                    @Override
                    public void onProgress(UploadInfo uploadInfo) {

                    }

                    @Override
                    public void onError(UploadInfo uploadInfo, Exception exception) {
                        View view = getView();
                        assert view != null;
                        Snackbar.make(view, "Hubo un error al subir el archivo", 800).show();
                        Log.e("send", exception.getMessage());
                    }

                    @Override
                    public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
                        //hideLoading();
                        View view = getView();
                        assert view != null;
                        Snackbar.make(view, "Archivo enviado con exito", 800).show();
                        Log.i("send", serverResponse.getHttpCode() + " " + serverResponse.getBodyAsString());
                    }

                    @Override
                    public void onCancelled(UploadInfo uploadInfo) {
                    }
                })
                .startUpload();
        //TODO agregar a la lista de multimedia los archivos que se esten subiendo y si indicardor
    }

    @Override
    public void onCheckedChanged(JSONObject subTask, boolean b) {
        try {
            if (b) {
                completeSubTask(subTask);
            } else {
                uncompleteSubTask(subTask);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void startPlaying(final JSONObject multimedia, final RecyclerView.Adapter adapter) throws JSONException {
        if (mPlayer != null) {
            stopPlaying();
        }
        try {
            String serviceUrl = multimedia.getString("url");
            String url = getString(R.string.url, serviceUrl);

            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(url);
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    multimedia.remove("isPlaying");
                    Log.i("multimedia", multimedia.toString());
                    adapter.notifyDataSetChanged();
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void startPlaying() {
        if (mPlayer != null) {
            stopPlaying();
        }
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void onRecord(boolean start, JSONObject task) {
        if (start) {
            checkBeforeStart();
        } else {
            stopRecording(task);
        }
    }

    private void stopPlaying() {
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        playSound();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording(JSONObject task) {
        try {
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
                startPlaying();
                uploadAudioVoice(task);
            }
        } catch (RuntimeException ex) {
            Log.e(LOG_TAG, "stop failed");
        } catch (JSONException | FileNotFoundException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void playSound() {
        MediaPlayer player = MediaPlayer.create(this.getContext(), R.raw.audio_record_ready);
        //TODO cambiar el metodo deprecated por el nuevo en api 26
        //player.prepare();
        //player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
        player.start();
        View view = getView();
        assert view != null;
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    private void checkBeforeStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this.getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                startRecording();
            }
        } else {
            startRecording();
        }
    }

    @Override
    public void tryStartRecord() {
        onRecord(true, null);
    }

    @Override
    public void tryStopRecord(JSONObject task) {
        onRecord(false, task);
    }

    @Override
    public void onClick(JSONObject multimedia, MultimediaListAdapter adapter) {
        try {
            switch (multimedia.getInt("tipo")) {
                case 2:
                    startPlaying(multimedia, adapter);
                    multimedia.put("isPlaying", true);
                    adapter.notifyDataSetChanged();
                    break;
                case 1:
                    Log.i("show", multimedia.toString());
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
