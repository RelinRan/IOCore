package androidx.io.core.net;

import java.io.File;

/**
 * 文件下载监听
 */
public interface OnDownloadListener {

    /**
     * 文件下载进度
     *
     * @param total    文件大小
     * @param progress 进度
     */
    void onDownloading(long total, long progress);

    /**
     * 文件下载完成
     *
     * @param file 文件
     */
    void onDownloadCompleted(File file);

    /**
     * 下载失败
     *
     * @param e
     */
    void onDownloadFailed(Exception e);

}
