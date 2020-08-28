package com.yutils.http;

import android.graphics.Bitmap;

import com.yutils.http.contract.YHttpProgressListener;
import com.yutils.http.contract.YSessionListener;
import com.yutils.http.model.Upload;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 网络请求基础类，柱塞式
 *
 * @author yujing 2020年7月28日10:22:35
 */
public class YHttpBase {
    protected String contentType = "application/x-www-form-urlencoded;charset=utf-8";
    protected int connectTimeout = 1000 * 20;
    /**
     * boundary就是request头和上传文件内容的分隔符
     */
    protected static final String BOUNDARY = "------------YuJing---------------";
    /**
     * session获取监听
     */
    protected YSessionListener sessionListener;
    /**
     * SessionId
     */
    protected String sessionId;

    //ContentType
    public String getContentType() {
        return contentType;
    }

    public YHttpBase setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    //Timeout
    public int getConnectTimeout() {
        return connectTimeout;
    }

    public YHttpBase setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    //crt证书
    protected String crtSSL;

    public String getCrtSSL() {
        return crtSSL;
    }

    public YHttpBase setCrtSSL(String crtSSL) {
        this.crtSSL = crtSSL;
        return this;
    }

    //RequestProperty
    protected Map<String, String> mapSetRequestProperty;
    protected Map<String, String> mapAddRequestProperty;

    public YHttpBase setRequestProperty(String key, String value) {
        if (mapSetRequestProperty == null)
            mapSetRequestProperty = new HashMap<>();
        mapSetRequestProperty.put(key, value);
        return this;
    }

    @SuppressWarnings("StringOperationCanBeSimplified")
    public YHttpBase addRequestProperty(String key, String value) {
        //可以重复key的map，但是key的内存地址要不一样
        if (mapAddRequestProperty == null)
            mapAddRequestProperty = new IdentityHashMap<>();
        mapAddRequestProperty.put(new String(key), value);
        return this;
    }

    //session监听
    public YHttpBase setSessionListener(YSessionListener ySessionListener) {
        this.sessionListener = ySessionListener;
        return this;
    }

    //setSessionId
    public YHttpBase setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * get请求，同步柱塞试
     *
     * @param requestUrl url
     * @return 请求结果
     * @throws Exception 异常
     */
    public byte[] get(String requestUrl) throws Exception {
        // 打开一个HttpURLConnection连接
        HttpURLConnection urlConn = YHttpURLConnectionFactory.create(requestUrl, crtSSL);
        // 设置连接主机超时时间
        urlConn.setConnectTimeout(connectTimeout);
        //设置从主机读取数据超时
        urlConn.setReadTimeout(connectTimeout);
        // 设置是否使用缓存  默认是true
        urlConn.setUseCaches(false);
        // 设置为Post请求
        urlConn.setRequestMethod("GET");
        //设置客户端与服务连接类型
        urlConn.setRequestProperty("accept", "*/*");
        urlConn.setRequestProperty("connection", "Keep-Alive");
        urlConn.setRequestProperty("Charset", "utf-8");
        urlConn.setRequestProperty("Content-Type", contentType);
        if (mapSetRequestProperty != null)
            for (Map.Entry<String, String> entry : mapSetRequestProperty.entrySet())
                urlConn.setRequestProperty(entry.getKey(), entry.getValue());
        if (mapAddRequestProperty != null)
            for (Map.Entry<String, String> entry : mapAddRequestProperty.entrySet())
                urlConn.addRequestProperty(entry.getKey(), entry.getValue());
        // 设置session
        setSession(urlConn);
        // 开始连接
        urlConn.connect();
        // 获取session
        getSession(urlConn);
        // 判断请求是否成功
        int responseCode = urlConn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("错误码：" + responseCode);
        }
        byte[] bytes = YHttpUtils.inputStreamToBytes(urlConn.getInputStream());
        // 关闭连接
        urlConn.disconnect();
        return bytes;
    }

    /**
     * post请求，同步柱塞试
     *
     * @param requestUrl   rul
     * @param requestBytes 请求内容
     * @return 请求结果
     * @throws Exception 异常
     */
    public byte[] post(String requestUrl, byte[] requestBytes) throws Exception {
        // 打开一个HttpURLConnection连接
        HttpURLConnection urlConn = YHttpURLConnectionFactory.create(requestUrl, crtSSL);
        // 设置连接超时时间
        urlConn.setConnectTimeout(connectTimeout);
        //设置从主机读取数据超时
        urlConn.setReadTimeout(connectTimeout);
        //设置请求允许输入 默认是true
        urlConn.setDoInput(true);
        // Post请求不能使用缓存
        urlConn.setUseCaches(false);
        // 设置为Post请求
        urlConn.setRequestMethod("POST");
        //设置本次连接是否自动处理重定向
        urlConn.setInstanceFollowRedirects(true);
        // 配置请求Content-Type
        urlConn.setRequestProperty("accept", "*/*");
        urlConn.setRequestProperty("connection", "Keep-Alive");
        urlConn.setRequestProperty("Charset", "utf-8");
        urlConn.setRequestProperty("Content-Type", contentType);//"application/x-www-form-urlencoded;charset=utf-8";
        if (mapSetRequestProperty != null)
            for (Map.Entry<String, String> entry : mapSetRequestProperty.entrySet())
                urlConn.setRequestProperty(entry.getKey(), entry.getValue());
        if (mapAddRequestProperty != null)
            for (Map.Entry<String, String> entry : mapAddRequestProperty.entrySet())
                urlConn.addRequestProperty(entry.getKey(), entry.getValue());
        // 设置session
        setSession(urlConn);
        // 发送POST请求必须设置如下两行
        urlConn.setDoOutput(true);
        urlConn.setDoInput(true);
        // 开始连接
        urlConn.connect();
        // 发送请求参数
        DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
        dos.write(requestBytes);
        dos.flush();
        dos.close();
        // 获取session
        getSession(urlConn);
        // 判断请求是否成功
        int responseCode = urlConn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("错误码：" + responseCode);
        }
        byte[] bytes = YHttpUtils.inputStreamToBytes(urlConn.getInputStream());
        // 关闭连接
        urlConn.disconnect();
        return bytes;
    }

    /**
     * 文件下载，同步柱塞试
     *
     * @param requestUrl url
     * @param file       下载的文件
     * @param listener   进度监听
     * @throws Exception 异常
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void downloadFile(String requestUrl, File file, YHttpProgressListener listener) throws Exception {
        File parent = file.getParentFile();
        if (!Objects.requireNonNull(parent).exists()) {
            parent.mkdirs();
        }
        if (file.exists()) {
            file.delete();// 删除存在文件
        }
        // 打开一个HttpURLConnection连接
        HttpURLConnection urlConn = YHttpURLConnectionFactory.create(requestUrl, crtSSL);
        // 设置连接主机超时时间
        urlConn.setConnectTimeout(connectTimeout);
        urlConn.setAllowUserInteraction(true);
        // 设置是否使用缓存
        urlConn.setUseCaches(false);
        //设置客户端与服务连接类型
        urlConn.addRequestProperty("Connection", "Keep-Alive");
        if (mapSetRequestProperty != null)
            for (Map.Entry<String, String> entry : mapSetRequestProperty.entrySet())
                urlConn.setRequestProperty(entry.getKey(), entry.getValue());
        if (mapAddRequestProperty != null)
            for (Map.Entry<String, String> entry : mapAddRequestProperty.entrySet())
                urlConn.addRequestProperty(entry.getKey(), entry.getValue());
        // 设置session
        setSession(urlConn);
        // 开始连接
        urlConn.connect();
        // 获取session
        getSession(urlConn);
        // 判断请求是否成功
        int responseCode = urlConn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("错误码：" + responseCode);
        }
        int downloadSize = 0;
        int fileSize = urlConn.getContentLength(); // 获取不到文件大小时候fileSize=-1
        int len;
        byte[] buffer = new byte[1024 * 8];
        FileOutputStream fos = new FileOutputStream(file);
        InputStream inputStream = urlConn.getInputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            // 写到本地
            fos.write(buffer, 0, len);
            downloadSize += len;
            listener.progress(downloadSize, fileSize);
        }
        // 关闭连接
        urlConn.disconnect();
    }

    /**
     * 加载，同步柱塞试
     *
     * @param requestUrl url
     * @param listener   进度监听
     * @return 结果
     * @throws Exception 异常
     */
    public byte[] load(String requestUrl, YHttpProgressListener listener) throws Exception {
        // 打开一个HttpURLConnection连接
        HttpURLConnection urlConn = YHttpURLConnectionFactory.create(requestUrl, crtSSL);
        // 设置连接主机超时时间
        urlConn.setConnectTimeout(connectTimeout);
        urlConn.setAllowUserInteraction(true);
        // 设置是否使用缓存
        urlConn.setUseCaches(false);
        // 设置为Post请求
        urlConn.setRequestMethod("GET");
        //设置客户端与服务连接类型
        urlConn.setRequestProperty("accept", "*/*");
        urlConn.setRequestProperty("connection", "Keep-Alive");
        urlConn.setRequestProperty("Charset", "utf-8");
        urlConn.setRequestProperty("Content-Type", contentType);//"application/x-www-form-urlencoded;charset=utf-8";
        if (mapSetRequestProperty != null)
            for (Map.Entry<String, String> entry : mapSetRequestProperty.entrySet())
                urlConn.setRequestProperty(entry.getKey(), entry.getValue());
        if (mapAddRequestProperty != null)
            for (Map.Entry<String, String> entry : mapAddRequestProperty.entrySet())
                urlConn.addRequestProperty(entry.getKey(), entry.getValue());
        // 设置session
        setSession(urlConn);
        // 开始连接
        urlConn.connect();
        // 获取session
        getSession(urlConn);
        // 判断请求是否成功
        int responseCode = urlConn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("错误码：" + responseCode);
        }
        int downloadSize = 0;
        int fileSize = urlConn.getContentLength(); // 获取不到文件大小时候fileSize=-1
        int len;
        byte[] buffer = new byte[1024 * 8];
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        InputStream inputStream = urlConn.getInputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bs.write(buffer, 0, len);
            downloadSize += len;
            listener.progress(downloadSize, fileSize);
        }
        bs.flush();
        byte[] bytes = bs.toByteArray();
        // 关闭连接
        urlConn.disconnect();
        return bytes;
    }

    /**
     * 文件上传，同步柱塞试
     *
     * @param requestUrl   url
     * @param requestBytes 请求内容
     * @param fileMap      key，文件
     * @return 请求结果
     * @throws Exception 异常
     */
    public byte[] upload(String requestUrl, byte[] requestBytes, Map<String, File> fileMap) throws Exception {
        List<Upload> uploads = new ArrayList<>();
        for (Map.Entry<String, File> entry : fileMap.entrySet()) {
            if (entry.getValue() == null) continue;
            uploads.add(new Upload(entry.getKey(), entry.getValue()));
        }
        return upload(requestUrl, requestBytes, uploads);
    }

    /**
     * 文件上传，同步柱塞试
     *
     * @param requestUrl   url
     * @param requestBytes 请求内容
     * @param uploads      上传的key，内容，文件名，contentType
     * @return 请求结果
     * @throws Exception 异常
     */
    public byte[] upload(String requestUrl, byte[] requestBytes, List<Upload> uploads) throws Exception {
        // 打开一个HttpURLConnection连接
        HttpURLConnection urlConn = YHttpURLConnectionFactory.create(requestUrl, crtSSL);
        // 设置连接超时时间
        urlConn.setConnectTimeout(connectTimeout);
        //设置从主机读取数据超时
        urlConn.setReadTimeout(connectTimeout);
        //设置请求允许输入 默认是true
        urlConn.setDoInput(true);
        // Post请求不能使用缓存
        urlConn.setUseCaches(false);
        // 设置为Post请求
        urlConn.setRequestMethod("POST");
        //设置本次连接是否自动处理重定向
        urlConn.setInstanceFollowRedirects(true);
        // 配置请求Content-Type
        urlConn.setRequestProperty("accept", "*/*");
        urlConn.setRequestProperty("connection", "Keep-Alive");
        urlConn.setRequestProperty("Charset", "utf-8");
        urlConn.setRequestProperty("Content-Type", contentType);//"application/x-www-form-urlencoded;charset=utf-8";
        //安卓6.0下使用谷歌浏览器67
        //Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.62 Mobile Safari/537.36
        //iPhoneX下使用谷歌浏览器67
        //Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X) AppleWebKit/602.1.50 (KHTML, like Gecko) CriOS/56.0.2924.75 Mobile/14E5239e Safari/602.1
        //win10下使用谷歌浏览器67
        //Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.62 Safari/537.36
        urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.62 Safari/537.36");
        urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        if (mapSetRequestProperty != null)
            for (Map.Entry<String, String> entry : mapSetRequestProperty.entrySet())
                urlConn.setRequestProperty(entry.getKey(), entry.getValue());
        if (mapAddRequestProperty != null)
            for (Map.Entry<String, String> entry : mapAddRequestProperty.entrySet())
                urlConn.addRequestProperty(entry.getKey(), entry.getValue());
        // 设置session
        setSession(urlConn);
        // 发送POST请求必须设置如下两行
        urlConn.setDoOutput(true);
        urlConn.setDoInput(true);
        // 开始连接
        urlConn.connect();
        // 发送请求参数
        send(urlConn, requestBytes, uploads);
        // 获取session
        getSession(urlConn);
        // 判断请求是否成功
        int responseCode = urlConn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("错误码：" + responseCode);
        }
        byte[] bytes = YHttpUtils.inputStreamToBytes(urlConn.getInputStream());
        // 关闭连接
        urlConn.disconnect();
        return bytes;
    }

    /**
     * 发送文件的数据组装
     *
     * @param httpURLConnection 连接对象
     * @param requestBytes      请求的参数
     * @param uploads           上传的文件
     * @throws IOException 异常
     */
    protected void send(HttpURLConnection httpURLConnection, byte[] requestBytes, List<Upload> uploads) throws IOException {
        OutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());
        // 文本参数
        if (requestBytes != null) out.write(requestBytes);
        // 文件
        if (uploads != null) {
            for (Upload item : uploads) {
                if (item.getBytes() != null) {
                    StringBuilder strBuf = new StringBuilder();
                    strBuf.append("\r\n--").append(BOUNDARY).append("\r\n")
                            .append("Content-Disposition: form-data; name=\"")
                            .append(item.getKey())
                            .append("\"; filename=\"")
                            .append(item.getFilename())
                            .append("\"\r\n");
                    strBuf.append("Content-Type:").append(item.getContentType()).append("\r\n\r\n");
                    out.write(strBuf.toString().getBytes());
                    out.write(item.getBytes());
                } else if (item.getFile() != null) {
                    StringBuilder strBuf = new StringBuilder();
                    strBuf.append("\r\n--").append(BOUNDARY).append("\r\n")
                            .append("Content-Disposition: form-data; name=\"")
                            .append(item.getKey())
                            .append("\"; filename=\"")
                            .append(item.getFilename())
                            .append("\"\r\n");
                    strBuf.append("Content-Type:").append(item.getContentType()).append("\r\n\r\n");
                    out.write(strBuf.toString().getBytes());
                    DataInputStream in = new DataInputStream(new FileInputStream(item.getFile()));
                    int bytes;
                    byte[] bufferOut = new byte[1024 * 8];
                    while ((bytes = in.read(bufferOut)) != -1)
                        out.write(bufferOut, 0, bytes);
                    in.close();
                } else {
                    try {
                        Class.forName("android.graphics.Bitmap");
                        if (item.getBitmap() != null) {
                            StringBuilder strBuf = new StringBuilder();
                            strBuf.append("\r\n--").append(BOUNDARY).append("\r\n")
                                    .append("Content-Disposition: form-data; name=\"")
                                    .append(item.getKey())
                                    .append("\"; filename=\"")
                                    .append(item.getFilename())
                                    .append("\"\r\n");
                            strBuf.append("Content-Type:").append(item.getContentType()).append("\r\n\r\n");
                            out.write(strBuf.toString().getBytes());
                            // bitmap转Bytes
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            if (item.getBitmap() instanceof Bitmap) {
                                Bitmap bitmap = (Bitmap) item.getBitmap();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                                out.write(baos.toByteArray());
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("上传Bitmap异常：" + e.getMessage());
                    }
                }
            }
        }
        out.write(("\r\n--" + BOUNDARY + "--\r\n").getBytes());// 结束标记
        out.flush();
        out.close();
    }

    /**
     * 设置session，把JSESSIONID变量的值设置成session
     *
     * @param httpURLConnection httpURLConnection
     */
    public void setSession(HttpURLConnection httpURLConnection) {
        if (sessionId != null) {
            httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=" + sessionId);
        }
    }

    /**
     * 获取session 并保存到JSESSIONID变量
     *
     * @param httpURLConnection httpURLConnection
     */
    public void getSession(HttpURLConnection httpURLConnection) {
        Map<String, List<String>> map = httpURLConnection.getHeaderFields();
        if (map != null) {
            List<String> list = map.get("Set-Cookie");
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    //System.out.println("Set-Cookie："+list.get(i));
                    int start = list.get(i).indexOf("JSESSIONID");
                    if (start != -1) {
                        int idStart = start + 10 + 1;
                        int idEnd = start + 10 + 1 + 32;
                        if (list.get(i).length() >= idEnd) {
                            String JSESSIONID = list.get(i).substring(idStart, idEnd);// 如：list.get(i)="JSESSIONID=743D39694F006763220CA0CA63FE8978";
                            if (sessionListener != null)
                                sessionListener.sessionId(JSESSIONID);
                            sessionId = JSESSIONID;
                        }
                    }
                }
            }
        }
    }
}
