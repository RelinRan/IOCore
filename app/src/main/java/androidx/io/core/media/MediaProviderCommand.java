package androidx.io.core.media;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.io.core.core.ImageProvider;
import androidx.io.core.core.UriProvider;

import java.io.File;

public class MediaProviderCommand implements Runnable {

    private MediaProvider provider;
    private int resultCode;
    private int requestCode;
    private Intent data;
    private OnMediaProviderListener listener;

    public MediaProviderCommand(MediaProvider provider, int requestCode, int resultCode, Intent data, OnMediaProviderListener listener) {
        this.provider = provider;
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.data = data;
        this.listener = listener;
    }

    @Override
    public void run() {
        if (resultCode == Activity.RESULT_OK && (requestCode == MediaProvider.REQUEST_PICK | requestCode == MediaProvider.REQUEST_GET_CONTENT)) {
            Uri uri = data.getData();
            String displayName = provider.queryDisplayName(uri);
            File file = provider.createFile(MediaProvider.DIRECTORY_PICK, displayName);
            if (!file.exists()) {
                provider.copy(uri, file);
            }
            String mimeType = provider.getContext().getContentResolver().getType(uri);
            boolean isImage = mimeType.startsWith("image");
            if (isImage) {
                ImageProvider.correct(file);
            }
            if (isImage && provider.isCrop()) {
                provider.crop(provider.getOptions().data(UriProvider.fromFile(provider.getContext(), file)).output(provider.createImageUri()));
            } else {
                if (provider.isCompress()){
                    file = ImageProvider.compress(provider.getContext(), file, provider.getMaxSize());
                }
                provider.getMessenger().send(requestCode, file, listener);
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == MediaProvider.REQUEST_CAPTURE) {
            String displayName = provider.queryDisplayName(provider.getOptions().output());
            File file = provider.createFile(MediaProvider.DIRECTORY_CAPTURE, displayName);
            provider.copy(provider.getOptions().output(), file);
            ImageProvider.correct(file);
            if (provider.isCrop()) {
                provider.crop(provider.getOptions().data(UriProvider.fromFile(provider.getContext(), file)).output(provider.createImageUri()));
            } else {
                if (provider.isCompress()){
                    file = ImageProvider.compress(provider.getContext(), file, provider.getMaxSize());
                }
                provider.getMessenger().send(requestCode, file, listener);
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == MediaProvider.REQUEST_CROP) {
            String displayName = provider.queryDisplayName(provider.getOptions().output());
            File file = provider.createFile(MediaProvider.DIRECTORY_CROP, displayName);
            provider.copy(provider.getOptions().output(), file);
            provider.delete(provider.getOptions().output());
            if (provider.isCompress()){
                file = ImageProvider.compress(provider.getContext(), file, provider.getMaxSize());
            }
            provider.getMessenger().send(requestCode, file, listener);
        }
    }
}
