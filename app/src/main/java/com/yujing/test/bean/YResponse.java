package com.yujing.test.bean;

import java.io.Serializable;

/**
 * <p>
 * 返回结果封装类
 * @author yujing 2019年11月21日15:09:51
 * </p>
 */
public class YResponse<T> implements Serializable {

  private int ResultType;
  private String Msg;
  private int ErrorCode;
  private String  ErrorMsg;
  private T Data;

  public int getResultType() {
    return ResultType;
  }

  public void setResultType(int resultType) {
    ResultType = resultType;
  }

  public String getMsg() {
    return Msg;
  }

  public void setMsg(String msg) {
    Msg = msg;
  }

  public int getErrorCode() {
    return ErrorCode;
  }

  public void setErrorCode(int errorCode) {
    ErrorCode = errorCode;
  }

  public String getErrorMsg() {
    return ErrorMsg;
  }

  public void setErrorMsg(String errorMsg) {
    ErrorMsg = errorMsg;
  }

  public T getData() {
    return Data;
  }

  public void setData(T data) {
    Data = data;
  }

  @Override
  public String toString() {
    return "YResponse{" +
            "ResultType=" + ResultType +
            ", Msg='" + Msg + '\'' +
            ", ErrorCode=" + ErrorCode +
            ", ErrorMsg='" + ErrorMsg + '\'' +
            ", Data=" + Data +
            '}';
  }
}
