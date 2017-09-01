package co.com.exile.exile.task;


import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.com.exile.exile.R;

class SubTaskListAdapter extends RecyclerView.Adapter<SubTaskListAdapter.SubTaskViewHolder> {

    private JSONArray subTasks;
    private onSubTaskCheckedChangeListener mCheckedChangeListener;
    private boolean showCompleted;

    SubTaskListAdapter(onSubTaskCheckedChangeListener mCheckedChangeListener) {
        this.mCheckedChangeListener = mCheckedChangeListener;
        showCompleted = false;
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
        Log.i("subtasks", subTasks.toString());
    }

    boolean isShowCompleted() {
        return showCompleted;
    }

    void setShowCompleted(boolean showCompleted) {
        this.showCompleted = showCompleted;
        notifyDataSetChanged();
    }

    int countCompleted() {
        int count = 0;
        if (subTasks == null) {
            return 0;
        }
        for (int i = 0; i < subTasks.length(); i++) {
            JSONObject task = null;
            try {
                task = subTasks.getJSONObject(i);
                Object completado = task.get("completado");
                if (!completado.equals(JSONObject.NULL)) {
                    count += 1;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    @Override
    public void onBindViewHolder(SubTaskViewHolder holder, int position) {
        try {
            JSONObject task = subTasks.getJSONObject(position);
            Object completado = task.get("completado");
            holder.view.setText(task.getString("nombre"));
            if (!completado.equals(JSONObject.NULL) && !showCompleted) {
                holder.setVisibility(false);
            } else {
                holder.setVisibility(true);
                holder.view.setChecked(!completado.equals(JSONObject.NULL));
                if (!completado.equals(JSONObject.NULL)) {
                    holder.view.setPaintFlags(holder.view.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    holder.view.setPaintFlags(holder.view.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                }
            }

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

        public void setVisibility(boolean isVisible) {
            RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            if (isVisible) {
                param.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                param.width = LinearLayout.LayoutParams.MATCH_PARENT;
                itemView.setVisibility(View.VISIBLE);
            } else {
                itemView.setVisibility(View.GONE);
                param.height = 0;
                param.width = 0;
            }
            itemView.setLayoutParams(param);
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            try {
                JSONObject task = subTasks.getJSONObject(this.getAdapterPosition());
                Object completado = task.get("completado");
                if ((b && completado.equals(JSONObject.NULL)) || (!b && !completado.equals(JSONObject.NULL))) {
                    mCheckedChangeListener.onCheckedChanged(task, b);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
