package co.com.exile.exile.task;


import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.com.exile.exile.R;

class MultimediaListAdapter extends RecyclerView.Adapter<MultimediaListAdapter.MultimediaViewHolder> {

    private JSONArray multimedia;
    private onMultimediaClickListener multimediaClickListener;
    private onMultimediaUpdate multimediaUpdate;

    MultimediaListAdapter(onMultimediaClickListener multimediaClickListener) {
        this.multimediaClickListener = multimediaClickListener;
    }

    void setMultimediaUpdate(onMultimediaUpdate multimediaUpdate) {
        this.multimediaUpdate = multimediaUpdate;
    }

    @Override
    public MultimediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.multimedia, parent, false);
        return new MultimediaViewHolder(view);
    }

    void setMultimedia(JSONArray multimedia) {
        this.multimedia = multimedia;
        notifyDataSetChanged();
    }

    final void notifyMultimediaChanged() {
        notifyDataSetChanged();
        multimediaUpdate.onUpdate();
    }

    @Override
    public void onBindViewHolder(MultimediaViewHolder holder, int position) {
        try {
            JSONObject file = multimedia.getJSONObject(position);

            if (file.getInt("tipo") == 1) {
                Picasso
                        .with(holder.img.getContext())
                        .load(file.getString("url"))
                        .resizeDimen(R.dimen.multimedia_size, R.dimen.multimedia_size)
                        .centerCrop()
                        .into(holder.img);
                holder.playBtn.setVisibility(View.GONE);
            } else {
                holder.img.setImageResource(R.drawable.ic_headset_24dp);
                holder.playBtn.setVisibility(View.VISIBLE);
            }

            if (file.has("isPlaying")) {
                holder.playBtn.setImageResource(R.drawable.ic_pause_circle_outline_24dp);
            } else {
                holder.playBtn.setImageResource(R.drawable.ic_play_circle_outline_24dp);
            }
            //// TODO: 8/09/17 agregar los indicadores de carga del audio
            if (file.has("isLoading")) {
                ((CardView) holder.itemView).setCardBackgroundColor(Color.parseColor("#b2b2b2"));
            } else {
                ((CardView) holder.itemView).setCardBackgroundColor(Color.parseColor("#ffad1f"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (multimedia == null) {
            return 0;
        }
        return multimedia.length();
    }

    interface onMultimediaClickListener {
        void onClick(JSONObject multimedia, MultimediaListAdapter adapter);
    }

    interface onMultimediaUpdate {
        void onUpdate();
    }

    class MultimediaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView img;
        ImageButton playBtn;

        MultimediaViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.multimedia_img);
            playBtn = itemView.findViewById(R.id.play_btn);
            itemView.setOnClickListener(this);
            playBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            try {
                multimediaClickListener.onClick(multimedia.getJSONObject(getAdapterPosition()), MultimediaListAdapter.this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
