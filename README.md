# YHttp网络请求，支持java，kotlin
> * 1.支持get，post请求
> * 2.支持文件上传下载，回调进度条
> * 3.post请求可为字符串，map，byte[]
> * 4.可以直接返回字符串
> * 5.直接返回对象
> * 6.异常回调原因
> * 7.支持https,设置ssl文件
> * 8.sessionID的获取和设置
> * 9.简单好用
采用java8.0，安卓10.0，API29，androidx。


## 当前最新版：————>[![](https://jitpack.io/v/yutils/yhttp.svg)](https://jitpack.io/#yutils/yhttp)

**[releases里面有JAR包。点击前往](https://github.com/yutils/yhttp/releases)**

## Gradle 引用

1. 在根build.gradle中添加
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. 子module添加依赖，当前最新版：————> [![](https://jitpack.io/v/yutils/yhttp.svg)](https://jitpack.io/#yutils/yhttp)

```
dependencies {
    implementation 'com.github.yutils:yhttp:1.0.0'
}
```

##  用法：
  1.YHttp返回结果在子线程，适合java工程
  2.YHttp返回结果在主线程，适合安卓工程

<font color=#0099ff size=4 >java</font>
``` java
String url="http://192.168.1.170:10136/api/getUser";
HashMap <String,Object> hashMap=new HashMap<>();
hashMap.put("id","123");

//请求返回字符串
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

//请求返回对象
YHttp.create().post(url, hashMap, new YHttpListener() {
    @Override
    public void success(byte[] bytes, String value) throws Exception {
        //请求结果,文本
    }

    @Override
    public void fail(String value) {
        //错误原因
    }
});
```

<font color=#0099ff size=4 >kotlin</font>
``` kotlin
var session = ""

val url = "http://www.xxx.xxx/xxx"
val hashMap: HashMap<String, Any> = hashMapOf("name" to "abc", "password" to "123456")
YHttp.create().setSessionListener { sessionId ->
    //获取的session
    session = sessionId
}.post(url, hashMap, object : YHttpListener {
    override fun success(bytes: ByteArray?, value: String?) {
    //成功
    }
    override fun fail(value: String?) {
    //失败
    }
})
    

//请求返回对象
YHttp.create().post(url, map, object : YObjectListener<User>() {
     override fun success(bytes: ByteArray?, value: User?) {
         runOnUiThread(Runnable {
             YToast.show(App.get(), value?.Name)
         })
     }
     override fun fail(value: String) {
         runOnUiThread(Runnable {
             YToast.show(App.get(), value)
         })
     }
})
//文件下载
val url = "http://dldir1.qq.com/qqfile/qq/QQ8.9.2/20760/QQ8.9.2.exe"
var f = File(getFilePath(this, "cs") + "/BB.exe")

YHttp.create().downloadFile(url, f, object : YHttpDownloadFileListener {
    override fun progress(downloadSize: Int, fileSize: Int) {
        text1.text = "$downloadSize/$fileSize"
        var progress = 100.0 * downloadSize / fileSize
        progress = (progress * 100).toInt().toDouble() / 100
        text2.text = "进度：$progress%"
    }

    override fun success(file: File) {}
    override fun fail(value: String) {}
})
```


## 注意添加权限：
> * 必须权限  android.permission.INTERNET


Github地址：[https://github.com/yutils/yhttp](https://github.com/yutils/yhttp)

我的CSDN：[https://blog.csdn.net/Yu1441](https://blog.csdn.net/Yu1441)

感谢关注微博：[细雨若静](https://weibo.com/32005200)