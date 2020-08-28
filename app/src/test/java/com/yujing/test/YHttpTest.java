package com.yujing.test;

import com.yujing.utils.YNumber;
import com.yutils.http.YHttp;
import com.yutils.http.contract.YHttpDownloadFileListener;
import com.yutils.http.contract.YHttpListener;
import com.yutils.http.contract.YObjectListener;
import com.yutils.http.model.Upload;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;

public class YHttpTest {

    @Test
    public void main() throws InterruptedException {
        YHttp.setShowLog(false);
        test1();
        Thread.sleep(2000);
        download();
    }

    String session;
    private void test1() {
        String url = "http://192.168.6.9:8090/crash/user/login";
        HashMap<String, Object> hashMap = new HashMap<>();// = hashMapOf("name" to "yujing", "password" to "wtugeqh")
        hashMap.put("name", "yujing");
        hashMap.put("password", "wtugeqh");

        YHttp.create().setSessionListener(s -> session = s).post(url, hashMap, new YHttpListener() {
            @Override
            public void success(byte[] bytes, String s) {
                System.out.println("请求成功：" + s);
                test2();
                test3();
            }

            @Override
            public void fail(String s) {
                System.out.println("请求失败：" + s);
            }
        });
    }

    private void test2() {
        String url = "http://192.168.6.9:8090/crash/";
        YHttp.create().setSessionId(session).get(url, new YObjectListener<User>() {
            @Override
            public void success(byte[] bytes, User value) {
                System.out.println("请求成功：" + value.toString());
            }

            @Override
            public void fail(String value) {
                System.out.println("请求失败：" + value);
            }
        });
    }

    private void test3() {
        String url = "http://192.168.6.9:8090/crash/upload/file";
        List<Upload> uploads = new ArrayList<>();
        uploads.add(new Upload("file1", new File("D:/1.jpg")));

        YHttp.create().setSessionId(session).upload(url, "", uploads, new YHttpListener() {
            @Override
            public void success(byte[] bytes, String value) throws Exception {
                System.out.println("上传成功：" + value.toString());
            }

            @Override
            public void fail(String value) {
                System.out.println("上传失败：" + value.toString());
            }
        });
    }

    private void download() {
        String url = "https://down.qq.com/qqweb/PCQQ/PCQQ_EXE/PCQQ2020.exe";
        File f = new File("D:/PCQQ2020.exe   ");
        YHttp.create().downloadFile(url, f, new YHttpDownloadFileListener() {
            @Override
            public void progress(int downloadSize, int fileSize) {
                System.out.println("当前进度：" + downloadSize + "  " + fileSize + "  " + YNumber.showNumber(downloadSize * 100d / fileSize));
            }

            @Override
            public void success(File file) {
                System.out.println("下载完成：" + file.getPath());
            }

            @Override
            public void fail(String value) {
                System.out.println("下载失败：" + value);
            }
        });
    }
}
