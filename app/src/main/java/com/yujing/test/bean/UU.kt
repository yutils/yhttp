package com.yujing.test.bean

data class UU(
    val code: Int,
    val `data`: List<Data>,
    val message: String
)

data class Data(
    val absoluteUrl: String,
    val filePath: String,
    val relativeUrl: String
)