package com.yutils.http.contract;

/**
 * 成功监听
 *
 * @author 余静 2021年3月24日23:24:03
 */
public interface YSuccessListener {
    void success(byte[] bytes, String value) throws Exception;
}