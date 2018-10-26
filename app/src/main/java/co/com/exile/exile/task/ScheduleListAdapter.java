package co.com.exile.exile.task;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import co.com.exile.exile.R;

public class ScheduleListAdapter extends RecyclerView.Adapter<ScheduleListAdapter.TaskViewHolder> {

    private JSONArray tasks;

    void setTasks(JSONArray tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.schedule_task, parent, false);
        return new ScheduleListAdapter.TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        try {
            JSONObject item = tasks.getJSONObject(position);

            SimpleDateFormat parser = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd", Locale.getDefault());
            Date date = parser.parse(item.getString("fecha"));

            holder.date.setText(formatter.format(date));
            holder.title.setText(item.getString("title"));
            holder.description.setText(item.getString("descripcion"));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (tasks == null) {
            return 0;
        }
        return tasks.length();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView date;
        TextView title;
        TextView description;

        TaskViewHolder(View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
        }
    }
}
