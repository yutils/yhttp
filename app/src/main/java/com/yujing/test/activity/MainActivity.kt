package com.yujing.test.activity

import android.content.Intent
import android.widget.TextView
import com.google.gson.Gson
import com.yujing.base.YBaseActivity
import com.yujing.test.R
import com.yujing.test.bean.UU
import com.yujing.test.bean.User
import com.yujing.test.databinding.ActivityAllTestBinding
import com.yujing.utils.*
import com.yutils.http.YHttp
import com.yutils.http.contract.ObjectListener
import com.yutils.http.contract.YHttpDownloadFileListener
import com.yutils.http.contract.YHttpListener
import com.yutils.http.contract.YObjectListener
import com.yutils.http.model.Upload
import com.yutils.view.utils.Create
import java.io.File


class MainActivity : YBaseActivity<ActivityAllTestBinding>(R.layout.activity_all_test) {
    lateinit var textView1: TextView
    lateinit var textView2: TextView
    val gson = YJson.getGsonDate("yyyy年MM月dd日 HH:mm:ss")
    override fun init() {
        YPermissions.requestAll(this)
        binding.wll.removeAllViews()
        binding.ll.removeAllViews()
        textView1 = Create.textView(binding.ll)
        textView2 = Create.textView(binding.ll)
        Create.button(binding.wll, "登录获取session") {
            net1()
        }
        Create.button(binding.wll, "获取用户信息") {
            net2()
        }
        Create.button(binding.wll, "拍照上传图片") {
            net3()
        }
        Create.button(binding.wll, "链式请求get") {
            net4()
        }
        Create.button(binding.wll, "链式请求get——Obj") {
            net5()
        }
        Create.button(binding.wll, "链式请求post") {
            net6()
        }
        Create.button(binding.wll, "链式请求post——Obj") {
            net7()
        }
        Create.button(binding.wll, "App更新") {
            update()
        }
        Create.button(binding.wll, "文件下载") {
            downLoad()
        }
        Create.button(binding.wll, "终止下载") {
            downLoadStop()
        }
    }

    //登录并保存session
    var session = ""
    private fun net1() {
        val url = "http://192.168.6.9:8090/crash/user/login"
        val hashMap: HashMap<String, Any> = hashMapOf("name" to "yujing", "password" to "wtugeqh")
        YHttp.create().setSessionListener { sessionId ->
            session = sessionId
            runOnUiThread { textView1.text = "sessionId：$sessionId" }
        }.post(url, hashMap, object : YHttpListener {
            override fun success(bytes: ByteArray?, value: String?) {
                textView2.text = "成功：$value"
            }

            override fun fail(value: String?) {
                textView2.text = "失败：$value"
            }
        })
    }

    //获取用户信息
    private fun net2() {
        val url = "http://192.168.6.9:8090/crash/"
        YHttp.create().setGson(gson).setSessionId(session)
            .get(url, object : YObjectListener<User>() {
                override fun success(bytes: ByteArray?, value: User?) {
                    textView2.text = "对象：${value.toString()} \n转换成json：${Gson().toJson(value)}"
                }

                override fun fail(value: String?) {
                    textView2.text = "失败：$value"
                }
            })
    }

    //长传图片，并且上次参数请求
    private val yPicture: YPicture = YPicture()
    private fun net3() {
        yPicture.gotoCamera(this)
        yPicture.setPictureFromCameraListener { uri, file, Flag ->
            var bitmap = YConvert.uri2Bitmap(this, uri)
//            YShow.show(this, "正在上传")
//            val url = "http://192.168.6.9:8090/crash/upload/file"
//            val params: HashMap<String, Any> = hashMapOf("name" to "yujing", "password" to "123456")
//            val list: MutableList<Upload> = ArrayList()
//            list.add(Upload("file1", file))
//            list.add(Upload("file2", "ABCDEF".toByteArray()).setFilename("abcd.txt"))
//            list.add(Upload("file3", bitmap))
//
//            //文件上传
//            YHttp.create().setSessionId(session)
//                .upload(url, params, list, object : YObjectListener<UU>() {
//                    override fun success(bytes: ByteArray?, value: UU?) {
//                        YShow.finish()
//                        text2.text = value.toString()
//                    }
//
//                    override fun fail(value: String?) {
//                        YShow.finish()
//                        text2.text = "失败：$value"
//                    }
//                })


            val list: MutableList<Upload> = ArrayList()
            list.add(Upload("file", bitmap))
            YShow.show(this, "正在上传...", "身份证正面照")
            YHttp.create().setSessionId(session)
                .setRequestProperty("X-Session-Token", "4ea64bef2bb54a5fbbe3c8e28da8b92a")
                .upload(
                    "http://152.136.213.28:8008/garbage/app/idNum/imgUrl",
                    "",
                    list,
                    object : YObjectListener<UU>() {
                        override fun success(bytes: ByteArray?, value: UU?) {
                            YShow.finish()
                            textView2.text = value.toString()
                        }

                        override fun fail(value: String?) {
                            YShow.finish()
                            textView2.text = "失败：$value"
                        }
                    })

//            val paramsMap = HashMap<String, Any>()
//            val uploadImg = HashMap<String, Bitmap>()
//            bitmap=YBitmapUtil.zoom(bitmap,540,960)
//            uploadImg["file"] = bitmap
//            YnetAndroid.uploadBitmap(
//                "http://152.136.213.28:8008/garbage/app/idNum/imgUrl", paramsMap, uploadImg,
//                object : Ynet.YnetListener {
//                    override fun success(value: String?) {
//                        YLog.d(value)
//                    }
//
//                    override fun fail(value: String?) {
//                        YLog.d(value)
//                    }
//                }
//            )
        }
    }

    private fun net4() {
        val url = "http://192.168.6.9:8090/crash/"
        YHttp.create()
            .get()
            .url(url)
            .setSuccessListener { bytes, value -> textView2.text = "成功：$value" }
            .setFailListener { value -> textView2.text = "失败：$value" }
            .start()
    }

    private fun net5() {
        val url = "http://192.168.6.9:8090/crash/"
        YHttp.create()
            .get()
            .url(url)
            .setSuccessListener { bytes, value -> textView2.text = "成功：$value" }
            .setFailListener { value -> textView2.text = "失败：$value" }
            .start()
    }

    private fun net6() {
        val url = "http://192.168.6.9:8090/crash/user/login"
        val gson = YJson.getGsonDate("yyyy年MM月dd日 HH:mm:ss")
        val hashMap: HashMap<String, Any> = hashMapOf("name" to "yujing", "password" to "wtugeqh")
        YHttp.create()
            .url(url)
            .post(hashMap)
            .setGson(gson)
            .setSuccessListener { bytes, value -> textView1.text = "成功：$value" }
            .setObjectListener(object : ObjectListener<User>() {
                override fun success(bytes: ByteArray?, value: User?) {
                    textView2.text = "对象：${value.toString()} \n转换成json：${Gson().toJson(value)}"
                }
            })
            .setFailListener { value -> textView2.text = "失败：$value" }
            .start()
    }

    private fun net7() {
        val gson = YJson.getGsonDate("yyyy年MM月dd日 HH:mm:ss")
        YHttp.create()
            .url("http://192.168.6.9:8090/crash/")
            .get()
            .setGson(gson)
            .setSuccessListener { bytes, value -> textView1.text = "成功：$value" }
            .setObjectListener(object : ObjectListener<User>() {
                override fun success(bytes: ByteArray?, value: User?) {
                    textView2.text = "对象：${value.toString()} \n转换成json：${gson.toJson(value)}"
                }
            })
            .setFailListener { value -> textView2.text = "失败：$value" }
            .start()
    }

    private var yVersionUpdate: YVersionUpdate? = null
    private fun update() {
        val url = "https://down.qq.com/qqweb/QQ_1/android_apk/AndroidQQ_8.4.5.4745_537065283.apk"
        YLog.i("url：$url")
        yVersionUpdate = YVersionUpdate(this, 20, false, url, "1.9.99", "\n修复了bug1引起的问题\n新增功能：aaa")
        yVersionUpdate?.update()
    }

    private var download = YHttp.create()
    private fun downLoad() {
        textView1.text = "开始下载"
        val url = "https://down.qq.com/qqweb/PCQQ/PCQQ_EXE/PCQQ2020.exe"
        var f = File(YPath.getFilePath(this, "download") + "/qq.exe")

        download.downloadFile(url, f, object :
            YHttpDownloadFileListener {
            override fun progress(downloadSize: Int, fileSize: Int) {
                val progress = (10000.0 * downloadSize / fileSize).toInt() / 100.0 //下载进度，保留2位小数
                textView1.text = "$downloadSize/$fileSize"
                textView2.text = "进度：$progress%"
            }

            override fun success(file: File) {
                textView2.text = "下载完成"
            }

            override fun fail(value: String) {
                textView2.text = value
            }
        })
    }

    private fun downLoadStop() {
        download.downloadFileStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        yPicture.onActivityResult(requestCode, resultCode, data)
        yVersionUpdate?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        yVersionUpdate?.onDestroy()
    }
}