package androidx.io.core.core;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;

import java.io.Serializable;

public class CameraOptions implements Serializable {

    /**
     * 是否拍照
     */
    private boolean capture;
    /**
     * 视频路径
     */
    private Uri output;
    /**
     * 视频宽度
     */
    private int width = 1920;
    /**
     * 视频高度
     */
    private int height = 1080;
    /**
     * 录制时间限制
     */
    private int duration = Integer.MAX_VALUE;
    /**
     * 视频编码
     */
    private int videoEncoder = MediaRecorder.VideoEncoder.H264;
    /**
     * 相机id
     */
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    public CameraOptions(boolean capture) {
        this.capture = capture;
    }

    public boolean isCapture() {
        return capture;
    }

    public void setCapture(boolean capture) {
        this.capture = capture;
    }

    public Uri getOutput() {
        return output;
    }

    public void setOutput(Uri output) {
        this.output = output;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getVideoEncoder() {
        return videoEncoder;
    }

    public void setVideoEncoder(int videoEncoder) {
        this.videoEncoder = videoEncoder;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    @Override
    public String toString() {
        return "CameraOptions{" +
                "capture=" + capture +
                ", output=" + output +
                ", width=" + width +
                ", height=" + height +
                ", duration=" + duration +
                ", videoEncoder=" + videoEncoder +
                ", cameraId=" + cameraId +
                '}';
    }
}
