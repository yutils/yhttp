# YHttp网络请求，支持原生java工程，原生kotlin工程，安卓工程。
> * 1.支持get，post请求，支持同步请求，支持异步请求
> * 2.支持各种格式文件上传下载，回调进度条
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

##  用法举例：
  1.YHttp返回结果在子线程，适合java工程
  2.YHttp返回结果在主线程，适合安卓工程

<font color=#0099ff size=4 >java</font>
``` java
String url = "http://www.xxx.xxx/xxx";

//同步请求get
YHttpBase yHttp = new YHttpBase().setConnectTimeout(3000).setContentType("application/json; charset=utf-8");
String json = new String(yHttp.get(url), StandardCharsets.UTF_8);

//同步请求post
String json = new String(yHttp.post(url,"id=123&name=123".getBytes()), StandardCharsets.UTF_8);

//异步请求
String url="http://192.168.6.9:8090/api/getUser";
HashMap <String,Object> hashMap=new HashMap<>();
hashMap.put("id","123");

//异步请求,返回字符串，如是安卓项目返回值自动回到主线程，监听设置成YHttpListener就返回字符串，监听设置成YObjectListener就返回对象
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


//异步请求,返回返回对象，如是安卓项目返回值自动回到主线程，监听设置成YHttpListener就返回字符串，监听设置成YObjectListener就返回对象
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


//java文件上传，如是安卓项目返回值自动回到主线程，监听设置成YHttpListener就返回字符串，监听设置成YObjectListener就返回对象
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


//文件下载，如是安卓项目返回值自动回到主线程
String url = "https://down.qq.com/qqweb/PCQQ/PCQQ_EXE/PCQQ2020.exe";
File f = new File( "D:/QQ.exe");
YHttp.create().downloadFile(url, f, new YHttpDownloadFileListener (){
    @Override
    public void progress(int downloadSize, int fileSize) {
        double progress = ((int)(10000.0 * downloadSize / fileSize))/100.0;//下载进度，保留2位小数
    }
    @Override
    public void success(File file) {
        //下载完成
    }
    @Override
    public void fail(String value) {
        //下载失败
    }
});
```

<font color=#0099ff size=4 >kotlin</font>
``` kotlin

var session = ""
val url = "http://www.xxx.xxx/xxx"
val hashMap: HashMap<String, Any> = hashMapOf("name" to "abc", "password" to "123456")

//请求。保存sessionId，如登录，如是安卓项目返回值自动回到主线程，监听设置成YHttpListener就返回字符串，监听设置成YObjectListener就返回对象
YHttp.create().setSessionListener { sessionId ->session = sessionId }.post(url, hashMap, object : YHttpListener {
    override fun success(bytes: ByteArray?, value: String?) {
    //成功
    }
    override fun fail(value: String?) {
    //失败
    }
})
    

//请求返回对象，带上sessionId，如业务操作，如是安卓项目返回值自动回到主线程，监听设置成YHttpListener就返回字符串，监听设置成YObjectListener就返回对象
YHttp.create().setSessionId(session).post(url, map, object : YObjectListener<User>() {
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


//文件上传，并且上次参数请求，如是安卓项目返回值自动回到主线程，监听设置成YHttpListener就返回字符串，监听设置成YObjectListener就返回对象
val url = "http://192.168.6.9:8090/crash/upload/file"
val params: HashMap<String, Any> = hashMapOf("name" to "yujing", "password" to "123456")
val list: MutableList<Upload> = ArrayList()
list.add(Upload("file1",file))
list.add(Upload("file2", "ABCDEF".toByteArray()).setFilename("abcd.txt"))
list.add(Upload("file3",bitmap))

YHttp.create().setSessionId(session).upload(url, params, list, object : YHttpListener {
    override fun success(bytes: ByteArray?, value: String?) {
        //成功
    }
    override fun fail(value: String?) {
        //失败
    }
})


//文件下载，如是安卓项目返回值自动回到主线程
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
```

## 如安卓中使用注意添加权限：
> * 必须权限 　　　　　　　　——>　　　　 android.permission.INTERNET
> * 返回对象必须引用Gson　　 ——>　　　　 implementation 'com.google.code.gson:gson:2.8.6'

Github地址：[https://github.com/yutils/yhttp](https://github.com/yutils/yhttp)

我的CSDN：[https://blog.csdn.net/Yu1441](https://blog.csdn.net/Yu1441)

感谢关注微博：[细雨若静](https://weibo.com/32005200)