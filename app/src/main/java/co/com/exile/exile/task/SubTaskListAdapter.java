package co.com.exile.exile.task;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.com.exile.exile.R;

class SubTaskListAdapter extends RecyclerView.Adapter<SubTaskListAdapter.SubTaskViewHolder> {

    private JSONArray subTasks;
    private onSubTaskCheckedChangeListener mCheckedChangeListener;

    SubTaskListAdapter(onSubTaskCheckedChangeListener mCheckedChangeListener) {
        this.mCheckedChangeListener = mCheckedChangeListener;
    }

    @Override
    public SubTaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.subtask, parent, false);
        return new SubTaskViewHolder(view);
    }

    void setSubTasks(JSONArray subTasks) {
        this.subTasks = subTasks;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(SubTaskViewHolder holder, int position) {
        try {
            JSONObject task = subTasks.getJSONObject(position);

            holder.view.setText(task.getString("nombre"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (subTasks == null) {
            return 0;
        }
        return subTasks.length();
    }

    interface onSubTaskCheckedChangeListener {
        void onCheckedChanged(JSONObject subTask, boolean b);
    }

    class SubTaskViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {

        CheckBox view;

        SubTaskViewHolder(View itemView) {
            super(itemView);
            view = (CheckBox) itemView;
            view.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            try {
                JSONObject task = subTasks.getJSONObject(this.getAdapterPosition());
                mCheckedChangeListener.onCheckedChanged(task, b);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
