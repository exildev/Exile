package co.com.exile.exile.task;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.com.exile.exile.R;

class MultimediaListAdapter extends RecyclerView.Adapter<MultimediaListAdapter.MultimediaViewHolder> {

    private JSONArray multimedia;

    @Override
    public MultimediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.multimedia, parent, false);
        return new MultimediaViewHolder(view);
    }

    public void setMultimedia(JSONArray multimedia) {
        this.multimedia = multimedia;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(MultimediaViewHolder holder, int position) {
        try {
            JSONObject task = multimedia.getJSONObject(position);
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

    class MultimediaViewHolder extends RecyclerView.ViewHolder {

        ImageView img;

        MultimediaViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.multimedia_img);
        }
    }
}
