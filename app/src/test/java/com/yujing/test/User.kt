package com.yujing.test

data class User(
    val code: Int,
    val data: DD?,
    val message: String
)

data class DD(
    val 可用内存: String,
    val 已使用内存: String,
    val 服务器时间: String,
    val 用户: 用户?
)

data class 用户(
    val createTime: String,
    val endLoginTime: String,
    val id: Int,
    val name: String,
    val nickName: String,
    val phone: String
)
class Test{
   fun test(){


   }
}