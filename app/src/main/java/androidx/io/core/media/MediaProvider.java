package androidx.io.core.media;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.io.core.R;
import androidx.io.core.app.TBSActivity;
import androidx.io.core.core.IOProvider;
import androidx.io.core.core.ImageProvider;
import androidx.io.core.core.UriProvider;
import androidx.io.core.video.VideoProvider;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaProvider {

    /**
     * 标识
     */
    public static String TAG = MediaProvider.class.getSimpleName();
    /**
     * 选择文件
     */
    public final static int REQUEST_PICK = 12001;
    /**
     * 选择 - 缓存文件夹
     */
    public final static String DIRECTORY_PICK = "Pick";
    /**
     * 系统拍照
     */
    public final static int REQUEST_CAPTURE = 12002;
    /**
     * 拍照 - 缓存文件夹
     */
    public final static String DIRECTORY_CAPTURE = "Capture";
    /**
     * 剪切
     */
    public final static int REQUEST_CROP = 12004;
    /**
     * 剪切 - 缓存文件夹
     */
    public final static String DIRECTORY_CROP = "Crop";
    /**
     * 最近操作过的文件
     */
    public final static int REQUEST_GET_CONTENT = 12005;
    /**
     * 文档 - 缓存文件夹
     */
    public final static String DIRECTORY_CONTENT = "Document";
    /**
     * 压缩文件夹
     */
    public final static String DIRECTORY_COMPRESS = "Compress";

    /**
     * 是否剪切
     */
    private boolean crop;
    /**
     * 是否压缩
     */
    private boolean compress = true;
    /**
     * 文件最大值
     */
    private long maxSize = 1024 * 1024;
    /**
     * 活动页面
     */
    private Activity activity;
    /**
     * 碎片页面
     */
    private Fragment fragment;
    /**
     * 剪切参数
     */
    private MediaOptions options;
    /**
     * 线程池
     */
    private ExecutorService executor;
    /**
     * 结果处理Handler
     */
    private MediaMessenger messenger;

    /**
     * 获取上下文
     *
     * @return
     */
    public Context getContext() {
        if (fragment != null) {
            return fragment.getContext();
        }
        return activity;
    }

    /**
     * 媒体提供者
     *
     * @param activity 活动页面
     */
    public MediaProvider(Activity activity) {
        this.activity = activity;
        this.options = new MediaOptions();
        this.executor = Executors.newFixedThreadPool(10);
        this.messenger = new MediaMessenger();
    }

    /**
     * 媒体提供者
     *
     * @param fragment 碎片页面
     */
    public MediaProvider(Fragment fragment) {
        this.fragment = fragment;
        this.options = new MediaOptions();
        this.executor = Executors.newFixedThreadPool(10);
        this.messenger = new MediaMessenger();
    }

    /**
     * 消息传递者
     *
     * @return
     */
    public MediaMessenger getMessenger() {
        return messenger;
    }

    /**
     * 设置剪切参数
     *
     * @param options
     */
    public void setOptions(MediaOptions options) {
        this.options = options;
    }

    /**
     * 获取剪切参数
     *
     * @return
     */
    public MediaOptions getOptions() {
        return options;
    }

    /**
     * 设置是否剪切
     *
     * @param crop
     */
    public void setCrop(boolean crop) {
        this.crop = crop;
    }

    /**
     * 是否剪切
     *
     * @return
     */
    public boolean isCrop() {
        return crop;
    }

    /**
     * 设置是否压缩
     *
     * @param compress
     */
    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    /**
     * 是否压缩
     *
     * @return
     */
    public boolean isCompress() {
        return compress;
    }

    /**
     * 设置文件最大值
     *
     * @param maxSize
     */
    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * 获取文件最大值
     *
     * @return
     */
    public long getMaxSize() {
        return maxSize;
    }

    /**
     * 选择文件
     *
     * @param mineType 媒体类型，例如：image/*、video/*、audio/*
     */
    public void pick(String mineType) {
        if (activity != null) {
            activity.startActivityForResult(buildPick(mineType), REQUEST_PICK);
        }
        if (fragment != null) {
            fragment.startActivityForResult(buildPick(mineType), REQUEST_PICK);
        }
    }

    /**
     * 系统拍照
     */
    public void capture() {
        if (activity != null) {
            activity.startActivityForResult(buildCapture(getOptions().output(createImageUri()).output()), REQUEST_CAPTURE);
        }
        if (fragment != null) {
            fragment.startActivityForResult(buildCapture(getOptions().output(createImageUri()).output()), REQUEST_CAPTURE);
        }
    }

    /**
     * 剪切图片
     *
     * @param options 剪切参数
     */
    public void crop(MediaOptions options) {
        if (activity != null) {
            activity.startActivityForResult(buildCrop(getContext(), options), REQUEST_CROP);
        }
        if (fragment != null) {
            fragment.startActivityForResult(buildCrop(getContext(), options), REQUEST_CROP);
        }
    }

    /**
     * 构建选择器Intent
     *
     * @param mineType 媒体类型 例如:image/*
     * @return
     */
    public static Intent buildPick(String mineType) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType(mineType);
        Log.i(TAG, "buildPick mineType: " + mineType);
        return intent;
    }

    /**
     * 构建系统拍照Intent
     *
     * @param output 图片输出
     * @return
     */
    public static Intent buildCapture(Uri output) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
        Log.i(TAG, "buildCapture output: " + output.toString());
        return intent;
    }

    /**
     * 构建剪切图片Intent
     *
     * @param context 上下文
     * @param options 图片参数
     * @return
     */
    public static Intent buildCrop(Context context, @NonNull MediaOptions options) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        String type = context.getContentResolver().getType(options.data());
        intent.setDataAndType(options.data(), type);
        intent.putExtra("crop", String.valueOf(options.isCrop()));
        intent.putExtra("aspectX", options.aspectX());
        intent.putExtra("aspectY", options.aspectY());
        intent.putExtra("outputX", options.outputX());
        intent.putExtra("outputY", options.outputY());
        intent.putExtra("return-data", String.valueOf(options.isReturnData()));
        intent.putExtra("circleCrop", String.valueOf(options.isCircleCrop()));
        intent.putExtra("noFaceDetection", String.valueOf(options.isNoFaceDetection()));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, options.output());
        Log.i(TAG, options.toString());
        return intent;
    }

    /**
     * 获取文件
     *
     * @param mimeType 文件类型，例如：application/pdf
     */
    public void get(String mimeType) {
        if (activity != null) {
            activity.startActivityForResult(buildGet(mimeType), REQUEST_GET_CONTENT);
        }
        if (fragment != null) {
            fragment.startActivityForResult(buildGet(mimeType), REQUEST_GET_CONTENT);
        }
    }

    /**
     * 构建获取文件Intent
     *
     * @param mimeType 文件类型，例如：application/pdf
     * @return
     */
    public static Intent buildGet(String mimeType) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setType(mimeType);
        return intent;
    }

    /**
     * 打开文档
     *
     * @param path 文件
     */
    public void open(String path) {
        if (activity != null) {
            activity.startActivity(buildOpen(getContext(), path));
        }
        if (fragment != null) {
            fragment.startActivity(buildOpen(getContext(), path));
        }
    }

    /**
     * 构建打开文档意图
     *
     * @param context 上下文
     * @param path    文件路径
     * @return
     */
    public static Intent buildOpen(Context context, String path) {
        String mimetype = IOProvider.getMimeType(path);
        Log.i(TAG, "buildOpen mimetype: " + mimetype);
        if (mimetype.startsWith("video")) {
            Intent intent = new Intent("com.tencent.smtt.tbs.video.PLAY");
            Bundle bundle = new Bundle();
            bundle.putString("videoUrl", path);
            intent.putExtra("extraData", bundle);
            return intent;
        }
        boolean isHttp = path.toLowerCase().startsWith("http");
        Intent intent = new Intent(context, TBSActivity.class);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(isHttp ? "url" : "filePath", path);
        return intent;
    }

    /**
     * 获取文档文件夹
     *
     * @param context 上下文
     * @param name    文件夹名,可选预选和自定义
     *                {@link #DIRECTORY_PICK}
     *                {@link #DIRECTORY_CAPTURE}
     *                {@link #DIRECTORY_CROP}
     *                {@link #DIRECTORY_CONTENT}
     * @return
     */
    public static File getMediaDirectory(Context context, String name) {
        return IOProvider.getCacheDir(context, name);
    }

    /**
     * 清除缓存文件
     *
     * @param context 上下文
     * @param dirName 文件夹名称
     * @return
     */
    public static void clear(Context context, String dirName) {
        IOProvider.deleteDir(IOProvider.getCacheDir(context, dirName));
    }

    /**
     * 构建剪切Uri
     *
     * @return
     */
    public Uri createImageUri() {
        return UriProvider.tempImageUri(getContext());
    }

    /**
     * 创建缓存文件
     *
     * @param dirName  文件夹名称
     * @param fileName 文件名称
     * @return
     */
    public File createFile(String dirName, String fileName) {
        return IOProvider.createCacheFile(getContext(), dirName, fileName);
    }

    /**
     * 拷贝文件
     *
     * @param from 来源
     * @param to   目标
     */
    public void copy(Uri from, File to) {
        UriProvider.copy(getContext(), from, to);
    }

    /**
     * 删除文件
     *
     * @param uri 文件
     */
    public void delete(Uri uri) {
        UriProvider.delete(getContext(), uri);
    }

    /**
     * 查询显示名称
     *
     * @param uri 文件地址
     * @return
     */
    public String queryDisplayName(Uri uri) {
        return UriProvider.queryDisplayName(getContext(), uri);
    }

    /**
     * 处理活动页面结果
     *
     * @param requestCode 请求代码
     * @param resultCode  结果代码
     * @param data        数据
     * @param listener    监听
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data, OnMediaProviderListener listener) {
        executor.execute(new MediaProviderCommand(this, requestCode, resultCode, data, listener));
    }

    /**
     * 内容Uri - [图片 + 音频 + 视频]
     *
     * @return
     */
    public static Uri getFilesExternalUri() {
        return MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
    }

    /**
     * 图片Uri
     *
     * @return
     */
    public static Uri getImagesExternalUri() {
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    /**
     * 视频Uri
     *
     * @return
     */
    public static Uri getVideoExternalUri() {
        return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    }

    /**
     * 音频Uri
     *
     * @return
     */
    public static Uri getAudioExternalUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    /**
     * 查询视频集合
     *
     * @param context 上下文
     * @return
     */
    public static List<Media> queryFiles(Context context) {
        String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";
        List<Media> list = new ArrayList<>();
        Cursor cursor = context.getApplicationContext().getContentResolver().query(getFilesExternalUri(),
                null, null, null, sortOrder);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
            String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
            long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED));
            int size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE));
            Media document = new Media();
            document.setType(type);
            document.setId(id);
            document.setName(name);
            document.setData(data);
            document.setDate(date);
            document.setSize(size);
            if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                setVideoThumbnail(context, document);
            }
            if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                setImageThumbnail(context, document);
            }
            if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO) {
                Bitmap bitmap = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.ARTIST));
                    long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DOCUMENT_ID));
                    if (artist != null && !artist.equals("<unknown>")) {
                        try {
                            Uri audioUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId);
                            bitmap = context.getContentResolver().loadThumbnail(audioUri, new Size(160, 160), new CancellationSignal());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    String art = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                    bitmap = BitmapFactory.decodeFile(art);
                }
                if (bitmap != null) {
                    File file = Thumbnail.addThumbnail(context, new File(data), bitmap);
                    document.setThumbnail(file);
                }
            }
            list.add(document);
        }
        return list;
    }

    /**
     * 查询图片
     *
     * @param context 上下文
     * @return
     */
    public static List<Media> queryImages(Context context) {
        String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";
        List<Media> list = new ArrayList<>();
        Cursor cursor = context.getApplicationContext().getContentResolver().query(getImagesExternalUri(),
                null, null, null, sortOrder);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME));
            String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
            long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_MODIFIED));
            int size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE));
            int width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.WIDTH));
            int height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.HEIGHT));
            Media document = new Media();
            document.setType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
            document.setId(id);
            document.setName(name);
            document.setData(data);
            document.setDate(date);
            document.setSize(size);
            document.setWidth(width);
            document.setHeight(height);
            setImageThumbnail(context, document);
            list.add(document);
        }
        return list;
    }

    /**
     * 查询视频
     *
     * @param context 上下文
     * @return
     */
    public static List<Media> queryVideo(Context context) {
        String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";
        List<Media> list = new ArrayList<>();
        Cursor cursor = context.getApplicationContext().getContentResolver().query(getVideoExternalUri(),
                null, null, null, sortOrder);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DISPLAY_NAME));
            String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA));
            long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATE_MODIFIED));
            int size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.SIZE));
            int width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.WIDTH));
            int height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.HEIGHT));
            long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION));
            Media document = new Media();
            document.setType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
            document.setId(id);
            document.setName(name);
            document.setData(data);
            document.setDate(date);
            document.setSize(size);
            document.setWidth(width);
            document.setHeight(height);
            document.setDuration(duration);
            setVideoThumbnail(context, document);
            list.add(document);
        }
        return list;
    }

    /**
     * 查询音频
     *
     * @param context 上下文
     * @return
     */
    public static List<Media> queryAudio(Context context) {
        String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";
        List<Media> list = new ArrayList<>();
        Cursor cursor = context.getApplicationContext().getContentResolver().query(getAudioExternalUri(),
                null, null, null, sortOrder);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME));
            String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA));
            long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_MODIFIED));
            int size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.SIZE));
            long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION));
            Media document = new Media();
            document.setType(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO);
            document.setId(id);
            document.setName(name);
            document.setData(data);
            document.setDate(date);
            document.setSize(size);
            document.setDuration(duration);
            //音频特有字段
            long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID));
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM));
            document.setAlbumId(albumId);
            document.setArtist(artist);
            document.setAlbum(album);
            Bitmap bitmap = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                if (artist != null && !artist.equals("<unknown>")) {
                    try {
                        Uri audioUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId);
                        bitmap = context.getContentResolver().loadThumbnail(audioUri, new Size(160, 160), new CancellationSignal());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                String art = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                bitmap = BitmapFactory.decodeFile(art);
            }
            if (bitmap != null) {
                File file = Thumbnail.addThumbnail(context, new File(data), bitmap);
                document.setThumbnail(file);
            }

            list.add(document);
        }
        return list;
    }

    /**
     * 设置缩略图
     *
     * @param context  上下文
     * @param document 文档
     */
    public static void setVideoThumbnail(Context context, Media document) {
        File file = new File(document.getData());
        File thumbnail = Thumbnail.getThumbnail(context, file);
        if (thumbnail == null || !thumbnail.exists() || (thumbnail.exists() && thumbnail.length() == 0)) {
            Bitmap bitmap = Thumbnail.createVideoThumbnail(file, 160, 160);
            if (bitmap!=null){
                thumbnail = Thumbnail.addThumbnail(context, file, bitmap);
            }
        }
        document.setThumbnail(thumbnail);
    }

    /**
     * 设置缩略图和时长
     *
     * @param context  上下文
     * @param document 文档
     */
    public static void setVideoThumbnailDuration(Context context, Media document) {
        File file = new File(document.getData());
        File thumbnail = Thumbnail.getThumbnail(context, file);
        if (thumbnail == null || !thumbnail.exists() || (thumbnail.exists() && thumbnail.length() == 0)) {
            Bitmap bitmap = Thumbnail.createVideoThumbnail(file, 160, 160);
            if (bitmap!=null){
                thumbnail = Thumbnail.addThumbnail(context, file,bitmap);
            }
        }
        document.setThumbnail(thumbnail);
        document.setDuration(VideoProvider.extractDuration(file));
    }

    /**
     * 设置图片缩略图
     *
     * @param context  上下文
     * @param document 文档
     */
    public static void setImageThumbnail(Context context, Media document) {
        File file = new File(document.getData());
        File thumbnail = Thumbnail.getThumbnail(context, file);
        if (thumbnail == null || !thumbnail.exists() || (thumbnail.exists() && thumbnail.length() == 0)) {
            Bitmap bitmap = Thumbnail.createImageThumbnail(file, 160, 160);
            if (bitmap!=null){
                thumbnail = Thumbnail.addThumbnail(context, file,bitmap);
            }
        }
        document.setThumbnail(thumbnail);
    }

    /**
     * 设置缩略图
     *
     * @param context  上下文
     * @param document 文档
     */
    public static void setAudioThumbnail(Context context, Media document) {
        File file = new File(document.getData());
        File thumbnail = Thumbnail.getThumbnail(context, file);
        if (thumbnail == null || !thumbnail.exists() || (thumbnail.exists() && thumbnail.length() == 0)) {
            Bitmap bitmap = Thumbnail.createAudioThumbnail(file, 160, 160);
            if (bitmap != null) {
                thumbnail = Thumbnail.addThumbnail(context, file, bitmap);
            }
        }
        document.setThumbnail(thumbnail);
    }

    /**
     * 获取文件Uri
     *
     * @param id 文件Id
     * @return
     */
    public static Uri getFilesExternalUri(int id) {
        return ContentUris.withAppendedId(getFilesExternalUri(), id);
    }

    /**
     * 获取图片Uri
     *
     * @param id 文件Id
     * @return
     */
    public static Uri getImagesExternalUri(int id) {
        return ContentUris.withAppendedId(getImagesExternalUri(), id);
    }

    /**
     * 获取音频Uri
     *
     * @param id 文件Id
     * @return
     */
    public static Uri getAudioExternalUri(int id) {
        return ContentUris.withAppendedId(getAudioExternalUri(), id);
    }

    /**
     * 获取视频Uri
     *
     * @param id 文件Id
     * @return
     */
    public static Uri getVideoExternalUri(int id) {
        return ContentUris.withAppendedId(getVideoExternalUri(), id);
    }

    /**
     * 创建视频缩略图
     *
     * @param path   视频文件
     * @param width  宽度
     * @param height 高度
     * @return
     */
    public static Bitmap createVideoThumbnail(String path, int width, int height) {
        Bitmap bitmap = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            try {
                bitmap = ThumbnailUtils.createVideoThumbnail(new File(path), new Size(width, height), new CancellationSignal());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
        }
        return ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
    }

    /**
     * 创建图片缩略图
     *
     * @param path   图片文件
     * @param width  宽度
     * @param height 高度
     * @return
     */
    public static Bitmap createImageThumbnail(String path, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    /**
     * 创建图片缩略图
     *
     * @param path   图片文件
     * @param width  宽度
     * @param height 高度
     * @return
     */
    public static Bitmap createAudioThumbnail(String path, int width, int height) {
        Bitmap bitmap = null;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            byte[] data = retriever.getEmbeddedPicture();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (bitmap != null) {
                return ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 搜索文档
     *
     * @param data     数据源
     * @param keywords 搜索关键字
     * @return
     */
    public static List<Media> search(List<Media> data, String keywords) {
        List<Media> documents = new ArrayList<>();
        for (int i = 0; i < (data == null ? 0 : data.size()); i++) {
            Media item = data.get(i);
            if (item.getName().contains(keywords)) {
                item.setKeywords(keywords);
                documents.add(item);
            }
        }
        return documents;
    }

    /**
     * 获取文件后缀
     *
     * @param file 文件
     * @return
     */
    public static String getSuffix(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return "";
        }
        String fileName = file.getName();
        if (fileName.equals("") || fileName.endsWith(".")) {
            return null;
        }
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1).toLowerCase(Locale.US);
        } else {
            return "";
        }
    }

    /**
     * 是否视频
     *
     * @param document 文档
     * @return
     */
    public static boolean isVideo(Media document) {
        return isVideo(new File(document.getData()));
    }

    /**
     * 是否视频
     *
     * @param file 文件
     * @return
     */
    public static boolean isVideo(File file) {
        return isVideo(getSuffix(file));
    }

    /**
     * 是否视频
     *
     * @param suffix 文件后缀
     * @return
     */
    public static boolean isVideo(String suffix) {
        String lower = suffix.toLowerCase();
        return lower.equals("mp4") || lower.equals("mpeg") || lower.equals("rmvb") || lower.equals("3gp");
    }

    /**
     * 是否图片
     *
     * @param document 文档
     * @return
     */
    public static boolean isPicture(Media document) {
        return isPicture(new File(document.getData()));
    }

    /**
     * 是否图片
     *
     * @param file 文件
     * @return
     */
    public static boolean isPicture(File file) {
        return isPicture(getSuffix(file));
    }

    /**
     * 是否图片
     *
     * @param suffix 文件后缀
     * @return
     */
    public static boolean isPicture(String suffix) {
        String lower = suffix.toLowerCase();
        return lower.equals("jpg") || lower.equals("jpeg") || lower.equals("png") || lower.equals("webp");
    }

    /**
     * 是否压缩文件
     *
     * @param suffix 文件后缀
     * @return
     */
    public static boolean isCompression(String suffix) {
        String lower = suffix.toLowerCase();
        return lower.equals("zip") || lower.equals("tar") || lower.equals("rar") || lower.equals("jar") || lower.equals("z") || lower.equals("7z");
    }

    /**
     * 是否world文件
     *
     * @param suffix 文件后缀
     * @return
     */
    public static boolean isDoc(String suffix) {
        String lower = suffix.toLowerCase();
        return lower.equals("doc") || lower.equals("docx");
    }

    /**
     * 是否PPT文件
     *
     * @param suffix 文件后缀
     * @return
     */
    public static boolean isPPT(String suffix) {
        String lower = suffix.toLowerCase();
        return lower.equals("ppt") || lower.equals("pptx");
    }

    /**
     * 是否表格文件
     *
     * @param suffix 文件后缀
     * @return
     */
    public static boolean isXLS(String suffix) {
        String lower = suffix.toLowerCase();
        return lower.equals("xls") || lower.equals("xlsx");
    }

    /**
     * 是否PPT文件
     *
     * @param suffix 文件后缀
     * @return
     */
    public static boolean isPDF(String suffix) {
        String lower = suffix.toLowerCase();
        return lower.equals("pdf");
    }

    /**
     * 是否bat文件
     *
     * @param suffix 文件后缀
     * @return
     */
    public static boolean isBat(String suffix) {
        String lower = suffix.toLowerCase();
        return lower.equals("bat");
    }

    /**
     * 是否数据库文件
     *
     * @param suffix 文件后缀
     * @return
     */
    public static boolean isDB(String suffix) {
        String lower = suffix.toLowerCase();
        return lower.equals("db");
    }

    /**
     * 是否TEXT文件
     *
     * @param suffix 文件后缀
     * @return
     */
    public static boolean isTXT(String suffix) {
        String lower = suffix.toLowerCase();
        return lower.equals("txt");
    }

    /**
     * 是否网页文件
     *
     * @param suffix 文件后缀
     * @return
     */
    public static boolean isHtml(String suffix) {
        String lower = suffix.toLowerCase();
        return lower.equals("html") || lower.equals("htm") || lower.equals("shtml") || lower.equals("asp") || lower.equals("aspx") || lower.equals("jsp") || lower.equals("php");
    }

    /**
     * 是否JSON文件
     *
     * @param suffix 文件后缀
     * @return
     */
    public static boolean isJson(String suffix) {
        String lower = suffix.toLowerCase();
        return lower.equals("json");
    }

    /**
     * 是否XML文件
     *
     * @param suffix 文件后缀
     * @return
     */
    public static boolean isXml(String suffix) {
        String lower = suffix.toLowerCase();
        return lower.equals("xml");
    }

    /**
     * 是否联系人文件
     *
     * @param suffix 文件后缀
     * @return
     */
    public static boolean isContact(String suffix) {
        String lower = suffix.toLowerCase();
        return lower.endsWith("vcf") || lower.endsWith("csv");
    }

    /**
     * 是否音频
     *
     * @param file 文件
     * @return
     */
    public static boolean isAudio(File file) {
        return isAudio(getSuffix(file));
    }

    /**
     * 是否音频
     *
     * @param suffix 文件后缀
     * @return
     */
    public static boolean isAudio(String suffix) {
        String lower = suffix.toLowerCase();
        return lower.equals("mp3") || lower.equals("wav") || lower.equals("mid") || lower.equals("cda") || lower.equals("aif") || lower.equals("aiff") || lower.equals("wma") || lower.equals("ra") || lower.equals("vqf") || lower.equals("ape") || lower.equals("mp3") || lower.equals("acm");
    }

    /**
     * 获取图标
     *
     * @param context 上下文
     * @param file    文件
     * @return
     */
    public static Bitmap getIcon(Context context, File file) {
        Resources resources = context.getResources();
        if (file.isDirectory()) {
            return BitmapFactory.decodeResource(resources, R.mipmap.io_core_dir);
        } else {
            String suffix = IOProvider.getSuffix(file);
            if (isPicture(suffix)) {
                File thumbnail = Thumbnail.getThumbnail(context, file);
                if (thumbnail.exists() && thumbnail.length() > 0) {
                    return BitmapFactory.decodeFile(thumbnail.getAbsolutePath());
                } else {
                    Bitmap bitmap = ImageProvider.createImageThumbnail(file.getAbsolutePath(), 160, 160);
                    Thumbnail.addThumbnail(context, file, bitmap);
                    return bitmap;
                }
            }
            if (isVideo(suffix)) {
                File thumbnail = Thumbnail.getThumbnail(context, file);
                if (thumbnail.exists() && thumbnail.length() > 0) {
                    return BitmapFactory.decodeFile(thumbnail.getAbsolutePath());
                } else {
                    Bitmap bitmap = VideoProvider.createVideoThumbnail(file.getAbsolutePath(), 160, 160);
                    Thumbnail.addThumbnail(context, file, bitmap);
                    return bitmap;
                }
            }
            if (isDoc(suffix)) {
                return BitmapFactory.decodeResource(resources, R.mipmap.io_core_doc);
            }
            if (isPPT(suffix)) {
                return BitmapFactory.decodeResource(resources, R.mipmap.io_core_ppt);
            }
            if (isPPT(suffix)) {
                return BitmapFactory.decodeResource(resources, R.mipmap.io_core_bat);
            }
            if (isXLS(suffix)) {
                return BitmapFactory.decodeResource(resources, R.mipmap.io_core_xls);
            }
            if (isPDF(suffix)) {
                return BitmapFactory.decodeResource(resources, R.mipmap.io_core_pdf);
            }
            if (isDB(suffix)) {
                return BitmapFactory.decodeResource(resources, R.mipmap.io_core_db);
            }
            if (isBat(suffix)) {
                return BitmapFactory.decodeResource(resources, R.mipmap.io_core_bat);
            }
            if (isTXT(suffix)) {
                return BitmapFactory.decodeResource(resources, R.mipmap.io_core_txt);
            }
            if (isHtml(suffix)) {
                return BitmapFactory.decodeResource(resources, R.mipmap.io_core_html);
            }
            if (isJson(suffix)) {
                return BitmapFactory.decodeResource(resources, R.mipmap.io_core_json);
            }
            if (isXml(suffix)) {
                return BitmapFactory.decodeResource(resources, R.mipmap.io_core_xml);
            }
            if (isContact(suffix)) {
                return BitmapFactory.decodeResource(resources, R.mipmap.io_core_vcf);
            }
            if (isCompression(suffix)) {
                return BitmapFactory.decodeResource(resources, R.mipmap.io_core_zip);
            }
            if (isAudio(suffix)) {
                return BitmapFactory.decodeResource(resources, R.mipmap.io_core_music);
            }
            return BitmapFactory.decodeResource(resources, R.mipmap.io_core_none);
        }
    }



}

