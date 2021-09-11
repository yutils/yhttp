package com.yutils.http;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 安卓常用方法，反射调用
 * @author yujing 2021年9月11日15:56:45
 */
/*
用法：
//判断是否是安卓
Android.isAndroid()
//主线程中运行
Android.runOnUiThread { }
//Log
Android.Log("d", "tag", "内容")
//Toast
Android.Toast(context,"内容")
//bitmap转Byte数组
Android.bitmapToByteArray(bitmap)
 */
@SuppressWarnings("ALL")
public class Android {
    //安卓里面的 Handler(Looper.getMainLooper());
    private static Object handler;

    static {
        try {
            if (handler == null) {
                //获取 Looper.getMainLooper()
                Class cLooper = Class.forName("android.os.Looper");
                Method mGetMainLooper = cLooper.getMethod("getMainLooper");
                Object looper = mGetMainLooper.invoke(null);

                //构造 Handler handler = new Handler(Looper.getMainLooper());
                Class cHandler = Class.forName("android.os.Handler");
                Constructor conHandler = cHandler.getConstructor(cLooper);
                handler = conHandler.newInstance(looper);
            }
        } catch (Exception e) {
            Log("e", "错误", e.getMessage());
        }
    }

    /**
     * 判断是否是安卓项目
     *
     * @return 是否
     */
    public static boolean isAndroid() {
        return handler != null;
    }

    /**
     * 在主线程中运行
     * 实际逻辑：
     * if (Looper.myLooper() == Looper.getMainLooper()) {
     * runnable.run();
     * } else {
     * HANDLER.post(runnable);
     * }
     *
     * @param runnable 执行的内容
     * @return 是否成功
     */
    public static boolean runOnUiThread(Runnable runnable) {
        if (!isAndroid()) {
            runnable.run();
            return true;
        }
        try {
            Class cLooper = Class.forName("android.os.Looper");
            Method mGetMainLooper = cLooper.getMethod("getMainLooper");
            Method mMyLooper = cLooper.getMethod("myLooper");
            if (mGetMainLooper.invoke(null) == mMyLooper.invoke(null)) {
                runnable.run();
            } else {
                Class classHandler = Class.forName("android.os.Handler");
                Method mPost = classHandler.getMethod("post", Runnable.class);
                mPost.invoke(handler, runnable);
            }
            return true;
        } catch (Exception e) {
            Log("e", "错误", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 调用安卓Log，如Log.d("tag" ,"内容")
     * 使用方法，如： Log(""内容")
     *
     * @param msg msg
     * @return 是否成功
     */
    public static boolean Log(String msg) {
        return Log("i", "Android", msg, null);
    }

    /**
     * 调用安卓Log，如Log.d("tag" ,"内容")
     * 使用方法，如： Log("d","tag" ,"内容")
     *
     * @param type log——类型
     * @param tag  log——tag
     * @param msg  log——内容
     * @return 是否成功
     */
    public static boolean Log(String type, String tag, String msg) {
        return Log(type, tag, msg, null);
    }

    /**
     * 调用安卓Log，如Log.d("tag" ,"内容" , throwable)
     * 使用方法，如： Log("d","tag" ,"内容" , throwable)
     *
     * @param type      log——类型
     * @param tag       log——tag
     * @param msg       log——内容
     * @param throwable log——throwable
     * @return 是否成功
     */
    public static boolean Log(String type, String tag, String msg, Throwable throwable) {
        try {
            Class cLog = Class.forName("android.util.Log");
            Method method = cLog.getMethod(type, String.class, String.class, Throwable.class);
            method.invoke(null, tag, msg, throwable);
            return true;
        } catch (Exception e) {
            System.out.println("打印日志错误:" + e.getMessage() + "\n日志:\ttype:" + type + "\ttag:" + tag + "\tmsg:" + msg);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 显示Toast
     * 实际逻辑：
     * Toast toast=Toast.makeText(context, "msg", Toast.LENGTH_SHORT)
     * toast.show()
     *
     * @param context context
     * @param msg     内容
     * @return 是否成功
     */
    public static boolean Toast(Object context, String msg) {
        return runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Class cToast = Class.forName("android.widget.Toast");
                    Method mMakeText = cToast.getMethod("makeText", Class.forName("android.content.Context"), Class.forName("java.lang.CharSequence"), int.class);
                    Object obj = mMakeText.invoke(null, context, msg, 0);
                    Method mShow = cToast.getMethod("show");
                    mShow.invoke(obj);
                } catch (Exception e) {
                    System.out.println("显示Toast错误:" + e.getMessage() + "\nmsg:" + msg);
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * bitmap转ByteArray
     * 相当于：
     * ByteArrayOutputStream baos = new ByteArrayOutputStream();
     * bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
     * baos.toByteArray();
     *
     * @param bitmap bitmap
     * @return ByteArray
     */
    public static byte[] bitmapToByteArray(Object bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Class cBitmap = Class.forName("android.graphics.Bitmap");
            Class cCompressFormat = Class.forName("android.graphics.Bitmap$CompressFormat");
            Object oFormat = null;
            for (int i = 0; i < cCompressFormat.getEnumConstants().length; i++) {
                //JPEG,PNG,WEBP
                if ("JPEG".equals(cCompressFormat.getEnumConstants()[i].toString())) {
                    oFormat = cCompressFormat.getEnumConstants()[i];
                    break;
                }
            }
            Method mCompress = cBitmap.getMethod("compress", cCompressFormat, int.class, OutputStream.class);
            mCompress.invoke(bitmap, oFormat, 90, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            System.out.println("转换bitmap错误:" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
