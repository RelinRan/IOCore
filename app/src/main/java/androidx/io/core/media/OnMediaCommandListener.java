package androidx.io.core.media;

import java.util.List;

public interface OnMediaCommandListener {

    /**
     * 媒体执行
     *
     * @param type   类型
     * @param medias 媒体数据
     */
    void onMediaCommand(int type, List<Media> medias);

}