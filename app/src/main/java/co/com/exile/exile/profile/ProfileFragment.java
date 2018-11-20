package co.com.exile.exile.profile;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.liuguangqiang.ipicker.IPicker;
import com.squareup.picasso.Picasso;

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
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.com.exile.exile.BaseFragment;
import co.com.exile.exile.LoginActivity;
import co.com.exile.exile.R;
import co.com.exile.exile.network.VolleySingleton;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends BaseFragment {
    private String avatar_form_url;
    private int userId;
    private String username;
    private String name;
    private String lastName;
    private String email;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        View editButton = rootView.findViewById(R.id.edit_profile);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfile();
            }
        });

        View changePasswordButton = rootView.findViewById(R.id.change_password);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });


        View signOutButton = rootView.findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });

        loadProfile(rootView);

        changeAvatar(rootView);
        return rootView;
    }

    private void logOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setMessage("Esta seguro que quiere cerrar la sesion")
                .setCancelable(false)
                .setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton("Cerrar sesión", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sendLogOut();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void sendLogOut() {
        String serviceUrl = getString(R.string.logout);
        String url = getUrl(serviceUrl);
        StringRequest loginRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        VolleySingleton.getInstance(this.getContext()).addToRequestQueue(loginRequest);
        deleteAccount();
        Intent intent = new Intent(this.getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void deleteAccount() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("LoginPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(getString(R.string.username));
        editor.remove(getString(R.string.password));
        editor.apply();
    }

    private void editProfile() {
        LayoutInflater inflater = getLayoutInflater();
        final View v = inflater.inflate(R.layout.edit_profile, null);
        TextInputEditText name = v.findViewById(R.id.name);
        name.setText(this.name);
        TextInputEditText lastName = v.findViewById(R.id.last_name);
        lastName.setText(this.lastName);
        TextInputEditText email = v.findViewById(R.id.email);
        email.setText(this.email);
        final AlertDialog dialog = new AlertDialog
                .Builder(this.getContext())
                .setView(v)
                .setTitle("Editar perfil")
                .setCancelable(false)
                .setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextInputLayout nameContainer = v.findViewById(R.id.name_container);
                        nameContainer.setError(null);
                        TextInputLayout lastNameContainer = v.findViewById(R.id.last_name_container);
                        lastNameContainer.setError(null);
                        TextInputLayout emailContainer = v.findViewById(R.id.email_container);
                        emailContainer.setError(null);

                        TextInputEditText name = v.findViewById(R.id.name);
                        TextInputEditText lastName = v.findViewById(R.id.last_name);
                        TextInputEditText email = v.findViewById(R.id.email);
                        editProfile(name.getText().toString(), lastName.getText().toString(), email.getText().toString(), dialog, v);
                    }
                });
            }
        });
        dialog.show();
    }

    private void editProfile(final String name, final String lastName, final String email, final AlertDialog dialog, final View v) {
        String serviceUrl = getString(R.string.edit_profile, userId);

        String url = getUrl(serviceUrl);
        StringRequest loginRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        dialog.dismiss();
                        loadProfile(getView());
                        Snackbar.make(getView(), "Perfil actualizado con exito", Snackbar.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse.statusCode == 400) {
                            showProfileErrors(dialog, v, new String(error.networkResponse.data));
                        } else {
                            Snackbar.make(getView(), "Error al hacer la consulta", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("first_name", name);
                params.put("last_name", lastName);
                params.put("email", email);
                return params;
            }
        };
        VolleySingleton.getInstance(this.getContext()).addToRequestQueue(loginRequest);
        showLoading(dialog, v);
    }

    public void changePassword() {
        LayoutInflater inflater = getLayoutInflater();
        final View v = inflater.inflate(R.layout.reset_password, null);
        final AlertDialog dialog = new AlertDialog
                .Builder(this.getContext())
                .setView(v)
                .setTitle("Cambiar contraseña")
                .setCancelable(false)
                .setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextInputLayout actualPasswordContainer = v.findViewById(R.id.actual_password_container);
                        actualPasswordContainer.setError(null);
                        TextInputLayout newPasswordContainer = v.findViewById(R.id.new_password_container);
                        newPasswordContainer.setError(null);
                        TextInputLayout passwordConfirmContainer = v.findViewById(R.id.password_confirm_container);
                        passwordConfirmContainer.setError(null);

                        TextInputEditText actualPassword = v.findViewById(R.id.actual_password);
                        TextInputEditText newPassword = v.findViewById(R.id.new_password);
                        TextInputEditText confirmPassword = v.findViewById(R.id.password_confirm);
                        changePassword(actualPassword.getText().toString(), newPassword.getText().toString(), confirmPassword.getText().toString(), dialog, v);
                    }
                });
            }
        });
        dialog.show();
    }

    private void changePassword(final String actualPassword, final String newPassword, final String confirmPassword, final AlertDialog dialog, final View v) {
        String serviceUrl = getString(R.string.change_password);

        String url = getUrl(serviceUrl);
        StringRequest loginRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        dialog.dismiss();
                        savePassword(newPassword);
                        Snackbar.make(getView(), "Contraseña cambiada con exito", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse.statusCode == 400) {
                            showErrors(dialog, v, new String(error.networkResponse.data));
                        } else {
                            Snackbar.make(getView(), "Error al hacer la consulta", 800).show();
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("old_password", actualPassword);
                params.put("new_password1", newPassword);
                params.put("new_password2", confirmPassword);
                return params;
            }
        };
        VolleySingleton.getInstance(this.getContext()).addToRequestQueue(loginRequest);
        showLoading(dialog, v);
    }

    private void savePassword(String password) {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("LoginPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.password), password);
        editor.apply();
    }

    private void showProfileErrors(AlertDialog dialog, View v, String response) {
        try {
            JSONObject errors = new JSONObject(response);
            if (errors.has("first_name")) {
                JSONArray messages = errors.getJSONArray("first_name");
                TextInputLayout container = v.findViewById(R.id.name_container);
                container.setError(messages.getString(0));
            }

            if (errors.has("last_name")) {
                JSONArray messages = errors.getJSONArray("last_name");
                TextInputLayout container = v.findViewById(R.id.last_name_container);
                container.setError(messages.getString(0));
            }

            if (errors.has("email")) {
                JSONArray messages = errors.getJSONArray("email");
                TextInputLayout container = v.findViewById(R.id.email_container);
                container.setError(messages.getString(0));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        v.findViewById(R.id.form_container).setVisibility(View.VISIBLE);
        v.findViewById(R.id.loading_container).setVisibility(View.GONE);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
    }

    private void showErrors(AlertDialog dialog, View v, String response) {
        try {
            JSONArray errors = new JSONArray(response);
            for (int i = 0; i < errors.length(); i++) {
                JSONArray error = errors.getJSONArray(i);
                JSONArray messages = error.getJSONArray(1);
                if (error.getString(0).equals("old_password") && messages.length() > 0) {
                    TextInputLayout actualPassword = v.findViewById(R.id.actual_password_container);
                    actualPassword.setError(messages.getString(0));
                } else if (error.getString(0).equals("new_password1") && messages.length() > 0) {
                    TextInputLayout newPassword = v.findViewById(R.id.new_password_container);
                    newPassword.setError(messages.getString(0));
                } else if (error.getString(0).equals("new_password2") && messages.length() > 0) {
                    TextInputLayout confirmPassword = v.findViewById(R.id.password_confirm_container);
                    confirmPassword.setError(messages.getString(0));
                } else if (error.getString(0).equals("first_name") && messages.length() > 0) {
                    TextInputLayout confirmPassword = v.findViewById(R.id.password_confirm_container);
                    confirmPassword.setError(messages.getString(0));
                }
                Log.i("error", error.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        v.findViewById(R.id.form_container).setVisibility(View.VISIBLE);
        v.findViewById(R.id.loading_container).setVisibility(View.GONE);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
    }

    private void showLoading(AlertDialog dialog, View v) {
        v.findViewById(R.id.form_container).setVisibility(View.GONE);
        v.findViewById(R.id.loading_container).setVisibility(View.VISIBLE);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
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
                    name = response.getString("first_name");
                    lastName = response.getString("last_name");
                    TextView name = rootView.findViewById(R.id.name);
                    name.setText(ProfileFragment.this.name + " " + lastName);

                    email = response.getString("email");
                    TextView email = rootView.findViewById(R.id.email);
                    email.setText(ProfileFragment.this.email);

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
                    userId = response.getInt("id");
                    username = response.getString("username");
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
