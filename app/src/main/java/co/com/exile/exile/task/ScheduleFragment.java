package co.com.exile.exile.task;


import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import co.com.exile.exile.BaseFragment;
import co.com.exile.exile.R;
import co.com.exile.exile.network.VolleySingleton;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleFragment extends BaseFragment {

    ScheduleListAdapter adapter = new ScheduleListAdapter();
    SwipeRefreshLayout mSwipe;


    public ScheduleFragment() {
        // Required empty public constructor
    }


    public static ScheduleFragment newInstance() {
        return new ScheduleFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipe = view.findViewById(R.id.swipe);
        mSwipe.setRefreshing(true);

        RecyclerView reportList = view.findViewById(R.id.task_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        reportList.setLayoutManager(layoutManager);
        reportList.setHasFixedSize(false);
        reportList.setAdapter(adapter);

        loadData();

        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
    }

    private void loadData() {

        Calendar start_date = Calendar.getInstance();
        start_date.add(Calendar.DAY_OF_MONTH, 1);

        Calendar end_date = Calendar.getInstance();
        end_date.add(Calendar.DAY_OF_MONTH, 31);

        SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd", Locale.getDefault());

        String serviceUrl = getString(R.string.schedule_task_url, formatter.format(start_date.getTime()), formatter.format(end_date.getTime()));

        String url = getUrl(serviceUrl);

        Log.e("tales6", url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray object_list = response.getJSONArray("object_list");
                    adapter.setTasks(object_list);
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

}
