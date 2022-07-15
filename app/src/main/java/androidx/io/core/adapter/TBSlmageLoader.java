package androidx.io.core.adapter;

import androidx.io.core.photo.PhotoView;

/**
 * TBS图片加载器
 */
public interface TBSlmageLoader {

    /**
     * 图片加载显示
     *
     * @param target 图片View
     * @param url    图片资源路径
     */
    void onItemImageShow(PhotoView target, String url);

}
