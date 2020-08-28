package com.yutils.http.model;

import java.io.File;

/**
 * 上传文件实体
 *
 * @author yujing 2020年8月28日12:08:34
 * 上传文件，检查到bytes不为null就走bytes，其次file，其次bitmap
 */
public class Upload {
    /**
     * 上传文件的key值
     */
    private String key;

    /**
     * 文件内容为byte[]，与File，Bitmap，互斥
     */
    private byte[] bytes;

    /**
     * 文件内容为file，与byte[]，Bitmap，互斥
     */
    private File file;

    /**
     * 文件内容为object，与File，byte[]，互斥
     * 为了兼容java原生项目，所以此处设置为Object
     */
    private Object object;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 此文件的contentType
     */
    private String contentType;

    public Upload(String key, byte[] bytes) {
        this.key = key;
        this.bytes = bytes;
        filename = key;
        contentType = "application/octet-stream";
    }

    public Upload(String key, File file) {
        this.key = key;
        this.file = file;
        filename = file.getName();
        if (file.getName().lastIndexOf(".png") != -1) {
            contentType = "image/png";
        } else if (file.getName().lastIndexOf(".jpg") != -1 || file.getName().lastIndexOf(".jpeg") != -1) {
            contentType = "image/jpeg";
        } else {
            contentType = "application/octet-stream";
        }
    }

    public Upload(String key, Object object) {
        this.key = key;
        this.object = object;
        filename = key;
        filename = "image" + System.currentTimeMillis() + ".jpg";
        contentType = "image/jpeg";
    }

    public String getKey() {
        return key;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public File getFile() {
        return file;
    }

    public Object getBitmap() {
        return object;
    }

    public String getFilename() {
        return filename;
    }

    public Upload setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public Upload setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
}
