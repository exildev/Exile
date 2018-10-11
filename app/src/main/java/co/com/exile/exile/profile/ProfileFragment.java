package co.com.exile.exile.profile;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.liuguangqiang.ipicker.IPicker;
import com.squareup.picasso.Picasso;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.List;

import co.com.exile.exile.BaseFragment;
import co.com.exile.exile.R;
import co.com.exile.exile.network.VolleySingleton;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends BaseFragment {
    private String avatar_form_url;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        View helpButton = rootView.findViewById(R.id.help_button);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), HelpActivity.class);
                startActivity(intent);
            }
        });

        View accountButton = rootView.findViewById(R.id.account_button);
        accountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AccountActivity.class);
                startActivity(intent);
            }
        });


        View notificationButton = rootView.findViewById(R.id.notification_button);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NotificationActivity.class);
                startActivity(intent);
            }
        });

        loadProfile(rootView);

        changeAvatar(rootView);
        return rootView;
    }

    private void changeAvatar(View rootView) {
        FloatingActionButton picPhoto = rootView.findViewById(R.id.pic_photo);
        final ImageView profile = rootView.findViewById(R.id.profile);

        picPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IPicker.setLimit(1);
                IPicker.open(getContext());
                IPicker.setOnSelectedListener(new IPicker.OnSelectedListener() {
                    @Override
                    public void onSelected(List<String> paths) {
                        if (paths.size() > 0) {
                            profile.setImageURI(Uri.fromFile(new File(paths.get(0))));
                            try {
                                uploadAvatar(paths.get(0));
                            } catch (FileNotFoundException | MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }

    private void uploadAvatar(String path) throws FileNotFoundException, MalformedURLException {

        String url = getUrl(avatar_form_url);
        UploadNotificationConfig notificationConfig = new UploadNotificationConfig()
                .setTitle("Subiendo archivo")
                .setInProgressMessage("Subiendo archivo a [[UPLOAD_RATE]] ([[PROGRESS]])")
                .setErrorMessage("Hubo un error al subir el archivo")
                .setCompletedMessage("Subida completada exitosamente en [[ELAPSED_TIME]]")
                .setAutoClearOnSuccess(true);

        new MultipartUploadRequest(getContext(), url)
                .addFileToUpload(path, "imagen")
                .setNotificationConfig(notificationConfig)
                .setMaxRetries(1)
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
                        if (serverResponse.getHttpCode() == 200 || serverResponse.getHttpCode() == 201) {
                            Log.i("succes", "success");
                        } else {
                            Log.i("succes", "fail");
                        }
                    }

                    @Override
                    public void onCancelled(UploadInfo uploadInfo) {

                    }
                })
                .startUpload();
    }


    private void loadProfile(final View rootView) {
        String serviceUrl = getString(R.string.profile_data);

        String url = getUrl(serviceUrl);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    TextView name = rootView.findViewById(R.id.name);
                    name.setText(response.getString("first_name") + " " + response.getString("last_name"));

                    TextView email = rootView.findViewById(R.id.email);
                    email.setText(response.getString("email"));

                    TextView charge = rootView.findViewById(R.id.charge);
                    charge.setText(response.getString("cargo"));

                    ImageView profile = rootView.findViewById(R.id.profile);
                    String url = getUrl(response.getString("avatar"));
                    Log.e("tales", url);
                    Picasso
                            .with(getContext())
                            .load(url)
                            .into(profile);

                    avatar_form_url = response.getString("url_avatar");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("response", response.toString());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("response", error.toString());
            }
        });
        VolleySingleton.getInstance(this.getContext()).addToRequestQueue(request);
    }

}
