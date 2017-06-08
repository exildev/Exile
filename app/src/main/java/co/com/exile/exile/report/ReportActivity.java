package co.com.exile.exile.report;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.com.exile.exile.R;
import co.com.exile.exile.network.VolleySingleton;

public class ReportActivity extends AppCompatActivity {
    String[] typesString;
    JSONArray mTypes;

    String[] placesString;
    JSONArray mPlaces;

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
        getTypes();
        getPlaces();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.report_menu, menu);
        return true;
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
}
