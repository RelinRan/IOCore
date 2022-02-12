package androidx.io.core.video;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

public class VideoMessenger extends Handler {

    public void send(float percent) {
        Message msg = obtainMessage();
        msg.what = 200;
        Bundle bundle = new Bundle();
        bundle.putFloat("percent", percent);
        msg.setData(bundle);
        sendMessage(msg);
    }

    private OnVideoCompressListener onVideoCompressListener;

    public VideoMessenger(OnVideoCompressListener onVideoCompressListener) {
        this.onVideoCompressListener = onVideoCompressListener;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        float percent = msg.getData().getFloat("percent");
        if (onVideoCompressListener != null) {
            onVideoCompressListener.onVideoCompressProgress(percent);
        }
    }
}
