package androidx.io.core.net;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 下载助手
 */
public class Downloader {

    private Command command;
    private ExecutorService service;

    /**
     * 构建下载器
     *
     * @param context 上下文
     * @param url     资源链接
     */
    public Downloader(Context context, String url) {
        command = new Command(context, url);
        service = Executors.newSingleThreadExecutor();
    }

    /**
     * 设置文件下载服务
     *
     * @param service
     */
    public void setService(ExecutorService service) {
        this.service = service;
    }

    /**
     * 获取文件下载服务
     *
     * @return
     */
    public ExecutorService getService() {
        return service;
    }

    /**
     * 获取下载命令
     *
     * @return
     */
    public Command getCommand() {
        return command;
    }

    /**
     * 设置缓存文件夹名称
     *
     * @param dirName
     */
    public void setDirName(String dirName) {
        command.setDirName(dirName);
    }

    /**
     * 设置是否覆盖下载
     *
     * @param override
     */
    public void setOverride(boolean override) {
        command.setOverride(override);
    }

    /**
     * 是否取消下载
     *
     * @return
     */
    public boolean isCancel() {
        return command.isCancel();
    }

    /**
     * 取消下载
     */
    public void cancel() {
        command.cancel();
    }

    /**
     * 是否暂停下载
     *
     * @return
     */
    public boolean isPause() {
        return command.isPause();
    }

    /**
     * 暂停下载
     */
    public void pause() {
        command.pause();
    }

    /**
     * 添加头部文件
     *
     * @param key
     * @param value
     */
    public void addHeader(String key, String value) {
        command.addHeader(key, value);
    }

    /**
     * 设置下载监听
     *
     * @param listener
     */
    public void setOnDownloadListener(OnDownloadListener listener) {
        command.setOnDownloadListener(listener);
    }

    /**
     * 开始下载
     */
    public void start() {
        service.execute(command);
    }

}

