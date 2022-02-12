package androidx.io.core.media;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 媒体解析线程
 */
public class MediaResolverCommand implements Runnable {

    private Context context;
    private int type;
    private OnMediaCommandListener listener;

    public MediaResolverCommand(Context context, int type, OnMediaCommandListener listener) {
        this.context = context;
        this.type = type;
        this.listener = listener;
    }

    @Override
    public void run() {
        List<Media> documents = new ArrayList<>();
        if (type== MediaResolver.FILES){
            documents.addAll(MediaProvider.queryImages(context));
            documents.addAll(MediaProvider.queryVideo(context));
            documents.addAll(MediaProvider.queryAudio(context));
        }
        if (type== MediaResolver.IMAGE){
            documents.addAll(MediaProvider.queryImages(context));
        }
        if (type== MediaResolver.VIDEO){
            documents.addAll(MediaProvider.queryVideo(context));
        }
        if (type== MediaResolver.AUDIO){
            documents.addAll(MediaProvider.queryAudio(context));
        }
        if (listener != null) {
            listener.onMediaCommand(type, documents);
        }
    }

}
