package co.com.exile.exile.report;


import android.app.Activity;
import android.content.Intent;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.com.exile.exile.BaseFragment;
import co.com.exile.exile.R;
import co.com.exile.exile.network.VolleySingleton;


public class ReportFragment extends BaseFragment {
    public static final int TYPE_OPEN = 1;
    public static final int TYPE_CLOSED = 2;

    private static final String ARG_TYPE = "type";
    SwipeRefreshLayout mSwipe;
    ReportListAdapter mAdapter;
    private int type;


    public ReportFragment() {
        // Required empty public constructor
    }


    public static ReportFragment newInstance(int type) {
        ReportFragment fragment = new ReportFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt(ARG_TYPE, TYPE_OPEN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        RecyclerView reportList = view.findViewById(R.id.report_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        reportList.setLayoutManager(layoutManager);
        reportList.setHasFixedSize(true);
        mAdapter = new ReportListAdapter();
        reportList.setAdapter(mAdapter);

        mAdapter.setReportClickListener(new ReportListAdapter.onReportClickListener() {
            @Override
            public void onClick(JSONObject report) {
                openReport(report);
            }
        });

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

    private void openReport(JSONObject report) {
        Intent intent = new Intent(getContext(), ReportDetailsActivity.class);
        intent.putExtra("report", report.toString());
        startActivity(intent);
    }

    private void loadData() {
        String t;
        if (type == TYPE_OPEN) {
            t = getString(R.string.boolean_param_false);
        } else {
            t = getString(R.string.boolean_param_true);
        }

        String serviceUrl = getString(R.string.report_list_url, t);

        String url = getUrl(serviceUrl);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("response", response.toString());
                try {
                    JSONArray object_list = response.getJSONArray("object_list");
                    mAdapter.setReports(object_list);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ReportFragmetPagerAdapter.ADD_NEW_REPORT && resultCode == Activity.RESULT_OK) {
            mSwipe.setRefreshing(true);
            loadData();
            Snackbar.make(mSwipe, "Reporte guardado con exito", Snackbar.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
