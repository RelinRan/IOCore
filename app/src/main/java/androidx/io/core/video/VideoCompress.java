package androidx.io.core.video;

import android.content.Context;
import android.net.Uri;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 视频压缩工具
 */
public class VideoCompress {

    public static final int COMPRESS_QUALITY_HIGH = 1;
    public static final int COMPRESS_QUALITY_MEDIUM = 2;
    public static final int COMPRESS_QUALITY_LOW = 3;

    private String srcPath;
    private FileDescriptor srcFile;
    private String desPath;
    private int quality;
    private ExecutorService service;
    private VideoMessenger handler;

    public VideoCompress(String srcPath, String desPath, int quality) {
        this.srcPath = srcPath;
        this.desPath = desPath;
        this.quality = quality;
        service = Executors.newSingleThreadExecutor();
    }

    public VideoCompress(FileDescriptor srcFile, String desPath, int quality) {
        this.srcFile = srcFile;
        this.desPath = desPath;
        this.quality = quality;
        service = Executors.newSingleThreadExecutor();
    }

    public VideoCompress(Context context, Uri uri, String desPath, int quality) {
        try {
            this.srcFile = context.getContentResolver().openFileDescriptor(uri, "rw").getFileDescriptor();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.desPath = desPath;
        this.quality = quality;
        service = Executors.newSingleThreadExecutor();
    }

    public void setOnVideoCompressListener(OnVideoCompressListener listener) {
        handler = new VideoMessenger(listener);
    }

    public void start() {
        service.execute(() -> VideoController.getInstance().convertVideo(srcPath, srcFile, desPath, quality, percent -> {
            if (handler != null) {
                handler.send(percent);
            }
        }));
    }

    public void stop() {
        if (handler != null && handler.hasMessages(200)) {
            handler.removeMessages(200);
        }
        if (!service.isShutdown()) {
            service.shutdown();
        }
    }


}
