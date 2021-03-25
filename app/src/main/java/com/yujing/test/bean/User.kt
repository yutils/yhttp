package com.yujing.test.bean

import java.util.*

data class User(
    val code: Int,
    val data: DD?,
    val message: String
)

data class DD(
    val 可用内存: String,
    val 已使用内存: String,
    val 服务器时间: Date,
    val 用户: 用户?
)

data class 用户(
    val createTime: Date,
    val endLoginTime: Date,
    val id: Int,
    val name: String,
    val nickName: String,
    val phone: String
)