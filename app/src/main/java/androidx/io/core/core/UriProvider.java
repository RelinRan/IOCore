package androidx.io.core.core;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Uri提供者
 */
public class UriProvider {

    /**
     * 日志标识
     */
    public static String TAG = UriProvider.class.getSimpleName();
    /**
     * 提供者文件夹
     */
    public final static String DIRECTORY_PROVIDER = "Provider";
    /**
     * 图像
     */
    public static final String DIRECTORY_IMAGE = "Images";
    /**
     * 图片
     */
    public static final String DIRECTORY_PICTURE = "Pictures";
    /**
     * 文档
     */
    public static final String DIRECTORY_DOCUMENT = "Documents";

    /**
     * 内存卡是否挂载
     *
     * @return
     */
    public static boolean isMounted() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }
        return true;
    }

    /**
     * 是否可写
     *
     * @return
     */
    public static boolean islStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * 是否可读
     *
     * @return
     */
    public static boolean isStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * 获取缓存文件夹
     *
     * @param context 上下文对象
     * @param dirName 文件夹名
     * @return
     */
    public static File getCacheDir(Context context, String dirName) {
        File directory = new File(context.getExternalCacheDir(), dirName);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    /**
     * 获取挂载的缓存文件夹
     *
     * @param context 上下文
     * @param name    文件夹名
     * @return
     */
    public static File getFilesDir(Context context, String name) {
        File directory = context.getExternalFilesDir(name);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    /**
     * 删除缓存文件夹
     *
     * @param context 上下文对象
     * @param name    文件夹名
     */
    public static void deleteCacheDir(Context context, String name) {
        IOProvider.deleteDir(new File(context.getExternalCacheDir(), name));
    }

    /**
     * 删除缓存文件夹
     *
     * @param context 上下文
     * @param name    文件夹名
     */
    public static void deleteFilesDir(Context context, String name) {
        IOProvider.deleteDir(context.getExternalFilesDir(name));
    }

    /**
     * 构建内容提供者Uri
     *
     * @param context 上下文对象
     * @param file    文件
     * @return
     */
    public static Uri fromFile(Context context, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authority = context.getApplicationContext().getPackageName() + ".fileProvider";
            Log.i(TAG, "fromFile authority: " + authority);
            uri = FileProvider.getUriForFile(context, authority, file);
        } else {
            uri = Uri.fromFile(file);
        }
        Log.i(TAG, "fromFile uri: " + uri.toString());
        return uri;
    }

    /**
     * 获取文件类型
     *
     * @param context   上下文对象
     * @param file      文件
     * @param authority 权限
     * @return
     */
    public static String getMimeType(Context context, File file, @NonNull String authority) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, authority, file);
        } else {
            uri = Uri.fromFile(file);
        }
        ContentResolver resolver = context.getContentResolver();
        return resolver.getType(uri);
    }

    /**
     * 获取文件类型
     *
     * @param context 上下文对象
     * @param uri     URI
     * @return
     */
    public static String getMimeType(Context context, Uri uri) {
        return context.getContentResolver().getType(uri);
    }

    /**
     * 拷贝文件
     *
     * @param context 上下文对象
     * @param uri  资源Uri
     * @param target  输出路径
     */
    public static void copy(Context context, Uri uri, File target) {
        FileChannel oc = null;
        FileChannel ic = null;
        try {
            oc = new FileOutputStream(target).getChannel();
            ParcelFileDescriptor descriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            ic = new FileInputStream(descriptor.getFileDescriptor()).getChannel();
            oc.transferFrom(ic, 0, ic.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                oc.close();
                ic.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拷贝副本
     *
     * @param context 上下文
     * @param uri     文件
     * @param dirName 文件夹名
     * @return
     */
    public static File copy(Context context, Uri uri, String dirName) {
        String filename = UriProvider.queryDisplayName(context, uri);
        File dir = new File(context.getExternalCacheDir(), dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, filename);
        FileChannel ic = null;
        FileChannel oc = null;
        try {
            FileDescriptor descriptor = context.getContentResolver().openFileDescriptor(uri, "r").getFileDescriptor();
            ic = new FileInputStream(descriptor).getChannel();
            oc = new FileInputStream(file).getChannel();
            oc.transferFrom(ic, 0, ic.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                oc.close();
                ic.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 创建图片Uri
     *
     * @param context 上下文
     * @return
     */
    public static Uri tempImageUri(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpeg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, "media store image description.");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    /**
     * 创建视频Uri
     *
     * @param context 上下文
     * @return
     */
    public static Uri tempVideoUri(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "VIDEO_" + timeStamp + ".mp4";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Video.Media.DESCRIPTION, "media store video description.");
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        return context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }

    /**
     * 创建音频Uri
     *
     * @param context 上下文
     * @param suffix  后缀名称，例如：arm、wav、mp3
     * @return
     */
    public static Uri tempAudioUri(Context context, String suffix) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "AUDIO_" + timeStamp + "." + suffix;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Audio.Media.ALBUM_ARTIST, "media store audio artist.");
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/" + suffix);
        return context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
    }

    /**
     * 查询显示名称
     *
     * @param context 上下文
     * @param uri     文件地址
     * @return
     */
    public static String queryDisplayName(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        while (cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
        }
        return null;
    }

    /**
     * 查询ID
     *
     * @param context 上下文
     * @param uri     文件地址
     * @return
     */
    public static long queryId(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        while (cursor.moveToNext()) {
            return cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
        }
        return 0;
    }

    /**
     * 删除Uri
     *
     * @param context 上下文对象
     * @param uri     Uri
     */
    public static int delete(Context context, Uri uri) {
        int count = context.getContentResolver().delete(uri, null, null);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE));
        return count;
    }

    /**
     * 删除URI
     *
     * @param context       上下文对象
     * @param uri           Uri
     * @param where         条件
     * @param selectionArgs 条件值
     */
    public static int delete(Context context, Uri uri, String where, String[] selectionArgs) {
        return context.getContentResolver().delete(uri, where, selectionArgs);
    }

    /**
     * 更新Uri
     *
     * @param context       上下文对象
     * @param uri           Uri
     * @param values        数据
     * @param where         条件
     * @param selectionArgs 条件值
     */
    public static int update(Context context, Uri uri, ContentValues values, String where, String[] selectionArgs) {
        return context.getContentResolver().update(uri, values, where, selectionArgs);
    }

}
