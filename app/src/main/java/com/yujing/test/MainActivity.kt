package com.yujing.test

import android.content.Intent
import com.google.gson.Gson
import com.yujing.test.bean.UU
import com.yujing.test.bean.User
import com.yujing.utils.*
import com.yutils.http.YHttp
import com.yutils.http.contract.YHttpDownloadFileListener
import com.yutils.http.contract.YHttpListener
import com.yutils.http.contract.YObjectListener
import com.yutils.http.model.Upload
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : BaseActivity() {

    override val layoutId: Int
        get() = R.layout.activity_main

    override fun init() {
        //var a=findViewById<Button>(R.id.button1)
        button1.text = "登录获取session"
        button1.setOnClickListener { net1() }
        button2.text = "获取用户信息"
        button2.setOnClickListener { net2() }
        button3.text = "拍照上传图片"
        button3.setOnClickListener { net3() }
        button4.text = "无"
        button4.setOnClickListener { net4() }
        button5.text = "无"
        button5.setOnClickListener { net5() }
        button6.text = "无"
        button6.setOnClickListener { net6() }
        button7.text = "App更新"
        button7.setOnClickListener { update() }
        button8.text = "文件下载"
        button8.setOnClickListener { downLoad() }
    }

    //登录并保存session
    var session = ""
    private fun net1() {
        val url = "http://192.168.6.9:8090/crash/user/login"
        val hashMap: HashMap<String, Any> = hashMapOf("name" to "yujing", "password" to "wtugeqh")
        YHttp.create().setSessionListener { sessionId ->
            session = sessionId
            runOnUiThread { text1.text = "sessionId：$sessionId" }
        }.post(url, hashMap, object : YHttpListener {
            override fun success(bytes: ByteArray?, value: String?) {
                text2.text = "成功：$value"
            }

            override fun fail(value: String?) {
                text2.text = "失败：$value"
            }
        })
    }

    //获取用户信息
    private fun net2() {
        val url = "http://192.168.6.9:8090/crash/"
        YHttp.create().setSessionId(session).get(url, object : YObjectListener<User>() {
            override fun success(bytes: ByteArray?, value: User?) {
                text2.text = "对象：${value.toString()} \n转换成json：${Gson().toJson(value)}"
            }

            override fun fail(value: String?) {
                text2.text = "失败：$value"
            }
        })
    }

    //长传图片，并且上次参数请求
    private val yPicture: YPicture = YPicture()
    private fun net3() {
        yPicture.gotoCamera(this)
        yPicture.setPictureFromCameraListener { uri, file, Flag ->
            val bitmap = YConvert.uri2Bitmap(this, uri)
            YShow.show(this, "正在上传")
            val url = "http://192.168.6.9:8090/crash/upload/file"
            val params: HashMap<String, Any> = hashMapOf("name" to "yujing", "password" to "123456")
            val list: MutableList<Upload> = ArrayList()
            list.add(Upload("file1", file))
            list.add(Upload("file2", "ABCDEF".toByteArray()).setFilename("abcd.txt"))
            list.add(Upload("file3", bitmap))

            //文件上传
            YHttp.create().setSessionId(session)
                .upload(url, params, list, object : YObjectListener<UU>() {
                    override fun success(bytes: ByteArray?, value: UU?) {
                        YShow.finish()
                        text2.text = value.toString()
                    }

                    override fun fail(value: String?) {
                        YShow.finish()
                        text2.text = "失败：$value"
                    }
                })
        }
    }

    private fun net4() {

    }

    private fun net5() {
        net6()
    }

    private fun net6(aaa: String = "asd") {

    }

    private fun update() {
        val url = "https://down.qq.com/qqweb/QQ_1/android_apk/AndroidQQ_8.4.5.4745_537065283.apk"
        val yVersionUpdate = YVersionUpdate(
            this,
            20,
            false,
            url,
            "1.9.99",
            "\n修复了bug1引起的问题\n新增功能：aaa"
        ).checkUpdate()
    }

    private fun downLoad() {
        val url = "https://down.qq.com/qqweb/PCQQ/PCQQ_EXE/PCQQ2020.exe"
        var f = File(YPath.getFilePath(this, "download") + "/qq.exe")

        YHttp.create().downloadFile(url, f, object :
            YHttpDownloadFileListener {
            override fun progress(downloadSize: Int, fileSize: Int) {
                val progress = (10000.0 * downloadSize / fileSize).toInt() / 100.0 //下载进度，保留2位小数
                text1.text = "$downloadSize/$fileSize"
                text2.text = "进度：$progress%"
            }

            override fun success(file: File) {}
            override fun fail(value: String) {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        yPicture.onActivityResult(requestCode, resultCode, data)
    }
}