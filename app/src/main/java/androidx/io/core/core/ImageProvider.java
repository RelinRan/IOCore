package androidx.io.core.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 位图提供者
 */
public class ImageProvider {

    /**
     * 日志
     */
    public final static String TAG = ImageProvider.class.getSimpleName();
    /**
     * 格式
     */
    public final static String PATTERN = "yyyyMMddHHmmss";
    /**
     * 压缩文件夹
     */
    public static String DIRECTORY_COMPRESS = "Compress";
    /**
     * 剪切文件夹
     */
    public static String DIRECTORY_CROP = "Crop";
    /**
     * 图片文件夹
     */
    public static String DIRECTORY_PICTURES = "Pictures";
    /**
     * 文档文件夹
     */
    public static String DIRECTORY_DOCUMENTS = "Documents";
    /**
     * 下载文件
     */
    public static String DIRECTORY_DOWNLOADS = "Download";

    /**
     * 获取缓存
     *
     * @param context 上下文
     * @param name    名称
     *                {@link #DIRECTORY_PICTURES}
     *                {@link #DIRECTORY_DOCUMENTS}
     *                {@link #DIRECTORY_COMPRESS}
     * @return
     */
    public static File getCacheDir(Context context, String name) {
        return IOProvider.getCacheDir(context, name);
    }

    /**
     * 创建文件名
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @return
     */
    public static String createName(String prefix, String suffix) {
        return "IMG_" + prefix + "." + suffix;
    }

    /**
     * 创建图片名称
     *
     * @param suffix 文件后缀，不包含"."
     * @return
     */
    public static String createName(String suffix) {
        return createName(new SimpleDateFormat(PATTERN).format(new Date()), suffix);
    }


    /**
     * 构建文件
     *
     * @param dirName 文件夹名称
     * @param suffix  文件后缀，不包含"."
     * @return
     */
    public static File createCacheFile(Context context, String dirName, String suffix) {
        return new File(getCacheDir(context, dirName), createName(suffix));
    }

    /**
     * 清除文件
     *
     * @param context 上下文
     * @param dirName 文件夹名称
     */
    public static void clear(Context context, String dirName) {
        IOProvider.deleteDir(IOProvider.getCacheDir(context, dirName));
    }

    /**
     * 转字节
     *
     * @param bitmap 位图
     * @param format 格式
     * @return
     */
    public static byte[] toByteArray(Bitmap bitmap, Bitmap.CompressFormat format) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(format, 100, bos);
        return bos.toByteArray();
    }

    /**
     * 转字节
     *
     * @param bitmap 位图
     * @return
     */
    public static byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        return bos.toByteArray();
    }

    /**
     * 获取大小
     *
     * @param bitmap 位图
     * @return
     */
    public static int length(Bitmap bitmap) {
        return toByteArray(bitmap).length;
    }

    /**
     * 纠正图片角度
     *
     * @param file 图片
     */
    public static void correct(File file) {
        String path = file.getAbsolutePath();
        toFile(rotate(path, angle(path)), path);
    }

    /**
     * 纠正图片角度
     *
     * @param path 图片路径
     */
    public static void correct(String path) {
        toFile(rotate(path, angle(path)), path);
    }

    /**
     * 获取图片需要旋转得角度
     *
     * @param path 图片路径
     * @return
     */
    public static int angle(String path) {
        int angle = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                angle = 270;
            }
            Log.i(TAG, "angle: " + angle + ",orientation: " + orientation + ",path: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return angle;
    }

    /**
     * 选择图片
     *
     * @param source  图片位图
     * @param degrees 旋转角度
     * @return
     */
    public static Bitmap rotate(Bitmap source, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        Log.i(TAG, "rotate degrees: " + degrees);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * 旋转图片
     *
     * @param data    图片数据
     * @param degrees 图片角度
     * @return
     */
    public static Bitmap rotate(byte[] data, float degrees) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 旋转图片
     *
     * @param path    图片路径
     * @param degrees 旋转角度
     * @param width   目标宽度
     * @param height  目标高度
     * @return
     */
    public static Bitmap rotate(String path, float degrees, int width, int height) {
        Bitmap source;
        if (width > 0 && height > 0) {
            source = BitmapFactory.decodeFile(path, inSampleSize(path, width, height));
        } else {
            source = BitmapFactory.decodeFile(path);
        }
        Log.i(TAG, "rotate path: " + path + ",degrees: " + degrees + ",width: " + width + ",height: " + height);
        return rotate(source, degrees);
    }

    /**
     * 旋转图片
     *
     * @param path    图片路径
     * @param degrees 旋转角度
     * @return
     */
    public static Bitmap rotate(String path, float degrees) {
        return rotate(path, degrees, -1, -1);
    }

    /**
     * 旋转图片
     *
     * @param data    数据
     * @param offset  开始
     * @param length  长度
     * @param degrees 角度
     * @param width   目标宽度
     * @param height  目标高度
     * @return
     */
    public static Bitmap rotate(byte[] data, int offset, int length, float degrees, int width, int height) {
        Log.i(TAG, "rotate degrees: " + degrees + ",width: " + width + ",height: " + height);
        Bitmap source;
        if (width > 0 && height > 0) {
            source = BitmapFactory.decodeByteArray(data, offset, length, inSampleSize(data, offset, length, width, height));
        } else {
            source = BitmapFactory.decodeByteArray(data, offset, length);
        }
        return rotate(source, degrees);
    }

    /**
     * 旋转图片
     *
     * @param inputStream 输入流
     * @param outPadding  内间距
     * @param degrees     角度
     * @param width       目标宽度
     * @param height      目标高度
     * @return
     */
    public static Bitmap rotate(InputStream inputStream, Rect outPadding, float degrees, int width, int height) {
        Log.i(TAG, "rotate degrees: " + degrees + ",width: " + width + ",height: " + height);
        Bitmap source;
        if (width > 0 && height > 0) {
            source = BitmapFactory.decodeStream(inputStream, outPadding, inSampleSize(inputStream, outPadding, width, height));
        } else {
            source = BitmapFactory.decodeStream(inputStream, outPadding, null);
        }
        return rotate(source, degrees);
    }

    /**
     * 压缩图片格式，支持PNG、JPG、JPEG、WEBP
     *
     * @param file 图片文件
     * @return
     */
    public static Bitmap.CompressFormat compressFormat(File file) {
        String upperName = file.getAbsolutePath().toUpperCase();
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        if (upperName.endsWith("PNG")) {
            format = Bitmap.CompressFormat.PNG;
        }
        if (upperName.endsWith("WEBP")) {
            format = Bitmap.CompressFormat.WEBP;
        }
        if (upperName.endsWith("JPEG") | upperName.endsWith("JPG")) {
            format = Bitmap.CompressFormat.JPEG;
        }
        return format;
    }

    /**
     * Bitmap压缩为ByteArrayOutputStream
     *
     * @param bitmap 位图
     * @param format 格式
     * @param max    限制大小,压缩到<=max,单位KB
     * @return
     */
    public static ByteArrayOutputStream compress(Bitmap bitmap, Bitmap.CompressFormat format, long max) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(format, 100, bos);
        int options = 100;
        long length = bos.toByteArray().length;
        long time = System.currentTimeMillis();
        Log.i(TAG, "compress format: " + format + ",max: " + max + "kb");
        Log.i(TAG, "compress before length : " + (length / 1024) + "kb");
        while (max > 0 && bos.toByteArray().length > max) {
            bos.reset();
            options -= 10;
            bitmap.compress(format, options, bos);
        }
        length = bos.toByteArray().length;
        time = System.currentTimeMillis() - time;
        Log.i(TAG, "compress after length : " + (length / 1024) + "kb" + ",time : " + time + "ms");
        return bos;
    }

    /**
     * 压缩图片
     *
     * @param bitmap 位图
     * @param max    限制大小,压缩到< = max,单位KB
     * @param format 格式
     * @return
     */
    public static Bitmap compress(Bitmap bitmap, long max, Bitmap.CompressFormat format) {
        ByteArrayOutputStream bos = compress(bitmap, format, max);
        byte[] bytes = bos.toByteArray();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 压缩图片
     *
     * @param bitmap  图片位图
     * @param format  图片路径
     * @param max     限制大小,压缩到<=max,单位KB
     * @param outPath 输出文件路径
     * @return
     */
    public static File compress(Bitmap bitmap, Bitmap.CompressFormat format, long max, String outPath) {
        ByteArrayOutputStream bos = compress(bitmap, format, max);
        BufferedOutputStream out = null;
        File file = new File(outPath);
        if (file.exists()) {
            file.delete();
        }
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            out.write(bos.toByteArray());
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    /**
     * 压缩图片
     *
     * @param bitmap 位图
     * @param width  目标宽度
     * @param height 目标高度
     * @param format 图片格式
     * @param max    最大
     * @return
     */
    public static Bitmap compress(Bitmap bitmap, int width, int height, Bitmap.CompressFormat format, long max) {
        byte[] data = toByteArray(bitmap);
        Bitmap inSampleBitmap = decodeByteArray(data, 0, data.length, width, height);
        return compress(inSampleBitmap, max, format);
    }

    /**
     * 压缩为最大值为max的Bitmap,单位kb
     *
     * @param path 文件路径
     * @param max  文件最大值，单位kb
     * @return
     */
    public static Bitmap compress(String path, long max) {
        return compress(path, Bitmap.CompressFormat.JPEG, max);
    }

    /**
     * 压缩为最大值为max的Bitmap,单位kb
     *
     * @param path   文件路径
     * @param format 图片格式
     * @param max    文件最大值，单位kb
     * @return
     */
    public static Bitmap compress(String path, Bitmap.CompressFormat format, long max) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        int inSampleSize = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(format, 100, bos);
        byte[] bytes = toByteArray(bitmap);
        Log.i(TAG, "compress original: " + path + ", length: " + (bytes.length / 1024) + "kb");
        long time = System.currentTimeMillis();
        while (bytes.length > max) {
            inSampleSize++;
            bitmap = decodePath(path, bitmap.getWidth() / inSampleSize, bitmap.getHeight() / inSampleSize);
            bos.reset();
            bitmap.compress(format, 100, bos);
            bytes = bos.toByteArray();
        }
        time = System.currentTimeMillis() - time;
        Log.i(TAG, "compress path: " + path + ", max: " + max + ", inSampleSize: " + inSampleSize + ", time: " + time + "ms");
        return bitmap;
    }

    /**
     * 压缩文件
     *
     * @param context 上下文
     * @param file    文件
     * @param max     文件最大值，单位byte
     * @return
     */
    public static File compress(Context context, File file, long max) {
        long time = System.currentTimeMillis();
        if (isCompressible(file.getAbsolutePath())) {
            Bitmap.CompressFormat format = compressFormat(file);
            String suffix = IOProvider.getSuffix(file);
            Bitmap toBitmap = compress(file.getAbsolutePath(), format, max);
            File target = createCacheFile(context, DIRECTORY_COMPRESS, suffix);
            toFile(toBitmap, format, 100, target.getAbsolutePath());
            Log.i(TAG, "compress target: " + target.getAbsolutePath() + ",length: " + (target.length() / 1024) + "kb");
            return target;
        } else {
            System.out.println("compress image is not ordinary bitmap.");
        }
        Log.i(TAG, "compress time: " + (System.currentTimeMillis() - time));
        return file;
    }

    /**
     * 解析成文件
     *
     * @param bitmap  图片位图
     * @param format  图片路径
     * @param quality 图片质量[1-100]
     * @param path    输出路径
     * @return
     */
    public static File toFile(Bitmap bitmap, Bitmap.CompressFormat format, int quality, String path) {
        File outFile = new File(path);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
            bitmap.compress(format, quality, bos);
            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outFile;
    }

    /**
     * Bitmap解析成文件
     *
     * @param bitmap 位图
     * @param path   输出路径
     * @return
     */
    public static File toFile(Bitmap bitmap, String path) {
        return toFile(bitmap, Bitmap.CompressFormat.JPEG, 100, path);
    }

    /**
     * 解析成文件
     *
     * @param context 上下文
     * @param bitmap  图片位图
     * @param format  图片路径
     * @param quality 图片质量[1-100]
     * @return
     */
    public static File toFile(Context context, Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        File file = createCacheFile(context, DIRECTORY_PICTURES, "jpeg");
        return toFile(bitmap, format, quality, file.getAbsolutePath());
    }

    /**
     * 解析成文件
     *
     * @param bitmap 图片位图
     * @return
     */
    public static File toFile(Context context, Bitmap bitmap) {
        File file = createCacheFile(context, DIRECTORY_PICTURES, "jpeg");
        return toFile(bitmap, Bitmap.CompressFormat.JPEG, 100, file.getAbsolutePath());
    }

    /**
     * 按大小解析图片
     *
     * @param path   图片路径
     * @param width  目标宽度
     * @param height 目标高度
     * @return
     */
    public static Bitmap decodePath(String path, int width, int height) {
        Log.i(TAG, "decodePath width : " + width + ",height : " + height + ",path : " + path);
        BitmapFactory.Options options = inSampleSize(path, width, height);
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 按大小解析图片
     *
     * @param inputStream 输入流
     * @param outPadding  内边距
     * @param width       目标宽度
     * @param height      目标高度
     * @return
     */
    public static Bitmap decodeStream(InputStream inputStream, Rect outPadding, int width, int height) {
        Log.i(TAG, "decodeStream width: " + width + ",height: " + height);
        BitmapFactory.Options options = inSampleSize(inputStream, outPadding, width, height);
        return BitmapFactory.decodeStream(inputStream, outPadding, options);
    }

    /**
     * 按大小解析图片
     *
     * @param data   数据
     * @param offset 开始
     * @param length 长度
     * @param width  宽度
     * @param height 高度
     * @return
     */
    public static Bitmap decodeByteArray(byte[] data, int offset, int length, int width, int height) {
        Log.i(TAG, "decodeByteArray width: " + width + ",height: " + height);
        BitmapFactory.Options options = inSampleSize(data, offset, length, width, height);
        return BitmapFactory.decodeByteArray(data, offset, length, options);
    }


    /**
     * 解析Uri为Bitmap
     *
     * @param context 上下文
     * @param uri     图片Uri
     * @return
     */
    public static Bitmap decodeUri(Context context, Uri uri) {
        Bitmap bitmap = null;
        if (context != null && uri != null) {
            try {
                bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 构建需要宽宽高的Bitmap options
     *
     * @param path   文件路径
     * @param width  目标宽度
     * @param height 目标高度
     * @return
     */
    public static BitmapFactory.Options inSampleSize(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int w = options.outWidth;
        int h = options.outHeight;
        int inSampleSize = Math.min(w / width, h / height);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = inSampleSize;
        options.inPurgeable = true;
        Log.i(TAG, "inSampleSize width: " + width + ",height: " + height + ",inSampleSize: " + inSampleSize + ",path: " + path);
        return options;
    }

    /**
     * 构建需要宽宽高的Bitmap options
     *
     * @param data   数据
     * @param offset 开始
     * @param length 长度
     * @param width  目标宽度
     * @param height 目标高度
     * @return
     */
    public static BitmapFactory.Options inSampleSize(byte[] data, int offset, int length, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, offset, length, options);
        int w = options.outWidth;
        int h = options.outHeight;
        int inSampleSize = Math.min(w / width, h / height);
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        options.inPurgeable = true;
        Log.i(TAG, "inSampleSize width: " + width + ",height: " + height + ",inSampleSize: " + inSampleSize);
        return options;
    }

    /**
     * 构建需要宽宽高的Bitmap options
     *
     * @param inputStream 输入流
     * @param outPadding  内间距
     * @param width       目标宽度
     * @param height      目标高度
     * @return
     */
    public static BitmapFactory.Options inSampleSize(InputStream inputStream, Rect outPadding, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, outPadding, options);
        int w = options.outWidth;
        int h = options.outHeight;
        int inSampleSize = Math.min(w / width, h / height);
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        options.inPurgeable = true;
        Log.i(TAG, "inSampleSize width: " + width + ",height: " + height + ",inSampleSize: " + inSampleSize);
        return options;
    }

    /**
     * 判断文件是否是图片
     *
     * @param file
     * @return
     */
    public static boolean isBitmap(File file) {
        return BitmapFactory.decodeFile(file.getAbsolutePath()) != null;
    }

    /**
     * 是否可压缩图片
     *
     * @param path 路径
     * @return
     */
    public static boolean isCompressible(String path) {
        if (path == null || path.length() == 0) {
            return false;
        }
        String upper = path.toUpperCase();
        if (upper.endsWith(".JPG") || upper.endsWith(".JPEG") || upper.endsWith(".PNG") || upper.endsWith(".WEBP")) {
            return true;
        }
        return false;
    }

    /**
     * 多行Base64处理为单行
     *
     * @param value 文本内容
     * @return
     */
    public static String toSingleLine(String value) {
        return value.replaceAll("[\\s*\t\n\r]", "");
    }

    /**
     * 文件转字符串
     *
     * @param file
     * @return
     */
    public static String encodeBase64(File file) {
        return encodeBase64(file, false);
    }

    /**
     * 文件转base64字符串
     *
     * @param file   文件
     * @param encode 是否URLEncoder
     * @return
     */
    public static String encodeBase64(File file, boolean encode) {
        byte[] bytes = IOProvider.decodeFile(file);
        String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
        if (encode) {
            try {
                base64 = URLEncoder.encode(base64, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return base64;
    }

    /**
     * Base64转File
     *
     * @param base64 Base64字符
     * @param path   路径
     */
    public static File decodeBase64(String base64, String path) {
        return decodeBase64(base64, path, false);
    }

    /**
     * Base64转File
     *
     * @param base64 Base64字符
     * @param path   路径
     * @param decode 是否URL解密
     */
    public static File decodeBase64(String base64, String path, boolean decode) {
        File file = new File(path);
        if (file.getParentFile().isDirectory() && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            byte[] buffer = Base64.decode(decode ? URLDecoder.decode(base64, "UTF-8") : base64, Base64.CRLF);
            FileOutputStream out = new FileOutputStream(file);
            out.write(buffer);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 文件转Base64String
     *
     * @param bitmap 位图
     * @return
     */
    public static String encodeBase64(Bitmap bitmap) {
        return encodeBase64(bitmap, false);
    }

    /**
     * 图片转Base64String
     *
     * @param bitmap 位图
     * @param encode 是否Url加密
     * @return
     */
    public static String encodeBase64(Bitmap bitmap, boolean encode) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String stringBitmap = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        if (encode) {
            try {
                stringBitmap = URLEncoder.encode(stringBitmap, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBitmap;
    }

    /**
     * Base64转Bitmap,默认不URLDecoder
     *
     * @param base64 图片Base64文本
     * @return
     */
    public static Bitmap decodeBase64(String base64) {
        return decodeBase64(base64, false);
    }

    /**
     * Base64转Bitmap
     *
     * @param base64 图片Base64文本
     * @param decode 是否Url解密
     * @return
     */
    public static Bitmap decodeBase64(String base64, boolean decode) {
        // 将字符串转换成Bitmap类型
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(decode ? URLDecoder.decode(base64, "UTF-8") : base64, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public static Bitmap createImageThumbnail(String path, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

}
