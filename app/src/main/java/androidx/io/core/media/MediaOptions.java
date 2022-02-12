package androidx.io.core.media;

import android.net.Uri;

/**
 * 剪切参数
 */
public class MediaOptions {
    /**
     * 剪切参数 - 是否剪切图片
     */
    private boolean crop;
    /**
     * 剪切参数 - 比例X
     */
    private int aspectX = 1;
    /**
     * 剪切参数 - 剪切比例Y
     */
    private int aspectY = 1;
    /**
     * 剪切参数 - 输出图片宽度
     */
    private int outputX = -1;
    /**
     * 剪切参数 - 输出图片高度
     */
    private int outputY = -1;
    /**
     * 剪切参数 - 是否返回数据
     */
    private boolean returnData = false;
    /**
     * 剪切参数 - 圆角剪切（部分机型支持）
     */
    private boolean circleCrop = false;
    /**
     * 是否面部识别
     */
    private boolean noFaceDetection = false;
    /**
     * 来源Uri，IntentProvider拍照和选择不用设置此字段，会自动处理。
     */
    private Uri data;
    /**
     * 输出Uri
     */
    private Uri output;

    public boolean isCrop() {
        return crop;
    }

    public MediaOptions crop(boolean crop) {
        this.crop = crop;
        return this;
    }

    public int aspectX() {
        return aspectX;
    }

    public MediaOptions aspectX(int aspectX) {
        this.aspectX = aspectX;
        return this;
    }

    public int aspectY() {
        return aspectY;
    }

    public MediaOptions aspectY(int aspectY) {
        this.aspectY = aspectY;
        return this;
    }

    public int outputX() {
        return outputX;
    }

    public MediaOptions outputX(int outputX) {
        this.outputX = outputX;
        return this;
    }

    public int outputY() {
        return outputY;
    }

    public MediaOptions outputY(int outputY) {
        this.outputY = outputY;
        return this;
    }

    public boolean isReturnData() {
        return returnData;
    }

    public MediaOptions returnData(boolean returnData) {
        this.returnData = returnData;
        return this;
    }

    public boolean isCircleCrop() {
        return circleCrop;
    }

    public MediaOptions circleCrop(boolean circleCrop) {
        this.circleCrop = circleCrop;
        return this;
    }

    public boolean isNoFaceDetection() {
        return noFaceDetection;
    }

    public MediaOptions noFaceDetection(boolean noFaceDetection) {
        this.noFaceDetection = noFaceDetection;
        return this;
    }

    public Uri data() {
        return data;
    }

    public MediaOptions data(Uri data) {
        this.data = data;
        return this;
    }

    public Uri output() {
        return output;
    }

    public MediaOptions output(Uri output) {
        this.output = output;
        return this;
    }

    @Override
    public String toString() {
        return "MediaOptions{" +
                "crop=" + crop +
                ", aspectX=" + aspectX +
                ", aspectY=" + aspectY +
                ", outputX=" + outputX +
                ", outputY=" + outputY +
                ", returnData=" + returnData +
                ", circleCrop=" + circleCrop +
                ", noFaceDetection=" + noFaceDetection +
                ", data=" + data +
                ", output=" + output +
                '}';
    }
}
