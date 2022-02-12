package androidx.io.core.net;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.File;

public class Messenger extends Handler {

    public static final int WHAT_DOWNLOADING = 0x801;
    public static final int WHAT_COMPLETED = 0x802;
    public static final int WHAT_FAILED = 0x803;

    private OnDownloadListener listener;

    public Messenger() {
    }

    public void setOnDownloadListener(OnDownloadListener listener) {
        this.listener = listener;
    }

    public void send(long total, long progress) {
        Message msg = obtainMessage();
        msg.what = WHAT_DOWNLOADING;
        Bundle bundle = new Bundle();
        bundle.putLong("total", total);
        bundle.putLong("progress", progress);
        msg.setData(bundle);
        sendMessage(msg);
    }

    public void send(File file) {
        Message msg = obtainMessage();
        msg.what = WHAT_COMPLETED;
        msg.obj = file;
        sendMessage(msg);
    }

    public void send(Exception e) {
        Message msg = obtainMessage();
        msg.what = WHAT_FAILED;
        msg.obj = e;
        sendMessage(msg);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        if (listener == null) {
            return;
        }
        Bundle data = msg.getData();
        Object obj = msg.obj;
        switch (msg.what) {
            case WHAT_DOWNLOADING:
                if (listener != null) {
                    listener.onDownloading(data.getLong("total"), data.getLong("progress"));
                }
                break;
            case WHAT_COMPLETED:
                if (listener != null) {
                    listener.onDownloadCompleted((File) obj);
                }
                break;
            case WHAT_FAILED:
                if (listener != null) {
                    listener.onDownloadFailed((Exception) obj);
                }
                break;
        }
    }
}
