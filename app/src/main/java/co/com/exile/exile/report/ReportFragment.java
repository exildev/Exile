package co.com.exile.exile.report;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.com.exile.exile.R;


public class ReportFragment extends Fragment {
    public static final int TYPE_OPEN = 1;
    public static final int TYPE_CLOSED = 2;

    private static final String ARG_TYPE = "type";
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
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

}
