package co.com.exile.exile.report;


import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.com.exile.exile.R;

class ReportListAdapter extends RecyclerView.Adapter<ReportListAdapter.ReportViewHolder> {

    private JSONArray reports;
    private onReportClickListener reportClickListener;

    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.report, parent, false);
        return new ReportViewHolder(view);
    }

    void setReports(JSONArray reports) {
        this.reports = reports;
        notifyDataSetChanged();
    }

    void setReportClickListener(onReportClickListener reportClickListener) {
        this.reportClickListener = reportClickListener;
    }

    @Override
    public void onBindViewHolder(ReportViewHolder holder, int position) {
        try {
            JSONObject report = reports.getJSONObject(position);
            String fecha = report.getString("fecha").split(" ")[0];
            String creatorR = report.getJSONObject("creator").getString("first_name") + " " + report.getJSONObject("creator").getString("last_name");
            holder.title.setText(report.getString("nombre"));
            holder.description.setText("Abierto el " + fecha + " por " + creatorR);
            holder.index.setText("#" + report.getInt("id") + "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (reports == null) {
            return 0;
        }
        return reports.length();
    }

    interface onReportClickListener {
        void onClick(JSONObject report);
    }

    class ReportViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title;
        TextView description;
        TextView index;
        View hasFiles;

        ReportViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.report_title);
            description = itemView.findViewById(R.id.report_description);
            index = itemView.findViewById(R.id.report_index);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            try {
                reportClickListener.onClick(reports.getJSONObject(getAdapterPosition()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
