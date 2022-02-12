package androidx.io.core.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 文件操作提供者
 */
public class IOProvider {

    /**
     * 日志标识
     */
    public static String TAG = IOProvider.class.getSimpleName();
    /**
     * byte
     */
    public static final int UNIT_BT = 1;
    /**
     * KiB
     */
    public static final int UNIT_KB = 2;
    /**
     * MiB
     */
    public static final int UNIT_MB = 3;
    /**
     * GiB
     */
    public static final int UNIT_GB = 4;
    /**
     * TiB
     */
    public static final int UNIT_TB = 5;

    /**
     * 获取外部缓存目录
     *
     * @param context 上下文
     * @return
     */
    public static File getCacheDir(Context context) {
        return context.getExternalCacheDir();
    }

    /**
     * 获取应用外部文件夹
     *
     * @param context 上下文
     * @param name    文件夹名称
     * @return
     */
    public static File getCacheDir(Context context, String name) {
        return makeCacheDir(context, name);
    }

    /**
     * 获取应用外部文件夹
     *
     * @param context 上下文
     * @param name    文件夹名称
     * @return
     */
    public static File getFilesDir(Context context, String name) {
        return makeFilesDir(context, name);
    }

    /**
     * 创建新文件夹
     *
     * @param context 上下文
     * @param name    文件夹名
     * @return
     */
    public static File makeFilesDir(Context context, String name) {
        File folder = context.getExternalFilesDir(name);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    /**
     * 创建新缓存文件夹
     *
     * @param name 文件夹名
     * @return
     */
    public static File makeCacheDir(Context context, String name) {
        File dir = new File(context.getExternalCacheDir(), name);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 创建文件
     *
     * @param context  上下文
     * @param dirName  文件夹名称
     * @param fileName 文件名称
     * @return
     */
    public static File createFile(Context context, String dirName, String fileName) {
        if (dirName == null || fileName == null) {
            return null;
        }
        return new File(getFilesDir(context, dirName), fileName);
    }

    /**
     * 创建缓存文件
     *
     * @param context  上下文
     * @param dirName  文件夹名
     * @param fileName 文件名
     * @return
     */
    public static File createCacheFile(Context context, String dirName, String fileName) {
        if (dirName == null || fileName == null) {
            return null;
        }
        return new File(getCacheDir(context, dirName), fileName);
    }

    /**
     * 复制文件
     *
     * @param from 来源
     * @param to   目标
     */
    public static void copy(File from, File to) {
        FileChannel oc = null;
        FileChannel ic = null;
        try {
            oc = new FileOutputStream(from).getChannel();
            ic = new FileInputStream(to).getChannel();
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
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param file 目录
     */
    public static void deleteDir(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteDir(child);
            }
        } else {
            file.delete();
        }
    }

    /**
     * 文件大小
     *
     * @param file 文件
     * @param unit 单位
     * @return
     */
    public static long length(File file, int unit) {
        long size = length(file);
        if (unit == UNIT_BT) {
            return size;
        }
        if (unit == UNIT_KB) {
            size /= 1024;
        }
        if (unit == UNIT_MB) {
            size /= Math.pow(1024, 2);
        }
        if (unit == UNIT_GB) {
            size /= Math.pow(1024, 3);
        }
        if (unit == UNIT_TB) {
            size /= Math.pow(1024, 4);
        }
        return size;
    }

    /**
     * 文件大小名称
     *
     * @param file 文件
     * @return
     */
    public static String lengthName(File file) {
        long b = file.length();
        if (b < 1024) {
            return b + "B";
        }
        long kb = b / 1024;
        if (kb < 1024) {
            return kb + "KB";
        }
        long m = kb / 1024;
        if (m < 1024) {
            return m + "M";
        }
        long g = m / 1024;
        return g + "G";
    }

    /**
     * 计算文件大小
     *
     * @param file 文件夹
     * @return
     */
    public static long length(File file) {
        long size = 0L;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File child : files) {
                size += length(child);
            }
        }
        size += file.length();
        return size;
    }

    /**
     * 获取文件后缀
     *
     * @param path 路径
     * @return
     */
    public static String getSuffix(String path) {
        return getSuffix(new File(path));
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
     * 获取文件类型
     *
     * @param path 文件路径
     * @return
     */
    public static String getMimeType(String path) {
        String lower = path.toLowerCase();
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimetype = fileNameMap.getContentTypeFor(path);
        if (mimetype == null) {
            if ((lower.contains("img") || lower.contains("image"))) {
                if (lower.contains("jpeg") || lower.contains("png") || lower.contains("jpg")) {
                    mimetype = "image/*";
                }
            }
        }
        if (mimetype == null) {
            mimetype = "application/*";
        }
        Log.i(TAG, "getMimeType path: " + path + ",mimetype: " + mimetype);
        return mimetype;
    }

    /**
     * 获取文件类型
     *
     * @param file 文件
     * @return
     */
    public static String getMimeType(File file) {
        String suffix = getSuffix(file);
        if (suffix == null) {
            return "file/*";
        }
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        if (type != null || !type.isEmpty()) {
            return type;
        }
        return "file/*";
    }

    /**
     * 创建文件名称
     *
     * @param url 资源地址
     * @return
     */
    public static String createFileName(String url) {
        if (url.contains("/") && url.contains(".")) {
            return url.substring(url.lastIndexOf("/") + 1);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        return format.format(format) + ".zip";
    }

    /**
     * 获取Assets文件内容
     *
     * @param context  上下文
     * @param fileName 文件名
     * @return
     */
    public static String readAssets(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 读取文件
     *
     * @param file
     * @return
     */
    public static String read(File file) {
        if (file == null) {
            return "The read file is empty and cannot be read.";
        }
        //定义一个字符串缓存，将字符串存放缓存中
        StringBuilder sb = new StringBuilder("");
        //定义一个fileReader对象，用来初始化BufferedReader
        FileReader reader;
        try {
            reader = new FileReader(file);
            //new一个BufferedReader对象，将文件内容读取到缓存
            BufferedReader bufferedReader = new BufferedReader(reader);
            String content;
            //逐行读取文件内容，不读取换行符和末尾的空格
            while ((content = bufferedReader.readLine()) != null) {
                //将读取的字符串添加换行符后累加存放在缓存中
                sb.append(content + "\n");
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 写入文件内容
     *
     * @param context  上下文
     * @param fileName 文件名称，例如：xxx.java
     * @param content  内容
     */
    public static void write(Context context, String fileName, String content) {
        write(context, Environment.DIRECTORY_DOCUMENTS, fileName, content);
    }

    /**
     * 写入文件
     *
     * @param context  上下文
     * @param dirName  文件夹名字
     * @param fileName 文件名字
     * @param content  文件内容
     */
    public static void write(Context context, String dirName, String fileName, String content) {
        File fileDir = makeFilesDir(context, dirName);
        File classFile = new File(fileDir.getAbsolutePath(), fileName);
        if (!classFile.exists()) {
            try {
                classFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(classFile));
            pw.print(content);
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取Assets文件内容
     *
     * @param context 上下文
     * @param name    文件名
     * @return
     */
    public static String openAssets(Context context, String name) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(assetManager.open(name)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 读取文件数据
     *
     * @return
     */
    public static StringBuffer read(String path) {
        StringBuffer stringBuilder = new StringBuffer();
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder;
    }

    /**
     * 将文件流转成文件
     *
     * @param inputStream 输入流
     * @param path        文件路径
     * @return
     */
    public static File decodeInputStream(InputStream inputStream, String path) {
        File file = new File(path);//文件夹
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
        //写入文件操作流程中
        int len;
        byte[] buffer = new byte[2048];
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
            while ((len = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, len);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * File转Bytes
     *
     * @param file
     * @return
     */
    public static byte[] decodeFile(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * Bytes转文件
     *
     * @param bytes 字节数据
     * @param path  文件路径
     * @return
     */
    public static File decodeBytes(byte[] bytes, String path) {
        File file = new File(path);
        if (file.getParentFile().exists()) {
            file.mkdirs();
        }
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            file = new File(path);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
            bos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }


    /**
     * 通过文件名获取资源id 例子：getResId("icon", R.drawable.class);
     *
     * @param variableName
     * @param cls
     * @return
     */
    public static int findResId(String variableName, Class<?> cls) {
        try {
            Field idField = cls.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

}
