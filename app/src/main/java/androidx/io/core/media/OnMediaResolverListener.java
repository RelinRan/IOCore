package androidx.io.core.media;

import java.util.List;

public interface OnMediaResolverListener {

    /**
     * 媒体解析
     *
     * @param type   类型
     * @param medias 媒体
     */
    void onMediaResolver(int type, List<Media> medias);

}
