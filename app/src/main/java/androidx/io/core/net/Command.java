package androidx.io.core.net;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * 文件下载指令
 */
public class Command implements Runnable {

    public String TAG = Command.class.getSimpleName();

    private Context context;
    private String url;
    private boolean override;
    private boolean pause;
    private boolean cancel;
    private String dirName = "Download";
    private long totalSize = 0;
    private Messenger messenger;
    private Map<String, String> headers;

    public Command(Context context, String url) {
        this.context = context;
        this.url = url;
        headers = new HashMap<>();
        messenger = new Messenger();
    }

    /**
     * 设置下载缓存路径的文件夹名
     *
     * @param dirName
     */
    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    /**
     * 获取文件夹
     *
     * @return
     */
    public File getDir() {
        return new File(context.getExternalCacheDir(), dirName);
    }

    /**
     * 获取下载的文件
     *
     * @param url 资源链接
     * @return
     */
    public File getFile(String url) {
        return new File(getDir(), createFileName(url));
    }

    /**
     * 是否覆盖下载
     *
     * @return
     */
    public boolean isOverride() {
        return override;
    }

    /**
     * 设置是否覆盖下载
     *
     * @param override
     */
    public void setOverride(boolean override) {
        this.override = override;
    }

    /**
     * 取消下载
     */
    public void cancel() {
        this.cancel = true;
    }

    /**
     * 是否取消
     *
     * @return
     */
    public boolean isCancel() {
        return cancel;
    }

    /**
     * 暂停
     */
    public void pause() {
        this.pause = true;
    }

    /**
     * 是否暂停
     *
     * @return
     */
    public boolean isPause() {
        return pause;
    }

    /**
     * 添加Header
     *
     * @param key   键
     * @param value 值
     */
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * 设置下载监听
     *
     * @param listener
     */
    public void setOnDownloadListener(OnDownloadListener listener) {
        messenger.setOnDownloadListener(listener);
    }

    @Override
    public void run() {
        this.cancel = false;
        this.pause = false;
        try {
            URL httpUrl = new URL(url);
            URLConnection urlConnection = httpUrl.openConnection();
            HttpURLConnection connection = (HttpURLConnection) httpUrl.openConnection();
            if (url.toUpperCase().startsWith("HTTPS")) {
                connection = (HttpURLConnection) urlConnection;
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
                httpsURLConnection.setHostnameVerifier(new HttpsHostnameVerifier());
                httpsURLConnection.setSSLSocketFactory(HttpsSSLSocketFactory.factory());
                connection = httpsURLConnection;
            }
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Charset", "UTF-8");
            long downloadedLength = getDownloadedLength(url);
            Log.i(TAG, "RANGE: " + downloadedLength);
            connection.setRequestProperty("RANGE", "bytes=" + downloadedLength + "-");
            if (headers != null) {
                for (String key : headers.keySet()) {
                    connection.setRequestProperty(key, headers.get(key));
                }
            }
            connection.connect();
            int code = connection.getResponseCode();
            Log.i(TAG, "code: " + code);
            int contentLength = connection.getContentLength();
            Log.i(TAG, "contentLength: " + contentLength);
            if (code == 416 || downloadedLength == contentLength) {
                messenger.send(createFile(url));
                return;
            }
            InputStream is = connection.getInputStream();
            write(is, contentLength, downloadedLength, createFile(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            messenger.send(e);
        } catch (ProtocolException e) {
            e.printStackTrace();
            messenger.send(e);
        } catch (IOException e) {
            e.printStackTrace();
            messenger.send(e);
        }
    }

    /**
     * 创建文件
     *
     * @param url 资源链接
     * @return
     */
    public File createFile(String url) {
        File dir = new File(context.getExternalCacheDir(), dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, createFileName(url));
    }

    /**
     * 创建Url文件名称
     *
     * @param url 资源链接
     * @return
     */
    public String createFileName(String url) {
        if (url.contains("/") && url.contains(".")) {
            String name = url.substring(url.lastIndexOf("/") + 1);
            if (name.contains(".")){
                return name;
            }
        }
        String lower = url.toLowerCase();
        if (lower.contains("img") || lower.contains("jpeg") || lower.contains("png")) {
            return url.substring(url.lastIndexOf("/") + 1) + ".jpeg";
        }
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".ZIP";
    }

    /**
     * 获取下载过的文件大小
     *
     * @param url 资源链接
     * @return
     */
    public long getDownloadedLength(String url) {
        File file = createFile(url);
        if (file.exists()) {
            if (isOverride()) {
                file.delete();
            } else {
                return file.length();
            }
        }
        return 0;
    }

    /***
     * 处理服务器返回数据
     * @param is                输入流
     * @param contentLength     文件大小
     * @param downloadedLength  下载大小
     * @param file              文件
     */
    public void write(InputStream is, long contentLength, long downloadedLength, File file) {
        RandomAccessFile access = null;
        try {
            if (downloadedLength == 0) {
                totalSize = contentLength;
            } else {
                totalSize = downloadedLength + contentLength;
            }
            if (totalSize == downloadedLength && downloadedLength != 0) {
                messenger.send(file);
                return;
            }
            if (totalSize == 0) {
                if (downloadedLength == 0) {
                    messenger.send(new IOException("The file length value is 0 and cannot be downloaded properly"));
                } else {
                    if (isOverride()) {
                        file.delete();
                    } else {
                        messenger.send(file);
                    }
                }
                return;
            }
            Log.i(TAG, "file: " + file.getAbsolutePath());
            access = new RandomAccessFile(file, "rw");
            access.seek(downloadedLength);
            int length;
            long progress = 0;
            byte[] buffer = new byte[2048];
            while ((length = is.read(buffer)) != -1) {
                if (isCancel() || isPause()) {
                    break;
                }
                access.write(buffer, 0, length);
                progress += length;
                messenger.send(totalSize, progress + downloadedLength);
            }
            messenger.send(file);
            Log.i(TAG, "write end file.");
        } catch (Exception e) {
            e.printStackTrace();
            messenger.send(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (access != null) {
                    access.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
