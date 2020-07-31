package com.yutils.http;

import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.yutils.http.contract.YHttpDownloadFileListener;
import com.yutils.http.contract.YHttpListener;
import com.yutils.http.contract.YHttpLoadListener;
import com.yutils.http.contract.YObjectListener;
import com.yutils.http.contract.YSessionListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * 网络请求类
 *
 * @author yujing 2020年7月28日10:22:35
 * 1.支持get，post请求
 * 2.支持文件上传下载，回调进度条
 * 3.post请求可为字符串，map，byte[]
 * 4.可以直接返回字符串
 * 5.直接返回对象
 * 6.异常回调原因
 * 7.支持https,设置ssl文件
 * 8.sessionID的获取和设置
 * 9.简单好用
 */
/*用法
    //java
    YHttp.create().post(url, hashMap, new YObjectListener<User>() {
        @Override
        public void success(byte[] bytes, User value) {
            //请求结果,对象
        }

        @Override
        public void fail(String value) {
            //错误原因
        }
    });

    //kotlin
    val url = "http://192.168.6.9:8090/crash/"
    YHttp.create().setSessionId(session).get(url, object : YObjectListener<User>(){
        override fun success(bytes: ByteArray?, value: User?) {
            //成功 User对象
        }

        override fun fail(value: String?) {
            //错误原因
        }
    })
 */
public class YHttp extends YHttpBase {
    private static final String TAG = "YHttp";
    private static volatile boolean showLog = true;
    private Object handler;

    public static void setShowLog(boolean showLog) {
        YHttp.showLog = showLog;
    }

    public YHttp() {
        //如果是能找到Handler对象，说明是安卓
        try {
            Class.forName("android.os.Handler");
            handler = new Handler();
        } catch (Exception ignored) {
        }
    }

    public static YHttp create() {
        return new YHttp();
    }

    @Override
    public YHttp setContentType(String contentType) {
        super.setContentType(contentType);
        return this;
    }

    @Override
    public YHttp setConnectTimeout(int connectTimeout) {
        super.setConnectTimeout(connectTimeout);
        return this;
    }

    @Override
    public YHttp setCrtSSL(String crtSSL) {
        super.setCrtSSL(crtSSL);
        return this;
    }

    @Override
    public YHttp setRequestProperty(String key, String value) {
        super.setRequestProperty(key, value);
        return this;
    }

    @Override
    public YHttp addRequestProperty(String key, String value) {
        super.addRequestProperty(key, value);
        return this;
    }

    @Override
    public YHttp setSessionListener(YSessionListener ySessionListener) {
        super.setSessionListener(ySessionListener);
        return this;
    }

    @Override
    public YHttp setSessionId(String sessionId) {
        super.setSessionId(sessionId);
        return this;
    }

    /**
     * get请求
     *
     * @param requestUrl url
     * @param listener   监听
     */
    public void get(final String requestUrl, final YHttpListener listener) {
        Thread thread = new Thread(() -> {
            try {
                byte[] bytes = get(requestUrl);
                String result = new String(bytes);
                if (showLog)
                    println("请求地址：\nGet--->" + requestUrl + "\n请求结果：" + result);
                //如果是安卓就用handler调回到主线程，如果是普通JAVA工程，直接回调到线程
                if (handler != null && handler instanceof Handler) {
                    ((Handler) handler).post(new YRunnable(() -> {
                        try {
                            listener.success(bytes, result);
                        } catch (Exception e) {
                            listener.fail("处理异常");
                            e.printStackTrace();
                        }
                    }));
                } else {
                    listener.success(bytes, result);
                }
            } catch (Exception e) {
                exception(e, listener);
            } finally {
                YHttpThreadPool.shutdown();
            }
        });
        YHttpThreadPool.add(thread);
    }

    /**
     * get请求
     *
     * @param requestUrl url
     * @param listener   监听
     * @param <T>        类型
     */
    public <T> void get(final String requestUrl, final YObjectListener<T> listener) {
        get(requestUrl, new YHttpListener() {
            @Override
            public void success(byte[] bytes, String value) {
                println("对象转换类型：" + listener.getType());
                try {
                    //如果是安卓就用handler调回到主线程，如果是普通JAVA工程，直接回调到线程
                    if (handler != null && handler instanceof Handler) {
                        ((Handler) handler).post(new YRunnable(() -> {
                            try {
                                if (String.class.equals(listener.getType())) {
                                    listener.success(bytes, (T) value);
                                } else if ("byte[]".equals(listener.getType().toString())) {
                                    listener.success(bytes, (T) bytes);
                                } else {
                                    Gson gson = new Gson();
                                    T object = gson.fromJson(value, listener.getType());
                                    listener.success(bytes, object);
                                }
                            } catch (Exception e) {
                                listener.fail("处理异常");
                                e.printStackTrace();
                            }
                        }));
                    } else {
                        if (String.class.equals(listener.getType())) {
                            listener.success(bytes, (T) value);
                        } else if ("byte[]".equals(listener.getType().toString())) {
                            listener.success(bytes, (T) bytes);
                        } else {
                            Gson gson = new Gson();
                            T object = gson.fromJson(value, listener.getType());
                            listener.success(bytes, object);
                        }
                    }
                } catch (java.lang.ClassCastException e) {
                    listener.fail("对象转换失败");
                    e.printStackTrace();
                } catch (Exception e) {
                    listener.fail("处理异常");
                    e.printStackTrace();
                }
            }

            @Override
            public void fail(String value) {
                listener.fail(value);
            }
        });
    }

    /**
     * post请求
     *
     * @param requestUrl url
     * @param paramsMap  key，value
     * @param listener   监听
     */
    public void post(final String requestUrl, Map<String, Object> paramsMap, final YHttpListener listener) {
        post(requestUrl, YHttpUtils.mapToParams(paramsMap).toString().getBytes(), listener);
    }

    /**
     * post请求
     *
     * @param requestUrl url
     * @param params     文本
     * @param listener   监听
     */
    public void post(final String requestUrl, String params, final YHttpListener listener) {
        post(requestUrl, params.getBytes(), listener);
    }

    /**
     * post请求
     *
     * @param requestUrl   url
     * @param requestBytes bytes
     * @param listener     监听
     */
    public void post(final String requestUrl, final byte[] requestBytes, final YHttpListener listener) {
        Thread thread = new Thread(() -> {
            try {
                byte[] bytes = post(requestUrl, requestBytes);
                String result = new String(bytes);
                if (showLog)
                    println("请求地址：\nPost--->" + requestUrl + (requestBytes == null ? "" : ("\n请求参数：" + new String(requestBytes))) + "\n请求结果：" + result);
                //如果是安卓就用handler调回到主线程，如果是普通JAVA工程，直接回调到线程
                if (handler != null && handler instanceof Handler) {
                    ((Handler) handler).post(new YRunnable(() -> {
                        try {
                            listener.success(bytes, result);
                        } catch (Exception e) {
                            listener.fail("处理异常");
                            e.printStackTrace();
                        }
                    }));
                } else {
                    listener.success(bytes, result);
                }
            } catch (Exception e) {
                exception(e, listener);
            } finally {
                YHttpThreadPool.shutdown();
            }
        });
        YHttpThreadPool.add(thread);
    }

    /**
     * post请求
     *
     * @param requestUrl url
     * @param paramsMap  key，value
     * @param listener   监听
     * @param <T>        类型
     */
    public <T> void post(String requestUrl, Map<String, Object> paramsMap, YObjectListener<T> listener) {
        post(requestUrl, YHttpUtils.mapToParams(paramsMap).toString().getBytes(), listener);
    }

    /**
     * post请求
     *
     * @param requestUrl url
     * @param params     文本
     * @param listener   监听
     * @param <T>        类型
     */
    public <T> void post(String requestUrl, String params, YObjectListener<T> listener) {
        post(requestUrl, params.getBytes(), listener);
    }

    /**
     * post请求
     *
     * @param requestUrl   url
     * @param requestBytes bytes
     * @param listener     监听
     * @param <T>          类型
     */
    public <T> void post(String requestUrl, byte[] requestBytes, final YObjectListener<T> listener) {
        post(requestUrl, requestBytes, new YHttpListener() {
            @Override
            public void success(byte[] bytes, String value) {
                println("对象转换类型：" + listener.getType());
                try {
                    //如果是安卓就用handler调回到主线程，如果是普通JAVA工程，直接回调到线程
                    if (handler != null && handler instanceof Handler) {
                        ((Handler) handler).post(new YRunnable(() -> {
                            try {
                                if (String.class.equals(listener.getType())) {
                                    listener.success(bytes, (T) value);
                                } else if ("byte[]".equals(listener.getType().toString())) {
                                    listener.success(bytes, (T) bytes);
                                } else {
                                    Gson gson = new Gson();
                                    T object = gson.fromJson(value, listener.getType());
                                    listener.success(bytes, object);
                                }
                            } catch (Exception e) {
                                listener.fail("处理异常");
                                e.printStackTrace();
                            }
                        }));
                    } else {
                        if (String.class.equals(listener.getType())) {
                            listener.success(bytes, (T) value);
                        } else if ("byte[]".equals(listener.getType().toString())) {
                            listener.success(bytes, (T) bytes);
                        } else {
                            Gson gson = new Gson();
                            T object = gson.fromJson(value, listener.getType());
                            listener.success(bytes, object);
                        }
                    }
                } catch (java.lang.ClassCastException e) {
                    listener.fail("对象转换失败");
                    e.printStackTrace();
                } catch (Exception e) {
                    listener.fail("处理异常");
                    e.printStackTrace();
                }
            }

            @Override
            public void fail(String value) {
                listener.fail(value);
            }
        });
    }

    /**
     * 文件上传post
     *
     * @param requestUrl url
     * @param paramsMap  key，value
     * @param fileMap    文件列表
     * @param listener   监听
     */
    public void upload(final String requestUrl, Map<String, Object> paramsMap, Map<String, File> fileMap, final YHttpListener listener) {
        upload(requestUrl, YHttpUtils.mapToParams(paramsMap).toString().getBytes(), fileMap, listener);
    }

    /**
     * 文件上传post
     *
     * @param requestUrl url
     * @param params     文本
     * @param fileMap    文件列表
     * @param listener   监听
     */
    public void upload(final String requestUrl, String params, Map<String, File> fileMap, final YHttpListener listener) {
        upload(requestUrl, params.getBytes(), fileMap, listener);
    }

    /**
     * 文件上传post
     *
     * @param requestUrl   url
     * @param requestBytes bytes
     * @param fileMap      文件列表
     * @param listener     监听
     */
    public void upload(final String requestUrl, final byte[] requestBytes, final Map<String, File> fileMap, final YHttpListener listener) {
        Thread thread = new Thread(() -> {
            try {
                if (showLog)
                    println("文件上传开始：\nupload--->" + requestUrl + (requestBytes == null ? "" : ("\n文件数：" + fileMap.size() + "\n请求参数：" + new String(requestBytes))));
                byte[] bytes = upload(requestUrl, requestBytes, fileMap);
                String result = new String(bytes);
                if (showLog)
                    println("文件上传完成：\nupload--->" + requestUrl + (requestBytes == null ? "" : ("\n文件数：" + fileMap.size() + "\n请求参数：" + new String(requestBytes))) + "\n请求结果：" + result);
                //如果是安卓就用handler调回到主线程，如果是普通JAVA工程，直接回调到线程
                if (handler != null && handler instanceof Handler) {
                    ((Handler) handler).post(new YRunnable(() -> {
                        try {
                            listener.success(bytes, result);
                        } catch (Exception e) {
                            listener.fail("处理异常");
                            e.printStackTrace();
                        }
                    }));
                } else {
                    listener.success(bytes, result);
                }
            } catch (Exception e) {
                exception(e, listener);
            } finally {
                YHttpThreadPool.shutdown();
            }
        });
        YHttpThreadPool.add(thread);
    }

    /**
     * 文件下载,get请求，回调进度
     *
     * @param requestUrl url
     * @param file       保存的文件
     * @param listener   监听
     */
    public void downloadFile(final String requestUrl, final File file, final YHttpDownloadFileListener listener) {
        Thread thread = new Thread(() -> {
            try {
                if (showLog)
                    println("文件下载开始：\nGet--->" + requestUrl + "\n保存路径：" + file.getPath());
                downloadFile(requestUrl, file, (size, sizeCount) -> {
                    //如果是安卓就用handler调回到主线程，如果是普通JAVA工程，直接回调到线程
                    if (handler != null && handler instanceof Handler) {
                        ((Handler) handler).post(new YRunnable(() -> {
                            try {
                                listener.progress(size, sizeCount);
                            } catch (Exception e) {
                                listener.fail("处理异常");
                                e.printStackTrace();
                            }
                        }));
                    } else {
                        listener.progress(size, sizeCount);
                    }
                });
                if (showLog)
                    println("文件下载完成：\nGet--->" + requestUrl + "\n保存路径：" + file.getPath());
                //如果是安卓就用handler调回到主线程，如果是普通JAVA工程，直接回调到线程
                if (handler != null && handler instanceof Handler) {
                    ((Handler) handler).post(new YRunnable(() -> {
                        try {
                            listener.success(file);
                        } catch (Exception e) {
                            listener.fail("处理异常");
                            e.printStackTrace();
                        }
                    }));
                } else {
                    listener.success(file);
                }
            } catch (Exception e) {
                exception(e, listener);
            } finally {
                YHttpThreadPool.shutdown();
            }
        });
        YHttpThreadPool.add(thread);
    }

    /**
     * 加载get请求，回调进度
     *
     * @param requestUrl url
     * @param listener   监听
     */
    public void load(final String requestUrl, final YHttpLoadListener listener) {
        Thread thread = new Thread(() -> {
            try {
                if (showLog)
                    println("文件加载开始：\nGet--->" + requestUrl);
                byte[] bytes = load(requestUrl, listener::progress);
                if (showLog)
                    println("文件加载完成：\nGet--->" + requestUrl);
                //如果是安卓就用handler调回到主线程，如果是普通JAVA工程，直接回调到线程
                if (handler != null && handler instanceof Handler) {
                    ((Handler) handler).post(new YRunnable(() -> {
                        try {
                            listener.success(bytes);
                        } catch (Exception e) {
                            listener.fail("处理异常");
                            e.printStackTrace();
                        }
                    }));
                } else {
                    listener.success(bytes);
                }
            } catch (Exception e) {
                exception(e, listener);
            } finally {
                YHttpThreadPool.shutdown();
            }
        });
        YHttpThreadPool.add(thread);
    }

    /**
     * 错误情况处理
     *
     * @param e        错误
     * @param listener 监听
     */
    void exception(Exception e, Object listener) {
        //如果是安卓就用handler调回到主线程，如果是普通JAVA工程，直接回调到线程
        if (handler != null && handler instanceof Handler) {
            ((Handler) handler).post(new YRunnable(() -> {
                if (e instanceof MalformedURLException) {
                    error("URL地址不规范", listener);
                } else if (e instanceof java.net.SocketTimeoutException) {
                    error("网络连接超时", listener);
                } else if (e instanceof UnsupportedEncodingException) {
                    error("不支持的编码", listener);
                } else if (e instanceof FileNotFoundException) {
                    error("找不到该地址", listener);
                } else if (e instanceof IOException) {
                    error("连接服务器失败", listener);
                } else {
                    error("请求失败 " + e.getMessage(), listener);
                }
            }));
        } else {
            if (e instanceof MalformedURLException) {
                error("URL地址不规范", listener);
            } else if (e instanceof java.net.SocketTimeoutException) {
                error("网络连接超时", listener);
            } else if (e instanceof UnsupportedEncodingException) {
                error("不支持的编码", listener);
            } else if (e instanceof FileNotFoundException) {
                error("找不到该地址", listener);
            } else if (e instanceof IOException) {
                error("连接服务器失败", listener);
            } else {
                error("请求失败 " + e.getMessage(), listener);
            }
        }

    }

    /**
     * 错误回调
     *
     * @param error    错误
     * @param listener 监听
     */
    void error(String error, Object listener) {
        if (listener instanceof YHttpListener) {
            println(TAG + error);
            ((YHttpListener) listener).fail(error);
        } else if (listener instanceof YHttpLoadListener) {
            println(TAG + error);
            ((YHttpLoadListener) listener).fail(error);
        } else if (listener instanceof YHttpDownloadFileListener) {
            println(TAG + error);
            ((YHttpDownloadFileListener) listener).fail(error);
        }
    }

    /**
     * 打印日志。如果发现包含Log就用Log打印，否则就用println
     *
     * @param str 日志
     */
    void println(String str) {
        try {
            Class.forName("android.util.Log");
            println("YHttp", str);
        } catch (Exception e) {
            System.out.println(str);
        }
    }

    /**
     * 打印日志
     *
     * @param TAG tag
     * @param msg 内容
     */
    private static void println(String TAG, String msg) {
        int LOG_MAX_LENGTH = 2000;
        int strLength = msg.length();
        int start = 0;
        int end = LOG_MAX_LENGTH;
        for (int i = 0; i < 100; i++) {
            //剩下的文本还是大于规定长度则继续重复截取并输出
            if (strLength > end) {
                String tag = TAG + " " + i;
                Log.d(tag, msg.substring(start, end));
                start = end;
                end = end + LOG_MAX_LENGTH;
            } else {
                String tag = i == 0 ? TAG : TAG + " " + i;
                Log.d(tag, msg.substring(start, strLength));
                break;
            }
        }
    }
}
