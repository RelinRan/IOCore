package androidx.io.core.video;

import android.net.Uri;

public class Video {

    /**
     * 视频ID
     */
    private final Long id;
    /**
     * 视频Uri
     */
    private final Uri uri;
    /**
     * 视频名称
     */
    private final String name;
    /**
     * 视频时长
     */
    private final int duration;
    /**
     * 视频文件大小
     */
    private final int size;
    /**
     * 视频宽度
     */
    private final int width;
    /**
     * 视频高度
     */
    private final int height;

    public Video(Long id,Uri uri, String name, int duration, int size,int width,int height) {
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.duration = duration;
        this.size = size;
        this.width = width;
        this.height = height;
    }

    public Long getId() {
        return id;
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public int getSize() {
        return size;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "Video{" +
                "id=" + id +
                ", uri=" + uri +
                ", name='" + name + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
