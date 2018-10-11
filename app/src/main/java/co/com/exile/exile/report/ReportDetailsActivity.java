package co.com.exile.exile.report;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import co.com.exile.exile.BaseActivity;
import co.com.exile.exile.R;
import co.com.exile.exile.network.VolleySingleton;

public class ReportDetailsActivity extends BaseActivity {

    AttachAdapter fotoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        loadReport();
    }

    private void loadReport() {
        if (getIntent().hasExtra("report")) {
            try {
                JSONObject report = new JSONObject(getIntent().getStringExtra("report"));
                Log.i("report", report.toString());

                TextView reportTitle = findViewById(R.id.report_title);
                reportTitle.setText(report.getString("nombre"));

                TextView reportDescription = findViewById(R.id.report_description);
                reportDescription.setText(report.getString("descripcion"));

                JSONObject creator = report.getJSONObject("creator");

                TextView creatorFullName = findViewById(R.id.creator_full_name);
                creatorFullName.setText(creator.getString("first_name") + " " + creator.getString("last_name"));

                TextView creatorEmail = findViewById(R.id.creator_email);
                creatorEmail.setText(creator.getString("email"));

                String dateString = report.getString("fecha");
                SimpleDateFormat parser = new SimpleDateFormat("MM/dd/yyyy K:mm a", Locale.US);
                Date dateParsed = parser.parse(dateString);
                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM. yyyy", Locale.US);
                String dateFormmated = formatter.format(dateParsed);

                TextView date = findViewById(R.id.date);
                date.setText(dateFormmated);

                RecyclerView fotos = findViewById(R.id.fotos);
                fotoAdapter = new AttachAdapter();
                fotoAdapter.setFotoClickListener(new AttachAdapter.onFotoClickListener() {
                    @Override
                    public void onClick(int position) {
                        showFotos(position);
                    }
                });
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                fotos.setLayoutManager(layoutManager);
                fotos.setHasFixedSize(true);
                fotos.setAdapter(fotoAdapter);
                loadPhotos(report.getInt("id"));

                ImageView creatorAvatar = findViewById(R.id.creator_avatar);
                String avatarURl = getUrl(creator.getString("avatar"));
                Picasso.with(this)
                        .load(avatarURl)
                        .into(creatorAvatar);
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void showFotos(int position) {
        new ImageViewer.Builder(this, fotoAdapter.getAttaches())
                .setStartPosition(position)
                .show();
    }

    private void loadPhotos(int id) {
        String serviceUrl = getString(R.string.report_fotos, id);

        String url = getUrl(serviceUrl);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("response", response.toString());
                try {
                    JSONArray fotos = response.getJSONArray("object_list");
                    putInRV(fotos);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("response", error.toString());
            }
        });
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void putInRV(JSONArray fotos) throws JSONException {
        String[] photoList = new String[fotos.length()];
        for (int i = 0; i < fotos.length(); i++) {
            JSONObject foto = fotos.getJSONObject(i);
            photoList[i] = foto.getString("url");
        }
        fotoAdapter.setAttaches(photoList);
    }

}
