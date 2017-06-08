package co.com.exile.exile.report;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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

public class ReportActivity extends AppCompatActivity {
    private static final int MAX_UPLOAD_FILES = 5;
    String[] typesString;
    JSONArray mTypes;
    String[] placesString;
    JSONArray mPlaces;
    String[] clientsString;
    JSONArray mClients;
    ArrayList<String> attaches;

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
}
