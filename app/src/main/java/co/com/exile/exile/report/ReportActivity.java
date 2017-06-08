package co.com.exile.exile.report;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.com.exile.exile.R;
import co.com.exile.exile.network.VolleySingleton;

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

    private GoogleApiClient mGoogleClient;
    private LocationRequest mLocationRequest;
    private Location mLocation;

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "KMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
                                typesString = new String[count + 1];
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
                            Snackbar.make(findViewById(R.id.main_container), "usuario y/o contraseña incorrecta", 800).show();
                        } else {
                            Snackbar.make(findViewById(R.id.main_container), "Error al hacer la consulta", 800).show();
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
                                placesString = new String[count + 1];
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
                            Snackbar.make(findViewById(R.id.main_container), "usuario y/o contraseña incorrecta", 800).show();
                        } else {
                            Snackbar.make(findViewById(R.id.main_container), "Error al hacer la consulta", 800).show();
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
                                clientsString = new String[count + 1];
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
                            Snackbar.make(findViewById(R.id.main_container), "usuario y/o contraseña incorrecta", 800).show();
                        } else {
                            Snackbar.make(findViewById(R.id.main_container), "Error al hacer la consulta", 800).show();
                        }
                    }
                });
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void renderTypes() {
        typesString[typesString.length - 1] = getString(R.string.report_type);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_selected, typesString) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView) v.findViewById(android.R.id.text1)).setText("");
                    ((TextView) v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount() - 1; // you dont display last item. It is used as hint.
            }

        };
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        final Spinner spinner = (Spinner) findViewById(R.id.type_spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getCount());
    }

    private void renderPlaces() {
        placesString[placesString.length - 1] = getString(R.string.report_place);

        Log.i("places", placesString[0] + "");
        Log.i("places", mPlaces.toString());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_selected, placesString) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView) v.findViewById(android.R.id.text1)).setText("");
                    ((TextView) v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount() - 1; // you dont display last item. It is used as hint.
            }

        };
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        final Spinner spinner = (Spinner) findViewById(R.id.place_spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getCount());
    }

    private void renderClients() {
        clientsString[clientsString.length - 1] = getString(R.string.report_client);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_selected, clientsString) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView) v.findViewById(android.R.id.text1)).setText("");
                    ((TextView) v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount() - 1; // you dont display last item. It is used as hint.
            }

        };
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        final Spinner spinner = (Spinner) findViewById(R.id.client_spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getCount());
    }

    private void addAttaches(List<String> paths) {
        attaches.clear();
        attaches.addAll(paths);

        final LinearLayout parent = (LinearLayout) findViewById(R.id.form_container);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < MAX_UPLOAD_FILES; i++) {
            if (attaches.size() > i) {
                String path = attaches.get(i);
                View attach;
                if (parent.getChildCount() > i + 9) {
                    attach = parent.getChildAt(9 + i);
                } else {
                    attach = inflater.inflate(R.layout.report_attach, parent, false);
                    parent.addView(attach);
                }
                renderAttach(parent, attach, path);
            } else if (parent.getChildCount() > i + 9) {
                parent.removeViews(9 + i, parent.getChildCount() - (9 + i));
            } else {
                break;
            }
        }
    }

    private void renderAttach(final ViewGroup parent, final View attach, final String path) {
        File image = new File(path);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 5;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        ImageView img = (ImageView) attach.findViewById(R.id.attach_image);
        img.setImageBitmap(bitmap);

        TextView name = (TextView) attach.findViewById(R.id.attach_name);
        name.setText(image.getName());

        TextView size = (TextView) attach.findViewById(R.id.attach_size);
        size.setText(humanReadableByteCount(image.length(), true));

        View delete = attach.findViewById(R.id.attach_delete);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.removeView(attach);
                attaches.remove(path);
            }
        });
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
