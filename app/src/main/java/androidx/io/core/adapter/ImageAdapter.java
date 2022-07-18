package androidx.io.core.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

/**
 * 轮播适配器
 */
public abstract class ImageAdapter<T> extends PagerAdapter implements ViewHolder.OnItemClickLister, ViewHolder.OnItemFocusChangeListener {

    /**
     * 上下文对象
     */
    private Context context;
    /**
     * 数据
     */
    private List<T> data;
    /**
     * ItemView
     */
    private View convertView;
    /**
     * 位置
     */
    private int position;
    /**
     * 是否循环
     */
    private boolean cycle = true;
    /**
     * 控件容器
     */
    private ViewHolder viewHolder;

    public ImageAdapter(Context context) {
        this.context = context;
    }

    /**
     * 获取View容器
     *
     * @return
     */
    public ViewHolder getViewHolder() {
        return viewHolder;
    }

    /**
     * 获取数据大小
     *
     * @return
     */
    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    /**
     * 自定义item视图
     *
     * @return
     */
    public int getItemLayoutResId() {
        return 0;
    }

    /**
     * 获取Item视图
     *
     * @param position 位置
     * @return
     */
    public int getItemViewType(int position) {
        return -1;
    }

    /**
     * 获取Item视图
     *
     * @param context
     * @param viewType
     * @return
     */
    protected View getItemView(Context context, int viewType) {
        return LayoutInflater.from(context).inflate(getItemLayoutResId(), null);
    }

    /**
     * 获取item
     *
     * @param position    位置
     * @param convertView item View
     * @param parent      父控件
     * @return
     */
    protected ViewHolder onCreateViewHolder(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            if (getItemLayoutResId() != 0) {
                convertView = getItemView(getContext(), getItemViewType(position));
            }
            viewHolder = new ViewHolder(convertView);
            viewHolder.setItemPosition(position);
            viewHolder.setOnItemClickLister(this);
            viewHolder.setOnItemFocusChangeListener(this);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        onItemBindViewHolder(viewHolder, position);
        return viewHolder;
    }

    /**
     * 绑定View数据
     *
     * @param holder   控件容器
     * @param position 位置
     */
    public abstract void onItemBindViewHolder(ViewHolder holder, int position);

    /**
     * 实例化Item
     *
     * @param parent   容器
     * @param position 位置
     * @return
     */
    @Override
    public Object instantiateItem(ViewGroup parent, int position) {
        this.position = position;
         viewHolder = onCreateViewHolder(position, null, parent);
        convertView = viewHolder.itemView;
        parent.addView(convertView);
        return convertView;
    }

    /**
     * 判断是否是同一个Item
     *
     * @param view
     * @param object
     * @return
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /**
     * 摧毁item
     *
     * @param container 容器
     * @param position  位置
     * @param object    对象
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        this.position = position;
        container.removeView((View) object);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (onDataSetChangeListener != null) {
            onDataSetChangeListener.onDataSetChanged(this);
        }
    }

    /**
     * 设置数据源
     *
     * @param data  轮播数据
     */
    public void setItems(List<T> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    /**
     * @return 上下文对象
     */
    public Context getContext() {
        return context;
    }

    /**
     * @return 数据源
     */
    public List<T> getItems() {
        return data;
    }

    /**
     * @return 当前位置
     */
    public int getPosition() {
        return position;
    }

    /**
     * @param position
     * @return Item对象
     */
    public T getItem(int position) {
        return data.get(position);
    }

    /**
     * @return Item View
     */
    public View getItemView() {
        return convertView;
    }

    @Override
    public void onItemClick(View v, int position) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(this, v, position);
        }
    }

    @Override
    public void onItemFocusChange(View v, int position, boolean hasFocus) {
        if (onItemFocusChangeListener != null) {
            onItemFocusChangeListener.onItemFocusChange(this, v, position, hasFocus);
        }
    }

    /**
     * 点击事件
     */
    private OnItemClickListener<T> onItemClickListener;

    /**
     * Item点击
     * @param <T>
     */
    public interface OnItemClickListener<T> {

        /**
         * Item点击
         *
         * @param adapter  适配器
         * @param view     视图
         * @param position 位置
         */
        void onItemClick(ImageAdapter<T> adapter, View view, int position);

    }

    /**
     * 设置点击事件
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 焦点改变点击事件
     */
    public OnItemFocusChangeListener<T> onItemFocusChangeListener;

    /**
     * 焦点改变事件
     *
     * @param <T>
     */
    public interface OnItemFocusChangeListener<T> {

        /**
         * 焦点修改
         *
         * @param adapter  适配器
         * @param v        控件
         * @param position 位置
         * @param hasFocus 是否获取焦点
         */
        void onItemFocusChange(ImageAdapter<T> adapter, View v, int position, boolean hasFocus);

    }

    /**
     * @return 焦点改变事件
     */
    public OnItemFocusChangeListener<T> getOnItemFocusChangeListener() {
        return onItemFocusChangeListener;
    }

    /**
     * 设置焦点改变事件
     *
     * @param onItemFocusChangeListener
     */
    public void setOnItemFocusChangeListener(OnItemFocusChangeListener<T> onItemFocusChangeListener) {
        this.onItemFocusChangeListener = onItemFocusChangeListener;
        notifyDataSetChanged();
    }

    /**
     * 数据改变监听
     */
    public OnDataSetChangeListener<T> onDataSetChangeListener;

    public interface OnDataSetChangeListener<T> {

        /**
         * 数据改变监听
         * @param adapter
         */
        void onDataSetChanged(ImageAdapter<T> adapter);

    }

    /**
     * 设置数据改变监听
     *
     * @param onDataSetChangeListener
     */
    public void setOnDataSetChangeListener(OnDataSetChangeListener<T> onDataSetChangeListener) {
        this.onDataSetChangeListener = onDataSetChangeListener;
    }

}
