package com.yutils.http;

import com.google.gson.Gson;
import com.yutils.http.contract.ObjectListener;
import com.yutils.http.contract.YFailListener;
import com.yutils.http.contract.YHttpDownloadFileListener;
import com.yutils.http.contract.YHttpListener;
import com.yutils.http.contract.YHttpLoadListener;
import com.yutils.http.contract.YObjectListener;
import com.yutils.http.contract.YSessionListener;
import com.yutils.http.contract.YSuccessListener;
import com.yutils.http.model.Upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 网络请求类
 *
 * @author yujing 2021年3月25日11:14:17
 * 详细说明和最新版请查看 https://github.com/yutils/yhttp
 * 优点：
 * 1.支持get，post，put，delete请求，一行实现同步请求，一行实现异步请求
 * 2.post请求可为字符串，map，byte[]
 * 3.直接返回请求结果为 字符串 或 对象 或 byte[]
 * 4.支持各种格式文件下载，回调进度条
 * 5.单次文件上传请求中可以包含多种不同格式文件
 * 6.日志详细记录请求数据和结果，可关闭日志，异常请求回调异常原因
 * 7.支持https,支持设置ssl文件
 * 8.sessionID的获取和设置，可以切换多个不同sessionID
 * 9.简单好用,作者持续更新
 * 10.支持安卓，请求结果直接回调到UI线程（主线程）
 * 11.自定义gson，如：可以直接序列化指定日期格式的内容
 * 12.链式调用，代码更加简洁，可以不监听失败回调
 */
/*基本用法举例
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

//java文件上传
String url = "http://192.168.6.9:8090/crash/upload/file";
List<Upload> uploads = new ArrayList<>();
uploads.add(new Upload("file1", new File("D:/1.jpg")));
uploads.add(new Upload("file2", "ABCDEF".getBytes()).setFilename("abcd.txt"));

YHttp.create().setSessionId(session).upload(url, "", uploads, new YHttpListener() {
    @Override
    public void success(byte[] bytes, String value){
        System.out.println("上传成功：" + value);
    }
    @Override
    public void fail(String value) {
        System.out.println("上传失败：" + value);
    }
});


//kotlin
val url = "https://down.qq.com/qqweb/PCQQ/PCQQ_EXE/PCQQ2020.exe"
YHttp.create().setSessionId(session).get(url, object : YObjectListener<User>(){
    override fun success(bytes: ByteArray?, value: User?) {
        //成功 User对象
    }

    override fun fail(value: String?) {
        //错误原因
    }
})

//文件上传
val url = "http://192.168.6.9:8090/crash/upload/file"
val list: MutableList<Upload> = ArrayList()
list.add(Upload("file1",file))
list.add(Upload("file2", "ABCDEF".toByteArray()).setFilename("abcd.txt"))
list.add(Upload("file3",bitmap))
YHttp.create().setSessionId(session).upload(url, "", list, object : YHttpListener {
    override fun success(bytes: ByteArray?, value: String?) {
        //成功
    }
    override fun fail(value: String?) {
        //失败
    }
})

//文件下载，如是安卓，返回值自动回到主线程
val url = "https://down.qq.com/qqweb/PCQQ/PCQQ_EXE/PCQQ2020.exe"
var f = File( "D:/BB.exe")
YHttp.create().downloadFile(url, f, object :
    YHttpDownloadFileListener {
    override fun progress(downloadSize: Int, fileSize: Int) {
         val progress = (10000.0 * downloadSize / fileSize).toInt() / 100.0 //下载进度，保留2位小数
    }
    override fun success(file: File) {}//下载完成
    override fun fail(value: String) {}//下载出错
})

//链式请求 java
String url = "http://192.168.6.9:8090/crash/user/login";
Map<String, Object> hashMap = new HashMap<String, Object>();// = hashMapOf("name" to "yujing", "password" to "wtugeqh")
hashMap.put("name", "yujing");
hashMap.put("password", "wtugeqh");
YHttp.create()
        .url(url)
        .post(hashMap)
        .setSuccessListener((bytes, value) -> {
            System.out.println("请求成功：" + value);
        }).start();

//链式,get
YHttp
.create()
.url("http://192.168.6.9:8090/crash/")
.get()
.setSuccessListener { bytes, value -> textView1.text = "成功：$value" }
.setFailListener { value -> textView2.text = "失败：$value" }
.start()

//链式,post
val url = "http://192.168.6.9:8090/crash/user/login"
val gson=YJson.getGsonDate( "yyyy年MM月dd日 HH:mm:ss")
val hashMap: HashMap<String, Any> = hashMapOf("name" to "yujing", "password" to "wtugeqh")
YHttp.create()
    .url(url)
    .post(hashMap)
    .setGson(gson)
    .setObjectListener(object : ObjectListener<User>() {
        override fun success(bytes: ByteArray?, value: User?) {
            textView2.text = "\n对象：${value.toString()}"
        }
    })
    .setFailListener { value -> textView2.text = "失败：$value" }
    .start()

//链式,自定义请求
YHttp.create()
    .url(url)
    .method("POST")
    .setContentType("application/x-www-form-urlencoded;charset=utf-8")
    .addRequestProperty("connection", "Keep-Alive")
    .body(hashMap)
    .setGson(gson)
    .setSessionId(session)
    .setCrtSSL("SSL证书内容")
    .setSuccessListener { bytes, value -> textView1.text = "成功：$value" }
    .setObjectListener(object : ObjectListener<User>() {
        override fun success(bytes: ByteArray?, value: User?) {
            textView2.text = "\n对象：${value.toString()}"
        }
    })
    .setFailListener { value -> textView2.text = "失败：$value" }
    .setSessionListener { sessionId -> session = sessionId }
    .start()
 */
public class YHttp<T> extends YHttpBase {
    private static volatile boolean showLog = true;
    private Object gson;//防止对方没引用Gson时完全无法使用
    private String requestUrl;//请求url
    private byte[] requestBytes;//请求内容
    private String requestMethod;//请求方式，只能是 "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
    private YSuccessListener successListener;//成功监听
    private ObjectListener<T> objectListener;//成功监听,返回对象
    private YFailListener failListener;//失败监听

    /**
     * 设置 是否显示日志
     *
     * @param showLog 是否显示日志
     */
    public static void setShowLog(boolean showLog) {
        YHttp.showLog = showLog;
    }

    public YHttp() {
    }

    /**
     * 获取YHttp实例
     *
     * @return YHttp
     */
    public static YHttp create() {
        return new YHttp();
    }

    /**
     * 获取当前Gson对象
     *
     * @return Gson
     */
    public Gson getGson() {
        if (gson != null && gson instanceof Gson) {
            return (Gson) gson;
        } else {
            gson = new Gson();
        }
        return (Gson) gson;
    }

    /**
     * 设置当前Gson对象
     *
     * @param gson Gson
     * @return YHttp
     */
    public YHttp setGson(Gson gson) {
        this.gson = gson;
        return this;
    }

    /**
     * @param contentType 设置contentType，常见如
     *                    "application/x-www-form-urlencoded;charset=utf-8"
     *                    "application/json;charset=utf-8"
     * @return YHttp
     */
    @Override
    public YHttp setContentType(String contentType) {
        super.setContentType(contentType);
        return this;
    }

    /**
     * 设置超时时间
     *
     * @param connectTimeout 毫秒
     * @return YHttp
     */
    @Override
    public YHttp setConnectTimeout(int connectTimeout) {
        super.setConnectTimeout(connectTimeout);
        return this;
    }

    /**
     * 设置SSL证书内容
     *
     * @param crtSSL ssl
     * @return YHttp
     */
    @Override
    public YHttp setCrtSSL(String crtSSL) {
        super.setCrtSSL(crtSSL);
        return this;
    }

    /**
     * 设置请求头
     * 如：
     * "connection","Keep-Alive"
     * "Charset","utf-8"
     *
     * @param key   key
     * @param value value
     * @return YHttp
     */
    @Override
    public YHttp setRequestProperty(String key, String value) {
        super.setRequestProperty(key, value);
        return this;
    }

    /**
     * 添加请求头
     * 如：
     * "connection","Keep-Alive"
     * "Charset","utf-8"
     *
     * @param key   key
     * @param value value
     * @return YHttp
     */
    @Override
    public YHttp addRequestProperty(String key, String value) {
        super.addRequestProperty(key, value);
        return this;
    }

    /**
     * 设置 Session回调
     *
     * @param ySessionListener 回调服务返回的sessionId
     * @return YHttp
     */
    @Override
    public YHttp setSessionListener(YSessionListener ySessionListener) {
        super.setSessionListener(ySessionListener);
        return this;
    }

    /**
     * 设置 sessionId
     *
     * @param sessionId sessionId
     * @return YHttp
     */
    @Override
    public YHttp setSessionId(String sessionId) {
        super.setSessionId(sessionId);
        return this;
    }

    /**
     * 设置 请求地址
     *
     * @param url url地址，http:// 或 https:// 开头
     * @return YHttp
     */
    public YHttp url(String url) {
        requestUrl = url;
        return this;
    }

    /**
     * 设置 请求内容
     *
     * @param json json/文本
     * @return YHttp
     */
    public YHttp body(String json) {
        requestBytes = json.getBytes();
        return this;
    }

    /**
     * 设置 请求内容
     *
     * @param paramsMap paramsMap，key，value方式
     * @return YHttp
     */
    public YHttp body(Map<String, Object> paramsMap) {
        requestBytes = YHttpUtils.mapToParams(paramsMap).toString().getBytes();
        return this;
    }

    /**
     * 设置 请求内容
     *
     * @param bytes bytes
     * @return YHttp
     */
    public YHttp body(byte[] bytes) {
        requestBytes = bytes;
        return this;
    }

    /**
     * 设置 请求方式
     *
     * @param method "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
     * @return YHttp
     */
    public YHttp method(String method) {
        this.requestMethod = method;
        return this;
    }

    /**
     * 设置 请求成功监听，回调字符串
     *
     * @param successListener 成功返回请求的字符串，和byte数组
     * @return YHttp
     */
    public YHttp setSuccessListener(YSuccessListener successListener) {
        this.successListener = successListener;
        return this;
    }

    /**
     * 设置 请求成功监听，回调对象
     *
     * @param objectListener 成功返回object
     * @return YHttp
     */
    public YHttp setObjectListener(ObjectListener<T> objectListener) {
        this.objectListener = objectListener;
        return this;
    }

    /**
     * 设置 请求失败监听
     *
     * @param failListener 失败返回原因
     * @return YHttp
     */
    public YHttp setFailListener(YFailListener failListener) {
        this.failListener = failListener;
        return this;
    }

    /**
     * 设置请求类型 get
     *
     * @return YHttp
     */
    public YHttp get() {
        requestMethod = "GET";
        return this;
    }

    /**
     * 设置请求类型 post
     *
     * @return YHttp
     */
    public YHttp post() {
        requestMethod = "POST";
        return this;
    }

    /**
     * 设置请求类型 post
     *
     * @param json json/字符串
     * @return YHttp
     */
    public YHttp post(String json) {
        if (getContentType() == null) setContentType("application/json;charset=utf-8");
        return post().body(json);
    }

    /**
     * 设置请求类型 post
     *
     * @param paramsMap paramsMap
     * @return YHttp
     */
    public YHttp post(Map<String, Object> paramsMap) {
        return post().body(paramsMap);
    }

    /**
     * 设置请求类型 post
     *
     * @param bytes bytes
     * @return YHttp
     */
    public YHttp post(byte[] bytes) {
        return post().body(bytes);
    }

    /**
     * 设置请求类型 put
     *
     * @return YHttp
     */
    public YHttp put() {
        requestMethod = "PUT";
        return this;
    }

    /**
     * 设置请求类型 put
     *
     * @param json json/字符串
     * @return YHttp
     */
    public YHttp put(String json) {
        if (getContentType() == null) setContentType("application/json;charset=utf-8");
        return put().body(json);
    }

    /**
     * 设置请求类型 put
     *
     * @param paramsMap paramsMap
     * @return YHttp
     */
    public YHttp put(Map<String, Object> paramsMap) {
        return put().body(paramsMap);
    }

    /**
     * 设置请求类型 put
     *
     * @param bytes bytes
     * @return YHttp
     */
    public YHttp put(byte[] bytes) {
        return put().body(bytes);
    }

    /**
     * 设置请求类型 delete
     *
     * @return YHttp
     */
    public YHttp delete() {
        requestMethod = "DELETE";
        return this;
    }

    /**
     * 设置请求类型 delete
     *
     * @param json json/字符串
     * @return YHttp
     */
    public YHttp delete(String json) {
        if (getContentType() == null) setContentType("application/json;charset=utf-8");
        return delete().body(json);
    }

    /**
     * 设置请求类型 delete
     *
     * @param paramsMap paramsMap
     * @return YHttp
     */
    public YHttp delete(Map<String, Object> paramsMap) {
        return delete().body(paramsMap);
    }

    /**
     * 设置请求类型 delete
     *
     * @param bytes bytes
     * @return YHttp
     */
    public YHttp delete(byte[] bytes) {
        return delete().body(bytes);
    }

    /**
     * 设置请求成功监听
     *
     * @param listener 成功返回object，失败返回原因
     */
    public void YObjectListener(YObjectListener<T> listener) {
        YObjectListener<T> listener2 = listener;
    }

    /**
     * 开始网络请求，链式时可以调用
     */
    public void start() {
        request(requestUrl, requestBytes, requestMethod, new YHttpListener() {
            @Override
            public void success(byte[] bytes, String value) throws Exception {
                if (successListener != null) successListener.success(bytes, value);
                if (objectListener != null) {
                    println("json转对象：" + objectListener.getType());
                    try {
                        //如果是安卓就用handler调回到主线程，如果是普通JAVA工程，直接回调到线程
                        Android.runOnUiThread(() -> {
                            try {
                                if (String.class.equals(objectListener.getType())) {
                                    objectListener.success(bytes, (T) value);
                                } else if ("byte[]".equals(objectListener.getType().toString())) {
                                    objectListener.success(bytes, (T) bytes);
                                } else {
                                    T object = getGson().fromJson(value, objectListener.getType());
                                    objectListener.success(bytes, object);
                                }
                            } catch (Exception e) {
                                if (failListener != null)
                                    failListener.fail("异常：" + e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    } catch (java.lang.ClassCastException e) {
                        if (failListener != null)
                            failListener.fail("对象转换失败");
                        e.printStackTrace();
                    } catch (Exception e) {
                        if (failListener != null)
                            failListener.fail("异常：" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void fail(String value) {
                failListener.fail(value);
            }
        });
    }
    //----------------------------------------------GET----------------------------------------------

    /**
     * get请求
     *
     * @param requestUrl url
     * @param listener   监听
     */
    public void get(final String requestUrl, YHttpListener listener) {
        request(requestUrl, new byte[0], "GET", listener);
    }

    /**
     * get请求
     *
     * @param requestUrl url
     * @param listener   监听
     */
    public void get(final String requestUrl, YObjectListener<T> listener) {
        request(requestUrl, new byte[0], "GET", listener);
    }
    //----------------------------------------------POST----------------------------------------------

    /**
     * post请求
     *
     * @param requestUrl url
     * @param paramsMap  key，value
     * @param listener   监听
     */
    public void post(final String requestUrl, Map<String, Object> paramsMap, YHttpListener listener) {
        request(requestUrl, YHttpUtils.mapToParams(paramsMap).toString().getBytes(), "POST", listener);
    }

    /**
     * post请求
     *
     * @param requestUrl url
     * @param json       json/文本
     * @param listener   监听
     */
    public void post(final String requestUrl, String json, YHttpListener listener) {
        if (getContentType() == null) setContentType("application/json;charset=utf-8");
        request(requestUrl, json.getBytes(), "POST", listener);
    }

    /**
     * post请求
     *
     * @param requestUrl   url
     * @param requestBytes bytes
     * @param listener     监听
     */
    public void post(final String requestUrl, byte[] requestBytes, YHttpListener listener) {
        request(requestUrl, requestBytes, "POST", listener);
    }

    /**
     * post请求
     *
     * @param requestUrl url
     * @param paramsMap  key，value
     * @param listener   监听
     */
    public void post(String requestUrl, Map<String, Object> paramsMap, YObjectListener<T> listener) {
        request(requestUrl, YHttpUtils.mapToParams(paramsMap).toString().getBytes(), "POST", listener);
    }

    /**
     * post请求
     *
     * @param requestUrl url
     * @param json       json/文本
     * @param listener   监听
     */
    public void post(String requestUrl, String json, YObjectListener<T> listener) {
        if (getContentType() == null) setContentType("application/json;charset=utf-8");
        request(requestUrl, json.getBytes(), "POST", listener);
    }

    /**
     * post请求
     *
     * @param requestUrl   url
     * @param requestBytes bytes
     * @param listener     监听
     */
    public void post(String requestUrl, byte[] requestBytes, YObjectListener<T> listener) {
        request(requestUrl, requestBytes, "POST", listener);
    }

    //----------------------------------------------PUT----------------------------------------------

    /**
     * put请求
     *
     * @param requestUrl url
     * @param paramsMap  key，value
     * @param listener   监听
     */
    public void put(String requestUrl, Map<String, Object> paramsMap, YHttpListener listener) {
        request(requestUrl, YHttpUtils.mapToParams(paramsMap).toString().getBytes(), "PUT", listener);
    }

    /**
     * put请求
     *
     * @param requestUrl url
     * @param json       json/文本
     * @param listener   监听
     */
    public void put(String requestUrl, String json, YHttpListener listener) {
        if (getContentType() == null) setContentType("application/json;charset=utf-8");
        request(requestUrl, json.getBytes(), "PUT", listener);
    }

    /**
     * put请求
     *
     * @param requestUrl   url
     * @param requestBytes bytes
     * @param listener     监听
     */
    public void put(String requestUrl, byte[] requestBytes, YHttpListener listener) {
        request(requestUrl, requestBytes, "PUT", listener);
    }

    /**
     * put请求
     *
     * @param requestUrl url
     * @param paramsMap  key，value
     * @param listener   监听
     */
    public void put(String requestUrl, Map<String, Object> paramsMap, YObjectListener<T> listener) {
        request(requestUrl, YHttpUtils.mapToParams(paramsMap).toString().getBytes(), "PUT", listener);
    }

    /**
     * put请求
     *
     * @param requestUrl url
     * @param json       json/文本
     * @param listener   监听
     */
    public void put(String requestUrl, String json, YObjectListener<T> listener) {
        if (getContentType() == null) setContentType("application/json;charset=utf-8");
        request(requestUrl, json.getBytes(), "PUT", listener);
    }

    /**
     * put请求
     *
     * @param requestUrl   url
     * @param requestBytes bytes
     * @param listener     监听
     */
    public void put(String requestUrl, byte[] requestBytes, YObjectListener<T> listener) {
        request(requestUrl, requestBytes, "PUT", listener);
    }

    //----------------------------------------------DELETE----------------------------------------------

    /**
     * delete请求
     *
     * @param requestUrl url
     * @param paramsMap  key，value
     * @param listener   监听
     */
    public void delete(String requestUrl, Map<String, Object> paramsMap, YHttpListener listener) {
        request(requestUrl, YHttpUtils.mapToParams(paramsMap).toString().getBytes(), "DELETE", listener);
    }

    /**
     * delete请求
     *
     * @param requestUrl url
     * @param json       json/文本
     * @param listener   监听
     */
    public void delete(String requestUrl, String json, YHttpListener listener) {
        if (getContentType() == null) setContentType("application/json;charset=utf-8");
        request(requestUrl, json.getBytes(), "DELETE", listener);
    }

    /**
     * delete请求
     *
     * @param requestUrl   url
     * @param requestBytes bytes
     * @param listener     监听
     */
    public void delete(String requestUrl, byte[] requestBytes, YHttpListener listener) {
        request(requestUrl, requestBytes, "DELETE", listener);
    }

    /**
     * delete请求
     *
     * @param requestUrl url
     * @param paramsMap  key，value
     * @param listener   监听
     */
    public void delete(String requestUrl, Map<String, Object> paramsMap, YObjectListener<T> listener) {
        request(requestUrl, YHttpUtils.mapToParams(paramsMap).toString().getBytes(), "DELETE", listener);
    }

    /**
     * delete请求
     *
     * @param requestUrl url
     * @param json       json/文本
     * @param listener   监听
     */
    public void delete(String requestUrl, String json, YObjectListener<T> listener) {
        if (getContentType() == null) setContentType("application/json;charset=utf-8");
        request(requestUrl, json.getBytes(), "DELETE", listener);
    }

    /**
     * delete请求
     *
     * @param requestUrl   url
     * @param requestBytes bytes
     * @param listener     监听
     */
    public void delete(String requestUrl, byte[] requestBytes, YObjectListener<T> listener) {
        request(requestUrl, requestBytes, "DELETE", listener);
    }

    //----------------------------------------------request----------------------------------------------

    /**
     * request请求
     *
     * @param requestUrl    url
     * @param paramsMap     key，value
     * @param requestMethod 请求类型，只能是 "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
     * @param listener      监听
     */
    public void request(String requestUrl, Map<String, Object> paramsMap, String requestMethod, YHttpListener listener) {
        request(requestUrl, YHttpUtils.mapToParams(paramsMap).toString().getBytes(), requestMethod, listener);
    }

    /**
     * request请求
     *
     * @param requestUrl    url
     * @param json          json/文本
     * @param requestMethod 请求类型，只能是 "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
     * @param listener      监听
     */
    public void request(String requestUrl, String json, String requestMethod, YHttpListener listener) {
        if (getContentType() == null) setContentType("application/json;charset=utf-8");
        request(requestUrl, json.getBytes(), requestMethod, listener);
    }

    /**
     * request请求
     *
     * @param requestUrl    url
     * @param paramsMap     key，value
     * @param requestMethod 请求类型，只能是 "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
     * @param listener      监听
     */
    public void request(String requestUrl, Map<String, Object> paramsMap, String requestMethod, YObjectListener<T> listener) {
        request(requestUrl, YHttpUtils.mapToParams(paramsMap).toString().getBytes(), requestMethod, listener);
    }

    /**
     * request请求
     *
     * @param requestUrl    url
     * @param json          json/文本
     * @param requestMethod 请求类型，只能是 "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
     * @param listener      监听
     */
    public void request(String requestUrl, String json, String requestMethod, YObjectListener<T> listener) {
        if (getContentType() == null) setContentType("application/json;charset=utf-8");
        request(requestUrl, json.getBytes(), requestMethod, listener);
    }

    /**
     * request请求
     *
     * @param requestUrl    url
     * @param requestBytes  byte数组
     * @param requestMethod 请求类型，只能是 "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
     * @param listener      监听
     */
    public void request(final String requestUrl, final byte[] requestBytes, String requestMethod, final YHttpListener listener) {
        Thread thread = new Thread(() -> {
            try {
                if (showLog) println("请求地址：" + requestMethod + "--->" + requestUrl);
                if (showLog && requestBytes != null) println("请求参数：" + new String(requestBytes));
                byte[] bytes = request(requestUrl, requestBytes, requestMethod);
                String result = new String(bytes);
                if (showLog) println("请求结果：" + result);
                Android.runOnUiThread(() -> {
                    try {
                        listener.success(bytes, result);
                    } catch (Exception e) {
                        listener.fail("异常：" + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                exception(e, listener);
            } finally {
                YHttpThreadPool.shutdown();
            }
        });
        thread.setName("request请求:" + requestUrl);
        YHttpThreadPool.add(thread);
    }

    /**
     * request请求
     *
     * @param requestUrl    url
     * @param requestBytes  byte数组
     * @param requestMethod 请求类型，只能是 "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
     * @param listener      监听
     */
    public void request(String requestUrl, byte[] requestBytes, String requestMethod, final YObjectListener<T> listener) {
        request(requestUrl, requestBytes, requestMethod, new YHttpListener() {
            @Override
            public void success(byte[] bytes, String value) {
                println("json转对象：" + listener.getType());
                try {
                    Android.runOnUiThread(() -> {
                        try {
                            if (String.class.equals(listener.getType())) {
                                listener.success(bytes, (T) value);
                            } else if ("byte[]".equals(listener.getType().toString())) {
                                listener.success(bytes, (T) bytes);
                            } else {
                                T object = getGson().fromJson(value, listener.getType());
                                listener.success(bytes, object);
                            }
                        } catch (Exception e) {
                            listener.fail("异常：" + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                } catch (java.lang.ClassCastException e) {
                    listener.fail("对象转换失败");
                    e.printStackTrace();
                } catch (Exception e) {
                    listener.fail("异常：" + e.getMessage());
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
    public void upload(String requestUrl, Map<String, Object> paramsMap, Map<String, File> fileMap, YHttpListener listener) {
        upload(requestUrl, YHttpUtils.mapToParams(paramsMap).toString().getBytes(), fileMap, listener);
    }

    /**
     * 文件上传post
     *
     * @param requestUrl url
     * @param json       json/文本
     * @param fileMap    文件列表
     * @param listener   监听
     */
    public void upload(String requestUrl, String json, Map<String, File> fileMap, YHttpListener listener) {
        if (getContentType() == null) setContentType("application/json;charset=utf-8");
        upload(requestUrl, json.getBytes(), fileMap, listener);
    }

    /**
     * 文件上传post
     *
     * @param requestUrl   url
     * @param requestBytes bytes
     * @param fileMap      文件列表
     * @param listener     监听
     */
    public void upload(String requestUrl, byte[] requestBytes, Map<String, File> fileMap, YHttpListener listener) {
        List<Upload> uploads = new ArrayList<>();
        for (Map.Entry<String, File> entry : fileMap.entrySet()) {
            if (entry.getValue() == null) continue;
            uploads.add(new Upload(entry.getKey(), entry.getValue()));
        }
        upload(requestUrl, requestBytes, uploads, listener);
    }

    /**
     * 文件上传post
     *
     * @param requestUrl url
     * @param paramsMap  key，value
     * @param uploads    上传的key，内容，文件名，contentType
     * @param listener   监听
     */
    public void upload(String requestUrl, Map<String, Object> paramsMap, List<Upload> uploads, YHttpListener listener) {
        upload(requestUrl, YHttpUtils.mapToParams(paramsMap).toString().getBytes(), uploads, listener);
    }

    /**
     * 文件上传post
     *
     * @param requestUrl url
     * @param json       json/文本
     * @param uploads    上传的key，内容，文件名，contentType
     * @param listener   监听
     */
    public void upload(String requestUrl, String json, List<Upload> uploads, YHttpListener listener) {
        if (getContentType() == null) setContentType("application/json;charset=utf-8");
        upload(requestUrl, json.getBytes(), uploads, listener);
    }

    /**
     * 文件上传post
     *
     * @param requestUrl   url
     * @param requestBytes bytes
     * @param uploads      上传的key，内容，文件名，contentType
     * @param listener     监听
     */
    public void upload(final String requestUrl, final byte[] requestBytes, final List<Upload> uploads, final YHttpListener listener) {
        Thread thread = new Thread(() -> {
            try {
                if (showLog)
                    println("文件上传开始：\nupload--->" + requestUrl + (requestBytes == null ? "" : ("\n文件数：" + uploads.size() + "\n请求参数：" + new String(requestBytes))));
                byte[] bytes = upload(requestUrl, requestBytes, uploads);
                String result = new String(bytes);
                if (showLog) println("文件上传完成：" + result);
                Android.runOnUiThread(() -> {
                    try {
                        listener.success(bytes, result);
                    } catch (Exception e) {
                        listener.fail("异常：" + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                exception(e, listener);
            } finally {
                YHttpThreadPool.shutdown();
            }
        });
        thread.setName("文件上传post:" + requestUrl);
        YHttpThreadPool.add(thread);
    }

    /**
     * 文件上传post，返回对象
     *
     * @param requestUrl url
     * @param paramsMap  参数
     * @param uploads    上传对象列表
     * @param listener   返回对象监听
     */
    public void upload(String requestUrl, Map<String, Object> paramsMap, List<Upload> uploads, YObjectListener<T> listener) {
        upload(requestUrl, YHttpUtils.mapToParams(paramsMap).toString().getBytes(), uploads, listener);
    }

    /**
     * 文件上传post，返回对象
     *
     * @param requestUrl url
     * @param json       json/文本
     * @param uploads    上传对象列表
     * @param listener   返回对象监听
     */
    public void upload(String requestUrl, String json, List<Upload> uploads, YObjectListener<T> listener) {
        if (getContentType() == null) setContentType("application/json;charset=utf-8");
        upload(requestUrl, json.getBytes(), uploads, listener);
    }

    /**
     * 文件上传post，返回对象
     *
     * @param requestUrl   url
     * @param requestBytes 参数
     * @param uploads      上传对象列表
     * @param listener     返回对象监听
     */
    public void upload(final String requestUrl, final byte[] requestBytes, final List<Upload> uploads, final YObjectListener<T> listener) {
        upload(requestUrl, requestBytes, uploads, new YHttpListener() {
            @Override
            public void success(byte[] bytes, String value) {
                println("json转对象：" + listener.getType());
                try {
                    Android.runOnUiThread(() -> {
                        try {
                            if (String.class.equals(listener.getType())) {
                                listener.success(bytes, (T) value);
                            } else if ("byte[]".equals(listener.getType().toString())) {
                                listener.success(bytes, (T) bytes);
                            } else {
                                T object = getGson().fromJson(value, listener.getType());
                                listener.success(bytes, object);
                            }
                        } catch (Exception e) {
                            listener.fail("异常：" + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                } catch (java.lang.ClassCastException e) {
                    listener.fail("对象转换失败");
                    e.printStackTrace();
                } catch (Exception e) {
                    listener.fail("异常：" + e.getMessage());
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
     * 文件下载,get请求，回调进度
     *
     * @param requestUrl url
     * @param file       保存的文件
     * @param listener   监听
     */
    public void downloadFile(final String requestUrl, final File file, final YHttpDownloadFileListener listener) {
        Thread thread = new Thread(() -> {
            try {
                if (showLog) println("文件下载开始：\nGET--->" + requestUrl);
                downloadFile(requestUrl, file, (size, sizeCount) -> {
                    Android.runOnUiThread(() -> {
                        try {
                            listener.progress(size, sizeCount);
                        } catch (Exception e) {
                            listener.fail("异常：" + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                });

                if (showLog)
                    println("文件下载完成：\nGET--->" + requestUrl + "\n保存路径：" + file.getPath());
                Android.runOnUiThread(() -> {
                    try {
                        listener.success(file);
                    } catch (Exception e) {
                        listener.fail("异常：" + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                exception(e, listener);
            } finally {
                YHttpThreadPool.shutdown();
            }
        });
        thread.setName("文件下载,get:" + requestUrl);
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
                    println("文件加载开始：\nGET--->" + requestUrl);
                byte[] bytes = load(requestUrl, (size, sizeCount) -> {
                    Android.runOnUiThread(() -> {
                        try {
                            listener.progress(size, sizeCount);
                        } catch (Exception e) {
                            listener.fail("异常：" + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                });
                if (showLog)
                    println("文件加载完成");
                Android.runOnUiThread(() -> {
                    try {
                        listener.success(bytes);
                    } catch (Exception e) {
                        listener.fail("异常：" + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                exception(e, listener);
            } finally {
                YHttpThreadPool.shutdown();
            }
        });
        thread.setName("加载get请求，回调进度:" + requestUrl);
        YHttpThreadPool.add(thread);
    }

    /**
     * 错误情况处理
     *
     * @param e        错误
     * @param listener 监听
     */
    void exception(Exception e, Object listener) {
        Android.runOnUiThread(() -> {
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
                if ("终止下载".equals(e.getMessage())) {
                    error(e.getMessage(), listener);
                } else {
                    error("请求失败 " + e.getMessage(), listener);
                }
            }
        });
    }

    /**
     * 错误回调
     *
     * @param error    错误
     * @param listener 监听
     */
    void error(String error, Object listener) {
        printlnE(error);
        if (listener instanceof YHttpListener) {
            ((YHttpListener) listener).fail(error);
        } else if (listener instanceof YHttpLoadListener) {
            ((YHttpLoadListener) listener).fail(error);
        } else if (listener instanceof YHttpDownloadFileListener) {
            ((YHttpDownloadFileListener) listener).fail(error);
        } else if (listener instanceof YFailListener) {
            ((YFailListener) listener).fail(error);
        }
    }

    /**
     * 打印日志。如果发现包含Log就用Log打印，否则就用println
     *
     * @param str 日志
     */
    void println(String str) {
        if (Android.isAndroid()) {
            println("d", "YHttp", str);
        } else {
            System.out.println(str);
        }
    }

    /**
     * 打印错误日志。如果发现包含Log就用Log打印，否则就用println
     *
     * @param str 错误内容
     */
    void printlnE(String str) {
        if (Android.isAndroid()) {
            println("e", "YHttp", str);
        } else {
            System.err.println(str);
        }
    }

    /**
     * 打印日志
     *
     * @param tag tag
     * @param msg 内容
     */
    private static void println(String type, String tag, String msg) {
        int LOG_MAX_LENGTH = 2000;
        int strLength = msg.length();
        int start = 0;
        int end = LOG_MAX_LENGTH;
        for (int i = 0; i < 100; i++) {
            //剩下的文本还是大于规定长度则继续重复截取并输出
            if (strLength > end) {
                String s = tag + " " + i;
                Android.Log(type, s, msg.substring(start, end));
                start = end;
                end = end + LOG_MAX_LENGTH;
            } else {
                String s = i == 0 ? tag : tag + " " + i;
                Android.Log(type, s, msg.substring(start, strLength));
                break;
            }
        }
    }
}