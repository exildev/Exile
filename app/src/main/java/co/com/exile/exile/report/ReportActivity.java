package co.com.exile.exile.report;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.liuguangqiang.ipicker.IPicker;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import co.com.exile.exile.R;
import co.com.exile.exile.network.VolleySingleton;
import moe.feng.common.stepperview.VerticalStepperItemView;

public class ReportActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, LocationListener {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 3;
    private static final int REQUEST_LOCATION_SETTINGS = 12;
    private static final int MAX_UPLOAD_FILES = 5;
    String[] typesString;
    JSONArray mTypes;
    String[] placesString;
    JSONArray mPlaces;
    String[] clientsString;
    JSONArray mClients;
    ArrayList<String> attaches;
    private VerticalStepperItemView mSteppers[] = new VerticalStepperItemView[5];
    private GoogleApiClient mGoogleClient;
    private LocationRequest mLocationRequest;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        attaches = new ArrayList<>();
        IPicker.setLimit(MAX_UPLOAD_FILES);
        IPicker.setOnSelectedListener(new IPicker.OnSelectedListener() {
            @Override
            public void onSelected(List<String> paths) {
                addAttaches(paths);
            }
        });
        getTypes();
        getPlaces();
        getClients();

        mGoogleClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleClient.connect();

        setStepper();
    }

    void setStepper() {

        //TODO Que los steps 1 y 5 se les pueda dar click y devolverse a esa opcion

        mSteppers[0] = findViewById(R.id.stepper_0);
        mSteppers[1] = findViewById(R.id.stepper_1);
        mSteppers[2] = findViewById(R.id.stepper_2);
        mSteppers[3] = findViewById(R.id.stepper_3);
        mSteppers[4] = findViewById(R.id.stepper_4);

        VerticalStepperItemView.bindSteppers(mSteppers);

        Button mNextBtn0 = findViewById(R.id.button_next_0);
        mNextBtn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextInputEditText nombre = findViewById(R.id.name_et);
                TextInputLayout layout = findViewById(R.id.name_il);
                if (!nombre.getText().toString().equals("")) {
                    mSteppers[0].nextStep();
                    mSteppers[0].setSummary(nombre.getText().toString());
                    layout.setError(null);
                } else {
                    layout.setError("Este campo es obligatorio");
                }
            }
        });

        Button mPrevBtn1 = findViewById(R.id.button_prev_1);
        mPrevBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSteppers[1].prevStep();
            }
        });

        Button mNextBtn1 = findViewById(R.id.button_next_1);
        mNextBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner spinner = findViewById(R.id.type_spinner);
                if (spinner.getSelectedItemPosition() > 0) {
                    mSteppers[1].setSummary(spinner.getSelectedItem().toString());
                } else {
                    mSteppers[1].setSummary(null);
                }
                mSteppers[1].nextStep();
            }
        });

        Button mPrevBtn2 = findViewById(R.id.button_prev_2);
        mPrevBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSteppers[2].prevStep();
            }
        });

        Button mNextBtn2 = findViewById(R.id.button_next_2);
        mNextBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner spinner = findViewById(R.id.place_spinner);
                if (spinner.getSelectedItemPosition() > 0) {
                    mSteppers[2].setSummary(spinner.getSelectedItem().toString());
                } else {
                    mSteppers[2].setSummary(null);
                }
                mSteppers[2].nextStep();
            }
        });

        Button mPrevBtn3 = findViewById(R.id.button_prev_3);
        mPrevBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSteppers[3].prevStep();
            }
        });

        Button mNextBtn3 = findViewById(R.id.button_next_3);
        mNextBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner spinner = findViewById(R.id.client_spinner);
                if (spinner.getSelectedItemPosition() > 0) {
                    mSteppers[3].setSummary(spinner.getSelectedItem().toString());
                } else {
                    mSteppers[3].setSummary(null);
                }
                mSteppers[3].nextStep();
            }
        });

        Button mPrevBtn4 = findViewById(R.id.button_prev_4);
        mPrevBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSteppers[4].prevStep();
            }
        });

        Button mNextBtn4 = findViewById(R.id.button_next_4);
        mNextBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextInputEditText desc = findViewById(R.id.description_et);
                TextInputLayout layout = findViewById(R.id.description_il);
                if (!desc.getText().toString().equals("")) {
                    mSteppers[4].setSummary(desc.getText().toString());
                    layout.setError(null);
                    mSteppers[4].setState(VerticalStepperItemView.STATE_DONE);
                    try {
                        if (attaches.size() > 0) {
                            sendWithFiles();
                        } else {
                            send();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    layout.setError("Este campo es obligatorio");
                }
                mSteppers[4].nextStep();
            }
        });
    }

    @Override
    public void onDestroy() {
        if (mGoogleClient.isConnected()) {
            stopLocationUpdates();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.report_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nav_attach) {
            IPicker.open(this, attaches);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION || requestCode == PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                validPermissions();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.gps_permissions_message)
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                validPermissions();
                            }
                        })
                        .setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                startLocationUpdates();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.activate_gps_message)
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                createLocationRequest();
                            }
                        })
                        .setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getTypes() {
        String serviceUrl = getString(R.string.report_type_url);
        String url = getString(R.string.url, serviceUrl);
        getTypes(url, 0);
    }

    private void getTypes(final String url, final int offset) {
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("response", response.toString());
                        try {
                            int count = response.getInt("count");
                            if (typesString == null) {
                                typesString = new String[count];
                                mTypes = new JSONArray();
                            }

                            JSONArray types = response.getJSONArray("object_list");
                            for (int i = 0; i < types.length(); i++) {
                                JSONObject type = types.getJSONObject(i);
                                typesString[i + offset] = type.getString("nombre");
                                mTypes.put(type);
                            }

                            if (response.has("next")) {
                                String serviceUrl = getString(R.string.report_type_url);
                                String url = getString(R.string.url, serviceUrl) + "?page=" + response.getInt("next");
                                getTypes(url, offset + response.getInt("num_rows"));
                            } else {
                                renderTypes();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && (error.networkResponse.statusCode == 404 || error.networkResponse.statusCode == 400)) {
                            Snackbar.make(findViewById(R.id.toolbar), "usuario y/o contraseña incorrecta", 800).show();
                        } else {
                            Snackbar.make(findViewById(R.id.toolbar), "Error al hacer la consulta", 800).show();
                        }
                    }
                });
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void getPlaces() {
        String serviceUrl = getString(R.string.report_places_url);
        String url = getString(R.string.url, serviceUrl);
        getPlaces(url, 0);

    }

    private void getPlaces(final String url, final int offset) {
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("response", response.toString());
                        try {
                            int count = response.getInt("count");
                            if (placesString == null) {
                                placesString = new String[count];
                                mPlaces = new JSONArray();
                            }

                            JSONArray types = response.getJSONArray("object_list");
                            for (int i = 0; i < types.length(); i++) {
                                JSONObject type = types.getJSONObject(i);
                                placesString[i + offset] = type.getString("nombre");
                                mPlaces.put(type);
                            }

                            if (response.has("next")) {
                                String serviceUrl = getString(R.string.report_places_url);
                                String url = getString(R.string.url, serviceUrl) + "?page=" + response.getInt("next");
                                getPlaces(url, offset + response.getInt("num_rows"));
                            } else {
                                renderPlaces();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && (error.networkResponse.statusCode == 404 || error.networkResponse.statusCode == 400)) {
                            Snackbar.make(findViewById(R.id.toolbar), "usuario y/o contraseña incorrecta", 800).show();
                        } else {
                            Snackbar.make(findViewById(R.id.toolbar), "Error al hacer la consulta", 800).show();
                        }
                    }
                });
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void getClients() {
        String serviceUrl = getString(R.string.report_clients_url);
        String url = getString(R.string.url, serviceUrl);
        getClients(url, 0);

    }

    private void getClients(final String url, final int offset) {
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("response", response.toString());
                        try {
                            int count = response.getInt("count");
                            if (clientsString == null) {
                                clientsString = new String[count];
                                mClients = new JSONArray();
                            }

                            JSONArray clients = response.getJSONArray("object_list");
                            for (int i = 0; i < clients.length(); i++) {
                                JSONObject client = clients.getJSONObject(i);
                                clientsString[i + offset] = client.getString("nombre");
                                mClients.put(client);
                            }

                            if (response.has("next")) {
                                String serviceUrl = getString(R.string.report_clients_url);
                                String url = getString(R.string.url, serviceUrl) + "?page=" + response.getInt("next");
                                getClients(url, offset + response.getInt("num_rows"));
                            } else {
                                renderClients();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && (error.networkResponse.statusCode == 404 || error.networkResponse.statusCode == 400)) {
                            Snackbar.make(findViewById(R.id.toolbar), "usuario y/o contraseña incorrecta", 800).show();
                        } else {
                            Snackbar.make(findViewById(R.id.toolbar), "Error al hacer la consulta", 800).show();
                        }
                    }
                });
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void renderTypes() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typesString);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        Spinner spinner = findViewById(R.id.type_spinner);
        spinner.setAdapter(adapter);
    }

    private void renderPlaces() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, placesString);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        final Spinner spinner = findViewById(R.id.place_spinner);
        spinner.setAdapter(adapter);
    }

    private void renderClients() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientsString);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        final Spinner spinner = findViewById(R.id.client_spinner);
        spinner.setAdapter(adapter);
    }

    private void addAttaches(List<String> paths) {
        attaches.clear();
        attaches.addAll(paths);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleClient,
                        builder.build());


        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        Log.i("settings", "si tal");
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    ReportActivity.this,
                                    REQUEST_LOCATION_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Log.i("settings", "no tal");
                        break;
                }
            }
        });
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            validPermissions();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleClient, this);
    }

    private void validPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            } else {
                createLocationRequest();
            }
        } else {
            createLocationRequest();
        }
    }

    private void send() {
        final EditText name = findViewById(R.id.name_et);
        final Spinner type = findViewById(R.id.type_spinner);
        final Spinner place = findViewById(R.id.place_spinner);
        final Spinner client = findViewById(R.id.client_spinner);
        final EditText description = findViewById(R.id.description_et);

        Log.i("Location", mLocation + "");

        stopLocationUpdates();

        String serviceUrl = getString(R.string.report_url);
        String url = getString(R.string.url, serviceUrl);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideLoading();
                        Log.i("reponse", response);
                        startLocationUpdates();
                        Snackbar.make(findViewById(R.id.name_et), "Reporte enviado con exito", 800).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        startLocationUpdates();
                        Snackbar.make(findViewById(R.id.name_et), "Hubo un error al subir el reporte", 800).show();
                        String str = new String(error.networkResponse.data);
                        Log.e("sendWithFiles", str);
                    }
                }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                int type_id = 0;
                int client_id = 0;
                int place_id = 0;
                try {
                    type_id = mTypes.getJSONObject(type.getSelectedItemPosition() - 1).getInt("id");
                    client_id = mClients.getJSONObject(client.getSelectedItemPosition() - 1).getInt("id");
                    place_id = mPlaces.getJSONObject(place.getSelectedItemPosition() - 1).getInt("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject body = new JSONObject();
                try {
                    body.put("nombre", name.getText().toString())
                            .put("descripcion", description.getText().toString())
                            .put("fotoreporte_set-TOTAL_FORMS", "0")
                            .put("fotoreporte_set-INITIAL_FORMS", "0")
                            .put("fotoreporte_set-MIN_NUM_FORMS", "0")
                            .put("fotoreporte_set-MAX_NUM_FORMS", "5");

                    if (type_id != 0) {
                        body.put("tipo", type_id + "");
                    }
                    if (client_id != 0) {
                        body.put("cliente", client_id + "");
                    }
                    if (place_id != 0) {
                        body.put("lugar", place_id + "");
                    }
                    if (mLocation != null) {
                        body.put("latitud", mLocation.getLatitude() + "")
                                .put("longitud", mLocation.getLongitude() + "");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return body.toString().getBytes();
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
        showLoading();
    }

    private void hideLoading() {
        findViewById(R.id.loading).setVisibility(View.GONE);
    }

    private void showLoading() {
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
    }

    private void sendWithFiles() throws FileNotFoundException {
        EditText name = findViewById(R.id.name_et);
        Spinner type = findViewById(R.id.type_spinner);
        Spinner place = findViewById(R.id.place_spinner);
        Spinner client = findViewById(R.id.client_spinner);
        EditText description = findViewById(R.id.description_et);

        int type_id = 0;
        int client_id = 0;
        int place_id = 0;
        try {
            type_id = mTypes.getJSONObject(type.getSelectedItemPosition()).getInt("id");
            client_id = mClients.getJSONObject(client.getSelectedItemPosition()).getInt("id");
            place_id = mPlaces.getJSONObject(place.getSelectedItemPosition()).getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        stopLocationUpdates();
        showLoading();
        UploadNotificationConfig notificationConfig = new UploadNotificationConfig()
                .setTitle("Subiendo reporte")
                .setInProgressMessage("Subiendo reporte a [[UPLOAD_RATE]] ([[PROGRESS]])")
                .setErrorMessage("Hubo un error al subir el reporte")
                .setCompletedMessage("Subida completada exitosamente en [[ELAPSED_TIME]]")
                .setAutoClearOnSuccess(true);


        String serviceUrl = getString(R.string.report_url);
        String url = getString(R.string.url, serviceUrl);
        MultipartUploadRequest upload =
                new MultipartUploadRequest(getBaseContext(), url)
                        .setNotificationConfig(notificationConfig)
                        .setAutoDeleteFilesAfterSuccessfulUpload(false)
                        .setMaxRetries(1)
                        .addParameter("nombre", name.getText().toString())
                        .addParameter("descripcion", description.getText().toString())
                        .addParameter("fotoreporte_set-TOTAL_FORMS", String.valueOf(attaches.size()))
                        .addParameter("fotoreporte_set-INITIAL_FORMS", "0")
                        .addParameter("fotoreporte_set-MIN_NUM_FORMS", "0")
                        .addParameter("fotoreporte_set-MAX_NUM_FORMS", "5");
        if (type_id != 0) {
            upload.addParameter("tipo", type_id + "");
        }
        if (client_id != 0) {
            upload.addParameter("cliente", client_id + "");
        }
        if (place_id != 0) {
            upload.addParameter("lugar", place_id + "");
        }
        if (mLocation != null) {
            upload.addParameter("latitud", mLocation.getLatitude() + "")
                    .addParameter("longitud", mLocation.getLongitude() + "");
        }

        for (int i = 0; i < attaches.size(); i++) {
            String image = attaches.get(i);
            upload.addFileToUpload(image, "fotoreporte_set-" + i + "-reporte");
        }
        try {
            upload.setDelegate(new UploadStatusDelegate() {
                @Override
                public void onProgress(UploadInfo uploadInfo) {

                }

                @Override
                public void onError(UploadInfo uploadInfo, Exception exception) {
                    hideLoading();
                    startLocationUpdates();
                    Snackbar.make(findViewById(R.id.name_et), "Hubo un error al subir el reporte", 800).show();
                    Log.e("sendWithFiles", exception.getMessage());
                }

                @Override
                public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
                    hideLoading();
                    Log.i("reponse", serverResponse.getBodyAsString());
                    startLocationUpdates();
                    Snackbar.make(findViewById(R.id.name_et), "Reporte enviado con exito", 800).show();
                }

                @Override
                public void onCancelled(UploadInfo uploadInfo) {
                }
            })
                    .startUpload();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
    }
}
