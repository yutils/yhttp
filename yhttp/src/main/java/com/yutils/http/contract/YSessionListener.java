package com.yutils.http.contract;

/**
 * sessionId 监听
 * 返回值为新的sessionId
 * @author 余静 2020年7月28日10:23:26
 */
public interface YSessionListener {
    void sessionId(String sessionId);
}