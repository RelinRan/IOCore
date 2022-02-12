package androidx.io.core.media;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 媒体文件线程解析
 */
public class MediaResolver implements OnMediaCommandListener {

    private static final String TAG = MediaResolver.class.getSimpleName();
    /**
     * 文件
     */
    public static final int FILES = 1;
    /**
     * 图片
     */
    public static final int IMAGE = 2;
    /**
     * 视频
     */
    public static final int VIDEO = 3;
    /**
     * 音频
     */
    public static final int AUDIO = 4;

    private long time;
    private Context context;
    private MediaMessenger messenger;
    private ExecutorService service;
    private OnMediaResolverListener onMediaResolverListener;

    /**
     * 文档解析
     *
     * @param context 上下文
     */
    public MediaResolver(Context context) {
        this.context = context;
        messenger = new MediaMessenger();
        service = Executors.newFixedThreadPool(10);
    }

    /**
     * 设置文件解析监听
     *
     * @param onMediaResolverListener
     */
    public void setOnMediaResolverListener(OnMediaResolverListener onMediaResolverListener) {
        this.onMediaResolverListener = onMediaResolverListener;
    }

    /**
     * 在将来的某个时间执行给定的命令。 命令可以在新线程、池线程或调用中执行线程，由 {@code Executor} 实现决定。
     *
     * @param type 命令可运行任务
     */
    public void execute(int type) {
        time = System.currentTimeMillis();
        service.execute(new MediaResolverCommand(context, type, this));
    }

    /**
     * 尝试停止所有正在执行的任务，停止处理等待任务，并返回任务列表等待执行。
     * <p>除了尽力阻止之外，没有任何保证处理主动执行的任务。 例如，典型的
     * 实现将通过 {@link Thread#interrupt} 取消，所以任何未能响应中断的任务可能永远不会终止。
     *
     * @return 从未开始执行的任务列表
     */
    public void shutdownNow() {
        service.shutdownNow();
    }

    /**
     * 启动先前提交的有序关闭任务被执行，但不会接受新任务。如果已经关闭，调用没有额外的效果。
     * <p>此方法不等待之前提交的任务
     * 完成执行。
     */
    public void shutdown() {
        service.shutdown();
    }

    /**
     * 如果此执行程序已关闭，则返回 {@code true}。
     *
     * @return {@code true} 如果这个执行器已经被关闭
     */
    public void isShutdown() {
        service.isShutdown();
    }

    /**
     * 如果所有任务在关闭后都已完成，则返回 {@code true}。
     * 请注意，{@code isTerminated} 永远不会是 {@code true}，除非
     * 首先调用 {@code shutdown} 或 {@code shutdownNow}。
     *
     * @return {@code true} 如果所有任务都在关机后完成
     */
    public void isTerminated() {
        service.isTerminated();
    }

    @Override
    public void onMediaCommand(int type, List<Media> documents) {
        long pass = System.currentTimeMillis() - time;
        Log.i(TAG, "type: " + type + ",count: " + documents.size() + ",time: " + (pass / 1000) + "s");
        messenger.send(type, documents, onMediaResolverListener);
    }

}
