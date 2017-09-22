package co.com.exile.exile.profile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import co.com.exile.exile.LoginActivity;
import co.com.exile.exile.R;
import co.com.exile.exile.network.VolleySingleton;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void logOut(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Esta seguro que quiere cerrar la sesion")
                .setCancelable(false)
                .setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton("Cerrar sesión", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        logOut();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void changePassword(View view) {
        LayoutInflater inflater = getLayoutInflater();
        final View v = inflater.inflate(R.layout.reset_password, null);
        final AlertDialog dialog = new AlertDialog
                .Builder(this)
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

    private void logOut() {
        String serviceUrl = getString(R.string.logout);
        String url = getString(R.string.url, serviceUrl);
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
        VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
        deleteAccount();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void deleteAccount() {
        SharedPreferences sharedPref = getSharedPreferences("LoginPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(getString(R.string.username));
        editor.remove(getString(R.string.password));
        editor.apply();
    }

    private void changePassword(final String actualPassword, final String newPassword, final String confirmPassword, final AlertDialog dialog, final View v) {
        String serviceUrl = getString(R.string.change_password);

        String url = getString(R.string.url, serviceUrl);
        StringRequest loginRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        dialog.dismiss();
                        savePassword(newPassword);
                        Snackbar.make(findViewById(R.id.toolbar), "Contraseña cambiada con exito", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse.statusCode == 400) {
                            showErrors(dialog, v, new String(error.networkResponse.data));
                        } else {
                            Snackbar.make(findViewById(R.id.toolbar), "Error al hacer la consulta", 800).show();
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
        VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
        showLoading(dialog, v);
    }

    private void showLoading(AlertDialog dialog, View v) {
        v.findViewById(R.id.form_container).setVisibility(View.GONE);
        v.findViewById(R.id.loading_container).setVisibility(View.VISIBLE);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
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

    private void savePassword(String password) {
        SharedPreferences sharedPref = getSharedPreferences("LoginPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.password), password);
        editor.apply();
    }
}
