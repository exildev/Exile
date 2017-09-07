package co.com.exile.exile.task;


import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.com.exile.exile.R;

class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {

    private JSONArray tasks;
    private SubTaskListAdapter.onSubTaskCheckedChangeListener mCheckedChangeListener;
    private MultimediaListAdapter.onMultimediaClickListener multimediaClickListener;
    private OnRecordVoice mOnRecordVoice;

    TaskListAdapter(SubTaskListAdapter.onSubTaskCheckedChangeListener mCheckedChangeListener, OnRecordVoice onRecordVoice, MultimediaListAdapter.onMultimediaClickListener multimediaClickListener) {
        this.mCheckedChangeListener = mCheckedChangeListener;
        this.mOnRecordVoice = onRecordVoice;
        this.multimediaClickListener = multimediaClickListener;
    }

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
    public void onBindViewHolder(final TaskViewHolder holder, int position) {
        try {
            JSONObject task = tasks.getJSONObject(position);

            Log.i("multimedia", task.getJSONArray("multimedia") + "");

            holder.title.setText(task.getString("nombre"));
            holder.description.setText(task.getString("descripcion"));
            final SubTaskListAdapter adapter = new SubTaskListAdapter(mCheckedChangeListener);
            LinearLayoutManager layoutManager = new LinearLayoutManager(holder.subTasks.getContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            holder.subTasks.setLayoutManager(layoutManager);
            holder.subTasks.setHasFixedSize(true);
            holder.subTasks.setAdapter(adapter);
            adapter.setSubTasks(task.getJSONArray("subtareas"));

            final MultimediaListAdapter adapter2 = new MultimediaListAdapter(multimediaClickListener);
            LinearLayoutManager layoutManager2 = new LinearLayoutManager(holder.multimedia.getContext());
            layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
            holder.multimedia.setLayoutManager(layoutManager2);
            holder.multimedia.setHasFixedSize(true);
            holder.multimedia.setAdapter(adapter2);
            adapter2.setMultimedia(task.getJSONArray("multimedia"));

            String text = holder.viewCompleted.getContext().getString(R.string.show_completed_subtasks, adapter.countCompleted());
            holder.viewCompleted.setText(text);

            holder.viewCompleted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("view completed", view.toString());
                    if (adapter.isShowCompleted()) {
                        String text = view.getContext().getString(R.string.show_completed_subtasks, adapter.countCompleted());
                        holder.viewCompleted.setText(text);
                    } else {
                        String text = view.getContext().getString(R.string.hide_completed_subtasks);
                        holder.viewCompleted.setText(text);
                    }
                    adapter.setShowCompleted(!adapter.isShowCompleted());
                }
            });
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

    interface OnRecordVoice {
        void tryStartRecord();

        void tryStopRecord(JSONObject task);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView description;
        RecyclerView subTasks;
        TextView viewCompleted;
        ImageButton voiceBtn;
        RecyclerView multimedia;

        TaskViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.task_name);
            description = itemView.findViewById(R.id.task_description);
            subTasks = itemView.findViewById(R.id.subtasks);
            viewCompleted = itemView.findViewById(R.id.view_completed);
            voiceBtn = itemView.findViewById(R.id.voice_btn);
            multimedia = itemView.findViewById(R.id.multimedia);

            voiceBtn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mOnRecordVoice.tryStartRecord();
                            break;
                        case MotionEvent.ACTION_UP:
                            try {
                                mOnRecordVoice.tryStopRecord(tasks.getJSONObject(getAdapterPosition()));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            try {
                                mOnRecordVoice.tryStopRecord(tasks.getJSONObject(getAdapterPosition()));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
        }
    }
}
