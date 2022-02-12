package androidx.io.core.video;

public interface OnVideoCompressListener {

    /**
     * 视频压缩进度
     *
     * @param percent 进度百分比
     */
    void onVideoCompressProgress(float percent);

}
