package androidx.io.core.video;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.io.core.core.IOProvider;
import androidx.io.core.core.UriProvider;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VideoProvider {

    public static final String TAG = VideoProvider.class.getSimpleName();
    /**
     * 选择视频文件
     */
    public static final int REQUEST_PICK = 13001;
    /**
     * 选择 - 缓存文件夹
     */
    public final static String DIRECTORY_PICK = "VideoPick";
    /**
     * 视频录制
     */
    public static final int REQUEST_RECORD = 13002;
    /**
     * 选择 - 缓存文件夹
     */
    public final static String DIRECTORY_RECORD = "VideoRecord";
    /**
     * 视频转换
     */
    public final static String DIRECTORY_TRANSFER = "VideoTransfer";

    private static String[] projection = new String[]{
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT
    };
    private Activity activity;
    private Fragment fragment;
    private Context context;
    /**
     * 线程池
     */
    private ExecutorService executor;
    /**
     * 结果处理Handler
     */
    private ResultHandler handler;

    /**
     * 构造Activity使用的视频提供者
     *
     * @param context
     */
    public VideoProvider(Context context) {
        this.context = context;
        this.executor = Executors.newFixedThreadPool(10);
        this.handler = new ResultHandler();
    }

    /**
     * 构造Activity使用的视频提供者
     *
     * @param activity
     */
    public VideoProvider(Activity activity) {
        this.activity = activity;
        this.executor = Executors.newFixedThreadPool(10);
        this.handler = new ResultHandler();
    }

    /**
     * 构造Fragment使用的视频提供者
     *
     * @param fragment
     */
    public VideoProvider(Fragment fragment) {
        this.fragment = fragment;
        this.executor = Executors.newFixedThreadPool(10);
        this.handler = new ResultHandler();
    }

    /**
     * 获取上下文
     *
     * @return
     */
    public Context getContext() {
        if (fragment != null) {
            return fragment.getContext();
        }
        if (activity != null) {
            return activity;
        }
        return context;
    }

    /**
     * 查询入口Uri
     *
     * @return
     */
    public static Uri getContentUri() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        }
        return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    }

    /**
     * 查询视频集合
     *
     * @param duration 视频时长
     * @param unit     时长单位
     * @param up       >=duration
     * @return
     */
    public static List<Video> query(Context context, long duration, TimeUnit unit, boolean up) {
        String selection = MediaStore.Video.Media.DURATION + (up ? " >= ?" : " <= ?");
        String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";
        String[] selectionArgs = new String[]{String.valueOf(TimeUnit.MILLISECONDS.convert(duration, unit))};
        return query(context, getContentUri(), selection, selectionArgs, sortOrder);
    }

    /**
     * 查询视频集合
     *
     * @param length 视频大小，单位byte
     * @param up     >=length
     * @return
     */
    public static List<Video> query(Context context, long length, boolean up) {
        String selection = MediaStore.Video.Media.SIZE + (up ? " >= ?" : " <= ?");
        String[] selectionArgs = new String[]{String.valueOf(length)};
        String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";
        return query(context, getContentUri(), selection, selectionArgs, sortOrder);
    }

    /**
     * 查询视频集合
     *
     * @param context       上下文
     * @param uri           视频URI
     * @param selection     条件表达式
     * @param selectionArgs 条件值
     * @param sortOrder     排序
     * @return
     */
    public static List<Video> query(Context context, Uri uri, String selection, String[] selectionArgs, String sortOrder) {
        List<Video> list = new ArrayList<>();
        try (Cursor cursor = context.getApplicationContext().getContentResolver().query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            int widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH);
            int heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int videoDuration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);
                int width = cursor.getInt(widthColumn);
                int height = cursor.getInt(heightColumn);
                Uri contentUri = ContentUris.withAppendedId(uri, id);
                list.add(new Video(id, contentUri, name, videoDuration, size, width, height));
            }
        }
        return list;
    }

    /**
     * 提炼时长
     *
     * @param file 视频文件
     * @return
     */
    public static long extractDuration(File file) {
        return extractDuration(file.getAbsolutePath());
    }

    /**
     * 提炼时长
     *
     * @param path 视频文件
     * @return
     */
    public static long extractDuration(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        return Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

    /**
     * 提炼时长
     *
     * @param context 上下文
     * @param uri     视频Uri
     * @return
     */
    public static long extractDuration(Context context, Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context.getContentResolver().openFileDescriptor(uri, "r").getFileDescriptor());
            return Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * 提炼宽高
     *
     * @param context 上下文
     * @param file    文件
     * @return
     */
    public static int[] extractSize(Context context, File file) {
        return extractSize(context, UriProvider.fromFile(context, file));
    }

    /**
     * 提炼宽高
     *
     * @param context 上下文
     * @param uri     视频Uri
     * @return
     */
    public static int[] extractSize(Context context, Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context.getContentResolver().openFileDescriptor(uri, "r").getFileDescriptor());
            int width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            int height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            return new int[]{width, height};
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new int[]{};
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
     * 转换为文件
     *
     * @param context 上下文
     * @param video   视频信息
     * @return
     */
    public static File transfer(Context context, Video video) {
        return transfer(context, video.getUri(), video.getName());
    }

    /**
     * 转换为文件
     *
     * @param context  上下文
     * @param uri      文件
     * @param fileName 文件名
     * @return
     */
    public static File transfer(Context context, Uri uri, String fileName) {
        File file = createFile(context, DIRECTORY_TRANSFER, fileName);
        if (!file.exists()) {
            try {
                ParcelFileDescriptor descriptor = context.getContentResolver().openFileDescriptor(uri, "r");
                FileDescriptor fileDescriptor = descriptor.getFileDescriptor();
                FileChannel inputChannel = new FileInputStream(fileDescriptor).getChannel();
                FileChannel outChannel = new FileOutputStream(file).getChannel();
                inputChannel.transferTo(0, inputChannel.size(), outChannel);
                return file;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 查询ID
     *
     * @param uri 文件
     * @return
     */
    public long queryId(Uri uri) {
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        while (cursor.moveToNext()) {
            return cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
        }
        return 0;
    }

    /**
     * 获取缩略图
     *
     * @param context 上下文
     * @param uri     文件
     * @return
     */
    public Bitmap loadThumbnail(Context context, Uri uri) {
        return loadThumbnail(context, uri, 640, 480);
    }

    /**
     * 获取缩略图
     *
     * @param context 上下文
     * @param uri     文件
     * @param width   宽度
     * @param height  高度
     * @return
     */
    public Bitmap loadThumbnail(Context context, Uri uri, int width, int height) {
        boolean isModify = width != -1 && height != -1;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return context.getContentResolver().loadThumbnail(uri, isModify ? Size.parseSize(width + "*" + height) : null, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long id = queryId(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outWidth = width;
        options.outHeight = height;
        return MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Video.Thumbnails.MINI_KIND, isModify ? options : null);
    }

    /**
     * 构建系统录制对象
     *
     * @param duration 时长限制，单位秒
     * @param quality  限制大小，单位byte
     * @param quality  视频录制的画质，1：高 0：低
     * @return
     */
    public Intent buildRecord(long duration, long size, int quality) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (duration != 0) {
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, duration);
        }
        if (size != 0) {
            intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, size);
        }
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, quality);
        return intent;
    }

    /**
     * 系统视频录制<br/>
     * 注意：对应页面必须调用{@link #onActivityResult(int, int, Intent, OnVideoProviderResultListener)} )}
     *
     */
    public void record() {
        record(0, 0, 1, REQUEST_RECORD);
    }

    /**
     * 系统视频录制<br/>
     * 注意：对应页面必须调用{@link #onActivityResult(int, int, Intent, OnVideoProviderResultListener)} )}
     *
     * @param requestCode 请求代码
     */
    public void record(int requestCode) {
        record(0, 0, 1, requestCode);
    }

    /**
     * 系统视频录制<br/>
     * 注意：对应页面必须调用{@link #onActivityResult(int, int, Intent, OnVideoProviderResultListener)} )}
     *
     * @param quality     视频录制的画质，1：高 0：低
     * @param requestCode 请求代码
     */
    public void record(int quality, int requestCode) {
        record(0, 0, quality, requestCode);
    }

    /**
     * 系统视频录制<br/>
     * 注意：对应页面必须调用{@link #onActivityResult(int, int, Intent, OnVideoProviderResultListener)} )}
     *
     * @param duration    时长限制，单位秒
     * @param quality     限制大小，单位byte
     * @param quality     视频录制的画质，1：高 0：低
     * @param requestCode 请求代码
     */
    public void record(long duration, long size, int quality, int requestCode) {
        if (fragment != null) {
            fragment.startActivityForResult(buildRecord(duration, size, quality), requestCode);
        }
        if (activity != null) {
            activity.startActivityForResult(buildRecord(duration, size, quality), requestCode);
        }
    }

    /**
     * 构建选择器Intent
     *
     * @return
     */
    public static Intent buildPick() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("video/*");
        return intent;
    }

    /**
     * 系统选择视频<br/>
     * 注意：对应页面必须调用{@link #onActivityResult(int, int, Intent, OnVideoProviderResultListener)} )}
     */
    public void pick() {
        if (activity != null) {
            activity.startActivityForResult(buildPick(), REQUEST_PICK);
        }
        if (fragment != null) {
            fragment.startActivityForResult(buildPick(), REQUEST_PICK);
        }
    }

    /**
     * 创建缓存文件
     *
     * @param context  上下文
     * @param dirName  文件夹名称
     * @param fileName 文件名称
     * @return
     */
    public static File createFile(Context context, String dirName, String fileName) {
        return IOProvider.createCacheFile(context, dirName, fileName);
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
     * 查询名称
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
    public void onActivityResult(int requestCode, int resultCode, Intent data, OnVideoProviderResultListener listener) {
        executor.execute(new ResultTask(requestCode, resultCode, data, listener));
    }

    private class ResultTask implements Runnable {

        private int resultCode;
        private int requestCode;
        private Intent data;
        private OnVideoProviderResultListener listener;

        public ResultTask(int requestCode, int resultCode, Intent data, OnVideoProviderResultListener listener) {
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.data = data;
            this.listener = listener;
        }

        @Override
        public void run() {
            if (resultCode == Activity.RESULT_OK && (requestCode == REQUEST_PICK)) {
                Uri uri = data.getData();
                String displayName = queryDisplayName(uri);
                File file = createFile(getContext(), DIRECTORY_PICK, displayName);
                if (!file.exists()) {
                    copy(uri, file);
                }
                sendResult(requestCode, file, listener);
            }
            if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_RECORD) {
                String displayName = queryDisplayName(data.getData());
                File file = createFile(getContext(), DIRECTORY_RECORD, displayName);
                copy(data.getData(), file);
                sendResult(requestCode, file, listener);
            }
        }
    }

    /**
     * 发送结果
     *
     * @param requestCode 请求代码
     * @param file        文件
     * @param listener    监听
     */
    private void sendResult(int requestCode, File file, OnVideoProviderResultListener listener) {
        Message message = handler.obtainMessage();
        message.obj = listener;
        Bundle bundle = new Bundle();
        bundle.putInt("requestCode", requestCode);
        bundle.putString("path", file.getAbsolutePath());
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private class ResultHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            OnVideoProviderResultListener listener = (OnVideoProviderResultListener) msg.obj;
            Bundle bundle = msg.getData();
            int requestCode = bundle.getInt("requestCode");
            String path = bundle.getString("path");
            setResult(requestCode, new File(path), listener);
        }
    }

    /**
     * 设置结果
     *
     * @param requestCode 代码
     * @param file        文件
     * @param listener    监听
     */
    protected void setResult(int requestCode, File file, OnVideoProviderResultListener listener) {
        if (listener != null) {
            listener.onVideoProviderResult(requestCode, file);
        }
    }

    public interface OnVideoProviderResultListener {

        /**
         * 意图结果处理
         *
         * @param requestCode 请求代码
         * @param file        缓存文件
         */
        void onVideoProviderResult(int requestCode, File file);

    }

}
