package com.yujing.test.bean

/**
 * 烟农信息
 */
/*
字段	名称	类型
Command	命令码	int
MsgId	消息Id	int
YDMC	烟点名称	string
YDBM	烟点编码	string
Name	烟农名称	string
HTH	    合同号	    string
YHKH	银行卡号	string
YYPZ	烟叶品种	string
HTZL	合同总量	decimal
YJZL	已交重量	decimal
YL	    余量	    decimal
JTDZ	家庭地址	string
ZZMJ	种值面积	decimal
SFZ	    身份证号	string
KFH	    开户行	    string
 */
class FarmersInfo {
    var Command = 0 //命令码
    var MsgId = 0//消息Id
    var YDMC: String? = null//烟点名称
    var YDBM: String? = null//烟点编码
    var Name: String? = null//烟农名称
    var HTH: String? = null//合同号
    var YHKH: String? = null//银行卡号
    var YYPZ: String? = null//烟叶品种
    var HTZL = 0.0//合同总量
    var YJZL = 0.0//已交重量
    var YL = 0//余量
    var JTDZ: String? = null//家庭地址
    var ZZMJ = 0//种值面积
    var SFZ: String? = null//身份证号
    var KFH: String? = null//开户行
}