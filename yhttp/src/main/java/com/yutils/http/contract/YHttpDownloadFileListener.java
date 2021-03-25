package com.yutils.http.contract;

import java.io.File;

/**
 * 文件下载监听
 *
 * @author yujing 2020年7月28日10:23:26
 */
public interface YHttpDownloadFileListener extends YHttpProgressListener, YFailListener {
    void success(File file);
}