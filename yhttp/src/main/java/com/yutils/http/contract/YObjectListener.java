package com.yutils.http.contract;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 请求结果返回对象监听
 * 【注意：】此处不能用interface只能使用 abstract class，因为要取出泛型T的具体实现类型，
 * interface不能取出T类型，
 * 所以只能采用abstract class。
 *
 * @param <T> 泛型
 * @author 余静 2020年7月28日10:23:26
 */

public abstract class YObjectListener<T> {
    private final Type type;

    protected YObjectListener() {
        //取出泛型具体类型
        type = getSuperclassTypeParameter(getClass());
    }

    //取出class的父类泛类类型
    static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return parameterized.getActualTypeArguments()[0];
    }

    /**
     * 取出泛型的具体类型
     *
     * @return Type
     */
    public Type getType() {
        return type;
    }

    public abstract void success(byte[] bytes, T value);

    public abstract void fail(String value);
}