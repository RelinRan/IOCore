package androidx.io.core.media;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.io.core.core.ImageProvider;
import androidx.io.core.video.VideoProvider;

import java.io.File;

public class Thumbnail {

    /**
     * 缩略图文件夹
     *
     * @param context 上下文
     * @return
     */
    public static File thumbnailDir(Context context) {
        File dir = new File(context.getExternalCacheDir(), "Thumbnail");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 添加缓存
     *
     * @param context   上下文
     * @param file      原文件
     * @param thumbnail 缩略图Bitmap
     */
    public static File addThumbnail(Context context, File file, Bitmap thumbnail) {
        File thumbnailFile = getThumbnail(context, file);
        ImageProvider.toFile(thumbnail, thumbnailFile.getAbsolutePath());
        return thumbnailFile;
    }

    /**
     * 获取缓存文件
     *
     * @param context 上下文
     * @param file    原文件
     * @return
     */
    public static File getThumbnail(Context context, File file) {
        String name = file.getName();
        name = name.replace(".mp3",".png");
        return new File(thumbnailDir(context), name);
    }

    /**
     * 获取图片缩略图
     *
     * @param file   图片
     * @param width  缩略图宽度
     * @param height 缩略图高度
     * @return
     */
    public static Bitmap createImageThumbnail(File file, int width, int height) {
        return ImageProvider.createImageThumbnail(file.getAbsolutePath(), width, height);
    }

    /**
     * 获取视频缩略图
     *
     * @param file   视频
     * @param width  缩略图宽度
     * @param height 缩略图高度
     * @return
     */
    public static Bitmap createVideoThumbnail(File file, int width, int height) {
        return VideoProvider.createVideoThumbnail(file.getAbsolutePath(), width, height);
    }

    /**
     * 获取音频专辑缩略图
     *
     * @param file   视频
     * @param width  缩略图宽度
     * @param height 缩略图高度
     * @return
     */
    public static Bitmap createAudioThumbnail(File file, int width, int height) {
        return MediaProvider.createAudioThumbnail(file.getAbsolutePath(), width, height);
    }

}
