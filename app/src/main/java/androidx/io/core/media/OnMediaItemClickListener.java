package androidx.io.core.media;

import android.view.View;

public interface OnMediaItemClickListener {

    /**
     * 媒体Item点击
     *
     * @param adapter  适配器
     * @param v        控件
     * @param position 位置
     */
    void onMediaItemClick(MediaAdapter adapter, View v, int position);

}