package androidx.io.core.media;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.io.core.R;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class PopMenuAdapter extends RecyclerView.Adapter<ViewHolder> {

    private Context context;
    private List<PopMenu> data;
    private OnMediaMenuItemClickListener onMediaMenuItemClickListener;

    public PopMenuAdapter(Context context) {
        this.context = context;
    }

    public void setOnMediaMenuItemClickListener(OnMediaMenuItemClickListener onMediaMenuItemClickListener) {
        this.onMediaMenuItemClickListener = onMediaMenuItemClickListener;
    }

    public void setData(List<PopMenu> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public PopMenu getItem(int position) {
        return data.get(position);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.io_core_media_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PopMenu item = getItem(position);
        LinearLayout item_view = holder.findView(R.id.item_view);
        ImageView iv_icon = holder.findView(R.id.iv_icon);
        TextView tv_name = holder.findView(R.id.tv_name);
        iv_icon.setImageResource(item.getIcon());
        tv_name.setText(item.getName());
        item_view.setOnClickListener(v -> {
            if (onMediaMenuItemClickListener != null) {
                onMediaMenuItemClickListener.onMediaMenuItemClick(item);
            }
        });
    }

}
