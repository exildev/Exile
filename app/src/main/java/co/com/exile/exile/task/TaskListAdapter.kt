package co.com.exile.exile.task


import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import co.com.exile.exile.R

internal class TaskListAdapter : RecyclerView.Adapter<TaskListAdapter.TaskViewHolder>() {

    private var tasks: JSONArray? = null
    private var mCheckedChangeListener: SubTaskListAdapter.onSubTaskCheckedChangeListener? = null
    private var multimediaClickListener: MultimediaListAdapter.onMultimediaClickListener? = null
    private var mOnRecordVoice: OnRecordVoice? = null
    private var mOnImageClick: OnImageClick? = null
    private var mainButtonClick: OnMainButtonClick? = null

    fun setmCheckedChangeListener(mCheckedChangeListener: SubTaskListAdapter.onSubTaskCheckedChangeListener): TaskListAdapter {
        this.mCheckedChangeListener = mCheckedChangeListener
        return this
    }

    fun setmOnRecordVoice(mOnRecordVoice: OnRecordVoice): TaskListAdapter {
        this.mOnRecordVoice = mOnRecordVoice
        return this
    }

    fun setMultimediaClickListener(multimediaClickListener: MultimediaListAdapter.onMultimediaClickListener): TaskListAdapter {
        this.multimediaClickListener = multimediaClickListener
        return this
    }

    fun setOnImageClick(mOnImageClick: OnImageClick): TaskListAdapter {
        this.mOnImageClick = mOnImageClick
        return this
    }

    fun setMainButtonClick(mainButtonClick: OnMainButtonClick): TaskListAdapter {
        this.mainButtonClick = mainButtonClick
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.task, parent, false)
        return TaskViewHolder(view)
    }

    fun setTasks(tasks: JSONArray) {
        this.tasks = tasks
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        try {
            val noti = tasks!!.getJSONObject(position)
            val task = noti.getJSONObject("tarea")

            holder.title.text = task.getString("nombre")
            holder.description.text = task.getString("descripcion")
            val adapter = SubTaskListAdapter(mCheckedChangeListener)
            val layoutManager = LinearLayoutManager(holder.subTasks.context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            holder.subTasks.layoutManager = layoutManager
            holder.subTasks.setHasFixedSize(true)
            holder.subTasks.adapter = adapter
            adapter.setSubTasks(noti.getJSONArray("subnotificaciones"))

            val multimediaListAdapter = MultimediaListAdapter(holder)
            val multimediaLayoutManager = LinearLayoutManager(holder.multimedia.context)
            multimediaLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
            multimediaLayoutManager.stackFromEnd = true
            holder.multimedia.layoutManager = multimediaLayoutManager
            holder.multimedia.setHasFixedSize(true)
            holder.multimedia.adapter = multimediaListAdapter
            holder.multimediaAdapter = multimediaListAdapter
            multimediaListAdapter.setMultimedia(noti.getJSONArray("multimedia"))
            multimediaListAdapter.setMultimediaUpdate { holder.multimedia.smoothScrollToPosition(multimediaListAdapter.itemCount - 1) }
            multimediaListAdapter.setMultimediaLongClick { multimedia ->
                try {
                    multimedia.put("selected", true)
                    multimediaListAdapter.notifyDataSetChanged()
                    noti.put("selecting", true)
                    holder.mainButton.setImageResource(R.drawable.ic_delete_24dp)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            if (noti.has("selecting")) {
                holder.mainButton.setImageResource(R.drawable.ic_delete_24dp)
            } else {
                holder.mainButton.setImageResource(R.drawable.ic_done_24dp)
            }

            val text = holder.viewCompleted.context.getString(R.string.show_completed_subtasks, adapter.countCompleted())
            holder.viewCompleted.text = text

            holder.viewCompleted.setOnClickListener { view ->
                Log.i("view completed", view.toString())
                if (adapter.isShowCompleted) {
                    val text = view.context.getString(R.string.show_completed_subtasks, adapter.countCompleted())
                    holder.viewCompleted.text = text
                } else {
                    val text = view.context.getString(R.string.hide_completed_subtasks)
                    holder.viewCompleted.text = text
                }
                adapter.isShowCompleted = !adapter.isShowCompleted
            }
        } catch (e: JSONException) {
            //e.printStackTrace();
        }

    }

    override fun getItemCount(): Int {
        return if (tasks == null) {
            0
        } else tasks!!.length()
    }

    internal interface OnRecordVoice {
        fun tryStartRecord()

        fun tryStopRecord(task: JSONObject, adapter: MultimediaListAdapter?)
    }

    internal interface OnImageClick {
        fun onImageClick(task: JSONObject, adapter: MultimediaListAdapter?)
    }

    internal interface OnMainButtonClick {
        fun onDeleteClick(task: JSONObject)

        fun onDoneClick(task: JSONObject)
    }

    internal inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), MultimediaListAdapter.onMultimediaClickListener {

        var title: TextView
        var description: TextView
        var subTasks: RecyclerView
        var viewCompleted: TextView
        var voiceBtn: ImageButton
        var multimedia: RecyclerView
        var multimediaAdapter: MultimediaListAdapter? = null
        var imageButton: ImageButton
        var mainButton: ImageButton

        init {

            title = itemView.findViewById(R.id.task_name)
            description = itemView.findViewById(R.id.task_description)
            subTasks = itemView.findViewById(R.id.subtasks)
            viewCompleted = itemView.findViewById(R.id.view_completed)
            voiceBtn = itemView.findViewById(R.id.voice_btn)
            multimedia = itemView.findViewById(R.id.multimedia)
            imageButton = itemView.findViewById(R.id.image_btn)
            mainButton = itemView.findViewById(R.id.main_button)

            voiceBtn.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> mOnRecordVoice!!.tryStartRecord()
                    MotionEvent.ACTION_UP -> try {
                        mOnRecordVoice!!.tryStopRecord(tasks!!.getJSONObject(adapterPosition), multimediaAdapter)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    MotionEvent.ACTION_CANCEL -> try {
                        mOnRecordVoice!!.tryStopRecord(tasks!!.getJSONObject(adapterPosition), multimediaAdapter)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    else -> {
                    }
                }
                true
            }

            imageButton.setOnClickListener {
                try {
                    mOnImageClick!!.onImageClick(tasks!!.getJSONObject(adapterPosition), multimediaAdapter)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            mainButton.setOnClickListener {
                try {
                    val task = tasks!!.getJSONObject(adapterPosition)
                    if (task.has("selecting")) {
                        mainButtonClick!!.onDeleteClick(task)
                    } else {
                        mainButtonClick!!.onDoneClick(task)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }

        override fun onClick(multimedia: JSONObject, adapter: MultimediaListAdapter) {
            try {
                val task = tasks!!.getJSONObject(adapterPosition)
                if (task.has("selecting")) {
                    if (multimedia.has("selected")) {
                        multimedia.remove("selected")
                        tryDeselect(task)
                    } else {
                        multimedia.put("selected", true)
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    multimediaClickListener!!.onClick(multimedia, adapter)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        @Throws(JSONException::class)
        private fun tryDeselect(task: JSONObject) {
            val multimedia = task.getJSONArray("multimedia")
            for (i in 0 until multimedia.length()) {
                val file = multimedia.getJSONObject(i)
                if (file.has("selected")) {
                    return
                }
            }
            task.remove("selecting")
            mainButton.setImageResource(R.drawable.ic_done_24dp)
        }
    }
}
