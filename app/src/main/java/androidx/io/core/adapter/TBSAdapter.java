package androidx.io.core.adapter;

import android.content.Context;

import androidx.io.core.R;
import androidx.io.core.photo.PhotoView;

public class TBSAdapter extends ImageAdapter<String> {

    private TBSlmageLoader imageLoader;

    public TBSAdapter(Context context) {
        super(context);
    }

    public void setImageLoader(TBSlmageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    @Override
    public int getItemLayoutResId() {
        return R.layout.ico_core_tbs_image;
    }

    @Override
    public void onItemBindViewHolder(ViewHolder holder, int position) {
        PhotoView photo_view = holder.find(R.id.photo_view);
        if (imageLoader != null) {
            imageLoader.onItemImageShow(photo_view, getItem(position));
        }
    }

}
