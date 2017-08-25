package co.com.exile.exile.task;


import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.com.exile.exile.R;

class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {

    private JSONArray tasks;

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.task, parent, false);
        return new TaskViewHolder(view);
    }

    void setTasks(JSONArray tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        try {
            JSONObject task = tasks.getJSONObject(position);
            //String fecha = report.getString("fecha").split(" ")[0];
            //String creatorR = report.getJSONObject("creatorR").getString("nombre");
            holder.title.setText(task.getString("nombre"));
            holder.description.setText(task.getString("descripcion"));
            SubTaskListAdapter adapter = new SubTaskListAdapter();
            LinearLayoutManager layoutManager = new LinearLayoutManager(holder.subTasks.getContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            holder.subTasks.setLayoutManager(layoutManager);
            holder.subTasks.setHasFixedSize(true);
            holder.subTasks.setAdapter(adapter);
            adapter.setSubTasks(task.getJSONArray("subtareas"));
            //holder.index.setText("#" + report.getInt("id") + "");
        } catch (JSONException e) {
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

        TextView title;
        TextView description;
        RecyclerView subTasks;
        View hasFiles;

        TaskViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.task_name);
            description = itemView.findViewById(R.id.task_description);
            subTasks = itemView.findViewById(R.id.subtasks);
        }
    }
}
