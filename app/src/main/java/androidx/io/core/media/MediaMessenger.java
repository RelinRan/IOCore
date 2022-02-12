package androidx.io.core.media;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.List;

public class MediaMessenger extends Handler {

    public static final int STORE = 1;
    public static final int MEDIA = 2;

    /**
     * 发送存储器消息
     *
     * @param type      类型
     * @param documents 文档集合
     * @param listener  解析监听
     */
    public void send(int type, List<Media> documents, OnMediaResolverListener listener) {
        MediaMessageBody body = new MediaMessageBody();
        body.setType(type);
        body.setListener(listener);
        body.setDocuments(documents);
        Message msg = obtainMessage();
        msg.obj = body;
        msg.what = STORE;
        sendMessage(msg);
    }

    /**
     * 发送结果
     *
     * @param requestCode 请求代码
     * @param file        文件
     * @param listener    监听
     */
    public void send(int requestCode, File file, OnMediaProviderListener listener) {
        Message message = obtainMessage();
        message.obj = listener;
        message.what = MEDIA;
        Bundle bundle = new Bundle();
        bundle.putInt("requestCode", requestCode);
        bundle.putString("path", file.getAbsolutePath());
        message.setData(bundle);
        sendMessage(message);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case STORE:
                MediaMessageBody body = (MediaMessageBody) msg.obj;
                if (body.getListener() != null) {
                    body.getListener().onMediaResolver(body.getType(), body.getDocuments());
                }
                break;
            case MEDIA:
                OnMediaProviderListener listener = (OnMediaProviderListener) msg.obj;
                Bundle bundle = msg.getData();
                int requestCode = bundle.getInt("requestCode");
                String path = bundle.getString("path");
                if (listener!=null){
                    listener.onMediaProviderResult(requestCode,new File(path));
                }
                break;
        }
    }
}
