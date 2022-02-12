package androidx.io.core.media;

import android.content.Context;

import java.util.List;

/**
 * 媒体执行器
 */
public class MediaExecutor implements OnMediaResolverListener {

    private List<Media> files;
    private List<Media> videos;
    private List<Media> images;
    private List<Media> audios;
    private MediaResolver resolver;
    private static MediaExecutor executor;
    private OnMediaResolverListener onMediaResolverListener;

    public MediaExecutor(Context context) {
        resolver = new MediaResolver(context);
        resolver.setOnMediaResolverListener(this);
        resolver.execute(MediaResolver.FILES);
        resolver.execute(MediaResolver.IMAGE);
        resolver.execute(MediaResolver.VIDEO);
        resolver.execute(MediaResolver.AUDIO);
    }

    /**
     * 初始化文档存储
     *
     * @param context 上下文
     * @return
     */
    public static MediaExecutor initialize(Context context) {
        if (executor == null) {
            synchronized (MediaExecutor.class) {
                if (executor == null) {
                    executor = new MediaExecutor(context);
                }
            }
        }
        return executor;
    }

    /**
     * 获取操作对象
     *
     * @return
     */
    public static MediaExecutor getExecutor() {
        return executor;
    }

    /**
     * 刷新数据
     */
    public void refresh() {
        resolver.execute(MediaResolver.FILES);
        resolver.execute(MediaResolver.IMAGE);
        resolver.execute(MediaResolver.VIDEO);
        resolver.execute(MediaResolver.AUDIO);
    }

    /**
     * 获取文件集合
     *
     * @return
     */
    public List<Media> getFiles() {
        return files;
    }

    /**
     * 获取图片集合
     *
     * @return
     */
    public List<Media> getImages() {
        return images;
    }

    /**
     * 获取视频集合
     *
     * @return
     */
    public List<Media> getVideos() {
        return videos;
    }

    /**
     * 获取音频集合
     *
     * @return
     */
    public List<Media> getAudios() {
        return audios;
    }

    /**
     * 设置文档解析监听
     *
     * @param onMediaResolverListener
     */
    public void setOnMediaResolverListener(OnMediaResolverListener onMediaResolverListener) {
        this.onMediaResolverListener = onMediaResolverListener;
    }

    @Override
    public void onMediaResolver(int type, List<Media> documents) {
        if (type == MediaResolver.FILES) {
            files = documents;
        }
        if (type == MediaResolver.VIDEO) {
            videos = documents;
        }
        if (type == MediaResolver.IMAGE) {
            images = documents;
        }
        if (type == MediaResolver.AUDIO) {
            audios = documents;
        }
        if (onMediaResolverListener != null) {
            onMediaResolverListener.onMediaResolver(type, documents);
        }
    }

}
