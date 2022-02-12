package androidx.io.core.video;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.io.core.core.CameraProvider;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 视频录制
 */
public class VideoRecorder implements SurfaceHolder.Callback, View.OnTouchListener {

    public static final String TAG = VideoRecorder.class.getSimpleName();
    public static final int WHAT_INIT = 0;
    public static final int WHAT_TIME = 1;
    public static int CAMERA_FACING_BACK_DEGREES = 90;
    public static int CAMERA_FACING_FRONT_DEGREES = 90;
    public static int RECORD_FACING_BACK_DEGREES = 90;
    public static int RECORD_FACING_FRONT_DEGREES = 270;

    private Context context;
    private Camera camera;
    private Camera.Size previewSize;
    private long duration = Integer.MAX_VALUE;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int previewDegrees = 90;
    private int recordDegrees = 90;
    private int width = 1920;
    private int height = 1080;
    private int encodingBitRate = 5 * 1024 * 1024;
    private int videoEncoder = MediaRecorder.VideoEncoder.H264;
    private Uri output;

    private boolean start;
    private RecordHandler handler;
    private MediaRecorder recorder;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private long millis = 0;
    private OnVideoTimerListener onVideoTimerListener;

    public VideoRecorder(Context context, SurfaceView surfaceView) {
        this.context = context;
        this.surfaceView = surfaceView;
    }

    /**
     * 录制开始预览
     */
    public void startPreview() {
        handler = new RecordHandler();
        surfaceView.setOnTouchListener(this);
        holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);
    }

    /**
     * 设置录制时间
     *
     * @param duration
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * 设置预览角度
     *
     * @param previewDegrees
     */
    public void setPreviewDegrees(int previewDegrees) {
        this.previewDegrees = previewDegrees;
    }

    /**
     * 设置录制角度
     *
     * @param recordDegrees
     */
    public void setRecordDegrees(int recordDegrees) {
        this.recordDegrees = recordDegrees;
    }

    /**
     * 设置相机ID
     *
     * @param cameraId
     */
    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    /**
     * 设置录制宽度
     *
     * @param width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * 设置录制高度
     *
     * @param height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * 设置录制编码频率
     *
     * @param encodingBitRate
     */
    public void setEncodingBitRate(int encodingBitRate) {
        this.encodingBitRate = encodingBitRate;
    }

    /**
     * 设置视频编码格式
     *
     * @param videoEncoder
     */
    public void setVideoEncoder(int videoEncoder) {
        this.videoEncoder = videoEncoder;
    }

    /**
     * 设置输出路径
     *
     * @param output
     */
    public void setOutput(Uri output) {
        this.output = output;
    }

    /**
     * 获取相机对象
     *
     * @return
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * 获取录制文件
     *
     * @return
     */
    public Uri getOutput() {
        return output;
    }

    /**
     * 删除录制文件
     */
    public void delete() {
        if (output == null) {
            return;
        }
        int count = context.getContentResolver().delete(output, null, null);
        Log.i(TAG, "delete count = " + count);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE));
    }

    /**
     * 是否开始
     *
     * @return
     */
    public boolean isStart() {
        return start;
    }

    /**
     * 开始
     */
    public void start() {
        camera.unlock();
        if (output == null) {
            output = tempVideoUri(context);
        }
        recorder = obtainMediaRecorder(context, camera, recordDegrees, holder.getSurface(), encodingBitRate, videoEncoder, output);
        recorder.start();
        startTimer();
        start = true;
        Log.i(TAG, "start");
    }

    /**
     * 停止
     */
    public void stop() {
        if (recorder != null) {
            recorder.setOnErrorListener(null);
            recorder.setPreviewDisplay(null);
            try {
                recorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stopTimer();
        start = false;
        Log.i(TAG, "stop");
    }

    /**
     * 释放对象
     */
    public void release() {
        if (camera != null) {
            camera.release();
        }
        if (recorder != null) {
            recorder.release();
        }
        if (handler != null) {
            stopTimer();
            handler.removeCallbacksAndMessages(null);
        }
        Log.i(TAG, "release");
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        handler.sendEmptyMessageDelayed(WHAT_INIT, 50);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged width: " + width + ",height: " + height);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        release();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        CameraProvider.focus(camera);
        return false;
    }

    private class RecordHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_INIT:
                    camera = CameraProvider.obtainCamera(cameraId, CameraProvider.getPreviewDegrees(cameraId), width, height);
                    previewSize = CameraProvider.findSupportedPreviewSize(camera, width, height);
                    Log.i(TAG, "init camera preview size " + previewSize.width + " * " + previewSize.height);
                    CameraProvider.startPreview(camera, surfaceView.getHolder());
                    break;
                case WHAT_TIME:
                    long value = System.currentTimeMillis() - millis;
                    if (onVideoTimerListener != null) {
                        onVideoTimerListener.onVideoTimer(value);
                    }
                    if (value < duration) {
                        sendEmptyMessageDelayed(WHAT_TIME, 100);
                    } else {
                        if (onVideoTimerListener != null) {
                            onVideoTimerListener.onVideoTimerFinish();
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 开启计时器
     */
    private void startTimer() {
        millis = System.currentTimeMillis();
        handler.sendEmptyMessage(WHAT_TIME);
    }

    /**
     * 停止计时器
     */
    private void stopTimer() {
        millis = System.currentTimeMillis();
        handler.removeMessages(WHAT_TIME);
    }

    /**
     * 设置视频计时器监听
     *
     * @param onVideoTimerListener
     */
    public void setOnVideoTimerListener(OnVideoTimerListener onVideoTimerListener) {
        this.onVideoTimerListener = onVideoTimerListener;
    }

    public interface OnVideoTimerListener {

        /**
         * 计时器时间
         *
         * @param millis 毫秒时间
         */
        void onVideoTimer(long millis);

        /**
         * 计时器时间结束
         */
        void onVideoTimerFinish();
    }

    /**
     * 是否背面摄像头
     *
     * @return
     */
    public boolean isFacingBack() {
        return cameraId == Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    /**
     * 切换摄像头
     *
     * @param cameraId 摄像头ID
     */
    public void switchCamera(int cameraId) {
        if (camera != null) {
            camera.release();
        }
        this.cameraId = cameraId;
        previewDegrees = isFacingBack() ? CAMERA_FACING_BACK_DEGREES : CAMERA_FACING_FRONT_DEGREES;
        recordDegrees = isFacingBack() ? RECORD_FACING_BACK_DEGREES : RECORD_FACING_FRONT_DEGREES;
        handler.sendEmptyMessage(WHAT_INIT);
    }

    /**
     * 设置录制参数
     *
     * @param context         上下问对象
     * @param camera          摄像头
     * @param degrees         旋转角度,竖屏：90；横屏：270
     * @param holder          显示器
     * @param encodingBitRate 视频编码比特率,例如：2*1024*512
     * @param videoEncoder    编码格式, MediaRecorder.VideoEncoder.H264
     * @param output          输入文件
     */
    public MediaRecorder obtainMediaRecorder(Context context, Camera camera, int degrees, Surface holder, int encodingBitRate, int videoEncoder, Uri output) {
        MediaRecorder recorder = new MediaRecorder();
        recorder.reset();
        recorder.setCamera(camera);
        recorder.setPreviewDisplay(holder);
        // 发生错误，停止录制
        recorder.setOnErrorListener((mr, what, extra) -> {
            recorder.stop();
            recorder.release();
        });
        // 设置音频采集方式
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //设置视频的采集方式
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //设置文件的输出格式
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //设置audio的编码格式
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //设置video的编码格式
        recorder.setVideoEncoder(videoEncoder);
        //设置录制的视频编码比特率
        recorder.setVideoEncodingBitRate(encodingBitRate);
        //设置视频分辨率
        if (previewSize != null) {
            int previewWidth = previewSize.width;
            int previewHeight = previewSize.height;
            Log.i(TAG, "previewWidth: " + previewWidth + ",previewHeight: " + previewHeight);
            try {
                recorder.setVideoSize(previewWidth, previewHeight);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //设置旋转角度
        recorder.setOrientationHint(degrees);
        //设置输出文件的路径
        try {
            FileDescriptor descriptor = context.getContentResolver().openFileDescriptor(output, "rw").getFileDescriptor();
            recorder.setOutputFile(descriptor);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //设置录制准备
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recorder;
    }

    /**
     * 创建视频Uri
     *
     * @param context 上下文
     * @return
     */
    public Uri tempVideoUri(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "VIDEO_" + timeStamp + ".mp4";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Video.Media.DESCRIPTION, "media store video description.");
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        return context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }

}
