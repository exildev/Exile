package co.com.exile.exile.report;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import co.com.exile.exile.R;


class AttachAdapter extends RecyclerView.Adapter<AttachAdapter.AttachViewHolder> {

    private String[] attaches;
    private onFotoClickListener fotoClickListener;

    void setFotoClickListener(onFotoClickListener fotoClickListener) {
        this.fotoClickListener = fotoClickListener;
    }

    String[] getAttaches() {
        return attaches;
    }

    void setAttaches(String[] attaches) {
        this.attaches = attaches;
        notifyDataSetChanged();
    }

    @Override
    public AttachViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.attach, parent, false);
        return new AttachViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AttachViewHolder holder, int position) {
        Picasso.with(holder.attach.getContext())
                .load(attaches[position])
                .into(holder.attach);
    }

    @Override
    public int getItemCount() {
        if (attaches != null) {
            return attaches.length;
        }
        return 0;
    }

    interface onFotoClickListener {
        void onClick(int position);
    }

    class AttachViewHolder extends RecyclerView.ViewHolder {

        ImageView attach;

        AttachViewHolder(View itemView) {
            super(itemView);

            attach = (ImageView) itemView;

            attach.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fotoClickListener.onClick(getAdapterPosition());
                }
            });
        }
    }
}
