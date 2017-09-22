package co.com.exile.exile.profile;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import co.com.exile.exile.R;
import co.com.exile.exile.network.VolleySingleton;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

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
                        }
                    }
                });
            }
        });

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

        return rootView;
    }

    private void loadProfile(final View rootView) {
        String serviceUrl = getString(R.string.profile_data);

        String url = getString(R.string.url, serviceUrl);
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
                    String url = getString(R.string.url, response.getString("avatar"));
                    Picasso
                            .with(getContext())
                            .load(url)
                            .into(profile);
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
