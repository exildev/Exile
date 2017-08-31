package co.com.exile.exile.task;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.com.exile.exile.R;
import co.com.exile.exile.network.VolleySingleton;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TodayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TodayFragment extends Fragment implements SubTaskListAdapter.onSubTaskCheckedChangeListener {

    TaskListAdapter mAdapter;
    SwipeRefreshLayout mSwipe;

    public TodayFragment() {
        // Required empty public constructor
    }

    public static TodayFragment newInstance() {
        return new TodayFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_today, container, false);

        RecyclerView reportList = view.findViewById(R.id.task_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        reportList.setLayoutManager(layoutManager);
        reportList.setHasFixedSize(true);
        mAdapter = new TaskListAdapter(this);
        reportList.setAdapter(mAdapter);

        mSwipe = view.findViewById(R.id.swipe);

        mSwipe.setRefreshing(true);
        loadData();

        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        return view;
    }

    private void loadData() {

        String serviceUrl = getString(R.string.task_url);

        String url = getString(R.string.url, serviceUrl);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("response", response.toString());
                try {
                    JSONArray object_list = response.getJSONArray("object_list");
                    mAdapter.setTasks(object_list);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSwipe.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("response", error.toString());
                mSwipe.setRefreshing(false);
            }
        });
        VolleySingleton.getInstance(this.getContext()).addToRequestQueue(request);
    }

    private void completeSubTask(final int id) {
        String serviceUrl = getString(R.string.subtask_complete);

        String url = getString(R.string.url, serviceUrl);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mSwipe.setRefreshing(false);
                        Log.i("reponse", response + " tales");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mSwipe.setRefreshing(false);
                        View view = getView();
                        if (view != null) {
                            Snackbar.make(view, "Hubo un error al enviar la solicitud", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject body = new JSONObject();
                try {
                    body.put("subtarea", id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return body.toString().getBytes();
            }
        };
        VolleySingleton.getInstance(this.getContext()).addToRequestQueue(request);
        mSwipe.setRefreshing(true);
    }

    private void uncompleteSubTask(final int id) {
        String serviceUrl = getString(R.string.subtask_uncomplete, id);

        String url = getString(R.string.url, serviceUrl);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mSwipe.setRefreshing(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mSwipe.setRefreshing(false);
                        View view = getView();
                        if (view != null) {
                            Snackbar.make(view, "Hubo un error al enviar la solicitud", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject body = new JSONObject();
                try {
                    body.put("subtarea", id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return body.toString().getBytes();
            }
        };
        VolleySingleton.getInstance(this.getContext()).addToRequestQueue(request);
        mSwipe.setRefreshing(true);
    }

    @Override
    public void onCheckedChanged(JSONObject subTask, boolean b) {
        try {
            if (b) {
                final int id = subTask.getInt("id");
                completeSubTask(id);
            } else {
                final int completado = subTask.getInt("completado");
                uncompleteSubTask(completado);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}
