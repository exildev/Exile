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
    private OnImageClick mOnImageClick;

    TaskListAdapter setmCheckedChangeListener(SubTaskListAdapter.onSubTaskCheckedChangeListener mCheckedChangeListener) {
        this.mCheckedChangeListener = mCheckedChangeListener;
        return this;
    }

    TaskListAdapter setmOnRecordVoice(OnRecordVoice mOnRecordVoice) {
        this.mOnRecordVoice = mOnRecordVoice;
        return this;
    }

    TaskListAdapter setMultimediaClickListener(MultimediaListAdapter.onMultimediaClickListener multimediaClickListener) {
        this.multimediaClickListener = multimediaClickListener;
        return this;
    }

    TaskListAdapter setOnImageClick(OnImageClick mOnImageClick) {
        this.mOnImageClick = mOnImageClick;
        return this;
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
            JSONObject noti = tasks.getJSONObject(position);
            JSONObject task = noti.getJSONObject("tarea");

            holder.title.setText(task.getString("nombre"));
            holder.description.setText(task.getString("descripcion"));
            final SubTaskListAdapter adapter = new SubTaskListAdapter(mCheckedChangeListener);
            LinearLayoutManager layoutManager = new LinearLayoutManager(holder.subTasks.getContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            holder.subTasks.setLayoutManager(layoutManager);
            holder.subTasks.setHasFixedSize(true);
            holder.subTasks.setAdapter(adapter);
            adapter.setSubTasks(noti.getJSONArray("subnotificaciones"));

            final MultimediaListAdapter multimediaListAdapter = new MultimediaListAdapter(multimediaClickListener);
            LinearLayoutManager multimediaLayoutManager = new LinearLayoutManager(holder.multimedia.getContext());
            multimediaLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            multimediaLayoutManager.setStackFromEnd(true);
            holder.multimedia.setLayoutManager(multimediaLayoutManager);
            holder.multimedia.setHasFixedSize(true);
            holder.multimedia.setAdapter(multimediaListAdapter);
            holder.multimediaAdapter = multimediaListAdapter;
            multimediaListAdapter.setMultimedia(noti.getJSONArray("multimedia"));
            multimediaListAdapter.setMultimediaUpdate(new MultimediaListAdapter.onMultimediaUpdate() {
                @Override
                public void onUpdate() {
                    holder.multimedia.smoothScrollToPosition(multimediaListAdapter.getItemCount() - 1);
                }
            });


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

        void tryStopRecord(JSONObject task, MultimediaListAdapter adapter);
    }

    interface OnImageClick {
        void onImageClick(JSONObject task, MultimediaListAdapter adapter);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView description;
        RecyclerView subTasks;
        TextView viewCompleted;
        ImageButton voiceBtn;
        RecyclerView multimedia;
        MultimediaListAdapter multimediaAdapter;
        ImageButton imageButton;

        TaskViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.task_name);
            description = itemView.findViewById(R.id.task_description);
            subTasks = itemView.findViewById(R.id.subtasks);
            viewCompleted = itemView.findViewById(R.id.view_completed);
            voiceBtn = itemView.findViewById(R.id.voice_btn);
            multimedia = itemView.findViewById(R.id.multimedia);
            imageButton = itemView.findViewById(R.id.image_btn);

            voiceBtn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mOnRecordVoice.tryStartRecord();
                            break;
                        case MotionEvent.ACTION_UP:
                            try {
                                mOnRecordVoice.tryStopRecord(tasks.getJSONObject(getAdapterPosition()), multimediaAdapter);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            try {
                                mOnRecordVoice.tryStopRecord(tasks.getJSONObject(getAdapterPosition()), multimediaAdapter);
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

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        mOnImageClick.onImageClick(tasks.getJSONObject(getAdapterPosition()), multimediaAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
