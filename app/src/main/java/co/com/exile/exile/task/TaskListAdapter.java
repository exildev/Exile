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
    private OnMainButtonClick mainButtonClick;

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

    TaskListAdapter setMainButtonClick(OnMainButtonClick mainButtonClick) {
        this.mainButtonClick = mainButtonClick;
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
            final JSONObject noti = tasks.getJSONObject(position);
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

            final MultimediaListAdapter multimediaListAdapter = new MultimediaListAdapter(holder);
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
            multimediaListAdapter.setMultimediaLongClick(new MultimediaListAdapter.onMultimediaLongClick() {
                @Override
                public void onLongClick(JSONObject multimedia) {
                    try {
                        multimedia.put("selected", true);
                        multimediaListAdapter.notifyDataSetChanged();
                        noti.put("selecting", true);
                        holder.mainButton.setImageResource(R.drawable.ic_delete_24dp);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            if (noti.has("selecting")) {
                holder.mainButton.setImageResource(R.drawable.ic_delete_24dp);
            } else {
                holder.mainButton.setImageResource(R.drawable.ic_done_24dp);
            }

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
            //e.printStackTrace();
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

    interface OnMainButtonClick {
        void onDeleteClick(JSONObject task);

        void onDoneClick(JSONObject task);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder implements MultimediaListAdapter.onMultimediaClickListener {

        TextView title;
        TextView description;
        RecyclerView subTasks;
        TextView viewCompleted;
        ImageButton voiceBtn;
        RecyclerView multimedia;
        MultimediaListAdapter multimediaAdapter;
        ImageButton imageButton;
        ImageButton mainButton;

        TaskViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.task_name);
            description = itemView.findViewById(R.id.task_description);
            subTasks = itemView.findViewById(R.id.subtasks);
            viewCompleted = itemView.findViewById(R.id.view_completed);
            voiceBtn = itemView.findViewById(R.id.voice_btn);
            multimedia = itemView.findViewById(R.id.multimedia);
            imageButton = itemView.findViewById(R.id.image_btn);
            mainButton = itemView.findViewById(R.id.main_button);

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

            mainButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        JSONObject task = tasks.getJSONObject(getAdapterPosition());
                        if (task.has("selecting")) {
                            mainButtonClick.onDeleteClick(task);
                        } else {
                            mainButtonClick.onDoneClick(task);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onClick(JSONObject multimedia, MultimediaListAdapter adapter) {
            try {
                JSONObject task = tasks.getJSONObject(getAdapterPosition());
                if (task.has("selecting")) {
                    if (multimedia.has("selected")) {
                        multimedia.remove("selected");
                        tryDeselect(task);
                    } else {
                        multimedia.put("selected", true);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    multimediaClickListener.onClick(multimedia, adapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void tryDeselect(JSONObject task) throws JSONException {
            JSONArray multimedia = task.getJSONArray("multimedia");
            for (int i = 0; i < multimedia.length(); i++) {
                JSONObject file = multimedia.getJSONObject(i);
                if (file.has("selected")) {
                    return;
                }
            }
            task.remove("selecting");
            mainButton.setImageResource(R.drawable.ic_done_24dp);
        }
    }
}
