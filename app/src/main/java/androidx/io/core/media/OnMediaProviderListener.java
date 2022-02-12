package androidx.io.core.media;

import java.io.File;

public interface OnMediaProviderListener {

        /**
         * 意图结果处理
         *
         * @param requestCode 请求代码
         * @param file        缓存文件
         */
        void onMediaProviderResult(int requestCode, File file);

    }