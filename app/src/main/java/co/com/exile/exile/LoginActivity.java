package co.com.exile.exile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import co.com.exile.exile.network.VolleySingleton;

public class LoginActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        autoLogin();
    }

    private void autoLogin() {
        SharedPreferences sharedPref = getSharedPreferences("LoginPref", Context.MODE_PRIVATE);
        String username = sharedPref.getString(getString(R.string.username), null);
        String password = sharedPref.getString(getString(R.string.password), null);

        if (username != null && password != null) {
            login(username, password);
        }
    }

    public void login(View view) {
        TextInputEditText username = findViewById(R.id.username);
        TextInputEditText password = findViewById(R.id.password);
        login(username.getText().toString(), password.getText().toString());
    }

    public void login(final String username, final String password) {
        String serviceUrl = getString(R.string.login);

        String url = getUrl(serviceUrl);
        StringRequest loginRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        saveAccount(username, password);
                        initMain();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                        if (error.networkResponse != null && (error.networkResponse.statusCode == 404 || error.networkResponse.statusCode == 400)) {
                            Snackbar.make(findViewById(R.id.main_container), "usuario y/o contraseña incorrecta", 800).show();
                        } else {
                            Snackbar.make(findViewById(R.id.main_container), "Error al hacer la consulta", 800).show();
                        }
                    }
                }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject body = new JSONObject();
                try {
                    body.put("username", username);
                    body.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return body.toString().getBytes();
            }
        };
        loginRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
        showLoading();
    }

    private void showLoading() {
        findViewById(R.id.login_ly).setVisibility(View.INVISIBLE);
        (findViewById(R.id.loading)).setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        findViewById(R.id.login_ly).setVisibility(View.VISIBLE);
        (findViewById(R.id.loading)).setVisibility(View.GONE);
    }

    private void initMain() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void saveAccount(String username, String password) {
        SharedPreferences sharedPref = getSharedPreferences("LoginPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.username), username);
        editor.putString(getString(R.string.password), password);
        editor.apply();
    }
}
