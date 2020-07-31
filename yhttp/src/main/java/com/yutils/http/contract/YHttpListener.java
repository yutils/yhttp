package com.yutils.http.contract;

/**
 * 请求信息监听
 *
 * @author 余静 2020年7月28日10:23:26
 */
public interface YHttpListener {
    void success(byte[] bytes, String value) throws Exception;

    void fail(String value);
}