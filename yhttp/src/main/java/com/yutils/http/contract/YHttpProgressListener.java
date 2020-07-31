package com.yutils.http.contract;

/**
 * 进度监听
 *
 * @author yujing 2020年7月28日10:23:26
 */
public interface YHttpProgressListener {
    void progress(int size, int sizeCount);
}
