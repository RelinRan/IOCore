package androidx.io.core.media;

import android.provider.MediaStore;

import java.io.File;
import java.util.UUID;

public class Media {

    public static final int DOCUMENT = MediaStore.Files.FileColumns.MEDIA_TYPE_DOCUMENT;
    public static final int IMAGE = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
    public static final int AUDIO = MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO;
    public static final int VIDEO = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
    public static final int NONE = MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;

    /**
     * 媒体ID
     */
    private long id = UUID.randomUUID().variant();
    /**
     * 媒体类型
     */
    private int type;
    /**
     * 名称
     */
    private String name;
    /**
     * 最后修改时间
     */
    private long date;
    /**
     * 数据路径
     */
    private String data;
    /**
     * 媒体大小
     */
    private long size;
    /**
     * 时长（视频、音频）
     */
    private long duration = 0;
    /**
     * 宽度（视频、音频）
     */
    private int width = 0;
    /**
     * 高度（视频、音频）
     */
    private int height = 0;
    /**
     * 专辑ID（音频）
     */
    private long albumId;
    /**
     * 专辑作家（音频）
     */
    private String artist;
    /**
     * 专辑名称（音频）
     */
    private String album;
    /**
     * 缩略图（视频、音频）
     */
    private File thumbnail;
    /**
     * 是否选中
     */
    private boolean check;
    /**
     * 文件夹下面的文件个数
     */
    private int count = -1;
    /**
     * 描述文字
     */
    private String desc;
    /**
     * 搜索关键字
     */
    private String keywords;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
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

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public File getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(File thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
}
