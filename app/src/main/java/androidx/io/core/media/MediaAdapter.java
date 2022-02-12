package androidx.io.core.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.io.core.R;
import androidx.recyclerview.widget.RecyclerView;

import androidx.io.core.core.IOProvider;
import androidx.io.core.core.ImageProvider;
import androidx.io.core.core.UriProvider;
import androidx.io.core.video.VideoProvider;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 媒体适配器
 */
public class MediaAdapter extends RecyclerView.Adapter<ViewHolder> {

    public final static int ITEM_FILE = 1;
    public final static int ITEM_IMAGE = 2;
    public final static int ITEM_VIDEO = 3;
    public final static int ITEM_AUDIO = 4;

    private int viewType = ITEM_FILE;
    private Context context;
    private List<Media> data;
    private boolean multiple = true;
    private OnMediaItemClickListener onMediaItemClickListener;

    public MediaAdapter(Context context) {
        this.context = context;
    }

    /**
     * 设置是否可多选
     *
     * @param multiple
     */
    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    /**
     * 是否可多选
     *
     * @return
     */
    public boolean isMultiple() {
        return multiple;
    }

    /**
     * 设置视图类型
     *
     * @param viewType
     */
    public void setViewType(int viewType) {
        this.viewType = viewType;
        notifyDataSetChanged();
    }

    /**
     * 获取Item
     *
     * @param position
     * @return
     */
    public Media getItem(int position) {
        return data.get(position);
    }

    /**
     * 获取数据源
     *
     * @return
     */
    public List<Media> getData() {
        return data;
    }

    /**
     * 设置数据源
     *
     * @param data
     */
    public void setData(List<Media> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    protected int getItemLayoutId(int viewType) {
        if (viewType == ITEM_FILE || viewType == ITEM_AUDIO) {
            return R.layout.io_core_item_file;
        }
        if (viewType == ITEM_IMAGE || viewType == ITEM_VIDEO) {
            return R.layout.io_core_item_image_video;
        }
        return R.layout.io_core_item_file;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(getItemLayoutId(viewType), parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == ITEM_FILE || viewType == ITEM_AUDIO) {
            Media item = getItem(position);
            File file = new File(item.getData());
            RelativeLayout item_view = holder.findView(R.id.item_view);
            ImageView iv_card_icon = holder.findView(R.id.iv_card_icon);
            ImageView iv_tag = holder.findView(R.id.iv_tag);
            TextView tv_duration = holder.findView(R.id.tv_duration);
            TextView tv_name = holder.findView(R.id.tv_name);
            TextView tv_desc = holder.findView(R.id.tv_desc);
            LinearLayout check_group = holder.findView(R.id.check_group);
            ImageView iv_check = holder.findView(R.id.iv_check);
            tv_name.setText(item.getName());
            tv_desc.setTag(position);
            showDesc(tv_desc, item);
            showIconThumbnail(iv_card_icon, iv_tag, tv_duration, file, position);
            check_group.setVisibility(file.isDirectory() ? View.GONE : View.VISIBLE);
            iv_check.setImageResource(item.isCheck() ? R.mipmap.io_core_item_check : R.mipmap.io_core_item_uncheck);
            addItemClick(item_view, position);
            addItemClick(check_group, position);
        }
        if (viewType == ITEM_IMAGE || viewType == ITEM_VIDEO) {
            Media item = getItem(position);
            File file = new File(item.getData());
            FrameLayout item_view = holder.findView(R.id.item_view);
            View v_left = holder.findView(R.id.v_left);
            ImageView iv_icon = holder.findView(R.id.iv_icon);
            ImageView iv_tag = holder.findView(R.id.iv_tag);
            TextView tv_duration = holder.findView(R.id.tv_duration);
            ImageView iv_check = holder.findView(R.id.iv_check);
            View iv_cover = holder.findView(R.id.iv_cover);
            TextView tv_name = holder.findView(R.id.tv_name);
            TextView tv_length = holder.findView(R.id.tv_length);
            v_left.setVisibility((position + 1) % 3 == 1 ? View.VISIBLE : View.GONE);
            showIconThumbnail(iv_icon, iv_tag, tv_duration, file, position);
            tv_name.setText(file.getName());
            tv_length.setText(IOProvider.lengthName(file));
            iv_cover.setVisibility(item.isCheck() ? View.VISIBLE : View.GONE);
            iv_check.setImageResource(item.isCheck() ? R.mipmap.io_core_item_check : R.mipmap.io_core_item_uncheck);
            addItemClick(iv_check, position);
            addItemClick(item_view, position);
        }
    }

    /**
     * 选中item
     *
     * @param position 位置
     */
    public void check(int position) {
        if (isMultiple()) {
            Media item = getItem(position);
            File file = new File(item.getData());
            if (!file.isDirectory()) {
                item.setCheck(!item.isCheck());
                notifyItemChanged(position);
            }
        } else {
            Media document = getItem(position);
            if (document.isCheck()) {
                document.setCheck(false);
                notifyItemChanged(position);
                return;
            }
            for (int i = 0; i < getItemCount(); i++) {
                Media item = getItem(i);
                item.setCheck(i == position);
            }
            notifyDataSetChanged();
        }
    }

    /**
     * 显示描述
     *
     * @param tv_desc 描述显示
     * @param item    文件
     */
    private void showDesc(TextView tv_desc, Media item) {
        String desc = item.getDesc();
        if (TextUtils.isEmpty(desc)) {
            File file = new File(item.getData());
            String date = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(file.lastModified());
            String lengthName = IOProvider.lengthName(new File(file.getPath()));
            if (item.getCount() == -1) {
                int count = file.isDirectory() ? (file.list() == null ? 0 : file.list().length) : 0;
                item.setCount(count);
            }
            if (file.isDirectory()) {
                tv_desc.setText(item.getCount() + "项" + "  " + date);
            } else {
                tv_desc.setText(lengthName + "  " + date);
            }
            item.setDesc(tv_desc.getText().toString());
        } else {
            tv_desc.setText(desc);
        }
    }

    /**
     * 显示图标+缩略图
     *
     * @param icon     图标View
     * @param tag      标识
     * @param duration 视频时长
     * @param file     文件
     * @param position 位置
     */
    private void showIconThumbnail(ImageView icon, ImageView tag, TextView duration, File file, int position) {
        icon.setTag(position);
        tag.setVisibility(View.GONE);
        duration.setVisibility(View.GONE);
        if (file.isDirectory()) {
            icon.setImageResource(R.mipmap.io_core_dir);
        } else {
            icon.setImageResource(R.mipmap.io_core_none);
            String suffix = IOProvider.getSuffix(file);
            if (MediaProvider.isPicture(suffix)) {
                showImageThumbnail(icon, position);
            }
            if (MediaProvider.isVideo(suffix)) {
                showVideoThumbnail(icon, tag, duration, position);
            }
            if (MediaProvider.isAudio(suffix)) {
                Media item = getItem(position);
                if (item.getThumbnail() == null) {
                    icon.setImageResource(R.mipmap.io_core_music);
                } else {
                    Bitmap bitmap = BitmapFactory.decodeFile(item.getThumbnail().getAbsolutePath());
                    icon.setImageBitmap(bitmap);
                }
            }
            if (MediaProvider.isDoc(suffix)) {
                icon.setImageResource(R.mipmap.io_core_doc);
            }
            if (MediaProvider.isPPT(suffix)) {
                icon.setImageResource(R.mipmap.io_core_ppt);
            }
            if (MediaProvider.isPDF(suffix)) {
                icon.setImageResource(R.mipmap.io_core_pdf);
            }
            if (MediaProvider.isTXT(suffix)) {
                icon.setImageResource(R.mipmap.io_core_txt);
            }
            if (MediaProvider.isHtml(suffix)) {
                icon.setImageResource(R.mipmap.io_core_html);
            }
            if (MediaProvider.isJson(suffix)) {
                icon.setImageResource(R.mipmap.io_core_json);
            }
            if (MediaProvider.isXml(suffix)) {
                icon.setImageResource(R.mipmap.io_core_xml);
            }
            if (MediaProvider.isContact(suffix)) {
                icon.setImageResource(R.mipmap.io_core_vcf);
            }
            if (MediaProvider.isCompression(suffix)) {
                icon.setImageResource(R.mipmap.io_core_zip);
            }

        }
    }

    /**
     * 显示图片缩略图
     *
     * @param v        图片View
     * @param position 位置
     */
    private void showImageThumbnail(ImageView v, int position) {
        Media document = getItem(position);
        File file = new File(document.getData());
        File thumbnail = document.getThumbnail();
        if (thumbnail != null && thumbnail.exists() && thumbnail.length() > 0) {
            v.setImageURI(UriProvider.fromFile(context, thumbnail));
        } else {
            Bitmap bitmap = ImageProvider.createImageThumbnail(file.getAbsolutePath(), 160, 160);
            Thumbnail.addThumbnail(context, file, bitmap);
            v.setImageBitmap(bitmap);
        }
    }

    /**
     * 显示视频缩略图
     *
     * @param v        图片View
     * @param tag      视频标识
     * @param duration 视频时长
     * @param position 位置
     */
    private void showVideoThumbnail(ImageView v, ImageView tag, TextView duration, int position) {
        Media document = getItem(position);
        File file = new File(document.getData());
        tag.setVisibility(View.VISIBLE);
        duration.setVisibility(View.VISIBLE);
        long time = document.getDuration();
        if (time == 0) {
            time = VideoProvider.extractDuration(file);
        }
        long second = time / 1000 % 60;
        long minutes = time / 1000 / 60;
        DecimalFormat format = new DecimalFormat("00");
        duration.setText(format.format(minutes) + ":" + format.format(second));
        File thumbnail = document.getThumbnail();
        if (thumbnail != null && thumbnail.exists() && thumbnail.length() > 0) {
            v.setImageURI(UriProvider.fromFile(context, thumbnail));
        } else {
            Bitmap bitmap = VideoProvider.createVideoThumbnail(file.getAbsolutePath(), 160, 160);
            Thumbnail.addThumbnail(context, file, bitmap);
            v.setImageBitmap(bitmap);
        }
    }

    /**
     * 添加点击事件
     *
     * @param view
     * @param position
     */
    private void addItemClick(View view, int position) {
        view.setOnClickListener(v -> {
            if (onMediaItemClickListener != null) {
                onMediaItemClickListener.onMediaItemClick(this, v, position);
            }
        });
    }

    /**
     * 设置文档item点击事件
     *
     * @param onMediaItemClickListener
     */
    public void setOnMediaItemClickListener(OnMediaItemClickListener onMediaItemClickListener) {
        this.onMediaItemClickListener = onMediaItemClickListener;
    }

    /**
     * 选中
     *
     * @param documents
     */
    public void check(List<Media> documents) {
        for (int i = 0; i < (documents == null ? 0 : documents.size()); i++) {
            Media document = documents.get(i);
            long docId = document.getId();
            for (int j = 0; j < getItemCount(); j++) {
                Media item = getItem(j);
                long itemId = item.getId();
                if (itemId == docId) {
                    item.setCheck(true);
                }
            }
        }
        notifyDataSetChanged();
    }

}
