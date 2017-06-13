package co.com.exile.exile.report;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import co.com.exile.exile.R;


class AttachAdapter extends RecyclerView.Adapter<AttachAdapter.AttachViewHolder> {

    private ArrayList<String> attaches;


    void setAttaches(ArrayList<String> attaches) {
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
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 5;
        Bitmap bitmap = BitmapFactory.decodeFile(attaches.get(position), options);
        holder.attach.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        if (attaches != null) {
            return attaches.size();
        }
        return 0;
    }

    class AttachViewHolder extends RecyclerView.ViewHolder {

        ImageView attach;

        AttachViewHolder(View itemView) {
            super(itemView);

            attach = (ImageView) itemView;
        }
    }
}
