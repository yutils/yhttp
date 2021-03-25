package com.yutils.http.contract;

/**
 * 文件下载监听
 *
 * @author yujing 2020年7月28日10:23:26
 */
public interface YHttpLoadListener extends YHttpProgressListener, YFailListener {
    void success(byte[] bytes);
}