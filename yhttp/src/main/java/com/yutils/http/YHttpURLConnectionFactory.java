package com.yutils.http;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * HttpURLConnection工厂,如果带有crtString，就创建HTTPS请求，否则就HTTP请求
 *
 * @author yujing 2020年7月28日10:22:35
 */
public class YHttpURLConnectionFactory {
    /**
     * 创建HttpURLConnection对象，如果请求包含https就创建HttpsURLConnection,并且创建证书
     *
     * @param url    请求地址
     * @param crtSSL crt证书
     * @return HttpURLConnection
     * @throws Exception Exception
     */
    public static HttpURLConnection create(String url, String crtSSL) throws Exception {
        if (crtSSL != null && (url.toLowerCase(Locale.getDefault()).contains("https://"))) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) (new URL(url)).openConnection();
            httpsURLConnection.setSSLSocketFactory(createSSLSocketFactory(crtSSL));
            //屏蔽https验证
            httpsURLConnection.setHostnameVerifier((hostname, session) -> true);
            return httpsURLConnection;
        } else {
            return (HttpURLConnection) (new URL(url)).openConnection();
        }
    }

    /**
     * 创建SSL套接字工厂
     *
     * @param crtString crt证书
     * @return SSLSocketFactory
     * @throws Exception Exception
     */
    private static SSLSocketFactory createSSLSocketFactory(String crtString) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        //如果是src/main/assets/test.crt，直接获取InputStream：getAssets().open("test.crt")
        Certificate ca = cf.generateCertificate(new ByteArrayInputStream(crtString.getBytes()));

        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, new SecureRandom());
        return context.getSocketFactory();
    }
}
