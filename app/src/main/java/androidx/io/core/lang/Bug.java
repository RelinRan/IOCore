package androidx.io.core.lang;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.io.core.core.IOProvider;
import androidx.io.core.core.UriProvider;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 本地捕捉异常工具
 */
public class Bug implements Thread.UncaughtExceptionHandler {

    /**
     * 日志标识
     */
    public final String TAG = Bug.class.getSimpleName();
    /**
     * 线程超时操作
     */
    private Future<?> future;

    /**
     * 保存错误日志监听
     */
    private OnBugListener listener;

    /**
     * 保存日志超时线程池
     */
    private ExecutorService threadPool = Executors.newSingleThreadExecutor();

    /**
     * 捕捉异常对象类
     */
    private Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

    /**
     * 上下文对象
     */
    public final Context context;

    /**
     * 文件名
     */
    public final String name;

    /**
     * 文件夹名称
     */
    public final String dir;

    /**
     * 异常捕捉
     *
     * @param builder
     */
    public Bug(Builder builder) {
        this.context = builder.context;
        this.dir = builder.dir;
        this.name = builder.name;
        this.listener = builder.listener;
        IOProvider.makeCacheDir(context, dir);
        start();
    }

    /**
     * 捕捉
     */
    private void start() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 异常构建者
     */
    public static class Builder {

        /**
         * 上下文对象
         */
        private Context context;

        /**
         * 文件夹名称
         */
        private String dir = "Bug";

        /**
         * 日志监听
         */
        private OnBugListener listener;

        /**
         * 日志名称
         */
        private String name = "BUG_" + new SimpleDateFormat("yyyyMMDD_HHmmss").format(new Date()) + ".txt";

        /**
         * 构建者对象
         *
         * @param context
         */
        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 设置文件夹名称
         *
         * @param name
         * @return
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        /**
         * 文件监听
         *
         * @param listener
         * @return
         */
        public Builder listener(OnBugListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * 构建
         *
         * @return
         */
        public Bug build() {
            return new Bug(this);
        }
    }

    /**
     * 获取奔溃异常
     *
     * @param thread
     * @param throwable
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        final String bug = buildApplicationDevice(false) + buildRuntimeException(false, throwable);
        Log.e(TAG, bug);
        if (UriProvider.isMounted()) {
            final File file = IOProvider.createCacheFile(context, dir, name);
            //保存错误的日志
            write(file, throwable);
            future = threadPool.submit(new Runnable() {
                public void run() {
                    if (listener != null) {
                        listener.onBug(file, bug);
                    }
                }
            });
            if (!future.isDone()) {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            exceptionHandler.uncaughtException(thread, throwable);
        } else {
            new RuntimeException("You Sdcard is not exist!").printStackTrace();
            Log.e(TAG, "You Sdcard is not exist!");
        }
    }

    /**
     * 营运设备信息
     *
     * @return
     */
    public String buildApplicationDevice(boolean isEnd) {
        StringBuilder sb = new StringBuilder();
        //项目信息
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        //项目名字
        sb.append(" \n");
        sb.append("┌───────────────────────────────────────────────────────").append("\n");
        sb.append("│").append(name).append("\n");
        sb.append("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄").append("\n");
        sb.append("│").append(packageManager.getApplicationLabel(applicationInfo)).append('\n');
        sb.append("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄").append("\n");
        try {
            PackageInfo pi = packageManager.getPackageInfo(applicationInfo.packageName, 0);
            //项目版本号
            sb.append("│Version Code:").append(pi.versionCode).append('\n');
            //项目版本名
            sb.append("│Version Name:").append(pi.versionName).append('\n');
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        sb.append("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄").append("\n");
        //设备信息
        sb.append("│").append("Equipment").append('\n');
        sb.append("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄").append("\n");
        //手机品牌
        sb.append("│Brand:").append(Build.BRAND).append('\n');
        //SDK版本
        sb.append("│Release:Android ").append(Build.VERSION.RELEASE).append('\n');
        //设备名
        sb.append("│Device:").append(Build.DEVICE).append('\n');
        //产品名
        sb.append("│Product:").append(Build.PRODUCT).append('\n');
        //制造商
        sb.append("│Manufacturer:").append(Build.MANUFACTURER).append('\n');
        //手机版本
        sb.append("│Version Code:").append(Build.DISPLAY).append('\n');
        //指纹
        sb.append("│Fingerprint:").append(Build.FINGERPRINT).append('\n');
        if (isEnd) {
            sb.append("└───────────────────────────────────────────────────────").append("\n");
        } else {
            sb.append("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄").append("\n");
        }
        return sb.toString();
    }

    /**
     * 构建运行时间
     *
     * @param throwable 异常
     * @return
     */
    public String buildRuntimeException(boolean topDivider, Throwable throwable) {
        StringBuffer sb = new StringBuffer();
        if (topDivider) {
            sb.append("┌───────────────────────────────────────────────────────\n");
        }
        sb.append("│RuntimeException").append("\n");
        sb.append("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄\n");
        sb.append("│").append(throwable.toString()).append('\n');
        sb.append("└────────────────────────────────────────────────────────").append("\n");
        return sb.toString();
    }

    /**
     * 同步保存错误日志
     *
     * @param file      保存错误信息的文件
     * @param throwable 保存错误信息
     */
    public synchronized void write(File file, Throwable throwable) {
        synchronized (file) {
            FileWriter fileWriter = null;
            BufferedWriter bufferedWriter = null;
            PrintWriter printWriter = null;
            try {
                fileWriter = new FileWriter(file);
                bufferedWriter = new BufferedWriter(fileWriter);
                printWriter = new PrintWriter(fileWriter);
                //拼接应用、设备信息和错误Log
                bufferedWriter.append(buildApplicationDevice(false)).append(buildRuntimeException(false, throwable));
                bufferedWriter.flush();
                throwable.printStackTrace(printWriter);
                printWriter.flush();
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
                closeWriter(fileWriter);
                closeWriter(bufferedWriter);
                closeWriter(printWriter);
            }
        }
    }

    /**
     * 关闭写入流通道
     *
     * @param closeable
     */
    private void closeWriter(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                Log.i(this.getClass().getSimpleName(), Bug.class.getClass().getSimpleName() + " " + ioe.toString());
            }
        }
    }

}
