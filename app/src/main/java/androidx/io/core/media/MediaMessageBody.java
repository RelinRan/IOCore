package androidx.io.core.media;

import java.util.List;

public class MediaMessageBody {

    private OnMediaResolverListener listener;
    private List<Media> documents;
    private int position;
    private int type;

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public OnMediaResolverListener getListener() {
        return listener;
    }

    public void setListener(OnMediaResolverListener listener) {
        this.listener = listener;
    }

    public List<Media> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Media> documents) {
        this.documents = documents;
    }
}
